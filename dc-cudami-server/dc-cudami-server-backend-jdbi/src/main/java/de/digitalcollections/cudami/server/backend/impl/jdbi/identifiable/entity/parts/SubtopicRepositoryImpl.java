package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.parts;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.parts.SubtopicRepository;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.Node;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.Topic;
import de.digitalcollections.model.api.identifiable.entity.parts.Subtopic;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.view.BreadcrumbNavigation;
import de.digitalcollections.model.impl.identifiable.IdentifierImpl;
import de.digitalcollections.model.impl.identifiable.NodeImpl;
import de.digitalcollections.model.impl.identifiable.entity.EntityImpl;
import de.digitalcollections.model.impl.identifiable.entity.TopicImpl;
import de.digitalcollections.model.impl.identifiable.entity.parts.SubtopicImpl;
import de.digitalcollections.model.impl.identifiable.resource.FileResourceImpl;
import de.digitalcollections.model.impl.identifiable.resource.ImageFileResourceImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import de.digitalcollections.model.impl.view.BreadcrumbNavigationImpl;
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
public class SubtopicRepositoryImpl extends EntityPartRepositoryImpl<Subtopic, Entity>
    implements SubtopicRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(SubtopicRepositoryImpl.class);

  // select all details shown/needed in single object details page
  private static final String FIND_ONE_BASE_SQL =
      "SELECT s.uuid s_uuid, s.label s_label, s.description s_description,"
          + " s.identifiable_type s_type,"
          + " s.created s_created, s.last_modified s_lastModified,"
          + " s.preview_hints s_previewImageRenderingHints,"
          + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
          + " file.uuid f_uuid, file.filename f_filename, file.mimetype f_mimeType, file.size_in_bytes f_sizeInBytes, file.uri f_uri, file.http_base_url f_httpBaseUrl"
          + " FROM subtopics as s"
          + " LEFT JOIN identifiers as id on s.uuid = id.identifiable"
          + " LEFT JOIN fileresources_image as file on s.previewfileresource = file.uuid";

  // select only what is shown/needed in paged list (to avoid unnecessary payload/traffic):
  private static final String REDUCED_FIND_ONE_BASE_SQL =
      "SELECT s.uuid s_uuid, s.label s_label, s.description s_description,"
          + " s.identifiable_type s_type,"
          + " s.created s_created, s.last_modified s_lastModified,"
          + " s.preview_hints s_previewImageRenderingHints,"
          + " file.uuid f_uuid, file.filename f_filename, file.mimetype f_mimeType, file.size_in_bytes f_sizeInBytes, file.uri f_uri, file.http_base_url f_httpBaseUrl"
          + " FROM subtopics as s"
          + " LEFT JOIN fileresources_image as file on s.previewfileresource = file.uuid";

  private static final String BASE_CHILDREN_QUERY =
      "SELECT s.uuid s_uuid, s.label s_label, s.description s_description,"
          + " s.identifiable_type s_type,"
          + " s.created s_created, s.last_modified s_lastModified,"
          + " s.preview_hints s_previewImageRenderingHints,"
          + " file.uuid f_uuid, file.filename f_filename, file.mimetype f_mimeType, file.size_in_bytes f_sizeInBytes, file.uri f_uri, file.http_base_url f_httpBaseUrl"
          + " FROM subtopics as s INNER JOIN subtopic_subtopics ss ON s.uuid = ss.child_subtopic_uuid"
          + " LEFT JOIN fileresources_image as file on s.previewfileresource = file.uuid"
          + " WHERE ss.parent_subtopic_uuid = :uuid";

  private static final String BREADCRUMB_QUERY =
      "WITH recursive breadcrumb (uuid,label,parent_uuid,depth)"
          + " AS ("
          + "        SELECT s.uuid as uuid, s.label as label, ss.parent_subtopic_uuid as parent_uuid,99 as depth"
          + "        FROM subtopics s, subtopic_subtopics ss"
          + "        WHERE uuid= :uuid and ss.child_subtopic_uuid = s.uuid"
          + ""
          + "        UNION ALL"
          + "        SELECT s.uuid as uuid, s.label as label, ss.parent_subtopic_uuid as parent_uuid, depth-1 as depth"
          + "        FROM subtopics s,"
          + "             subtopic_subtopics ss,"
          + "             breadcrumb b"
          + "        WHERE b.uuid = ss.child_subtopic_uuid and ss.parent_subtopic_uuid = s.uuid AND ss.parent_subtopic_uuid is not null"
          + "    )"
          + " SELECT * from breadcrumb"
          + " UNION"
          + " SELECT null as uuid, t.label as label, null as parent_uuid, 0 as depth"
          + " FROM topics t, topic_subtopics ts, breadcrumb b"
          + " WHERE ts.subtopic_uuid = b.parent_uuid and t.uuid = ts.topic_uuid"
          + " ORDER BY depth ASC";

  private static final String BREADCRUMB_WITHOUT_PARENT_QUERY =
      "SELECT s.uuid as uuid, s.label as label"
          + "        FROM subtopics s"
          + "        WHERE uuid= :uuid";

  @Autowired
  public SubtopicRepositoryImpl(Jdbi dbi, IdentifierRepository identifierRepository) {
    super(dbi, identifierRepository);
  }

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM subtopics";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOne().get());
    return count;
  }

  @Override
  public PageResponse<Subtopic> find(PageRequest pageRequest) {
    StringBuilder query = new StringBuilder(REDUCED_FIND_ONE_BASE_SQL);
    addPageRequestParams(pageRequest, query);

    List<SubtopicImpl> result =
        new ArrayList(
            dbi.withHandle(
                h ->
                    h.createQuery(query.toString())
                        .registerRowMapper(BeanMapper.factory(SubtopicImpl.class, "s"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .reduceRows(
                            new LinkedHashMap<UUID, SubtopicImpl>(),
                            (map, rowView) -> {
                              SubtopicImpl subtopic =
                                  map.computeIfAbsent(
                                      rowView.getColumn("s_uuid", UUID.class),
                                      fn -> {
                                        return rowView.getRow(SubtopicImpl.class);
                                      });

                              if (rowView.getColumn("f_uuid", UUID.class) != null) {
                                subtopic.setPreviewImage(
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
  public Subtopic findOne(UUID uuid) {
    String query = FIND_ONE_BASE_SQL + " WHERE s.uuid = :uuid";

    SubtopicImpl result =
        dbi.withHandle(
                h ->
                    h.createQuery(query)
                        .bind("uuid", uuid)
                        .registerRowMapper(BeanMapper.factory(SubtopicImpl.class, "s"))
                        .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .reduceRows(
                            new LinkedHashMap<UUID, SubtopicImpl>(),
                            (map, rowView) -> {
                              SubtopicImpl subtopic =
                                  map.computeIfAbsent(
                                      rowView.getColumn("s_uuid", UUID.class),
                                      fn -> {
                                        return rowView.getRow(SubtopicImpl.class);
                                      });

                              if (rowView.getColumn("f_uuid", UUID.class) != null) {
                                subtopic.setPreviewImage(
                                    rowView.getRow(ImageFileResourceImpl.class));
                              }

                              if (rowView.getColumn("id_uuid", UUID.class) != null) {
                                IdentifierImpl identifier = rowView.getRow(IdentifierImpl.class);
                                subtopic.addIdentifier(identifier);
                              }

                              return map;
                            }))
            .get(uuid);

    if (result != null) {
      // TODO could be replaced with another join in above query...
      result.setChildren(getChildren(result));
    }
    return result;
  }

  @Override
  public Subtopic findOne(Identifier identifier) {
    if (identifier.getIdentifiable() != null) {
      return findOne(identifier.getIdentifiable());
    }

    String namespace = identifier.getNamespace();
    String identifierId = identifier.getId();

    String query = FIND_ONE_BASE_SQL + " WHERE id.identifier = :id AND id.namespace = :namespace";

    Optional<SubtopicImpl> result =
        dbi
            .withHandle(
                h ->
                    h.createQuery(query)
                        .bind("id", identifierId)
                        .bind("namespace", namespace)
                        .registerRowMapper(BeanMapper.factory(SubtopicImpl.class, "s"))
                        .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .reduceRows(
                            new LinkedHashMap<UUID, SubtopicImpl>(),
                            (map, rowView) -> {
                              SubtopicImpl subtopic =
                                  map.computeIfAbsent(
                                      rowView.getColumn("s_uuid", UUID.class),
                                      fn -> {
                                        return rowView.getRow(SubtopicImpl.class);
                                      });

                              if (rowView.getColumn("f_uuid", UUID.class) != null) {
                                subtopic.setPreviewImage(
                                    rowView.getRow(ImageFileResourceImpl.class));
                              }

                              if (rowView.getColumn("id_uuid", UUID.class) != null) {
                                IdentifierImpl dbIdentifier = rowView.getRow(IdentifierImpl.class);
                                subtopic.addIdentifier(dbIdentifier);
                              }

                              return map;
                            }))
            .values()
            .stream()
            .findFirst();

    Subtopic subtopic = result.orElse(null);
    if (subtopic != null) {
      // TODO could be replaced with another join in above query...
      subtopic.setChildren(getChildren(subtopic));
    }
    return subtopic;
  }

  @Override
  public List<Subtopic> getChildren(UUID uuid) {
    // minimal data required (= identifiable fields) for creating text links/teasers in a list
    String query = BASE_CHILDREN_QUERY + " ORDER BY ss.sortIndex ASC";

    List<Subtopic> result =
        new ArrayList(
            dbi.withHandle(
                h ->
                    h.createQuery(query)
                        .bind("uuid", uuid)
                        .registerRowMapper(BeanMapper.factory(SubtopicImpl.class, "s"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .reduceRows(
                            new LinkedHashMap<UUID, SubtopicImpl>(),
                            (map, rowView) -> {
                              SubtopicImpl subtopic =
                                  map.computeIfAbsent(
                                      rowView.getColumn("s_uuid", UUID.class),
                                      fn -> {
                                        return rowView.getRow(SubtopicImpl.class);
                                      });

                              if (rowView.getColumn("f_uuid", UUID.class) != null) {
                                subtopic.setPreviewImage(
                                    rowView.getRow(ImageFileResourceImpl.class));
                              }
                              return map;
                            })
                        .values()));
    return result;
  }

  @Override
  public PageResponse<Subtopic> getChildren(UUID uuid, PageRequest pageRequest) {
    // minimal data required (= identifiable fields) for creating text links/teasers in a list
    StringBuilder query = new StringBuilder(BASE_CHILDREN_QUERY);
    addPageRequestParams(pageRequest, query);
    List<Subtopic> result =
        new ArrayList(
            dbi.withHandle(
                h ->
                    h.createQuery(query.toString())
                        .bind("uuid", uuid)
                        .registerRowMapper(BeanMapper.factory(SubtopicImpl.class, "s"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .reduceRows(
                            new LinkedHashMap<UUID, SubtopicImpl>(),
                            (map, rowView) -> {
                              SubtopicImpl subtopic =
                                  map.computeIfAbsent(
                                      rowView.getColumn("s_uuid", UUID.class),
                                      fn -> {
                                        return rowView.getRow(SubtopicImpl.class);
                                      });

                              if (rowView.getColumn("f_uuid", UUID.class) != null) {
                                subtopic.setPreviewImage(
                                    rowView.getRow(ImageFileResourceImpl.class));
                              }
                              return map;
                            })
                        .values()));
    String sql =
        "SELECT count(*) FROM subtopics as s"
            + " INNER JOIN subtopic_subtopics ss ON s.uuid = ss.child_subtopic_uuid"
            + " WHERE ss.parent_subtopic_uuid = :uuid";
    long total =
        dbi.withHandle(
            h -> h.createQuery(sql).bind("uuid", uuid).mapTo(Long.class).findOne().get());
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public List<Entity> getEntities(Subtopic subtopic) {
    return getEntities(subtopic.getUuid());
  }

  @Override
  public List<Entity> getEntities(UUID subtopicUuid) {
    // minimal data required (= identifiable fields) for creating text links/teasers in a list
    String query =
        "SELECT e.uuid e_uuid, e.refid e_refId, e.label e_label, e.description e_description,"
            + " e.identifiable_type e_type, e.entity_type e_entityType,"
            + " e.created e_created, e.last_modified e_lastModified,"
            + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
            + " file.uuid f_uuid, file.filename f_filename, file.mimetype f_mimeType, file.size_in_bytes f_sizeInBytes, file.uri f_uri, file.http_base_url f_httpBaseUrl"
            + " FROM entities as e INNER JOIN subtopic_entities se ON e.uuid = se.entity_uuid"
            + " LEFT JOIN fileresources_image as file on e.previewfileresource = file.uuid"
            + " LEFT JOIN identifiers as id on e.uuid = id.identifiable"
            + " WHERE se.subtopic_uuid = :uuid"
            + " ORDER BY se.sortIndex ASC";

    List<Entity> result =
        dbi
            .withHandle(
                h ->
                    h.createQuery(query)
                        .bind("uuid", subtopicUuid)
                        .registerRowMapper(BeanMapper.factory(EntityImpl.class, "e"))
                        .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .reduceRows(
                            new LinkedHashMap<UUID, EntityImpl>(),
                            (map, rowView) -> {
                              EntityImpl entity =
                                  map.computeIfAbsent(
                                      rowView.getColumn("e_uuid", UUID.class),
                                      fn -> {
                                        return rowView.getRow(EntityImpl.class);
                                      });

                              if (rowView.getColumn("f_uuid", UUID.class) != null) {
                                entity.setPreviewImage(rowView.getRow(ImageFileResourceImpl.class));
                              }

                              if (rowView.getColumn("id_uuid", UUID.class) != null) {
                                IdentifierImpl dbIdentifier = rowView.getRow(IdentifierImpl.class);
                                entity.addIdentifier(dbIdentifier);
                              }

                              return map;
                            }))
            .values()
            .stream()
            .map(e -> (Entity) e)
            .collect(Collectors.toList());
    return result;
  }

  @Override
  public List<FileResource> getFileResources(UUID subtopicUuid) {
    // minimal data required (= identifiable fields) for creating text links/teasers in a list
    String query =
        "SELECT f.uuid f_uuid, f.label f_label, f.description f_description,"
            + " f.identifiable_type f_type,"
            + " f.created f_created, f.last_modified f_lastModified,"
            + " f.filename f_filename, f.mimetype f_mimeType,"
            + " pf.uuid pf_uuid, pf.filename pf_filename, pf.mimetype pf_mimeType, pf.size_in_bytes pf_sizeInBytes, pf.uri pf_uri, pf.http_base_url pf_httpBaseUrl"
            + " FROM fileresources as f INNER JOIN subtopic_fileresources sf ON f.uuid = sf.fileresource_uuid"
            + " LEFT JOIN fileresources_image as pf on f.previewfileresource = pf.uuid"
            + " WHERE sf.subtopic_uuid = :uuid"
            + " ORDER BY sf.sortIndex ASC";

    List<FileResource> result =
        dbi
            .withHandle(
                h ->
                    h.createQuery(query)
                        .bind("uuid", subtopicUuid)
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
                            }))
            .values()
            .stream()
            .map(FileResource.class::cast)
            .collect(Collectors.toList());
    return result;
  }

  @Override
  public Subtopic getParent(UUID uuid) {
    String query =
        REDUCED_FIND_ONE_BASE_SQL
            + " INNER JOIN subtopic_subtopics ss ON s.uuid = ss.parent_subtopic_uuid"
            + " WHERE ss.child_subtopic_uuid = :uuid";

    Optional<SubtopicImpl> result =
        dbi
            .withHandle(
                h ->
                    h.createQuery(query)
                        .bind("uuid", uuid)
                        .registerRowMapper(BeanMapper.factory(SubtopicImpl.class, "s"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .reduceRows(
                            new LinkedHashMap<UUID, SubtopicImpl>(),
                            (map, rowView) -> {
                              SubtopicImpl parentSubtopic =
                                  map.computeIfAbsent(
                                      rowView.getColumn("s_uuid", UUID.class),
                                      fn -> {
                                        return rowView.getRow(SubtopicImpl.class);
                                      });

                              if (rowView.getColumn("f_uuid", UUID.class) != null) {
                                parentSubtopic.setPreviewImage(
                                    rowView.getRow(ImageFileResourceImpl.class));
                              }
                              return map;
                            }))
            .values()
            .stream()
            .findFirst();
    return result.orElse(null);
  }

  @Override
  public Subtopic save(Subtopic subtopic) {
    subtopic.setUuid(UUID.randomUUID());
    subtopic.setCreated(LocalDateTime.now());
    subtopic.setLastModified(LocalDateTime.now());
    final UUID previewImageUuid =
        subtopic.getPreviewImage() == null ? null : subtopic.getPreviewImage().getUuid();

    String query =
        "INSERT INTO subtopics("
            + "uuid, label, description, previewfileresource, preview_hints,"
            + " identifiable_type,"
            + " created, last_modified"
            + ") VALUES ("
            + ":uuid, :label::JSONB, :description::JSONB, :previewFileResource, :previewImageRenderingHints::JSONB,"
            + " :type,"
            + " :created, :lastModified"
            + ")";

    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("previewFileResource", previewImageUuid)
                .bindBean(subtopic)
                .execute());

    // save identifiers
    Set<Identifier> identifiers = subtopic.getIdentifiers();
    saveIdentifiers(identifiers, subtopic);

    Subtopic result = findOne(subtopic.getUuid());
    return result;
  }

  @Override
  public List<Entity> saveEntities(UUID subtopicUuid, List<Entity> entities) {
    // as we store the whole list new: delete old entries
    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM subtopic_entities WHERE subtopic_uuid = :uuid")
                .bind("uuid", subtopicUuid)
                .execute());

    if (entities != null) {
      // we assume that the entities are already saved...
      dbi.useHandle(
          handle -> {
            PreparedBatch preparedBatch =
                handle.prepareBatch(
                    "INSERT INTO subtopic_entities(subtopic_uuid, entity_uuid, sortIndex) VALUES(:uuid, :entityUuid, :sortIndex)");
            for (Entity entity : entities) {
              preparedBatch
                  .bind("uuid", subtopicUuid)
                  .bind("entityUuid", entity.getUuid())
                  .bind("sortIndex", getIndex(entities, entity))
                  .add();
            }
            preparedBatch.execute();
          });
    }
    return getEntities(subtopicUuid);
  }

  @Override
  public List<Subtopic> getSubtopicsOfEntity(UUID entityUuid) {
    String query =
        REDUCED_FIND_ONE_BASE_SQL
            + " INNER JOIN subtopic_entities se ON s.uuid = se.subtopic_uuid"
            + " WHERE se.entity_uuid = :uuid";

    List<Subtopic> result =
        dbi
            .withHandle(
                h ->
                    h.createQuery(query)
                        .bind("uuid", entityUuid)
                        .registerRowMapper(BeanMapper.factory(SubtopicImpl.class, "s"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .reduceRows(
                            new LinkedHashMap<UUID, SubtopicImpl>(),
                            (map, rowView) -> {
                              SubtopicImpl parentSubtopic =
                                  map.computeIfAbsent(
                                      rowView.getColumn("s_uuid", UUID.class),
                                      fn -> {
                                        return rowView.getRow(SubtopicImpl.class);
                                      });

                              if (rowView.getColumn("f_uuid", UUID.class) != null) {
                                parentSubtopic.setPreviewImage(
                                    rowView.getRow(ImageFileResourceImpl.class));
                              }
                              return map;
                            }))
            .values()
            .stream()
            .map(Subtopic.class::cast)
            .collect(Collectors.toList());
    return result;
  }

  @Override
  public List<Subtopic> getSubtopicsOfFileResource(UUID fileResourceUuid) {
    String query =
        REDUCED_FIND_ONE_BASE_SQL
            + " INNER JOIN subtopic_fileresources sf ON s.uuid = sf.subtopic_uuid"
            + " WHERE sf.fileresource_uuid = :uuid";

    List<Subtopic> result =
        dbi
            .withHandle(
                h ->
                    h.createQuery(query)
                        .bind("uuid", fileResourceUuid)
                        .registerRowMapper(BeanMapper.factory(SubtopicImpl.class, "s"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .reduceRows(
                            new LinkedHashMap<UUID, SubtopicImpl>(),
                            (map, rowView) -> {
                              SubtopicImpl parentSubtopic =
                                  map.computeIfAbsent(
                                      rowView.getColumn("s_uuid", UUID.class),
                                      fn -> {
                                        return rowView.getRow(SubtopicImpl.class);
                                      });

                              if (rowView.getColumn("f_uuid", UUID.class) != null) {
                                parentSubtopic.setPreviewImage(
                                    rowView.getRow(ImageFileResourceImpl.class));
                              }
                              return map;
                            }))
            .values()
            .stream()
            .map(Subtopic.class::cast)
            .collect(Collectors.toList());
    return result;
  }

  @Override
  public List<FileResource> saveFileResources(UUID subtopicUuid, List<FileResource> fileResources) {
    // as we store the whole list new: delete old entries
    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM subtopic_fileresources WHERE subtopic_uuid = :uuid")
                .bind("uuid", subtopicUuid)
                .execute());

    if (fileResources != null) {
      // we assume that the fileresources are already saved...
      dbi.useHandle(
          handle -> {
            PreparedBatch preparedBatch =
                handle.prepareBatch(
                    "INSERT INTO subtopic_fileresources(subtopic_uuid, fileresource_uuid, sortIndex) VALUES(:uuid, :fileResourceUuid, :sortIndex)");
            for (FileResource fileResource : fileResources) {
              preparedBatch
                  .bind("uuid", subtopicUuid)
                  .bind("fileResourceUuid", fileResource.getUuid())
                  .bind("sortIndex", getIndex(fileResources, fileResource))
                  .add();
            }
            preparedBatch.execute();
          });
    }
    return getFileResources(subtopicUuid);
  }

  @Override
  public Subtopic saveWithParentSubtopic(Subtopic subtopic, UUID parentSubtopicUuid) {
    final UUID childSubtopicUuid =
        subtopic.getUuid() == null ? save(subtopic).getUuid() : subtopic.getUuid();
    Integer sortindex =
        retrieveNextSortIndexForParentChildren(
            dbi, "subtopic_subtopics", "parent_subtopic_uuid", parentSubtopicUuid);
    
    dbi.withHandle(
        h ->
            h.createUpdate(
                    "INSERT INTO subtopic_subtopics(parent_subtopic_uuid, child_subtopic_uuid, sortindex)"
                        + " VALUES (:parent_subtopic_uuid, :child_subtopic_uuid, :sortindex)")
                .bind("parent_subtopic_uuid", parentSubtopicUuid)
                .bind("child_subtopic_uuid", childSubtopicUuid)
                .bind("sortindex", sortindex)
                .execute());

    return findOne(childSubtopicUuid);
  }

  @Override
  public Integer deleteFromParentSubtopic(UUID subtopicUuid, UUID parentSubtopicUuid) {
    Integer count =
        dbi.withHandle(
            h ->
                h.createUpdate(
                        "DELETE FROM subtopic_subtopics WHERE parent_subtopic_uuid=:parent_subtopic_uuid AND child_subtopic_uuid=:child_subtopic_uuid")
                    .bind("parent_subtopic_uuid", parentSubtopicUuid)
                    .bind("child_subtopic_uuid", subtopicUuid)
                    .execute());
    return count;
  }

  @Override
  public Subtopic saveWithParentTopic(Subtopic subtopic, UUID parentTopicUuid) {
    final UUID childSubtopicUuid =
        subtopic.getUuid() == null ? save(subtopic).getUuid() : subtopic.getUuid();
    Integer sortindex =
        retrieveNextSortIndexForParentChildren(dbi, "topic_subtopics", "topic_uuid", parentTopicUuid);

    dbi.withHandle(
        h ->
            h.createUpdate(
                    "INSERT INTO topic_subtopics(topic_uuid, subtopic_uuid, sortindex)"
                        + " VALUES (:parent_topic_uuid, :child_subtopic_uuid, :sortindex)")
                .bind("parent_topic_uuid", parentTopicUuid)
                .bind("child_subtopic_uuid", childSubtopicUuid)
                .bind("sortindex", sortindex)
                .execute());

    return findOne(childSubtopicUuid);
  }

  @Override
  public Integer deleteFromParentTopic(UUID subtopicUuid, UUID topicUuid) {
    Integer count =
        dbi.withHandle(
            h ->
                h.createUpdate(
                        "DELETE FROM topic_subtopics WHERE topic_uuid=:topic_uuid AND subtopic_uuid=:subtopic_uuid")
                    .bind("topic_uuid", topicUuid)
                    .bind("subtopic_uuid", subtopicUuid)
                    .execute());
    return count;
  }

  @Override
  public Subtopic update(Subtopic subtopic) {
    subtopic.setLastModified(LocalDateTime.now());

    // do not update/left out from statement (not changed since insert):
    // uuid, created, identifiable_type
    final UUID previewImageUuid =
        subtopic.getPreviewImage() == null ? null : subtopic.getPreviewImage().getUuid();

    String query =
        "UPDATE subtopics SET"
            + " label=:label::JSONB, description=:description::JSONB,"
            + " previewfileresource=:previewFileResource, preview_hints=:previewImageRenderingHints::JSONB,"
            + " last_modified=:lastModified"
            + " WHERE uuid=:uuid";

    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("previewFileResource", previewImageUuid)
                .bindBean(subtopic)
                .execute());

    // save identifiers
    // as we store the whole list new: delete old entries
    deleteIdentifiers(subtopic);
    Set<Identifier> identifiers = subtopic.getIdentifiers();
    saveIdentifiers(identifiers, subtopic);

    Subtopic result = findOne(subtopic.getUuid());
    return result;
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[] {"created", "lastModified"};
  }

  @Override
  protected String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "created":
        return "s.created";
      case "lastModified":
        return "s.last_modified";
      default:
        return null;
    }
  }

  @Override
  public BreadcrumbNavigation getBreadcrumbNavigation(UUID nodeUuid) {

    List<NodeImpl> result =
        dbi.withHandle(
            h ->
                h.createQuery(BREADCRUMB_QUERY)
                    .bind("uuid", nodeUuid)
                    .registerRowMapper(BeanMapper.factory(NodeImpl.class))
                    .mapTo(NodeImpl.class)
                    .list());

    if (result.isEmpty()) {
      // Special case: If we are on a top level subtopic, we have no parent, so
      // we must construct a breadcrumb more or less manually
      result =
          dbi.withHandle(
              h ->
                  h.createQuery(BREADCRUMB_WITHOUT_PARENT_QUERY)
                      .bind("uuid", nodeUuid)
                      .registerRowMapper(BeanMapper.factory(NodeImpl.class))
                      .mapTo(NodeImpl.class)
                      .list());
    }

    List<Node> nodes = result.stream().map(s -> (Node) s).collect(Collectors.toList());
    return new BreadcrumbNavigationImpl(nodes);
  }

  @Override
  public Topic getTopic(UUID rootSubtopicUuid) {
    String query =
        "SELECT uuid, refid, label"
            + " FROM topics"
            + " INNER JOIN topic_subtopics ts ON uuid = ts.topic_uuid"
            + " WHERE ts.subtopic_uuid = :uuid";

    TopicImpl result =
        dbi.withHandle(
            h ->
                h.createQuery(query)
                    .bind("uuid", rootSubtopicUuid)
                    .mapToBean(TopicImpl.class)
                    .one());
    return result;
  }
}
