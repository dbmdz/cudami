package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.DigitalObjectRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.FileResourceMetadataRepository;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.entity.Collection;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.entity.Project;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.SearchPageRequest;
import de.digitalcollections.model.api.paging.SearchPageResponse;
import de.digitalcollections.model.impl.identifiable.IdentifierImpl;
import de.digitalcollections.model.impl.identifiable.entity.CollectionImpl;
import de.digitalcollections.model.impl.identifiable.entity.DigitalObjectImpl;
import de.digitalcollections.model.impl.identifiable.entity.ProjectImpl;
import de.digitalcollections.model.impl.identifiable.resource.FileResourceImpl;
import de.digitalcollections.model.impl.identifiable.resource.ImageFileResourceImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import de.digitalcollections.model.impl.paging.SearchPageResponseImpl;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class DigitalObjectRepositoryImpl extends EntityRepositoryImpl<DigitalObject>
    implements DigitalObjectRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(DigitalObjectRepositoryImpl.class);

  // select all details shown/needed in single object details page
  private static final String FIND_ONE_BASE_SQL =
      "SELECT d.uuid d_uuid, d.refid d_refId, d.label d_label, d.description d_description,"
          + " d.identifiable_type d_type, d.entity_type d_entityType,"
          + " d.created d_created, d.last_modified d_lastModified,"
          + " d.preview_hints d_previewImageRenderingHints,"
          // TODO: add d.license d_license, d.version d_version, when features added
          + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
          + " file.uuid f_uuid, file.filename f_filename, file.mimetype f_mimeType, file.size_in_bytes f_sizeInBytes, file.uri f_uri, file.http_base_url f_httpBaseUrl,"
          // related file resources
          + " fr.uuid fr_uuid, fr.filename fr_filename, fr.mimetype fr_mimetype, fr.size_in_bytes fr_sizeInBytes, fr.uri fr_uri"
          + " FROM digitalobjects as d"
          + " LEFT JOIN identifiers as id on d.uuid = id.identifiable"
          + " LEFT JOIN fileresources_image as file on d.previewfileresource = file.uuid"
          + " LEFT JOIN digitalobject_fileresources as df on d.uuid = df.digitalobject_uuid"
          + " LEFT JOIN fileresources as fr on fr.uuid = df.fileresource_uuid";

  // select only what is shown/needed in paged list (to avoid unnecessary payload/traffic):
  private static final String REDUCED_FIND_ONE_BASE_SQL =
      "SELECT d.uuid d_uuid, d.refid d_refId, d.label d_label, d.description d_description,"
          + " d.identifiable_type d_type, d.entity_type d_entityType,"
          + " d.created d_created, d.last_modified d_lastModified,"
          + " d.preview_hints d_previewImageRenderingHints,"
          + " file.uuid f_uuid, file.filename f_filename, file.mimetype f_mimeType, file.size_in_bytes f_sizeInBytes, file.uri f_uri, file.http_base_url f_httpBaseUrl"
          + " FROM digitalobjects as d"
          + " LEFT JOIN fileresources_image as file on d.previewfileresource = file.uuid";

  private final FileResourceMetadataRepository fileResourceMetadataRepository;

  @Autowired
  public DigitalObjectRepositoryImpl(
      Jdbi dbi,
      IdentifierRepository identifierRepository,
      FileResourceMetadataRepository fileResourceMetadataRepository) {
    super(dbi, identifierRepository);
    this.fileResourceMetadataRepository = fileResourceMetadataRepository;
  }

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM digitalobjects";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOne().get());
    return count;
  }

  @Override
  public PageResponse<DigitalObject> find(PageRequest pageRequest) {
    StringBuilder query = new StringBuilder(REDUCED_FIND_ONE_BASE_SQL);
    addPageRequestParams(pageRequest, query);

    List<DigitalObjectImpl> result =
        new ArrayList(
            dbi.withHandle(
                h ->
                    h.createQuery(query.toString())
                        .registerRowMapper(BeanMapper.factory(DigitalObjectImpl.class, "d"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .reduceRows(
                            new LinkedHashMap<UUID, DigitalObjectImpl>(),
                            (map, rowView) -> {
                              DigitalObjectImpl digitalObject =
                                  map.computeIfAbsent(
                                      rowView.getColumn("d_uuid", UUID.class),
                                      fn -> {
                                        return rowView.getRow(DigitalObjectImpl.class);
                                      });

                              if (rowView.getColumn("f_uuid", UUID.class) != null) {
                                digitalObject.setPreviewImage(
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
  public SearchPageResponse<DigitalObject> find(SearchPageRequest searchPageRequest) {
    // select only what is shown/needed in paged result list:
    StringBuilder query =
        new StringBuilder(
            "SELECT d.uuid d_uuid, d.refid d_refId, d.label d_label, d.description d_description,"
                + " d.entity_type d_entityType,"
                + " file.uuid f_uuid, file.filename f_filename, file.mimetype f_mimeType, file.size_in_bytes f_sizeInBytes, file.uri f_uri, file.http_base_url f_httpBaseUrl"
                + " FROM digitalobjects as d"
                + " LEFT JOIN fileresources_image as file on d.previewfileresource = file.uuid"
                + " LEFT JOIN LATERAL jsonb_object_keys(d.label) l(keys) on d.label is not null"
                + " LEFT JOIN LATERAL jsonb_object_keys(d.description) n(keys) on d.description is not null"
                + " WHERE (d.label->>l.keys ilike '%' || :searchTerm || '%'"
                + " OR d.description->>n.keys ilike '%' || :searchTerm || '%')");
    addPageRequestParams(searchPageRequest, query);

    List<DigitalObject> result =
        new ArrayList(
            dbi.withHandle(
                h ->
                    h.createQuery(query.toString())
                        .bind("searchTerm", searchPageRequest.getQuery())
                        .registerRowMapper(BeanMapper.factory(DigitalObjectImpl.class, "d"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .reduceRows(
                            new LinkedHashMap<UUID, DigitalObject>(),
                            (map, rowView) -> {
                              DigitalObject digitalObject =
                                  map.computeIfAbsent(
                                      rowView.getColumn("d_uuid", UUID.class),
                                      uuid -> rowView.getRow(DigitalObjectImpl.class));
                              if (rowView.getColumn("f_uuid", String.class) != null) {
                                digitalObject.setPreviewImage(
                                    rowView.getRow(ImageFileResourceImpl.class));
                              }
                              return map;
                            })
                        .values()));

    String countQuery =
        "SELECT count(*) FROM digitalobjects as d"
            + " LEFT JOIN LATERAL jsonb_object_keys(d.label) l(keys) on d.label is not null"
            + " LEFT JOIN LATERAL jsonb_object_keys(d.description) n(keys) on d.description is not null"
            + " WHERE (d.label->>l.keys ilike '%' || :searchTerm || '%'"
            + " OR d.description->>n.keys ilike '%' || :searchTerm || '%')";
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
  public DigitalObject findByIdentifier(String namespace, String id) {
    return findOne(new IdentifierImpl(null, namespace, id));
  }

  @Override
  public DigitalObject findOne(UUID uuid) {
    String query = FIND_ONE_BASE_SQL + " WHERE d.uuid = :uuid";

    DigitalObjectImpl result =
        dbi.withHandle(
                h ->
                    h.createQuery(query)
                        .bind("uuid", uuid)
                        .registerRowMapper(BeanMapper.factory(DigitalObjectImpl.class, "d"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .registerRowMapper(BeanMapper.factory(FileResourceImpl.class, "fr"))
                        .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                        .reduceRows(
                            new LinkedHashMap<UUID, DigitalObjectImpl>(),
                            (map, rowView) -> {
                              DigitalObjectImpl digitalObject =
                                  map.computeIfAbsent(
                                      rowView.getColumn("d_uuid", UUID.class),
                                      fn -> {
                                        return rowView.getRow(DigitalObjectImpl.class);
                                      });

                              if (rowView.getColumn("f_uuid", UUID.class) != null) {
                                digitalObject.setPreviewImage(
                                    rowView.getRow(ImageFileResourceImpl.class));
                              }

                              if (rowView.getColumn("id_uuid", UUID.class) != null) {
                                IdentifierImpl identifier = rowView.getRow(IdentifierImpl.class);
                                digitalObject.addIdentifier(identifier);
                              }

                              if (rowView.getColumn("fr_uuid", UUID.class) != null) {
                                FileResourceImpl fileResource =
                                    rowView.getRow(FileResourceImpl.class);
                                if (!digitalObject.getFileResources().contains(fileResource)) {
                                  digitalObject.getFileResources().add(fileResource);
                                }
                              }

                              return map;
                            }))
            .get(uuid);
    return result;
  }

  @Override
  public DigitalObject findOne(Identifier identifier) {
    if (identifier.getIdentifiable() != null) {
      return findOne(identifier.getIdentifiable());
    }

    String namespace = identifier.getNamespace();
    String identifierId = identifier.getId();

    String query = FIND_ONE_BASE_SQL + " WHERE id.identifier = :id AND id.namespace = :namespace";

    Optional<DigitalObjectImpl> result =
        dbi
            .withHandle(
                h ->
                    h.createQuery(query)
                        .bind("id", identifierId)
                        .bind("namespace", namespace)
                        .registerRowMapper(BeanMapper.factory(DigitalObjectImpl.class, "d"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .registerRowMapper(BeanMapper.factory(FileResourceImpl.class, "fr"))
                        .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                        .reduceRows(
                            new LinkedHashMap<UUID, DigitalObjectImpl>(),
                            (map, rowView) -> {
                              DigitalObjectImpl digitalObject =
                                  map.computeIfAbsent(
                                      rowView.getColumn("d_uuid", UUID.class),
                                      fn -> {
                                        return rowView.getRow(DigitalObjectImpl.class);
                                      });

                              if (rowView.getColumn("f_uuid", UUID.class) != null) {
                                digitalObject.setPreviewImage(
                                    rowView.getRow(ImageFileResourceImpl.class));
                              }

                              if (rowView.getColumn("id_uuid", UUID.class) != null) {
                                IdentifierImpl dbIidentifier = rowView.getRow(IdentifierImpl.class);
                                digitalObject.addIdentifier(dbIidentifier);
                              }

                              if (rowView.getColumn("fr_uuid", UUID.class) != null) {
                                FileResourceImpl fileResource =
                                    rowView.getRow(FileResourceImpl.class);
                                if (!digitalObject.getFileResources().contains(fileResource)) {
                                  digitalObject.getFileResources().add(fileResource);
                                }
                              }

                              return map;
                            }))
            .values()
            .stream()
            .findFirst();
    return result.orElse(null);
  }

  @Override
  public PageResponse<Collection> getCollections(UUID digitalObjectUuid, PageRequest pageRequest) {
    final String baseQuery =
        "SELECT c.uuid c_uuid, c.label c_label, c.refid c_refId,"
            + " c.created c_created, c.last_modified c_lastModified,"
            + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
            + " file.uuid pf_uuid, file.filename pf_filename, file.mimetype pf_mimeType, file.size_in_bytes pf_sizeInBytes, file.uri pf_uri, file.http_base_url pf_httpBaseUrl"
            + " FROM collections as c"
            + " LEFT JOIN identifiers as id on c.uuid = id.identifiable"
            + " LEFT JOIN fileresources_image as file on c.previewfileresource = file.uuid"
            + " LEFT JOIN collection_digitalobjects as cd on c.uuid = cd.collection_uuid"
            + " WHERE cd.digitalobject_uuid = :uuid";
    StringBuilder query = new StringBuilder(baseQuery);
    // handle optional filtering params
    String filterClauses = getFilterClauses(pageRequest.getFiltering());
    if (filterClauses.length() > 0) {
      query.append(" AND ").append(filterClauses);
    }
    query.append(" ORDER BY c.label");
    // we add fix sorting in above query; otherwise we get in conflict with allowed sorting
    // and column names of this repository (it is for digitalobjects, not sublists of
    // collections...)
    pageRequest.setSorting(null);
    addPageRequestParams(pageRequest, query);

    List<Collection> result =
        dbi.withHandle(
            h ->
                h
                    .createQuery(query.toString())
                    .bind("uuid", digitalObjectUuid)
                    .registerRowMapper(BeanMapper.factory(CollectionImpl.class, "c"))
                    .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "pf"))
                    .reduceRows(
                        new LinkedHashMap<UUID, CollectionImpl>(),
                        (map, rowView) -> {
                          CollectionImpl collection =
                              map.computeIfAbsent(
                                  rowView.getColumn("c_uuid", UUID.class),
                                  fn -> {
                                    return rowView.getRow(CollectionImpl.class);
                                  });

                          if (rowView.getColumn("pf_uuid", UUID.class) != null) {
                            collection.setPreviewImage(rowView.getRow(ImageFileResourceImpl.class));
                          }

                          if (rowView.getColumn("id_uuid", UUID.class) != null) {
                            IdentifierImpl dbIdentifier = rowView.getRow(IdentifierImpl.class);
                            collection.addIdentifier(dbIdentifier);
                          }
                          return map;
                        })
                    .values()
                    .stream()
                    .map(Collection.class::cast)
                    .collect(Collectors.toList()));
    String countQuery =
        "SELECT count(*) FROM collections as c"
            + " LEFT JOIN collection_digitalobjects as cd on c.uuid = cd.collection_uuid"
            + " WHERE cd.digitalobject_uuid = :uuid";
    if (filterClauses.length() > 0) {
      countQuery += " AND " + filterClauses;
    }
    final String sqlCount = countQuery;
    long total =
        dbi.withHandle(
            h ->
                h.createQuery(sqlCount)
                    .bind("uuid", digitalObjectUuid)
                    .mapTo(Long.class)
                    .findOne()
                    .get());
    PageResponse<Collection> pageResponse = new PageResponseImpl<>(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public List<FileResource> getFileResources(UUID digitalObjectUuid) {
    String query =
        "SELECT f.uuid f_uuid, f.label f_label,"
            + " f.created f_created, f.last_modified f_lastModified,"
            + " f.filename f_filename, f.mimetype f_mimeType, f.size_in_bytes f_sizeInBytes, f.uri f_uri,"
            + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
            + " file.uuid pf_uuid, file.filename pf_filename, file.mimetype pf_mimeType, file.size_in_bytes pf_sizeInBytes, file.uri pf_uri, file.http_base_url pf_httpBaseUrl"
            + " FROM fileresources as f"
            + " LEFT JOIN identifiers as id on f.uuid = id.identifiable"
            + " LEFT JOIN fileresources_image as file on f.previewfileresource = file.uuid"
            + " LEFT JOIN digitalobject_fileresources as df on f.uuid = df.fileresource_uuid"
            + " WHERE df.digitalobject_uuid = :uuid"
            + " ORDER BY df.sortIndex ASC";

    List<FileResource> result =
        dbi.withHandle(
            h ->
                h
                    .createQuery(query)
                    .bind("uuid", digitalObjectUuid)
                    .registerRowMapper(BeanMapper.factory(FileResourceImpl.class, "f"))
                    .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
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

                          if (rowView.getColumn("id_uuid", UUID.class) != null) {
                            IdentifierImpl dbIdentifier = rowView.getRow(IdentifierImpl.class);
                            fileResource.addIdentifier(dbIdentifier);
                          }
                          return map;
                        })
                    .values()
                    .stream()
                    .map(FileResource.class::cast)
                    .collect(Collectors.toList()));
    return result;
  }

  @Override
  public List<ImageFileResource> getImageFileResources(UUID digitalObjectUuid) {
    String query =
        "SELECT f.uuid f_uuid, f.label f_label,"
            + " f.created f_created, f.last_modified f_lastModified,"
            + " f.filename f_filename, f.mimetype f_mimeType, f.size_in_bytes f_sizeInBytes, f.uri f_uri,"
            + " f.height f_height, f.width f_width,"
            + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
            + " file.uuid pf_uuid, file.filename pf_filename, file.mimetype pf_mimeType, file.size_in_bytes pf_sizeInBytes, file.uri pf_uri, file.http_base_url pf_httpBaseUrl"
            + " FROM fileresources_image as f"
            + " LEFT JOIN identifiers as id on f.uuid = id.identifiable"
            + " LEFT JOIN fileresources_image as file on f.previewfileresource = file.uuid"
            + " LEFT JOIN digitalobject_fileresources as df on f.uuid = df.fileresource_uuid"
            + " WHERE df.digitalobject_uuid = :uuid"
            + " ORDER BY df.sortIndex ASC";

    List<ImageFileResource> result =
        dbi.withHandle(
            h ->
                h
                    .createQuery(query)
                    .bind("uuid", digitalObjectUuid)
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                    .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                    // TODO: test it if it is working, because I think there were problems using two
                    // rowmappers of same type...
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "pf"))
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
                            IdentifierImpl dbIdentifier = rowView.getRow(IdentifierImpl.class);
                            fileResource.addIdentifier(dbIdentifier);
                          }
                          return map;
                        })
                    .values()
                    .stream()
                    .map(ImageFileResource.class::cast)
                    .collect(Collectors.toList()));
    return result;
  }

  @Override
  public PageResponse<Project> getProjects(UUID digitalObjectUuid, PageRequest pageRequest) {
    final String baseQuery =
        "SELECT p.uuid p_uuid, p.label p_label, p.refid p_refId,"
            + " p.created p_created, p.last_modified p_lastModified,"
            + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
            + " file.uuid pf_uuid, file.filename pf_filename, file.mimetype pf_mimeType, file.size_in_bytes pf_sizeInBytes, file.uri pf_uri, file.http_base_url pf_httpBaseUrl"
            + " FROM projects as p"
            + " LEFT JOIN identifiers as id on p.uuid = id.identifiable"
            + " LEFT JOIN fileresources_image as file on p.previewfileresource = file.uuid"
            + " LEFT JOIN project_digitalobjects as pd on p.uuid = pd.project_uuid"
            + " WHERE pd.digitalobject_uuid = :uuid"
            + " ORDER BY p.label";
    StringBuilder query = new StringBuilder(baseQuery);

    // we add fix sorting in above query; otherwise we get in conflict with allowed sorting
    // and column names of this repository (it is for digitalobjects, not sublists of
    // projects...)
    pageRequest.setSorting(null);
    addPageRequestParams(pageRequest, query);

    List<Project> result =
        dbi.withHandle(
            h ->
                h
                    .createQuery(query.toString())
                    .bind("uuid", digitalObjectUuid)
                    .registerRowMapper(BeanMapper.factory(ProjectImpl.class, "p"))
                    .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "pf"))
                    .reduceRows(
                        new LinkedHashMap<UUID, ProjectImpl>(),
                        (map, rowView) -> {
                          ProjectImpl project =
                              map.computeIfAbsent(
                                  rowView.getColumn("p_uuid", UUID.class),
                                  fn -> {
                                    return rowView.getRow(ProjectImpl.class);
                                  });

                          if (rowView.getColumn("pf_uuid", UUID.class) != null) {
                            project.setPreviewImage(rowView.getRow(ImageFileResourceImpl.class));
                          }

                          if (rowView.getColumn("id_uuid", UUID.class) != null) {
                            IdentifierImpl dbIdentifier = rowView.getRow(IdentifierImpl.class);
                            project.addIdentifier(dbIdentifier);
                          }
                          return map;
                        })
                    .values()
                    .stream()
                    .map(Project.class::cast)
                    .collect(Collectors.toList()));
    String countQuery =
        "SELECT count(*) FROM projects as p"
            + " LEFT JOIN project_digitalobjects as pd on p.uuid = pd.project_uuid"
            + " WHERE pd.digitalobject_uuid = :uuid";
    long total =
        dbi.withHandle(
            h ->
                h.createQuery(countQuery)
                    .bind("uuid", digitalObjectUuid)
                    .mapTo(Long.class)
                    .findOne()
                    .get());
    PageResponse<Project> pageResponse = new PageResponseImpl<>(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public DigitalObject save(DigitalObject digitalObject) {
    digitalObject.setUuid(UUID.randomUUID());
    digitalObject.setCreated(LocalDateTime.now());
    digitalObject.setLastModified(LocalDateTime.now());
    // refid is generated as serial, DO NOT SET!
    final UUID previewImageUuid =
        digitalObject.getPreviewImage() == null ? null : digitalObject.getPreviewImage().getUuid();

    String query =
        "INSERT INTO digitalobjects("
            + "uuid, label, description, previewFileResource, preview_hints,"
            + " identifiable_type, entity_type,"
            + " created, last_modified"
            + ") VALUES ("
            + ":uuid, :label::JSONB, :description::JSONB, :previewFileResource, :previewImageRenderingHints::JSONB,"
            + " :type, :entityType,"
            + " :created, :lastModified"
            + ")";

    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("previewFileResource", previewImageUuid)
                .bindBean(digitalObject)
                .execute());

    // for now we implement first interesting use case: new digital object with new fileresources...
    final List<FileResource> fileResources = digitalObject.getFileResources();
    saveFileResources(digitalObject, fileResources);

    // save identifiers
    Set<Identifier> identifiers = digitalObject.getIdentifiers();
    saveIdentifiers(identifiers, digitalObject);

    DigitalObject result = findOne(digitalObject.getUuid());
    return result;
  }

  @Override
  public List<FileResource> saveFileResources(
      DigitalObject digitalObject, List<FileResource> fileResources) {
    if (fileResources == null) {
      return null;
    }
    return saveFileResources(digitalObject.getUuid(), fileResources);
  }

  @Override
  public List<FileResource> saveFileResources(
      UUID digitalObjectUuid, List<FileResource> fileResources) {

    // as we store the whole list new: delete old entries
    dbi.withHandle(
        h ->
            h.createUpdate(
                    "DELETE FROM digitalobject_fileresources WHERE digitalobject_uuid = :uuid")
                .bind("uuid", digitalObjectUuid)
                .execute());

    if (fileResources != null) {
      // first save fileresources
      for (FileResource fileResource : fileResources) {
        if (fileResource.getUuid() == null) {
          fileResource = fileResourceMetadataRepository.save(fileResource);
        }
      }

      // second: save relations to digital object
      dbi.useHandle(
          handle -> {
            PreparedBatch preparedBatch =
                handle.prepareBatch(
                    "INSERT INTO digitalobject_fileresources(digitalobject_uuid, fileresource_uuid, sortIndex) VALUES(:uuid, :fileResourceUuid, :sortIndex)");
            for (FileResource fileResource : fileResources) {
              preparedBatch
                  .bind("uuid", digitalObjectUuid)
                  .bind("fileResourceUuid", fileResource.getUuid())
                  .bind("sortIndex", getIndex(fileResources, fileResource))
                  .add();
            }
            preparedBatch.execute();
          });
    }
    return getFileResources(digitalObjectUuid);
  }

  @Override
  public void deleteFileResources(UUID digitalObjectUuid) {
    dbi.withHandle(
        h ->
            h.createUpdate(
                    "DELETE FROM digitalobject_fileresources WHERE digitalobject_uuid = :uuid")
                .bind("uuid", digitalObjectUuid)
                .execute());
  }

  @Override
  public DigitalObject update(DigitalObject digitalObject) {
    digitalObject.setLastModified(LocalDateTime.now());

    // do not update/left out from statement (not changed since insert):
    // uuid, created, identifiable_type, entity_type, refid
    final UUID previewImageUuid =
        digitalObject.getPreviewImage() == null ? null : digitalObject.getPreviewImage().getUuid();

    String query =
        "UPDATE digitalobjects SET"
            + " label=:label::JSONB, description=:description::JSONB,"
            + " previewFileResource=:previewFileResource, preview_hints=:previewImageRenderingHints::JSONB,"
            + " last_modified=:lastModified"
            + " WHERE uuid=:uuid";

    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("previewFileResource", previewImageUuid)
                .bindBean(digitalObject)
                .execute());

    // save identifiers
    // as we store the whole list new: delete old entries
    deleteIdentifiers(digitalObject);
    Set<Identifier> identifiers = digitalObject.getIdentifiers();
    saveIdentifiers(identifiers, digitalObject);

    DigitalObject result = findOne(digitalObject.getUuid());
    return result;
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[] {"created", "lastModified", "refId"};
  }

  @Override
  protected String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "created":
        return "d.created";
      case "lastModified":
        return "d.last_modified";
      case "refId":
        return "d.refid";
      default:
        return null;
    }
  }

  @Override
  public boolean deleteIdentifiers(UUID digitalObjectUuid) {
    DigitalObject digitalObject = findOne(digitalObjectUuid);
    if (digitalObject == null) {
      return false;
    }

    identifierRepository.delete(
        digitalObject.getIdentifiers().stream()
            .map(Identifier::getUuid)
            .collect(Collectors.toList()));

    return true;
  }
}
