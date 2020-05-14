package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.FileResourceMetadataRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifiableRepositoryImpl;
import de.digitalcollections.model.api.filter.FilterCriterion;
import de.digitalcollections.model.api.filter.Filtering;
import de.digitalcollections.model.api.filter.enums.FilterOperation;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.resource.ApplicationFileResource;
import de.digitalcollections.model.api.identifiable.resource.AudioFileResource;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.api.identifiable.resource.LinkedDataFileResource;
import de.digitalcollections.model.api.identifiable.resource.MimeType;
import de.digitalcollections.model.api.identifiable.resource.TextFileResource;
import de.digitalcollections.model.api.identifiable.resource.VideoFileResource;
import de.digitalcollections.model.api.identifiable.resource.enums.FileResourceType;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.SearchPageRequest;
import de.digitalcollections.model.api.paging.SearchPageResponse;
import de.digitalcollections.model.impl.identifiable.IdentifierImpl;
import de.digitalcollections.model.impl.identifiable.resource.ApplicationFileResourceImpl;
import de.digitalcollections.model.impl.identifiable.resource.AudioFileResourceImpl;
import de.digitalcollections.model.impl.identifiable.resource.FileResourceImpl;
import de.digitalcollections.model.impl.identifiable.resource.ImageFileResourceImpl;
import de.digitalcollections.model.impl.identifiable.resource.LinkedDataFileResourceImpl;
import de.digitalcollections.model.impl.identifiable.resource.TextFileResourceImpl;
import de.digitalcollections.model.impl.identifiable.resource.VideoFileResourceImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import de.digitalcollections.model.impl.paging.SearchPageResponseImpl;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class FileResourceMetadataRepositoryImpl extends IdentifiableRepositoryImpl<FileResource>
    implements FileResourceMetadataRepository {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(FileResourceMetadataRepositoryImpl.class);

  // select all details shown/needed in single object details page
  private static final String FIND_ONE_BASE_SQL =
      "SELECT f.uuid f_uuid, f.label f_label, f.description f_description,"
          + " f.identifiable_type f_type,"
          + " f.created f_created, f.last_modified f_lastModified,"
          + " f.filename f_filename, f.mimetype f_mimetype, f.size_in_bytes f_sizeInBytes, f.uri f_uri,"
          + " f.preview_hints f_previewImageRenderingHints,"
          + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
          + " file.uuid pf_uuid, file.filename pf_filename, file.mimetype pf_mimetype, file.size_in_bytes pf_size_in_bytes, file.uri pf_uri, file.iiif_base_url pf_iiifBaseUrl"
          + " FROM fileresources as f"
          + " LEFT JOIN identifiers as id on f.uuid = id.identifiable"
          + " LEFT JOIN fileresources_image as file on f.previewfileresource = file.uuid";

  // select only what is shown/needed in paged list (to avoid unnecessary payload/traffic):
  private static final String REDUCED_FIND_ONE_BASE_SQL =
      "SELECT f.uuid f_uuid, f.label f_label, f.description f_description,"
          + " f.identifiable_type f_type,"
          + " f.created f_created, f.last_modified f_lastModified,"
          + " f.filename f_filename, f.mimetype f_mimetype, f.size_in_bytes f_sizeInBytes, f.uri f_uri,"
          + " f.preview_hints f_previewImageRenderingHints,"
          + " file.uuid pf_uuid, file.uri pf_uri, file.filename pf_filename, file.iiif_base_url pf_iiifBaseUrl"
          + " FROM fileresources as f"
          + " LEFT JOIN fileresources_image as file on f.previewfileresource = file.uuid";

  @Autowired
  public FileResourceMetadataRepositoryImpl(Jdbi dbi, IdentifierRepository identifierRepository) {
    super(dbi, identifierRepository);
  }

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM fileresources";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOne().get());
    return count;
  }

  @Override
  public FileResource createByMimeType(MimeType mimeType) {
    if (mimeType == null) {
      mimeType = MimeType.MIME_APPLICATION_OCTET_STREAM;
    }
    FileResource result;
    String primaryType = mimeType.getPrimaryType();
    switch (primaryType) {
      case "audio":
        result = new AudioFileResourceImpl();
        break;
      case "image":
        result = new ImageFileResourceImpl();
        break;
      case "text":
        result = new TextFileResourceImpl();
        break;
      case "video":
        result = new VideoFileResourceImpl();
        break;
      case "application":
        if ("ld+json".equals(mimeType.getSubType())) {
          result = new LinkedDataFileResourceImpl();
          break;
        }
        result = new ApplicationFileResourceImpl();
        break;
      default:
        result = new ApplicationFileResourceImpl();
    }
    result.setMimeType(mimeType);
    // FIXME: don't do it!!! check it why needed....
    final UUID uuid = UUID.randomUUID();
    result.setUuid(uuid);
    return result;
  }

  @Override
  public PageResponse<FileResource> find(PageRequest pageRequest) {
    StringBuilder query = new StringBuilder(REDUCED_FIND_ONE_BASE_SQL);
    addPageRequestParams(pageRequest, query);

    List<FileResourceImpl> result =
        new ArrayList(
            dbi.withHandle(
                h ->
                    h.createQuery(query.toString())
                        .registerRowMapper(BeanMapper.factory(FileResourceImpl.class, "f"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "pf"))
                        .reduceRows(
                            new LinkedHashMap<UUID, FileResourceImpl>(),
                            (map, rowView) -> {
                              FileResourceImpl fileResource =
                                  map.computeIfAbsent(
                                      rowView.getColumn("f_uuid", UUID.class),
                                      fn -> {
                                        return rowView.getRow(FileResourceImpl.class);
                                      });

                              if (rowView.getColumn("pf_uuid", UUID.class) != null) {
                                fileResource.setPreviewImage(
                                    rowView.getRow(ImageFileResourceImpl.class));
                              }
                              return map;
                            })
                        .values()));

    long total = count();
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public SearchPageResponse<FileResource> find(SearchPageRequest searchPageRequest) {
    // select only what is shown/needed in paged result list:
    StringBuilder query =
        new StringBuilder(
            REDUCED_FIND_ONE_BASE_SQL
                + " LEFT JOIN LATERAL jsonb_object_keys(f.label) l(keys) on f.label is not null"
                + " LEFT JOIN LATERAL jsonb_object_keys(f.description) d(keys) on f.description is not null"
                + " WHERE (f.label->>l.keys ilike '%' || :searchTerm || '%'"
                + " OR f.description->>d.keys ilike '%' || :searchTerm || '%'"
                + " OR f.filename ilike '%' || :searchTerm || '%')");
    String filterQuery = "";
    Filtering filtering = searchPageRequest.getFiltering();
    if (filtering != null) {
      FilterCriterion<FileResourceType> fileResourceTypeCriterion =
          filtering.getFilterCriterionFor("fileResourceType");
      if (FilterOperation.EQUALS == fileResourceTypeCriterion.getOperation()) {
        FileResourceType fileResourceType = (FileResourceType) fileResourceTypeCriterion.getValue();
        switch (fileResourceType) {
          case IMAGE:
            filterQuery = " AND f.mimetype ilike 'image/%'";
            query.append(filterQuery);
            break;
          default:
            break;
        }
      }
    }

    addPageRequestParams(searchPageRequest, query);

    List<FileResource> result =
        new ArrayList(
            dbi.withHandle(
                h ->
                    h.createQuery(query.toString())
                        .bind("searchTerm", searchPageRequest.getQuery())
                        .registerRowMapper(BeanMapper.factory(FileResourceImpl.class, "f"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "pf"))
                        .reduceRows(
                            new LinkedHashMap<UUID, FileResource>(),
                            (map, rowView) -> {
                              FileResource fileResource =
                                  map.computeIfAbsent(
                                      rowView.getColumn("f_uuid", UUID.class),
                                      uuid -> rowView.getRow(FileResourceImpl.class));
                              if (rowView.getColumn("pf_uuid", String.class) != null) {
                                fileResource.setPreviewImage(
                                    rowView.getRow(ImageFileResourceImpl.class));
                              }
                              return map;
                            })
                        .values()));

    String countQuery =
        "SELECT count(*) FROM fileresources as f"
            + " LEFT JOIN LATERAL jsonb_object_keys(f.label) l(keys) on f.label is not null"
            + " LEFT JOIN LATERAL jsonb_object_keys(f.description) d(keys) on f.description is not null"
            + " WHERE (f.label->>l.keys ilike '%' || :searchTerm || '%'"
            + " OR f.description->>d.keys ilike '%' || :searchTerm || '%')"
            + filterQuery;
    long total =
        dbi.withHandle(
            h ->
                h.createQuery(countQuery)
                    .bind("searchTerm", searchPageRequest.getQuery())
                    .mapTo(Long.class)
                    .findOne()
                    .get());

    SearchPageResponse pageResponse = new SearchPageResponseImpl(result, searchPageRequest, total);
    return pageResponse;
  }

  @Override
  public FileResource findOne(UUID uuid) {
    String query = "SELECT mimetype FROM fileresources WHERE uuid=:uuid";
    String mimetype =
        dbi.withHandle(
            h ->
                h.createQuery(query).bind("uuid", uuid).mapTo(String.class).findOne().orElse(null));

    if (mimetype == null) {
      return null;
    }

    // reuse already existing method "createByMimeType" to avoid code duplication:
    FileResource typedFileResource = createByMimeType(MimeType.fromTypename(mimetype));
    if (typedFileResource instanceof ApplicationFileResource) {
      return findOneApplicationFileResource(uuid);
    } else if (typedFileResource instanceof AudioFileResource) {
      return findOneAudioFileResource(uuid);
    } else if (typedFileResource instanceof ImageFileResource) {
      return findOneImageFileResource(uuid);
    } else if (typedFileResource instanceof LinkedDataFileResource) {
      return findOneLinkedDataFileResource(uuid);
    } else if (typedFileResource instanceof TextFileResource) {
      return findOneTextFileResource(uuid);
    } else if (typedFileResource instanceof VideoFileResource) {
      return findOneVideoFileResource(uuid);
    }
    return null;
  }

  @Override
  public FileResource findOne(Identifier identifier) {
    if (identifier.getIdentifiable() != null) {
      return findOne(identifier.getIdentifiable());
    }

    String namespace = identifier.getNamespace();
    String id = identifier.getId();

    String query =
        "SELECT f.uuid f_uuid"
            + " FROM fileresources as f"
            + " LEFT JOIN identifiers as id on f.uuid = id.identifiable"
            + " WHERE id.identifier = :id AND id.namespace = :namespace";

    UUID uuid =
        dbi.withHandle(
            h ->
                h.createQuery(query)
                    .bind("id", id)
                    .bind("namespace", namespace)
                    .mapTo(UUID.class)
                    .findOne()
                    .orElse(null));

    if (uuid == null) {
      return null;
    }
    return findOne(uuid);
  }

  private ApplicationFileResource findOneApplicationFileResource(UUID uuid) {
    String query =
        "SELECT f.uuid f_uuid, f.label f_label, f.description f_description,"
            + " f.identifiable_type f_type,"
            + " f.created f_created, f.last_modified f_lastModified,"
            + " f.filename f_filename, f.mimetype f_mimetype, f.size_in_bytes f_sizeInBytes, f.uri f_uri,"
            + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
            + " file.uuid pf_uuid, file.filename pf_filename, file.mimetype pf_mimetype, file.size_in_bytes pf_size_in_bytes, file.uri pf_uri"
            + " FROM fileresources_application as f"
            + " LEFT JOIN identifiers as id on f.uuid = id.identifiable"
            + " LEFT JOIN fileresources_image as file on f.previewfileresource = file.uuid"
            + " WHERE f.uuid = :uuid";
    ApplicationFileResourceImpl result =
        dbi.withHandle(
                h ->
                    h.createQuery(query)
                        .bind("uuid", uuid)
                        .registerRowMapper(
                            BeanMapper.factory(ApplicationFileResourceImpl.class, "f"))
                        .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "pf"))
                        .reduceRows(
                            new LinkedHashMap<UUID, ApplicationFileResourceImpl>(),
                            (map, rowView) -> {
                              ApplicationFileResourceImpl fileResource =
                                  map.computeIfAbsent(
                                      rowView.getColumn("f_uuid", UUID.class),
                                      fn -> {
                                        return rowView.getRow(ApplicationFileResourceImpl.class);
                                      });

                              if (rowView.getColumn("pf_uuid", UUID.class) != null) {
                                fileResource.setPreviewImage(
                                    rowView.getRow(ImageFileResourceImpl.class));
                              }

                              if (rowView.getColumn("id_uuid", UUID.class) != null) {
                                IdentifierImpl identifier = rowView.getRow(IdentifierImpl.class);
                                fileResource.addIdentifier(identifier);
                              }

                              return map;
                            }))
            .get(uuid);
    return result;
  }

  private AudioFileResource findOneAudioFileResource(UUID uuid) {
    String query =
        "SELECT f.uuid f_uuid, f.label f_label, f.description f_description,"
            + " f.identifiable_type f_type,"
            + " f.created f_created, f.last_modified f_lastModified,"
            + " f.filename f_filename, f.mimetype f_mimetype, f.size_in_bytes f_sizeInBytes, f.uri f_uri,"
            + " f.duration f_duration," // file resource type specific fields
            + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
            + " file.uuid pf_uuid, file.filename pf_filename, file.mimetype pf_mimetype, file.size_in_bytes pf_size_in_bytes, file.uri pf_uri"
            + " FROM fileresources_audio as f"
            + " LEFT JOIN identifiers as id on f.uuid = id.identifiable"
            + " LEFT JOIN fileresources_image as file on f.previewfileresource = file.uuid"
            + " WHERE f.uuid = :uuid";

    AudioFileResourceImpl result =
        dbi.withHandle(
                h ->
                    h.createQuery(query)
                        .bind("uuid", uuid)
                        .registerRowMapper(BeanMapper.factory(AudioFileResourceImpl.class, "f"))
                        .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "pf"))
                        .reduceRows(
                            new LinkedHashMap<UUID, AudioFileResourceImpl>(),
                            (map, rowView) -> {
                              AudioFileResourceImpl fileResource =
                                  map.computeIfAbsent(
                                      rowView.getColumn("f_uuid", UUID.class),
                                      fn -> {
                                        return rowView.getRow(AudioFileResourceImpl.class);
                                      });

                              if (rowView.getColumn("pf_uuid", UUID.class) != null) {
                                fileResource.setPreviewImage(
                                    rowView.getRow(ImageFileResourceImpl.class));
                              }

                              if (rowView.getColumn("id_uuid", UUID.class) != null) {
                                IdentifierImpl identifier = rowView.getRow(IdentifierImpl.class);
                                fileResource.addIdentifier(identifier);
                              }

                              return map;
                            }))
            .get(uuid);
    return result;
  }

  private ImageFileResource findOneImageFileResource(UUID uuid) {
    String query =
        "SELECT f.uuid f_uuid, f.label f_label, f.description f_description,"
            + " f.identifiable_type f_type,"
            + " f.created f_created, f.last_modified f_lastModified,"
            + " f.filename f_filename, f.mimetype f_mimetype, f.size_in_bytes f_sizeInBytes, f.uri f_uri,"
            // file resource type specific fields:
            + " f.height f_height, f.width f_width, f.iiif_base_url f_iiifBaseUrl,"
            + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
            + " file.uuid pf_uuid, file.filename pf_filename, file.mimetype pf_mimetype, file.size_in_bytes pf_size_in_bytes, file.uri pf_uri"
            + " FROM fileresources_image as f"
            + " LEFT JOIN identifiers as id on f.uuid = id.identifiable"
            + " LEFT JOIN fileresources_image as file on f.previewfileresource = file.uuid"
            + " WHERE f.uuid = :uuid";

    ImageFileResourceImpl result =
        dbi.withHandle(
                h ->
                    h.createQuery(query)
                        .bind("uuid", uuid)
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                        .registerRowMapper(BeanMapper.factory(FileResourceImpl.class, "pf"))
                        // FIXME: FileResourceImpl.class is a workaround, because using
                        // ImageFileResoureImpl leads to empty result bean... maybe clash of jdbi
                        // being
                        // not able to map to two identical classes
                        .reduceRows(
                            new LinkedHashMap<UUID, ImageFileResourceImpl>(),
                            (map, rowView) -> {
                              ImageFileResourceImpl fileResource =
                                  map.computeIfAbsent(
                                      rowView.getColumn("f_uuid", UUID.class),
                                      fn -> {
                                        return rowView.getRow(ImageFileResourceImpl.class);
                                      });

                              if (rowView.getColumn("pf_uuid", UUID.class) != null) {
                                fileResource.setPreviewImage(
                                    rowView.getRow(ImageFileResourceImpl.class));
                              }

                              if (rowView.getColumn("id_uuid", UUID.class) != null) {
                                IdentifierImpl identifier = rowView.getRow(IdentifierImpl.class);
                                fileResource.addIdentifier(identifier);
                              }

                              return map;
                            }))
            .get(uuid);
    return result;
  }

  private LinkedDataFileResource findOneLinkedDataFileResource(UUID uuid) {
    String query =
        "SELECT f.uuid f_uuid, f.label f_label, f.description f_description,"
            + " f.identifiable_type f_type,"
            + " f.created f_created, f.last_modified f_lastModified,"
            + " f.filename f_filename, f.mimetype f_mimetype, f.size_in_bytes f_sizeInBytes, f.uri f_uri,"
            + " f.context f_context, f.object_type f_objectType," // file resource type specific
            // fields
            + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
            + " file.uuid pf_uuid, file.filename pf_filename, file.mimetype pf_mimetype, file.size_in_bytes pf_size_in_bytes, file.uri pf_uri"
            + " FROM fileresources_linkeddata as f"
            + " LEFT JOIN identifiers as id on f.uuid = id.identifiable"
            + " LEFT JOIN fileresources_image as file on f.previewfileresource = file.uuid"
            + " WHERE f.uuid = :uuid";
    LinkedDataFileResourceImpl result =
        dbi.withHandle(
                h ->
                    h.createQuery(query)
                        .bind("uuid", uuid)
                        .registerRowMapper(
                            BeanMapper.factory(LinkedDataFileResourceImpl.class, "f"))
                        .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "pf"))
                        .reduceRows(
                            new LinkedHashMap<UUID, LinkedDataFileResourceImpl>(),
                            (map, rowView) -> {
                              LinkedDataFileResourceImpl fileResource =
                                  map.computeIfAbsent(
                                      rowView.getColumn("f_uuid", UUID.class),
                                      fn -> {
                                        return rowView.getRow(LinkedDataFileResourceImpl.class);
                                      });

                              if (rowView.getColumn("pf_uuid", UUID.class) != null) {
                                fileResource.setPreviewImage(
                                    rowView.getRow(ImageFileResourceImpl.class));
                              }

                              if (rowView.getColumn("id_uuid", UUID.class) != null) {
                                IdentifierImpl identifier = rowView.getRow(IdentifierImpl.class);
                                fileResource.addIdentifier(identifier);
                              }

                              return map;
                            }))
            .get(uuid);
    return result;
  }

  private TextFileResource findOneTextFileResource(UUID uuid) {
    String query =
        "SELECT f.uuid f_uuid, f.label f_label, f.description f_description,"
            + " f.identifiable_type f_type,"
            + " f.created f_created, f.last_modified f_lastModified,"
            + " f.filename f_filename, f.mimetype f_mimetype, f.size_in_bytes f_sizeInBytes, f.uri f_uri,"
            + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
            + " file.uuid pf_uuid, file.filename pf_filename, file.mimetype pf_mimetype, file.size_in_bytes pf_size_in_bytes, file.uri pf_uri"
            + " FROM fileresources_text as f"
            + " LEFT JOIN identifiers as id on f.uuid = id.identifiable"
            + " LEFT JOIN fileresources_image as file on f.previewfileresource = file.uuid"
            + " WHERE f.uuid = :uuid";
    TextFileResourceImpl result =
        dbi.withHandle(
                h ->
                    h.createQuery(query)
                        .bind("uuid", uuid)
                        .registerRowMapper(BeanMapper.factory(TextFileResourceImpl.class, "f"))
                        .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "pf"))
                        .reduceRows(
                            new LinkedHashMap<UUID, TextFileResourceImpl>(),
                            (map, rowView) -> {
                              TextFileResourceImpl fileResource =
                                  map.computeIfAbsent(
                                      rowView.getColumn("f_uuid", UUID.class),
                                      fn -> {
                                        return rowView.getRow(TextFileResourceImpl.class);
                                      });

                              if (rowView.getColumn("pf_uuid", UUID.class) != null) {
                                fileResource.setPreviewImage(
                                    rowView.getRow(ImageFileResourceImpl.class));
                              }

                              if (rowView.getColumn("id_uuid", UUID.class) != null) {
                                IdentifierImpl identifier = rowView.getRow(IdentifierImpl.class);
                                fileResource.addIdentifier(identifier);
                              }

                              return map;
                            }))
            .get(uuid);
    return result;
  }

  private VideoFileResource findOneVideoFileResource(UUID uuid) {
    String query =
        "SELECT f.uuid f_uuid, f.label f_label, f.description f_description,"
            + " f.identifiable_type f_type,"
            + " f.created f_created, f.last_modified f_lastModified,"
            + " f.filename f_filename, f.mimetype f_mimetype, f.size_in_bytes f_sizeInBytes, f.uri f_uri,"
            + " f.duration f_duration," // file resource type specific fields
            + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
            + " file.uuid pf_uuid, file.filename pf_filename, file.mimetype pf_mimetype, file.size_in_bytes pf_size_in_bytes, file.uri pf_uri"
            + " FROM fileresources_video as f"
            + " LEFT JOIN identifiers as id on f.uuid = id.identifiable"
            + " LEFT JOIN fileresources_image as file on f.previewfileresource = file.uuid"
            + " WHERE f.uuid = :uuid";
    VideoFileResourceImpl result =
        dbi.withHandle(
                h ->
                    h.createQuery(query)
                        .bind("uuid", uuid)
                        .registerRowMapper(BeanMapper.factory(VideoFileResourceImpl.class, "f"))
                        .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "pf"))
                        .reduceRows(
                            new LinkedHashMap<UUID, VideoFileResourceImpl>(),
                            (map, rowView) -> {
                              VideoFileResourceImpl fileResource =
                                  map.computeIfAbsent(
                                      rowView.getColumn("f_uuid", UUID.class),
                                      fn -> {
                                        return rowView.getRow(VideoFileResourceImpl.class);
                                      });

                              if (rowView.getColumn("pf_uuid", UUID.class) != null) {
                                fileResource.setPreviewImage(
                                    rowView.getRow(ImageFileResourceImpl.class));
                              }

                              if (rowView.getColumn("id_uuid", UUID.class) != null) {
                                IdentifierImpl identifier = rowView.getRow(IdentifierImpl.class);
                                fileResource.addIdentifier(identifier);
                              }

                              return map;
                            }))
            .get(uuid);
    return result;
  }

  @Override
  public FileResource save(FileResource fileResource) {
    if (fileResource.getUuid() == null) {
      fileResource.setUuid(UUID.randomUUID());
    }
    fileResource.setCreated(LocalDateTime.now());
    fileResource.setLastModified(LocalDateTime.now());
    final UUID previewImageUuid =
        fileResource.getPreviewImage() == null ? null : fileResource.getPreviewImage().getUuid();

    final String baseColumnsSql =
        "uuid, label, description, previewfileresource, preview_hints, identifiable_type, created, last_modified, filename, mimetype, size_in_bytes, uri";
    final String basePropertiesSql =
        ":uuid, :label::JSONB, :description::JSONB, :previewFileResource, :previewImageRenderingHints::JSONB, :type, :created, :lastModified, :filename, :mimeType, :sizeInBytes, :uri";

    if (fileResource instanceof ApplicationFileResource) {
      // no special columns
      dbi.withHandle(
          h ->
              h.createUpdate(
                      "INSERT INTO fileresources_application("
                          + baseColumnsSql
                          + ") VALUES ("
                          + basePropertiesSql
                          + ")")
                  .bind("previewFileResource", previewImageUuid)
                  .bindBean(fileResource)
                  .execute());
    } else if (fileResource instanceof AudioFileResource) {
      dbi.withHandle(
          h ->
              h.createUpdate(
                      "INSERT INTO fileresources_audio("
                          + baseColumnsSql
                          + ", duration) VALUES ("
                          + basePropertiesSql
                          + ", :duration)")
                  .bind("previewFileResource", previewImageUuid)
                  .bindBean(fileResource)
                  .execute());
    } else if (fileResource instanceof ImageFileResource) {
      final UUID previewUuid = previewImageUuid == null ? fileResource.getUuid() : previewImageUuid;
      dbi.withHandle(
          h ->
              h.createUpdate(
                      "INSERT INTO fileresources_image("
                          + baseColumnsSql
                          + ", width, height, iiif_base_url) VALUES ("
                          + basePropertiesSql
                          + ", :width, :height, :iiifBaseUrl)")
                  .bind("previewFileResource", previewUuid)
                  .bindBean(fileResource)
                  .execute());
    } else if (fileResource instanceof LinkedDataFileResource) {
      dbi.withHandle(
          h ->
              h.createUpdate(
                      "INSERT INTO fileresources_linkeddata("
                          + baseColumnsSql
                          + ", context, object_type) VALUES ("
                          + basePropertiesSql
                          + ", :context, :objectType)")
                  .bind("previewFileResource", previewImageUuid)
                  .bindBean(fileResource)
                  .execute());
    } else if (fileResource instanceof TextFileResource) {
      // no special columns
      dbi.withHandle(
          h ->
              h.createUpdate(
                      "INSERT INTO fileresources_text("
                          + baseColumnsSql
                          + ") VALUES ("
                          + basePropertiesSql
                          + ")")
                  .bind("previewFileResource", previewImageUuid)
                  .bindBean(fileResource)
                  .execute());
    } else if (fileResource instanceof VideoFileResource) {
      dbi.withHandle(
          h ->
              h.createUpdate(
                      "INSERT INTO fileresources_video("
                          + baseColumnsSql
                          + ", duration) VALUES ("
                          + basePropertiesSql
                          + ", :duration)")
                  .bind("previewFileResource", previewImageUuid)
                  .bindBean(fileResource)
                  .execute());
    } else {
      throw new IllegalArgumentException(
          "unknown file resource type " + fileResource.getMimeType().toString());
    }

    // save identifiers
    Set<Identifier> identifiers = fileResource.getIdentifiers();
    saveIdentifiers(identifiers, fileResource);

    FileResource result = findOne(fileResource.getUuid());
    return result;
  }

  @Override
  public FileResource update(FileResource fileResource) {
    fileResource.setLastModified(LocalDateTime.now());
    // do not update/left out from statement (not changed since insert):
    // uuid, created, identifiable_type
    final UUID previewImageUuid =
        fileResource.getPreviewImage() == null ? null : fileResource.getPreviewImage().getUuid();

    final String baseColumnsSql =
        "label=:label::JSONB, description=:description::JSONB,"
            + " previewfileresource=:previewFileResource, preview_hints=:previewImageRenderingHints::JSONB,"
            + " last_modified=:lastModified";

    if (fileResource instanceof ApplicationFileResource) {
      // no special columns
      String query = "UPDATE fileresources_application SET " + baseColumnsSql + " WHERE uuid=:uuid";
      dbi.withHandle(
          h ->
              h.createUpdate(query)
                  .bind("previewFileResource", previewImageUuid)
                  .bindBean(fileResource)
                  .execute());
    } else if (fileResource instanceof AudioFileResource) {
      String query =
          "UPDATE fileresources_audio SET "
              + baseColumnsSql
              + ", duration=:duration WHERE uuid=:uuid";
      dbi.withHandle(
          h ->
              h.createUpdate(query)
                  .bind("previewFileResource", previewImageUuid)
                  .bindBean(fileResource)
                  .execute());
    } else if (fileResource instanceof ImageFileResource) {
      final UUID previewUuid = previewImageUuid == null ? fileResource.getUuid() : previewImageUuid;
      String query =
          "UPDATE fileresources_image SET "
              + baseColumnsSql
              + ", width=:width, height=:height, iiif_base_url=:iiifBaseUrl WHERE uuid=:uuid";
      dbi.withHandle(
          h ->
              h.createUpdate(query)
                  .bind("previewFileResource", previewUuid)
                  .bindBean(fileResource)
                  .execute());
    } else if (fileResource instanceof LinkedDataFileResource) {
      String query =
          "UPDATE fileresources_linkeddata SET "
              + baseColumnsSql
              + ", context=:context, object_type=:objectType WHERE uuid=:uuid";
      dbi.withHandle(
          h ->
              h.createUpdate(query)
                  .bind("previewFileResource", previewImageUuid)
                  .bindBean(fileResource)
                  .execute());
    } else if (fileResource instanceof TextFileResource) {
      // no special columns
      String query = "UPDATE fileresources_text SET " + baseColumnsSql + " WHERE uuid=:uuid";
      dbi.withHandle(
          h ->
              h.createUpdate(query)
                  .bind("previewFileResource", previewImageUuid)
                  .bindBean(fileResource)
                  .execute());
    } else if (fileResource instanceof VideoFileResource) {
      String query =
          "UPDATE fileresources_video SET "
              + baseColumnsSql
              + ", duration=:duration WHERE uuid=:uuid";
      dbi.withHandle(
          h ->
              h.createUpdate(query)
                  .bind("previewFileResource", previewImageUuid)
                  .bindBean(fileResource)
                  .execute());
    } else {
      throw new IllegalArgumentException(
          "unknown file resource type " + fileResource.getMimeType().toString());
    }

    // save identifiers
    // as we store the whole list new: delete old entries
    deleteIdentifiers(fileResource);
    Set<Identifier> identifiers = fileResource.getIdentifiers();
    saveIdentifiers(identifiers, fileResource);

    FileResource result = findOne(fileResource.getUuid());
    return result;
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[] {"created", "filename", "lastModified", "sizeInBytes"};
  }

  @Override
  protected String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "created":
        return "f.created";
      case "filename":
        return "f.filename";
      case "lastModified":
        return "f.last_modified";
      case "sizeInBytes":
        return "f.size_in_bytes";
      default:
        return null;
    }
  }
}
