package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.TopicRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource.FileResourceMetadataRepositoryImpl;
import de.digitalcollections.model.filter.Filtering;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.Topic;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import de.digitalcollections.model.view.BreadcrumbNavigation;
import de.digitalcollections.model.view.BreadcrumbNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class TopicRepositoryImpl extends EntityRepositoryImpl<Topic> implements TopicRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(TopicRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "to";
  public static final String TABLE_ALIAS = "t";
  public static final String TABLE_NAME = "topics";

  public static String getSqlInsertFields() {
    return EntityRepositoryImpl.getSqlInsertFields();
  }

  /* Do not change order! Must match order in getSqlInsertFields!!! */
  public static String getSqlInsertValues() {
    return EntityRepositoryImpl.getSqlInsertValues();
  }

  public static String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    return getSqlSelectReducedFields(tableAlias, mappingPrefix);
  }

  public static String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return EntityRepositoryImpl.getSqlSelectReducedFields(tableAlias, mappingPrefix);
  }

  public static String getSqlUpdateFieldValues() {
    return EntityRepositoryImpl.getSqlUpdateFieldValues();
  }

  private final EntityRepositoryImpl<Entity> entityRepositoryImpl;
  private final FileResourceMetadataRepositoryImpl fileResourceMetadataRepositoryImpl;

  @Autowired
  public TopicRepositoryImpl(
      Jdbi dbi,
      EntityRepositoryImpl entityRepositoryImpl,
      FileResourceMetadataRepositoryImpl fileResourceMetadataRepositoryImpl,
      CudamiConfig cudamiConfig) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        Topic.class,
        getSqlSelectAllFields(TABLE_ALIAS, MAPPING_PREFIX),
        getSqlSelectReducedFields(TABLE_ALIAS, MAPPING_PREFIX),
        getSqlInsertFields(),
        getSqlInsertValues(),
        getSqlUpdateFieldValues(),
        cudamiConfig.getOffsetForAlternativePaging());
    this.entityRepositoryImpl = entityRepositoryImpl;
    this.fileResourceMetadataRepositoryImpl = fileResourceMetadataRepositoryImpl;
  }

  @Override
  public boolean addChildren(UUID parentUuid, List<UUID> childrenUuids) {
    if (parentUuid == null || childrenUuids == null) {
      return false;
    }
    Integer nextSortIndex =
        retrieveNextSortIndexForParentChildren(
            dbi, "topic_topics", "parent_topic_uuid", parentUuid);

    dbi.useHandle(
        handle -> {
          PreparedBatch preparedBatch =
              handle.prepareBatch(
                  "INSERT INTO topic_topics(parent_topic_uuid, child_topic_uuid, sortIndex)"
                      + " VALUES (:parentTopicUuid, :childTopicUuid, :sortIndex) ON CONFLICT (parent_topic_uuid, child_topic_uuid) DO NOTHING");
          childrenUuids.forEach(
              childUuid -> {
                preparedBatch
                    .bind("parentTopicUuid", parentUuid)
                    .bind("childTopicUuid", childUuid)
                    .bind("sortIndex", nextSortIndex + getIndex(childrenUuids, childUuid))
                    .add();
              });
          preparedBatch.execute();
        });
    return true;
  }

  @Override
  public SearchPageResponse<Topic> findChildren(UUID uuid, SearchPageRequest searchPageRequest) {
    String commonSql =
        " FROM "
            + tableName
            + " AS "
            + tableAlias
            + " INNER JOIN topic_topics cc ON "
            + tableAlias
            + ".uuid = cc.child_topic_uuid"
            + " WHERE cc.parent_topic_uuid = :uuid";
    Map<String, Object> argumentMappings = new HashMap<>();
    argumentMappings.put("uuid", uuid);

    String searchTerm = searchPageRequest.getQuery();
    if (StringUtils.hasText(searchTerm)) {
      commonSql += " AND " + getCommonSearchSql(tableAlias);
      argumentMappings.put("searchTerm", this.escapeTermForJsonpath(searchTerm));
    }

    StringBuilder innerQuery = new StringBuilder("SELECT cc.sortindex AS idx, *" + commonSql);
    addFiltering(searchPageRequest, innerQuery, argumentMappings);

    String orderBy = getOrderBy(searchPageRequest.getSorting());
    if (!StringUtils.hasText(orderBy)) {
      orderBy = "ORDER BY idx ASC";
      innerQuery.append(
          " ORDER BY cc.sortindex"); // must be the column itself to use window functions
    }
    addPageRequestParams(searchPageRequest, innerQuery);

    List<Topic> result =
        retrieveList(sqlSelectReducedFields, innerQuery, argumentMappings, orderBy);

    StringBuilder countQuery =
        new StringBuilder("SELECT count(" + tableAlias + ".uuid)" + commonSql);
    addFiltering(searchPageRequest, countQuery, argumentMappings);
    long total = retrieveCount(countQuery, argumentMappings);

    return new SearchPageResponse<>(result, searchPageRequest, total);
  }

  @Override
  public Topic findOne(UUID uuid, Filtering filtering) {
    Topic topic = super.findOne(uuid, filtering);

    if (topic != null) {
      topic.setChildren(getChildren(topic));
    }
    return topic;
  }

  @Override
  public Topic findOne(Identifier identifier) {
    Topic topic = super.findOne(identifier);

    if (topic != null) {
      topic.setChildren(getChildren(topic));
    }
    return topic;
  }

  @Override
  public List<Entity> getAllEntities(UUID topicUuid) {
    final String entityTableAlias = entityRepositoryImpl.getTableAlias();
    final String entityTableName = entityRepositoryImpl.getTableName();

    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT te.sortindex AS idx, * FROM "
                + entityTableName
                + " AS "
                + entityTableAlias
                + " INNER JOIN topic_entities te ON "
                + entityTableAlias
                + ".uuid = te.entity_uuid"
                + " WHERE te.topic_uuid = :uuid"
                + " ORDER BY idx ASC");
    Map<String, Object> argumentMappings = new HashMap<>();
    argumentMappings.put("uuid", topicUuid);

    List<Entity> result =
        entityRepositoryImpl
            .retrieveList(
                entityRepositoryImpl.getSqlSelectReducedFields(),
                innerQuery,
                argumentMappings,
                "ORDER BY idx ASC")
            .stream()
            .map(Entity.class::cast)
            .collect(Collectors.toList());

    return result;
  }

  @Override
  public BreadcrumbNavigation getBreadcrumbNavigation(UUID nodeUuid) {
    List<BreadcrumbNode> result =
        dbi.withHandle(
            h ->
                h.createQuery(
                        "WITH recursive breadcrumb (uuid,label,refId,parentId,depth)"
                            + " AS ("
                            + "        SELECT t.uuid AS uuid, t.label AS label, t.refid AS refId, tt.parent_topic_uuid AS parentId, 99 AS depth"
                            + "        FROM topics t, topic_topics tt"
                            + "        WHERE uuid= :uuid and tt.child_topic_uuid = t.uuid"
                            + ""
                            + "        UNION ALL"
                            + "        SELECT t.uuid AS uuid, t.label AS label, t.refid AS refID, tt.parent_topic_uuid AS parentId, depth-1 AS depth"
                            + "        FROM topics t, topic_topics tt, breadcrumb b"
                            + "        WHERE b.uuid = tt.child_topic_uuid AND tt.parent_topic_uuid = t.uuid AND tt.parent_topic_uuid IS NOT null"
                            + "    )"
                            + " SELECT cast(refId AS VARCHAR) as targetId, label, depth FROM breadcrumb"
                            + " ORDER BY depth ASC")
                    .bind("uuid", nodeUuid)
                    .mapTo(BreadcrumbNode.class)
                    .list());

    if (result.isEmpty()) {
      // Special case: If we are on a top level topic, we have no parent, so
      // we must construct a breadcrumb more or less manually
      result =
          dbi.withHandle(
              h ->
                  h.createQuery(
                          "SELECT cast(refId AS VARCHAR) as targetId, label AS label"
                              + " FROM topics WHERE uuid= :uuid")
                      .bind("uuid", nodeUuid)
                      .mapTo(BreadcrumbNode.class)
                      .list());
    }

    return new BreadcrumbNavigation(result);
  }

  @Override
  public List<Topic> getChildren(UUID uuid) {
    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT tt.sortindex AS idx, * FROM "
                + tableName
                + " AS "
                + tableAlias
                + " INNER JOIN topic_topics tt ON "
                + tableAlias
                + ".uuid = tt.child_topic_uuid"
                + " WHERE tt.parent_topic_uuid = :uuid"
                + " ORDER BY idx ASC");
    Map<String, Object> argumentMappings = new HashMap<>();
    argumentMappings.put("uuid", uuid);

    List<Topic> result =
        retrieveList(sqlSelectReducedFields, innerQuery, argumentMappings, "ORDER BY idx ASC");
    return result;
  }

  @Override
  public PageResponse<Topic> getChildren(UUID nodeUuid, PageRequest pageRequest) {
    String commonSql =
        " FROM "
            + tableName
            + " AS "
            + tableAlias
            + " INNER JOIN topic_topics tt ON "
            + tableAlias
            + ".uuid = tt.child_topic_uuid"
            + " WHERE tt.parent_topic_uuid = :uuid";

    Map<String, Object> argumentMappings = new HashMap<>();
    argumentMappings.put("uuid", nodeUuid);

    StringBuilder innerQuery = new StringBuilder("SELECT tt.sortindex AS idx, *" + commonSql);
    addFiltering(pageRequest, innerQuery, argumentMappings);
    pageRequest.setSorting(null);
    innerQuery.append(
        " ORDER BY tt.sortindex"); // must be the column itself to use window functions
    addPageRequestParams(pageRequest, innerQuery);

    List<Topic> result =
        retrieveList(sqlSelectReducedFields, innerQuery, argumentMappings, "ORDER BY idx ASC");

    StringBuilder countQuery = new StringBuilder("SELECT count(*)" + commonSql);
    addFiltering(pageRequest, countQuery, argumentMappings);
    long total = retrieveCount(countQuery, argumentMappings);

    return new PageResponse<>(result, pageRequest, total);
  }

  @Override
  public PageResponse<Entity> getEntities(UUID topicUuid, PageRequest pageRequest) {
    String commonSql =
        " FROM "
            + entityRepositoryImpl.getTableName()
            + " AS "
            + entityRepositoryImpl.getTableAlias()
            + " INNER JOIN topic_entities te ON "
            + entityRepositoryImpl.getTableAlias()
            + ".uuid = te.entity_uuid"
            + " WHERE te.topic_uuid = :uuid";

    Map<String, Object> argumentMappings = new HashMap<>();
    argumentMappings.put("uuid", topicUuid);

    StringBuilder innerQuery = new StringBuilder("SELECT te.sortindex AS idx, *" + commonSql);
    entityRepositoryImpl.addFiltering(pageRequest, innerQuery, argumentMappings);
    pageRequest.setSorting(null);
    innerQuery.append(
        " ORDER BY te.sortindex"); // must be the column itself to use window functions
    addPageRequestParams(pageRequest, innerQuery);

    List<Entity> result =
        entityRepositoryImpl.retrieveList(
            entityRepositoryImpl.getSqlSelectReducedFields(),
            innerQuery,
            argumentMappings,
            "ORDER BY idx ASC");

    StringBuilder countQuery = new StringBuilder("SELECT count(*)" + commonSql);
    entityRepositoryImpl.addFiltering(pageRequest, countQuery, argumentMappings);
    long total = retrieveCount(countQuery, argumentMappings);

    return new PageResponse<>(result, pageRequest, total);
  }

  @Override
  public List<FileResource> getFileResources(UUID topicUuid) {
    PageResponse<FileResource> response;
    PageRequest request = new PageRequest(0, 100);
    Set<FileResource> fileResources = new HashSet<>();
    do {
      response = this.getFileResources(topicUuid, request);
      if (response == null || !response.hasContent()) {
        break;
      }
      fileResources.addAll(response.getContent());
    } while ((request = response.nextPageRequest()) != null);
    return new ArrayList<>(fileResources);
  }

  @Override
  public PageResponse<FileResource> getFileResources(UUID topicUuid, PageRequest pageRequest) {
    final String frTableAlias = fileResourceMetadataRepositoryImpl.getTableAlias();
    final String frTableName = fileResourceMetadataRepositoryImpl.getTableName();
    String commonSql =
        " FROM "
            + frTableName
            + " AS "
            + frTableAlias
            + " INNER JOIN topic_fileresources tf ON "
            + frTableAlias
            + ".uuid = tf.fileresource_uuid"
            + " WHERE tf.topic_uuid = :uuid";
    Map<String, Object> argumentMappings = new HashMap<>();
    argumentMappings.put("uuid", topicUuid);

    StringBuilder innerQuery = new StringBuilder("SELECT tf.sortindex AS idx, * ");
    innerQuery.append(commonSql);
    fileResourceMetadataRepositoryImpl.addFiltering(pageRequest, innerQuery, argumentMappings);
    innerQuery.append(
        " ORDER BY tf.sortindex"); // must be the column itself to use window functions
    pageRequest.setSorting(null);
    this.addPageRequestParams(pageRequest, innerQuery);

    List<FileResource> result =
        fileResourceMetadataRepositoryImpl.retrieveList(
            fileResourceMetadataRepositoryImpl.getSqlSelectReducedFields(),
            innerQuery,
            argumentMappings,
            "ORDER BY idx ASC");

    long total =
        fileResourceMetadataRepositoryImpl.retrieveCount(
            new StringBuilder("SELECT count(*) " + commonSql), argumentMappings);

    return new PageResponse<>(result, pageRequest, total);
  }

  @Override
  public List<Locale> getLanguagesOfEntities(UUID topicUuid) {
    String entityTableName = this.entityRepositoryImpl.getTableName();
    String entityTableAlias = this.entityRepositoryImpl.getTableAlias();
    String sql =
        "SELECT DISTINCT jsonb_object_keys("
            + entityTableAlias
            + ".label) AS languages "
            + "FROM "
            + entityTableName
            + " AS "
            + entityTableAlias
            + " INNER JOIN topic_entities te ON "
            + entityTableAlias
            + ".uuid = te.entity_uuid "
            + "WHERE te.topic_uuid = :uuid";
    return this.dbi.withHandle(
        h -> h.createQuery(sql).bind("uuid", topicUuid).mapTo(Locale.class).list());
  }

  @Override
  public List<Locale> getLanguagesOfFileResources(UUID topicUuid) {
    String fileResourceTableName = this.fileResourceMetadataRepositoryImpl.getTableName();
    String fileResourceTableAlias = this.fileResourceMetadataRepositoryImpl.getTableAlias();
    String sql =
        "SELECT DISTINCT jsonb_object_keys("
            + fileResourceTableAlias
            + ".label) AS languages "
            + "FROM "
            + fileResourceTableName
            + " AS "
            + fileResourceTableAlias
            + " INNER JOIN topic_fileresources tf ON "
            + fileResourceTableAlias
            + ".uuid = tf.fileresource_uuid "
            + "WHERE tf.topic_uuid = :uuid";
    return this.dbi.withHandle(
        h -> h.createQuery(sql).bind("uuid", topicUuid).mapTo(Locale.class).list());
  }

  @Override
  public Topic getParent(UUID nodeUuid) {
    String sqlAdditionalJoins =
        " INNER JOIN topic_topics tt ON " + tableAlias + ".uuid = tt.parent_topic_uuid";

    Filtering filtering =
        Filtering.defaultBuilder().filterNative("tt.child_topic_uuid").isEquals(nodeUuid).build();

    Topic result = retrieveOne(sqlSelectReducedFields, sqlAdditionalJoins, filtering);

    return result;
  }

  @Override
  public List<Topic> getParents(UUID uuid) {
    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT * FROM "
                + tableName
                + " AS "
                + tableAlias
                + " INNER JOIN topic_topics tt ON "
                + tableAlias
                + ".uuid = tt.parent_topic_uuid"
                + " WHERE tt.child_topic_uuid = :uuid");
    Map<String, Object> argumentMappings = new HashMap<>();
    argumentMappings.put("uuid", uuid);
    List<Topic> result = retrieveList(sqlSelectReducedFields, innerQuery, argumentMappings, null);
    return result;
  }

  @Override
  public PageResponse<Topic> getRootNodes(PageRequest pageRequest) {
    String commonSql =
        " FROM "
            + tableName
            + " AS "
            + tableAlias
            + " WHERE NOT EXISTS (SELECT FROM topic_topics WHERE child_topic_uuid = "
            + tableAlias
            + ".uuid)";
    return find(pageRequest, commonSql, new HashMap<>());
  }

  @Override
  public SearchPageResponse<Topic> findRootNodes(SearchPageRequest searchPageRequest) {
    String commonSql =
        " FROM "
            + tableName
            + " AS "
            + tableAlias
            + " WHERE ("
            + " NOT EXISTS (SELECT FROM topic_topics WHERE child_topic_uuid = "
            + tableAlias
            + ".uuid))";

    String searchTerm = searchPageRequest.getQuery();
    if (!StringUtils.hasText(searchTerm)) {
      return find(searchPageRequest, commonSql, Collections.EMPTY_MAP);
    }

    Map<String, Object> argumentMappings = new HashMap<>();
    argumentMappings.put("searchTerm", this.escapeTermForJsonpath(searchTerm));
    commonSql += " AND " + getCommonSearchSql(tableAlias);
    return find(searchPageRequest, commonSql, argumentMappings);
  }

  @Override
  public List<Locale> getRootNodesLanguages() {
    String query =
        "SELECT DISTINCT languages"
            + " FROM "
            + tableName
            + " AS "
            + tableAlias
            + ", jsonb_object_keys("
            + tableAlias
            + ".label) AS languages"
            + " WHERE NOT EXISTS (SELECT FROM topic_topics WHERE child_topic_uuid = "
            + tableAlias
            + ".uuid)";
    List<Locale> result = dbi.withHandle(h -> h.createQuery(query).mapTo(Locale.class).list());
    return result;
  }

  @Override
  public List<Topic> getTopicsOfEntity(UUID entityUuid) {
    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT * FROM "
                + tableName
                + " AS "
                + tableAlias
                + " INNER JOIN topic_entities te ON "
                + tableAlias
                + ".uuid = te.topic_uuid"
                + " WHERE te.entity_uuid = :uuid");
    Map<String, Object> argumentMappings = new HashMap<>();
    argumentMappings.put("uuid", entityUuid);

    List<Topic> result = retrieveList(sqlSelectReducedFields, innerQuery, argumentMappings, null);
    return result;
  }

  @Override
  public List<Topic> getTopicsOfFileResource(UUID fileResourceUuid) {
    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT * FROM "
                + tableName
                + " AS "
                + tableAlias
                + " INNER JOIN topic_fileresources tf ON "
                + tableAlias
                + ".uuid = tf.topic_uuid"
                + " WHERE tf.fileresource_uuid = :uuid");
    Map<String, Object> argumentMappings = new HashMap<>();
    argumentMappings.put("uuid", fileResourceUuid);
    List<Topic> result = retrieveList(sqlSelectReducedFields, innerQuery, argumentMappings, null);
    return result;
  }

  @Override
  public boolean removeChild(UUID parentUuid, UUID childUuid) {
    if (parentUuid == null || childUuid == null) {
      return false;
    }
    final String sql =
        "DELETE FROM topic_topics WHERE parent_topic_uuid=:parentTopicUuid AND child_topic_uuid=:childTopicUuid";

    dbi.withHandle(
        h ->
            h.createUpdate(sql)
                .bind("parentTopicUuid", parentUuid)
                .bind("childTopicUuid", childUuid)
                .execute());
    return true;
  }

  @Override
  public Topic save(Topic topic) {
    super.save(topic);
    Topic result = findOne(topic.getUuid());
    return result;
  }

  @Override
  public List<Entity> saveEntities(UUID topicUuid, List<Entity> entities) {
    // as we store the whole list new: delete old entries
    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM topic_entities WHERE topic_uuid = :uuid")
                .bind("uuid", topicUuid)
                .execute());

    if (entities != null) {
      // we assume that the entities are already saved...
      dbi.useHandle(
          handle -> {
            PreparedBatch preparedBatch =
                handle.prepareBatch(
                    "INSERT INTO topic_entities(topic_uuid, entity_uuid, sortIndex) VALUES(:uuid, :entityUuid, :sortIndex)");
            for (Entity entity : entities) {
              preparedBatch
                  .bind("uuid", topicUuid)
                  .bind("entityUuid", entity.getUuid())
                  .bind("sortIndex", getIndex(entities, entity))
                  .add();
            }
            preparedBatch.execute();
          });
    }
    return getAllEntities(topicUuid);
  }

  @Override
  public List<FileResource> saveFileResources(UUID topicUuid, List<FileResource> fileResources) {
    // as we store the whole list new: delete old entries
    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM topic_fileresources WHERE topic_uuid = :uuid")
                .bind("uuid", topicUuid)
                .execute());

    if (fileResources != null) {
      // we assume that the fileresources are already saved...
      dbi.useHandle(
          handle -> {
            PreparedBatch preparedBatch =
                handle.prepareBatch(
                    "INSERT INTO topic_fileresources(topic_uuid, fileresource_uuid, sortIndex) VALUES(:uuid, :fileResourceUuid, :sortIndex)");
            for (FileResource fileResource : fileResources) {
              preparedBatch
                  .bind("uuid", topicUuid)
                  .bind("fileResourceUuid", fileResource.getUuid())
                  .bind("sortIndex", getIndex(fileResources, fileResource))
                  .add();
            }
            preparedBatch.execute();
          });
    }
    return getFileResources(topicUuid);
  }

  @Override
  public Topic saveWithParent(UUID childUuid, UUID parentUuid) {
    Integer nextSortIndex =
        retrieveNextSortIndexForParentChildren(
            dbi, "topic_topics", "parent_topic_uuid", parentUuid);

    dbi.withHandle(
        h ->
            h.createUpdate(
                    "INSERT INTO topic_topics(parent_topic_uuid, child_topic_uuid, sortindex)"
                        + " VALUES (:parentTopicUuid, :childTopicUuid, :sortIndex)")
                .bind("parentTopicUuid", parentUuid)
                .bind("childTopicUuid", childUuid)
                .bind("sortIndex", nextSortIndex)
                .execute());

    return findOne(childUuid);
  }

  @Override
  public Topic update(Topic topic) {
    super.update(topic);
    Topic result = findOne(topic.getUuid());
    return result;
  }

  @Override
  public boolean updateChildrenOrder(UUID parentUuid, List<Topic> children) {
    if (parentUuid == null || children == null) {
      return false;
    }
    String query =
        "UPDATE topic_topics"
            + " SET sortindex = :idx"
            + " WHERE child_topic_uuid = :childUuid AND parent_topic_uuid = :parentUuid;";
    dbi.withHandle(
        h -> {
          PreparedBatch batch = h.prepareBatch(query);
          int idx = 0;
          for (Topic child : children) {
            batch
                .bind("idx", idx++)
                .bind("childUuid", child.getUuid())
                .bind("parentUuid", parentUuid)
                .add();
          }
          return batch.execute();
        });
    return true;
  }
}
