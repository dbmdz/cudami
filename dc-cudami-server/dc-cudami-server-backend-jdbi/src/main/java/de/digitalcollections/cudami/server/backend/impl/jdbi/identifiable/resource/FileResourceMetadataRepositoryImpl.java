package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.FileResourceMetadataRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifiableRepositoryImpl;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.resource.ApplicationFileResource;
import de.digitalcollections.model.api.identifiable.resource.AudioFileResource;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.api.identifiable.resource.LinkedDataFileResource;
import de.digitalcollections.model.api.identifiable.resource.MimeType;
import de.digitalcollections.model.api.identifiable.resource.TextFileResource;
import de.digitalcollections.model.api.identifiable.resource.VideoFileResource;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.IdentifierImpl;
import de.digitalcollections.model.impl.identifiable.resource.ApplicationFileResourceImpl;
import de.digitalcollections.model.impl.identifiable.resource.AudioFileResourceImpl;
import de.digitalcollections.model.impl.identifiable.resource.FileResourceImpl;
import de.digitalcollections.model.impl.identifiable.resource.ImageFileResourceImpl;
import de.digitalcollections.model.impl.identifiable.resource.LinkedDataFileResourceImpl;
import de.digitalcollections.model.impl.identifiable.resource.TextFileResourceImpl;
import de.digitalcollections.model.impl.identifiable.resource.VideoFileResourceImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
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

  private final IdentifierRepository identifierRepository;

  @Autowired
  public FileResourceMetadataRepositoryImpl(Jdbi dbi, IdentifierRepository identifierRepository) {
    super(dbi);
    this.identifierRepository = identifierRepository;
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
    StringBuilder query =
        new StringBuilder(
            "SELECT uuid, label, description,"
                + " created, last_modified,"
                + " filename, mimetype, size_in_bytes, uri"
                + " FROM fileresources");
    addPageRequestParams(pageRequest, query);
    List<? extends FileResource> result =
        dbi.withHandle(
            h -> h.createQuery(query.toString()).mapToBean(FileResourceImpl.class).list());
    long total = count();
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
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
        "select f.*"
            + " from fileresources as f"
            + " left join identifiers as id on f.uuid = id.identifiable"
            + " left join digitalobject_fileresources as df on df.fileresource_uuid = f.uuid"
            + " left join digitalobjects as di on di.uuid = df.digitalobject_uuid"
            + " left join versions as v on di.version = v.uuid"
            + " where id.identifier = :id"
            + " and id.namespace = :namespace"
            + " and v.status = 'active'";

    FileResource fileResource =
        dbi.withHandle(
            h ->
                h.createQuery(query)
                    .bind("id", id)
                    .bind("namespace", namespace)
                    .mapToBean(FileResourceImpl.class)
                    .findOne()
                    .orElse(null));

    if (fileResource == null) {
      return null;
    }
    return findOne(fileResource.getUuid());
  }

  private ApplicationFileResource findOneApplicationFileResource(UUID uuid) {
    String query =
        "SELECT f.uuid f_uuid, f.label f_label, f.description f_description,"
            + " f.identifiable_type f_identifiable_type,"
            + " f.created f_created, f.last_modified f_last_modified,"
            + " f.filename f_filename, f.mimetype f_mimetype, f.size_in_bytes f_size_in_bytes, f.uri f_uri,"
            + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id"
            + " FROM fileresources_application as f"
            + " LEFT JOIN identifiers as id on f.uuid = id.identifiable"
            + " WHERE f.uuid = :uuid";
    Optional<ApplicationFileResource> fileResourceOpt =
        dbi.withHandle(
            h ->
                h.createQuery(query).bind("uuid", uuid)
                    .registerRowMapper(BeanMapper.factory(ApplicationFileResourceImpl.class, "f"))
                    .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                    .reduceRows(
                        new LinkedHashMap<UUID, ApplicationFileResource>(),
                        (map, rowView) -> {
                          ApplicationFileResource fr =
                              map.computeIfAbsent(
                                  rowView.getColumn("f_uuid", UUID.class),
                                  id -> rowView.getRow(ApplicationFileResourceImpl.class));
                          if (rowView.getColumn("id_uuid", UUID.class) != null) {
                            fr.addIdentifier(rowView.getRow(IdentifierImpl.class));
                          }
                          return map;
                        })
                    .values().stream()
                    .findFirst());
    if (!fileResourceOpt.isPresent()) {
      return null;
    }
    return fileResourceOpt.get();
  }

  private AudioFileResource findOneAudioFileResource(UUID uuid) {
    String query =
        "SELECT f.uuid f_uuid, f.label f_label, f.description f_description,"
            + " f.identifiable_type f_identifiable_type,"
            + " f.created f_created, f.last_modified f_last_modified,"
            + " f.filename f_filename, f.mimetype f_mimetype, f.size_in_bytes f_size_in_bytes, f.uri f_uri,"
            + " f.duration f_duration," // file resource type specific fields
            + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id"
            + " FROM fileresources_audio as f"
            + " LEFT JOIN identifiers as id on f.uuid = id.identifiable"
            + " WHERE f.uuid = :uuid";
    Optional<AudioFileResource> fileResourceOpt =
        dbi.withHandle(
            h ->
                h.createQuery(query).bind("uuid", uuid)
                    .registerRowMapper(BeanMapper.factory(AudioFileResourceImpl.class, "f"))
                    .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                    .reduceRows(
                        new LinkedHashMap<UUID, AudioFileResource>(),
                        (map, rowView) -> {
                          AudioFileResource fr =
                              map.computeIfAbsent(
                                  rowView.getColumn("f_uuid", UUID.class),
                                  id -> rowView.getRow(AudioFileResourceImpl.class));
                          if (rowView.getColumn("id_uuid", UUID.class) != null) {
                            fr.addIdentifier(rowView.getRow(IdentifierImpl.class));
                          }
                          return map;
                        })
                    .values().stream()
                    .findFirst());
    if (!fileResourceOpt.isPresent()) {
      return null;
    }
    return fileResourceOpt.get();
  }

  private ImageFileResource findOneImageFileResource(UUID uuid) {
    String query =
        "SELECT f.uuid f_uuid, f.label f_label, f.description f_description,"
            + " f.identifiable_type f_identifiable_type,"
            + " f.created f_created, f.last_modified f_last_modified,"
            + " f.filename f_filename, f.mimetype f_mimetype, f.size_in_bytes f_size_in_bytes, f.uri f_uri,"
            + " f.height f_height, f.width f_width," // file resource type specific fields
            + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id"
            + " FROM fileresources_image as f"
            + " LEFT JOIN identifiers as id on f.uuid = id.identifiable"
            + " WHERE f.uuid = :uuid";
    Optional<ImageFileResource> fileResourceOpt =
        dbi.withHandle(
            h ->
                h.createQuery(query).bind("uuid", uuid)
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                    .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                    .reduceRows(
                        new LinkedHashMap<UUID, ImageFileResource>(),
                        (map, rowView) -> {
                          ImageFileResource fr =
                              map.computeIfAbsent(
                                  rowView.getColumn("f_uuid", UUID.class),
                                  id -> rowView.getRow(ImageFileResourceImpl.class));
                          if (rowView.getColumn("id_uuid", UUID.class) != null) {
                            fr.addIdentifier(rowView.getRow(IdentifierImpl.class));
                          }
                          return map;
                        })
                    .values().stream()
                    .findFirst());
    if (!fileResourceOpt.isPresent()) {
      return null;
    }
    return fileResourceOpt.get();
  }

  private LinkedDataFileResource findOneLinkedDataFileResource(UUID uuid) {
    String query =
        "SELECT f.uuid f_uuid, f.label f_label, f.description f_description,"
            + " f.identifiable_type f_identifiable_type,"
            + " f.created f_created, f.last_modified f_last_modified,"
            + " f.filename f_filename, f.mimetype f_mimetype, f.size_in_bytes f_size_in_bytes, f.uri f_uri,"
            + " f.context, f.object_type," // file resource type specific fields
            + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id"
            + " FROM fileresources_linkeddata as f"
            + " LEFT JOIN identifiers as id on f.uuid = id.identifiable"
            + " WHERE f.uuid = :uuid";
    Optional<LinkedDataFileResource> fileResourceOpt =
        dbi.withHandle(
            h ->
                h.createQuery(query).bind("uuid", uuid)
                    .registerRowMapper(BeanMapper.factory(LinkedDataFileResourceImpl.class, "f"))
                    .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                    .reduceRows(
                        new LinkedHashMap<UUID, LinkedDataFileResource>(),
                        (map, rowView) -> {
                          LinkedDataFileResource fr =
                              map.computeIfAbsent(
                                  rowView.getColumn("f_uuid", UUID.class),
                                  id -> rowView.getRow(LinkedDataFileResourceImpl.class));
                          if (rowView.getColumn("id_uuid", UUID.class) != null) {
                            fr.addIdentifier(rowView.getRow(IdentifierImpl.class));
                          }
                          return map;
                        })
                    .values().stream()
                    .findFirst());
    if (!fileResourceOpt.isPresent()) {
      return null;
    }
    return fileResourceOpt.get();
  }

  private TextFileResource findOneTextFileResource(UUID uuid) {
    String query =
        "SELECT f.uuid f_uuid, f.label f_label, f.description f_description,"
            + " f.identifiable_type f_identifiable_type,"
            + " f.created f_created, f.last_modified f_last_modified,"
            + " f.filename f_filename, f.mimetype f_mimetype, f.size_in_bytes f_size_in_bytes, f.uri f_uri,"
            + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id"
            + " FROM fileresources_text as f"
            + " LEFT JOIN identifiers as id on f.uuid = id.identifiable"
            + " WHERE f.uuid = :uuid";
    Optional<TextFileResource> fileResourceOpt =
        dbi.withHandle(
            h ->
                h.createQuery(query).bind("uuid", uuid)
                    .registerRowMapper(BeanMapper.factory(TextFileResourceImpl.class, "f"))
                    .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                    .reduceRows(
                        new LinkedHashMap<UUID, TextFileResource>(),
                        (map, rowView) -> {
                          TextFileResource fr =
                              map.computeIfAbsent(
                                  rowView.getColumn("f_uuid", UUID.class),
                                  id -> rowView.getRow(TextFileResourceImpl.class));
                          if (rowView.getColumn("id_uuid", UUID.class) != null) {
                            fr.addIdentifier(rowView.getRow(IdentifierImpl.class));
                          }
                          return map;
                        })
                    .values().stream()
                    .findFirst());
    if (!fileResourceOpt.isPresent()) {
      return null;
    }
    return fileResourceOpt.get();
  }

  private VideoFileResource findOneVideoFileResource(UUID uuid) {
    String query =
        "SELECT f.uuid f_uuid, f.label f_label, f.description f_description,"
            + " f.identifiable_type f_identifiable_type,"
            + " f.created f_created, f.last_modified f_last_modified,"
            + " f.filename f_filename, f.mimetype f_mimetype, f.size_in_bytes f_size_in_bytes, f.uri f_uri,"
            + " f.duration f_duration," // file resource type specific fields
            + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id"
            + " FROM fileresources_video as f"
            + " LEFT JOIN identifiers as id on f.uuid = id.identifiable"
            + " WHERE f.uuid = :uuid";
    Optional<VideoFileResource> fileResourceOpt =
        dbi.withHandle(
            h ->
                h.createQuery(query).bind("uuid", uuid)
                    .registerRowMapper(BeanMapper.factory(VideoFileResourceImpl.class, "f"))
                    .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                    .reduceRows(
                        new LinkedHashMap<UUID, VideoFileResource>(),
                        (map, rowView) -> {
                          VideoFileResource fr =
                              map.computeIfAbsent(
                                  rowView.getColumn("f_uuid", UUID.class),
                                  id -> rowView.getRow(VideoFileResourceImpl.class));
                          if (rowView.getColumn("id_uuid", UUID.class) != null) {
                            fr.addIdentifier(rowView.getRow(IdentifierImpl.class));
                          }
                          return map;
                        })
                    .values().stream()
                    .findFirst());
    if (!fileResourceOpt.isPresent()) {
      return null;
    }
    return fileResourceOpt.get();
  }

  @Override
  public FileResource save(FileResource fileResource) {
    if (fileResource.getUuid() == null) {
      fileResource.setUuid(UUID.randomUUID());
    }
    fileResource.setCreated(LocalDateTime.now());
    fileResource.setLastModified(LocalDateTime.now());

    final String baseColumnsSql =
        "uuid, created, description, identifiable_type, label, last_modified, filename, mimetype, size_in_bytes, uri";
    final String basePropertiesSql =
        ":uuid, :created, :description::JSONB, :type, :label::JSONB, :lastModified, :filename, :mimeType, :sizeInBytes, :uri";

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
                  .bindBean(fileResource)
                  .execute());
    } else if (fileResource instanceof ImageFileResource) {
      dbi.withHandle(
          h ->
              h.createUpdate(
                      "INSERT INTO fileresources_image("
                          + baseColumnsSql
                          + ", width, height) VALUES ("
                          + basePropertiesSql
                          + ", :width, :height)")
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
                  .bindBean(fileResource)
                  .execute());
    } else {
      throw new IllegalArgumentException(
          "unknown file resource type " + fileResource.getMimeType().toString());
    }

    // save file resource identifiers
    List<Identifier> identifiers = fileResource.getIdentifiers();
    if (identifiers != null) {
      for (Identifier identifier : identifiers) {
        identifier.setIdentifiable(fileResource.getUuid());
        // newly created file resource, no pre existing identifiers, so just save
        identifierRepository.save(identifier);
      }
    }

    FileResource dbFileResource = findOne(fileResource.getUuid());
    return dbFileResource;
  }

  @Override
  public FileResource update(FileResource fileResource) {
    fileResource.setLastModified(LocalDateTime.now());

    final String baseColumnsSql =
        "description=:description::JSONB, label=:label::JSONB, last_modified=:lastModified, filename=:filename, mimetype=:mimeType, size_in_bytes=:sizeInBytes, uri=:uri";

    FileResource result;
    if (fileResource instanceof ApplicationFileResource) {
      // no special columns
      result =
          dbi.withHandle(
              h ->
                  h.createQuery(
                          "UPDATE fileresources_application SET "
                              + baseColumnsSql
                              + " WHERE uuid=:uuid RETURNING *")
                      .bindBean(fileResource)
                      .mapToBean(ApplicationFileResourceImpl.class)
                      .findOne()
                      .orElse(null));
    } else if (fileResource instanceof AudioFileResource) {
      result =
          dbi.withHandle(
              h ->
                  h.createQuery(
                          "UPDATE fileresources_audio SET "
                              + baseColumnsSql
                              + ", duration=:duration WHERE uuid=:uuid RETURNING *")
                      .bindBean(fileResource)
                      .mapToBean(AudioFileResourceImpl.class)
                      .findOne()
                      .orElse(null));
    } else if (fileResource instanceof ImageFileResource) {
      result =
          dbi.withHandle(
              h ->
                  h.createQuery(
                          "UPDATE fileresources_image SET "
                              + baseColumnsSql
                              + ", width=:width, height=:height WHERE uuid=:uuid RETURNING *")
                      .bindBean(fileResource)
                      .mapToBean(ImageFileResourceImpl.class)
                      .findOne()
                      .orElse(null));
    } else if (fileResource instanceof LinkedDataFileResource) {
      result =
          dbi.withHandle(
              h ->
                  h.createQuery(
                          "UPDATE fileresources_linkeddata SET "
                              + baseColumnsSql
                              + ", context=:context, object_type=:objectType WHERE uuid=:uuid RETURNING *")
                      .bindBean(fileResource)
                      .mapToBean(LinkedDataFileResourceImpl.class)
                      .findOne()
                      .orElse(null));
    } else if (fileResource instanceof TextFileResource) {
      // no special columns
      result =
          dbi.withHandle(
              h ->
                  h.createQuery(
                          "UPDATE fileresources_text SET "
                              + baseColumnsSql
                              + " WHERE uuid=:uuid RETURNING *")
                      .bindBean(fileResource)
                      .mapToBean(TextFileResourceImpl.class)
                      .findOne()
                      .orElse(null));
    } else if (fileResource instanceof VideoFileResource) {
      result =
          dbi.withHandle(
              h ->
                  h.createQuery(
                          "UPDATE fileresources_video SET "
                              + baseColumnsSql
                              + ", duration=:duration WHERE uuid=:uuid RETURNING *")
                      .bindBean(fileResource)
                      .mapToBean(VideoFileResourceImpl.class)
                      .findOne()
                      .orElse(null));
    } else {
      throw new IllegalArgumentException(
          "unknown file resource type " + fileResource.getMimeType().toString());
    }
    return result;
  }
}
