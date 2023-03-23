package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.TopicRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource.FileResourceMetadataRepositoryImpl;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.Topic;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.view.BreadcrumbNavigation;
import de.digitalcollections.model.view.BreadcrumbNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class TopicRepositoryImpl extends EntityRepositoryImpl<Topic> implements TopicRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(TopicRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "to";
  public static final String TABLE_ALIAS = "t";
  public static final String TABLE_NAME = "topics";

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
  public PageResponse<Topic> findChildren(UUID uuid, PageRequest pageRequest) {
    final String crossTableAlias = "xtable";

    StringBuilder commonSql =
        new StringBuilder(
            " FROM "
                + tableName
                + " AS "
                + tableAlias
                + " INNER JOIN topic_topics AS "
                + crossTableAlias
                + " ON "
                + tableAlias
                + ".uuid = "
                + crossTableAlias
                + ".child_topic_uuid"
                + " WHERE "
                + crossTableAlias
                + ".parent_topic_uuid = :uuid");
    Map<String, Object> argumentMappings = new HashMap<>(0);
    argumentMappings.put("uuid", uuid);
    addFiltering(pageRequest, commonSql, argumentMappings);

    StringBuilder innerQuery =
        new StringBuilder("SELECT " + crossTableAlias + ".sortindex AS idx, * " + commonSql);
    String orderBy = addCrossTablePagingAndSorting(pageRequest, innerQuery, crossTableAlias);
    List<Topic> result =
        retrieveList(getSqlSelectReducedFields(), innerQuery, argumentMappings, orderBy);

    StringBuilder countQuery =
        new StringBuilder("SELECT count(" + tableAlias + ".uuid)" + commonSql);
    long total = retrieveCount(countQuery, argumentMappings);

    return new PageResponse<>(result, pageRequest, total);
  }

  @Override
  public PageResponse<Entity> findEntities(UUID topicUuid, PageRequest pageRequest) {
    final String crossTableAlias = "xtable";

    final String entityTableAlias = entityRepositoryImpl.getTableAlias();
    final String entityTableName = entityRepositoryImpl.getTableName();
    StringBuilder commonSql =
        new StringBuilder(
            " FROM "
                + entityTableName
                + " AS "
                + entityTableAlias
                + " INNER JOIN topic_entities AS "
                + crossTableAlias
                + " ON "
                + entityTableAlias
                + ".uuid = "
                + crossTableAlias
                + ".entity_uuid"
                + " WHERE "
                + crossTableAlias
                + ".topic_uuid = :uuid");
    Map<String, Object> argumentMappings = new HashMap<>(0);
    argumentMappings.put("uuid", topicUuid);
    Filtering filtering = pageRequest.getFiltering();
    // as filtering has other target object type (entity) than this repository (topic)
    // we have to rename filter field names to target table alias and column names:
    mapFilterExpressionsToOtherTableColumnNames(filtering, entityRepositoryImpl);
    addFiltering(pageRequest, commonSql, argumentMappings);

    StringBuilder innerQuery =
        new StringBuilder("SELECT " + crossTableAlias + ".sortindex AS idx, * " + commonSql);
    String orderBy = addCrossTablePagingAndSorting(pageRequest, innerQuery, crossTableAlias);
    List<Entity> result =
        entityRepositoryImpl.retrieveList(
            entityRepositoryImpl.getSqlSelectReducedFields(),
            innerQuery,
            argumentMappings,
            orderBy);

    StringBuilder countQuery = new StringBuilder("SELECT count(*)" + commonSql);
    long total = retrieveCount(countQuery, argumentMappings);

    return new PageResponse<>(result, pageRequest, total);
  }

  @Override
  public PageResponse<FileResource> findFileResources(UUID topicUuid, PageRequest pageRequest) {
    final String crossTableAlias = "xtable";

    final String frTableAlias = fileResourceMetadataRepositoryImpl.getTableAlias();
    final String frTableName = fileResourceMetadataRepositoryImpl.getTableName();
    StringBuilder commonSql =
        new StringBuilder(
            " FROM "
                + frTableName
                + " AS "
                + frTableAlias
                + " INNER JOIN topic_fileresources AS "
                + crossTableAlias
                + " ON "
                + frTableAlias
                + ".uuid = "
                + crossTableAlias
                + ".fileresource_uuid"
                + " WHERE "
                + crossTableAlias
                + ".topic_uuid = :uuid");
    Map<String, Object> argumentMappings = new HashMap<>(0);
    argumentMappings.put("uuid", topicUuid);
    Filtering filtering = pageRequest.getFiltering();
    // as filtering has other target object type (digitalobject) than this repository (collection)
    // we have to rename filter field names to target table alias and column names:
    mapFilterExpressionsToOtherTableColumnNames(filtering, fileResourceMetadataRepositoryImpl);
    addFiltering(pageRequest, commonSql, argumentMappings);

    StringBuilder innerQuery =
        new StringBuilder("SELECT " + crossTableAlias + ".sortindex AS idx, * " + commonSql);
    String orderBy = addCrossTablePagingAndSorting(pageRequest, innerQuery, crossTableAlias);
    List<FileResource> result =
        fileResourceMetadataRepositoryImpl.retrieveList(
            fileResourceMetadataRepositoryImpl.getSqlSelectReducedFields(),
            innerQuery,
            argumentMappings,
            orderBy);

    StringBuilder countQuery = new StringBuilder("SELECT count(*)" + commonSql);
    long total = retrieveCount(countQuery, argumentMappings);

    return new PageResponse<>(result, pageRequest, total);
  }

  @Override
  public PageResponse<Topic> findRootNodes(PageRequest pageRequest) {
    String commonSql =
        " FROM "
            + tableName
            + " AS "
            + tableAlias
            + " WHERE NOT EXISTS (SELECT FROM topic_topics WHERE child_topic_uuid = "
            + tableAlias
            + ".uuid)";
    return find(pageRequest, commonSql);
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
  public Topic getByIdentifier(Identifier identifier) {
    Topic topic = super.getByIdentifier(identifier);

    if (topic != null) {
      topic.setChildren(getChildren(topic));
    }
    return topic;
  }

  @Override
  public Topic getByUuidAndFiltering(UUID uuid, Filtering filtering) {
    Topic topic = super.getByUuidAndFiltering(uuid, filtering);

    if (topic != null) {
      topic.setChildren(getChildren(topic));
    }
    return topic;
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
        retrieveList(getSqlSelectReducedFields(), innerQuery, argumentMappings, "ORDER BY idx ASC");
    return result;
  }

  @Override
  public List<FileResource> getFileResources(UUID topicUuid) {
    PageResponse<FileResource> response;
    PageRequest request = new PageRequest(0, 100);
    Set<FileResource> fileResources = new HashSet<>();
    do {
      response = this.findFileResources(topicUuid, request);
      if (response == null || !response.hasContent()) {
        break;
      }
      fileResources.addAll(response.getContent());
    } while ((request = response.nextPageRequest()) != null);
    return new ArrayList<>(fileResources);
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
        Filtering.builder()
            .add(
                FilterCriterion.nativeBuilder()
                    .withExpression("tt.child_topic_uuid")
                    .isEquals(nodeUuid)
                    .build())
            .build();

    Topic result = retrieveOne(getSqlSelectReducedFields(), filtering, sqlAdditionalJoins);

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
    List<Topic> result =
        retrieveList(getSqlSelectReducedFields(), innerQuery, argumentMappings, null);
    return result;
  }

  @Override
  public List<Locale> getRootNodesLanguages() {
    String query =
        "SELECT DISTINCT jsonb_object_keys("
            + tableAlias
            + ".label) AS languages"
            + " FROM "
            + tableName
            + " AS "
            + tableAlias
            + " WHERE NOT EXISTS (SELECT FROM topic_topics WHERE child_topic_uuid = "
            + tableAlias
            + ".uuid)";
    List<Locale> result = dbi.withHandle(h -> h.createQuery(query).mapTo(Locale.class).list());
    return result;
  }

  @Override
  public List<Topic> findTopicsOfEntity(UUID entityUuid) {
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

    List<Topic> result =
        retrieveList(getSqlSelectReducedFields(), innerQuery, argumentMappings, null);
    return result;
  }

  @Override
  public List<Topic> findTopicsOfFileResource(UUID fileResourceUuid) {
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
    List<Topic> result =
        retrieveList(getSqlSelectReducedFields(), innerQuery, argumentMappings, null);
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

    return getByUuid(childUuid);
  }

  @Override
  public List<Entity> setEntities(UUID topicUuid, List<Entity> entities) {
    // as we store the whole list new: delete old entries
    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM topic_entities WHERE topic_uuid = :uuid")
                .bind("uuid", topicUuid)
                .execute());

    if (entities != null && entities.size() > 0) {
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
      int size = entities.size();
      PageRequest pageRequest = PageRequest.builder().pageNumber(0).pageSize(size - 1).build();
      PageResponse<Entity> pageResponse = findEntities(topicUuid, pageRequest);
      return pageResponse.getContent();
    }
    return null;
  }

  @Override
  public List<FileResource> setFileResources(UUID topicUuid, List<FileResource> fileResources) {
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
