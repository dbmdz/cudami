package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.parts;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.parts.SubtopicRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifiableRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.EntityRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource.FileResourceMetadataRepositoryImpl;
import de.digitalcollections.model.api.filter.Filtering;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.Node;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.Topic;
import de.digitalcollections.model.api.identifiable.entity.parts.Subtopic;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.view.BreadcrumbNavigation;
import de.digitalcollections.model.impl.identifiable.NodeImpl;
import de.digitalcollections.model.impl.identifiable.entity.TopicImpl;
import de.digitalcollections.model.impl.identifiable.entity.parts.SubtopicImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import de.digitalcollections.model.impl.view.BreadcrumbNavigationImpl;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class SubtopicRepositoryImpl extends EntityPartRepositoryImpl<Subtopic>
    implements SubtopicRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(SubtopicRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "st";
  public static final String TABLE_ALIAS = "s";
  public static final String TABLE_NAME = "subtopics";

  public static String getSqlAllFields(String tableAlias, String mappingPrefix) {
    return getSqlReducedFields(tableAlias, mappingPrefix);
  }

  public static String getSqlReducedFields(String tableAlias, String mappingPrefix) {
    return IdentifiableRepositoryImpl.getSqlReducedFields(tableAlias, mappingPrefix);
  }

  private final EntityRepositoryImpl entityRepositoryImpl;
  private final FileResourceMetadataRepositoryImpl fileResourceMetadataRepositoryImpl;

  @Autowired
  public SubtopicRepositoryImpl(
      Jdbi dbi,
      IdentifierRepository identifierRepository,
      EntityRepositoryImpl entityRepositoryImpl,
      FileResourceMetadataRepositoryImpl fileResourceMetadataRepositoryImpl) {
    super(dbi, identifierRepository, TABLE_NAME, TABLE_ALIAS, MAPPING_PREFIX, SubtopicImpl.class);
    this.entityRepositoryImpl = entityRepositoryImpl;
    this.fileResourceMetadataRepositoryImpl = fileResourceMetadataRepositoryImpl;
    this.sqlAllFields = getSqlAllFields(tableAlias, mappingPrefix);
    this.sqlReducedFields = getSqlReducedFields(tableAlias, mappingPrefix);
  }

  @Override
  public boolean addChildren(UUID parentUuid, List<Subtopic> collections) {
    throw new UnsupportedOperationException("Not supported yet.");
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
  public Subtopic findOne(UUID uuid, Filtering filtering) {
    Subtopic subtopic = super.findOne(uuid, filtering);

    if (subtopic != null) {
      subtopic.setChildren(getChildren(subtopic));
    }
    return subtopic;
  }

  @Override
  public Subtopic findOne(Identifier identifier) {
    Subtopic subtopic = super.findOne(identifier);

    if (subtopic != null) {
      subtopic.setChildren(getChildren(subtopic));
    }
    return subtopic;
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[] {"created", "lastModified"};
  }

  @Override
  public BreadcrumbNavigation getBreadcrumbNavigation(UUID nodeUuid) {

    List<Node> result =
        dbi.withHandle(
            h ->
                h.createQuery(
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
                            + " ORDER BY depth ASC")
                    .bind("uuid", nodeUuid)
                    .registerRowMapper(BeanMapper.factory(NodeImpl.class))
                    .mapTo(NodeImpl.class)
                    .map(Node.class::cast)
                    .list());

    if (result.isEmpty()) {
      // Special case: If we are on a top level subtopic, we have no parent, so
      // we must construct a breadcrumb more or less manually
      result =
          dbi.withHandle(
              h ->
                  h.createQuery(
                          "SELECT s.uuid as uuid, s.label as label"
                              + "        FROM subtopics s"
                              + "        WHERE uuid= :uuid")
                      .bind("uuid", nodeUuid)
                      .registerRowMapper(BeanMapper.factory(NodeImpl.class))
                      .mapTo(NodeImpl.class)
                      .map(Node.class::cast)
                      .list());
    }

    return new BreadcrumbNavigationImpl(result);
  }

  @Override
  public List<Subtopic> getChildren(UUID uuid) {
    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT * FROM "
                + tableName
                + " AS "
                + tableAlias
                + " INNER JOIN subtopic_subtopics ss ON "
                + tableAlias
                + ".uuid = ss.child_subtopic_uuid"
                + " WHERE ss.parent_subtopic_uuid = :uuid"
                + " ORDER BY ss.sortIndex ASC");

    List<Subtopic> result = retrieveList(sqlReducedFields, innerQuery, Map.of("uuid", uuid));
    return result;
  }

  @Override
  public PageResponse<Subtopic> getChildren(UUID uuid, PageRequest pageRequest) {
    String commonSql =
        " FROM "
            + tableName
            + " AS "
            + tableAlias
            + " INNER JOIN webpage_webpages ww ON "
            + tableAlias
            + ".uuid = ss.child_subtopic_uuid"
            + " WHERE ss.parent_subtopic_uuid = :uuid";

    StringBuilder innerQuery = new StringBuilder("SELECT *" + commonSql);
    addFiltering(pageRequest, innerQuery);
    if (pageRequest.getSorting() == null) {
      innerQuery.append(" ORDER BY ss.sortIndex ASC");
    }
    addPageRequestParams(pageRequest, innerQuery);

    List<Subtopic> result = retrieveList(sqlReducedFields, innerQuery, Map.of("uuid", uuid));

    StringBuilder countQuery = new StringBuilder("SELECT count(*)" + commonSql);
    addFiltering(pageRequest, countQuery);
    long total = retrieveCount(countQuery, Map.of("uuid", uuid));

    return new PageResponseImpl<>(result, pageRequest, total);
  }

  @Override
  protected String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "created":
        return tableAlias + ".created";
      case "lastModified":
        return tableAlias + ".last_modified";
      default:
        return null;
    }
  }

  @Override
  public List<Entity> getEntities(UUID subtopicUuid) {
    final String entityTableAlias = entityRepositoryImpl.getTableAlias();
    final String entityTableName = entityRepositoryImpl.getTableName();

    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT * FROM "
                + entityTableName
                + " AS "
                + entityTableAlias
                + " INNER JOIN subtopic_entities se ON "
                + entityTableAlias
                + ".uuid = se.entity_uuid"
                + " WHERE se.subtopic_uuid = :uuid"
                + " ORDER BY se.sortIndex ASC");

    List<Entity> result =
        entityRepositoryImpl.retrieveList(
            entityRepositoryImpl.getSqlReducedFields(), innerQuery, Map.of("uuid", subtopicUuid));

    return result;
  }

  @Override
  public List<FileResource> getFileResources(UUID subtopicUuid) {
    final String frTableAlias = fileResourceMetadataRepositoryImpl.getTableAlias();
    final String frTableName = fileResourceMetadataRepositoryImpl.getTableName();

    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT * FROM "
                + frTableName
                + " AS "
                + frTableAlias
                + " INNER JOIN subtopic_fileresources sf ON "
                + frTableAlias
                + ".uuid = sf.fileresource_uuid"
                + " WHERE sf.subtopic_uuid = :uuid"
                + " ORDER BY sf.sortIndex ASC");

    List<FileResource> result =
        fileResourceMetadataRepositoryImpl.retrieveList(
            fileResourceMetadataRepositoryImpl.getSqlReducedFields(),
            innerQuery,
            Map.of("uuid", subtopicUuid));

    return result;
  }

  @Override
  public Subtopic getParent(UUID uuid) {
    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT * FROM "
                + tableName
                + " AS "
                + tableAlias
                + " INNER JOIN subtopic_subtopics ss ON "
                + tableAlias
                + ".uuid = ss.parent_subtopic_uuid"
                + " WHERE ss.child_subtopic_uuid = :uuid");
    Subtopic result = retrieveOne(sqlReducedFields, innerQuery, null, Map.of("uuid", uuid));

    return result;
  }

  @Override
  public List<Subtopic> getParents(UUID uuid) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public PageResponse<Subtopic> getRootNodes(PageRequest pageRequest) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<Subtopic> getSubtopicsOfEntity(UUID entityUuid) {
    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT * FROM "
                + tableName
                + " AS "
                + tableAlias
                + " INNER JOIN subtopic_entities se ON "
                + tableAlias
                + ".uuid = se.subtopic_uuid"
                + " WHERE se.entity_uuid = :uuid");

    List<Subtopic> result = retrieveList(sqlReducedFields, innerQuery, Map.of("uuid", entityUuid));
    return result;
  }

  @Override
  public List<Subtopic> getSubtopicsOfFileResource(UUID fileResourceUuid) {
    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT * FROM "
                + tableName
                + " AS "
                + tableAlias
                + " INNER JOIN subtopic_fileresources sf ON "
                + tableAlias
                + ".uuid = sf.subtopic_uuid"
                + " WHERE sf.fileresource_uuid = :uuid");

    List<Subtopic> result =
        retrieveList(sqlReducedFields, innerQuery, Map.of("uuid", fileResourceUuid));
    return result;
  }

  @Override
  public Topic getTopic(UUID rootSubtopicUuid) {
    String query =
        "SELECT uuid, refid, label"
            + " FROM topics"
            + " INNER JOIN topic_subtopics ts ON uuid = ts.topic_uuid"
            + " WHERE ts.subtopic_uuid = :uuid";

    Topic result =
        dbi.withHandle(
            h ->
                h.createQuery(query)
                    .bind("uuid", rootSubtopicUuid)
                    .mapToBean(TopicImpl.class)
                    .one());
    return result;
  }

  @Override
  public boolean removeChild(UUID parentUuid, UUID childUuid) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Subtopic save(Subtopic subtopic) {
    subtopic.setUuid(UUID.randomUUID());
    subtopic.setCreated(LocalDateTime.now());
    subtopic.setLastModified(LocalDateTime.now());
    final UUID previewImageUuid =
        subtopic.getPreviewImage() == null ? null : subtopic.getPreviewImage().getUuid();

    String query =
        "INSERT INTO "
            + tableName
            + "("
            + "uuid, label, description, previewfileresource, preview_hints, custom_attrs,"
            + " identifiable_type,"
            + " created, last_modified"
            + ") VALUES ("
            + ":uuid, :label::JSONB, :description::JSONB, :previewFileResource, :previewImageRenderingHints::JSONB, :customAttributes::JSONB,"
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
  public Subtopic saveWithParent(Subtopic subtopic, UUID parentSubtopicUuid) {
    final UUID childSubtopicUuid =
        subtopic.getUuid() == null ? save(subtopic).getUuid() : subtopic.getUuid();
    Integer nextSortindex =
        retrieveNextSortIndexForParentChildren(
            dbi, "subtopic_subtopics", "parent_subtopic_uuid", parentSubtopicUuid);

    String query =
        "INSERT INTO subtopic_subtopics(parent_subtopic_uuid, child_subtopic_uuid, sortindex)"
            + " VALUES (:parent_subtopic_uuid, :child_subtopic_uuid, :sortindex)";
    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("parent_subtopic_uuid", parentSubtopicUuid)
                .bind("child_subtopic_uuid", childSubtopicUuid)
                .bind("sortindex", nextSortindex)
                .execute());

    return findOne(childSubtopicUuid);
  }

  @Override
  public Subtopic saveWithParentTopic(Subtopic subtopic, UUID parentTopicUuid) {
    final UUID childSubtopicUuid =
        subtopic.getUuid() == null ? save(subtopic).getUuid() : subtopic.getUuid();

    Integer nextSortindex =
        retrieveNextSortIndexForParentChildren(
            dbi, "topic_subtopics", "topic_uuid", parentTopicUuid);

    String query =
        "INSERT INTO topic_subtopics(topic_uuid, subtopic_uuid, sortindex)"
            + " VALUES (:parent_topic_uuid, :child_subtopic_uuid, :sortindex)";
    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("parent_topic_uuid", parentTopicUuid)
                .bind("child_subtopic_uuid", childSubtopicUuid)
                .bind("sortindex", nextSortindex)
                .execute());

    return findOne(childSubtopicUuid);
  }

  @Override
  public Subtopic update(Subtopic subtopic) {
    subtopic.setLastModified(LocalDateTime.now());

    // do not update/left out from statement (not changed since insert):
    // uuid, created, identifiable_type
    final UUID previewImageUuid =
        subtopic.getPreviewImage() == null ? null : subtopic.getPreviewImage().getUuid();

    String query =
        "UPDATE "
            + tableName
            + " SET"
            + " label=:label::JSONB, description=:description::JSONB,"
            + " previewfileresource=:previewFileResource, preview_hints=:previewImageRenderingHints::JSONB, custom_attrs=:customAttributes::JSONB,"
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
    identifierRepository.deleteByIdentifiable(subtopic);
    Set<Identifier> identifiers = subtopic.getIdentifiers();
    saveIdentifiers(identifiers, subtopic);

    Subtopic result = findOne(subtopic.getUuid());
    return result;
  }

  @Override
  public boolean updateChildrenOrder(UUID parentUuid, List<Subtopic> children) {
    if (parentUuid == null || children == null) {
      return false;
    }
    String query =
        "UPDATE subtopic_subtopics"
            + " SET sortindex = :idx"
            + " WHERE child_subtopic_uuid = :childSubtopicUuid AND parent_subtopic_uuid = :parentSubtopicUuid;";
    dbi.withHandle(
        h -> {
          PreparedBatch batch = h.prepareBatch(query);
          int idx = 0;
          for (Subtopic subtopic : children) {
            batch
                .bind("idx", idx++)
                .bind("childSubtopicUuid", subtopic.getUuid())
                .bind("parentSubtopicUuid", parentUuid)
                .add();
          }
          return batch.execute();
        });
    return true;
  }
}
