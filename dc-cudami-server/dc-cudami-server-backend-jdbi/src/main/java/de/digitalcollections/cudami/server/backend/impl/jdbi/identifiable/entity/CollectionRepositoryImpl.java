package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.CollectionRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent.CorporateBodyRepositoryImpl;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.view.BreadcrumbNavigation;
import de.digitalcollections.model.view.BreadcrumbNode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

@Repository
public class CollectionRepositoryImpl extends EntityRepositoryImpl<Collection>
    implements CollectionRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(CollectionRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "col";
  public static final String TABLE_ALIAS = "c";
  public static final String TABLE_NAME = "collections";

  @Override
  public String getSqlInsertFields() {
    return super.getSqlInsertFields() + ", publication_end, publication_start, text";
  }

  /* Do not change order! Must match order in getSqlInsertFields!!! */
  @Override
  public String getSqlInsertValues() {
    return super.getSqlInsertValues() + ", :publicationEnd, :publicationStart, :text::JSONB";
  }

  @Override
  public String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    return getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".text "
        + mappingPrefix
        + "_text";
  }

  @Override
  public String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return super.getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".publication_start "
        + mappingPrefix
        + "_publicationStart, "
        + tableAlias
        + ".publication_end "
        + mappingPrefix
        + "_publicationEnd";
  }

  @Override
  public String getSqlUpdateFieldValues() {
    return super.getSqlUpdateFieldValues()
        + ", publication_end=:publicationEnd, publication_start=:publicationStart, text=:text::JSONB";
  }

  @Lazy @Autowired private CorporateBodyRepositoryImpl corporateBodyRepositoryImpl;

  @Lazy @Autowired private DigitalObjectRepositoryImpl digitalObjectRepositoryImpl;

  @Autowired
  public CollectionRepositoryImpl(Jdbi dbi, CudamiConfig cudamiConfig) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        Collection.class,
        cudamiConfig.getOffsetForAlternativePaging());
  }

  @Override
  public boolean addChildren(UUID parentUuid, List<UUID> childrenUuids) {
    if (parentUuid == null || childrenUuids == null) {
      return false;
    }
    Integer nextSortIndex =
        retrieveNextSortIndexForParentChildren(
            dbi, "collection_collections", "parent_collection_uuid", parentUuid);

    dbi.useHandle(
        handle -> {
          PreparedBatch preparedBatch =
              handle.prepareBatch(
                  "INSERT INTO collection_collections(parent_collection_uuid, child_collection_uuid, sortIndex)"
                      + " VALUES (:parentCollectionUuid, :childCollectionUuid, :sortIndex) ON CONFLICT (parent_collection_uuid, child_collection_uuid) DO NOTHING");
          childrenUuids.forEach(
              childUuid -> {
                preparedBatch
                    .bind("parentCollectionUuid", parentUuid)
                    .bind("childCollectionUuid", childUuid)
                    .bind("sortIndex", nextSortIndex + getIndex(childrenUuids, childUuid))
                    .add();
              });
          preparedBatch.execute();
        });
    return true;
  }

  @Override
  public boolean addDigitalObjects(UUID collectionUuid, List<DigitalObject> digitalObjects) {
    if (collectionUuid != null && digitalObjects != null) {
      Integer nextSortIndex =
          retrieveNextSortIndexForParentChildren(
              dbi, "collection_digitalobjects", "collection_uuid", collectionUuid);

      // save relation to collection
      dbi.useHandle(
          handle -> {
            PreparedBatch preparedBatch =
                handle.prepareBatch(
                    "INSERT INTO collection_digitalobjects(collection_uuid, digitalobject_uuid, sortIndex) VALUES (:uuid, :digitalObjectUuid, :sortIndex) ON CONFLICT (collection_uuid, digitalobject_uuid) DO NOTHING");
            digitalObjects.forEach(
                digitalObject -> {
                  preparedBatch
                      .bind("uuid", collectionUuid)
                      .bind("digitalObjectUuid", digitalObject.getUuid())
                      .bind("sortIndex", nextSortIndex + getIndex(digitalObjects, digitalObject))
                      .add();
                });
            preparedBatch.execute();
          });
      return true;
    }
    return false;
  }

  @Override
  public PageResponse<Collection> findChildren(UUID uuid, PageRequest pageRequest) {
    final String crossTableAlias = "xtable";

    StringBuilder commonSql =
        new StringBuilder(
            " FROM "
                + tableName
                + " AS "
                + tableAlias
                + " INNER JOIN collection_collections AS "
                + crossTableAlias
                + " ON "
                + tableAlias
                + ".uuid = "
                + crossTableAlias
                + ".child_collection_uuid"
                + " WHERE "
                + crossTableAlias
                + ".parent_collection_uuid = :uuid");
    Map<String, Object> argumentMappings = new HashMap<>(0);
    argumentMappings.put("uuid", uuid);
    String executedSearchTerm = addSearchTerm(pageRequest, commonSql, argumentMappings);
    addFiltering(pageRequest, commonSql, argumentMappings);

    StringBuilder innerQuery =
        new StringBuilder("SELECT " + crossTableAlias + ".sortindex AS idx, * " + commonSql);
    String orderBy = addCrossTablePageRequestParams(pageRequest, innerQuery, crossTableAlias);
    List<Collection> result =
        retrieveList(getSqlSelectReducedFields(), innerQuery, argumentMappings, orderBy);

    StringBuilder countQuery =
        new StringBuilder("SELECT count(" + tableAlias + ".uuid)" + commonSql);
    long total = retrieveCount(countQuery, argumentMappings);

    return new PageResponse<>(result, pageRequest, total, executedSearchTerm);
  }

  @Override
  public PageResponse<DigitalObject> findDigitalObjects(
      UUID collectionUuid, PageRequest pageRequest) {
    final String crossTableAlias = "xtable";

    final String digitalObjectTableAlias = digitalObjectRepositoryImpl.getTableAlias();
    final String digitalObjectTableName = digitalObjectRepositoryImpl.getTableName();
    StringBuilder commonSql =
        new StringBuilder(
            " FROM "
                + digitalObjectTableName
                + " AS "
                + digitalObjectTableAlias
                + " INNER JOIN collection_digitalobjects AS "
                + crossTableAlias
                + " ON "
                + digitalObjectTableAlias
                + ".uuid = "
                + crossTableAlias
                + ".digitalobject_uuid"
                + " WHERE "
                + crossTableAlias
                + ".collection_uuid = :uuid");
    Map<String, Object> argumentMappings = new HashMap<>(0);
    argumentMappings.put("uuid", collectionUuid);
    String executedSearchTerm = addSearchTerm(pageRequest, commonSql, argumentMappings);
    Filtering filtering = pageRequest.getFiltering();
    // as filtering has other target object type (digitalobject) than this repository (collection)
    // we have to rename filter field names to target table alias and column names:
    mapFilterExpressionsToOtherTableColumnNames(filtering, digitalObjectRepositoryImpl);
    addFiltering(pageRequest, commonSql, argumentMappings);

    StringBuilder innerQuery =
        new StringBuilder("SELECT " + crossTableAlias + ".sortindex AS idx, * " + commonSql);
    String orderBy = addCrossTablePageRequestParams(pageRequest, innerQuery, crossTableAlias);
    List<DigitalObject> result =
        digitalObjectRepositoryImpl.retrieveList(
            digitalObjectRepositoryImpl.getSqlSelectReducedFields(),
            innerQuery,
            argumentMappings,
            orderBy);

    StringBuilder countQuery = new StringBuilder("SELECT count(*)" + commonSql);
    long total = retrieveCount(countQuery, argumentMappings);

    return new PageResponse<>(result, pageRequest, total, executedSearchTerm);
  }

  @Override
  public List<CorporateBody> findRelatedCorporateBodies(UUID uuid, Filtering filtering) {
    final String cbTableAlias = corporateBodyRepositoryImpl.getTableAlias();
    final String cbTableName = corporateBodyRepositoryImpl.getTableName();

    // We do a double join with "rel_entity_entities" because we have two different
    // predicates:
    // - one is fix ("is_part_of"): defines the relation between collection and project
    // - the other one is given as part of the parameter "filtering" for defining relation
    //   between corporatebody and project
    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT * FROM "
                + cbTableName
                + " AS "
                + cbTableAlias
                + " LEFT JOIN rel_entity_entities AS r ON "
                + cbTableAlias
                + ".uuid = r.object_uuid"
                + " LEFT JOIN rel_entity_entities AS rel ON r.subject_uuid = rel.subject_uuid"
                + " WHERE rel.object_uuid = :uuid"
                + " AND rel.predicate = 'is_part_of'");
    FilterCriterion predicate = filtering.getFilterCriterionFor("predicate");
    if (predicate != null) {
      String predicateFilter = String.format(" AND r.predicate = '%s'", predicate.getValue());
      innerQuery.append(predicateFilter);
    }

    Map<String, Object> argumentMappings = new HashMap<>();
    argumentMappings.put("uuid", uuid);
    List<CorporateBody> result =
        corporateBodyRepositoryImpl.retrieveList(
            corporateBodyRepositoryImpl.getSqlSelectReducedFields(),
            innerQuery,
            argumentMappings,
            null);

    return result;
  }

  @Override
  public PageResponse<Collection> findRootNodes(PageRequest pageRequest) {
    String commonSql =
        " FROM "
            + tableName
            + " AS "
            + tableAlias
            + " WHERE ("
            + " NOT EXISTS (SELECT FROM collection_collections WHERE child_collection_uuid = "
            + tableAlias
            + ".uuid))";
    return find(pageRequest, commonSql);
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    List<String> allowedOrderByFields = super.getAllowedOrderByFields();
    allowedOrderByFields.addAll(Arrays.asList("publicationEnd", "publicationStart"));
    return allowedOrderByFields;
  }

  @Override
  public BreadcrumbNavigation getBreadcrumbNavigation(UUID nodeUuid) {
    List<BreadcrumbNode> result =
        dbi.withHandle(
            h ->
                h.createQuery(
                        "WITH recursive breadcrumb (uuid,label,refId,parentId,depth)"
                            + " AS ("
                            + "        SELECT c.uuid AS uuid, c.label AS label, c.refid AS refId, cc.parent_collection_uuid AS parentId, 99 AS depth"
                            + "        FROM collections c, collection_collections cc"
                            + "        WHERE uuid= :uuid and cc.child_collection_uuid = c.uuid"
                            + ""
                            + "        UNION ALL"
                            + "        SELECT c.uuid AS uuid, c.label AS label, c.refid AS refID, cc.parent_collection_uuid AS parentId, depth-1 AS depth"
                            + "        FROM collections c, collection_collections cc, breadcrumb b"
                            + "        WHERE b.uuid = cc.child_collection_uuid AND cc.parent_collection_uuid = c.uuid AND cc.parent_collection_uuid IS NOT null"
                            + "    )"
                            + " SELECT cast(refId AS VARCHAR) as targetId, label, depth FROM breadcrumb"
                            + " ORDER BY depth ASC")
                    .bind("uuid", nodeUuid)
                    .mapTo(BreadcrumbNode.class)
                    .list());

    if (result.isEmpty()) {
      // Special case: If we are on a top level collection, we have no parent, so
      // we must construct a breadcrumb more or less manually
      result =
          dbi.withHandle(
              h ->
                  h.createQuery(
                          "SELECT cast(refId AS VARCHAR) as targetId, label AS label"
                              + " FROM collections WHERE uuid= :uuid")
                      .bind("uuid", nodeUuid)
                      .mapTo(BreadcrumbNode.class)
                      .list());
    }

    return new BreadcrumbNavigation(result);
  }

  @Override
  public Collection getByIdentifier(Identifier identifier) {
    Collection collection = super.getByIdentifier(identifier);

    if (collection != null) {
      collection.setChildren(getChildren(collection));
    }
    return collection;
  }

  @Override
  public Collection getByRefId(long refId) {
    Collection collection = super.getByRefId(refId);

    if (collection != null) {
      collection.setChildren(getChildren(collection));
    }
    return collection;
  }

  @Override
  public Collection getByUuidAndFiltering(UUID uuid, Filtering filtering) {
    Collection collection = super.getByUuidAndFiltering(uuid, filtering);

    if (collection != null) {
      collection.setChildren(getChildren(collection));
    }
    return collection;
  }

  @Override
  public List<Collection> getChildren(UUID uuid) {
    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT cc.sortindex AS idx, * FROM "
                + tableName
                + " AS "
                + tableAlias
                + " INNER JOIN collection_collections cc ON "
                + tableAlias
                + ".uuid = cc.child_collection_uuid"
                + " WHERE cc.parent_collection_uuid = :uuid"
                + " ORDER BY cc.sortindex ASC");
    Map<String, Object> argumentMappings = new HashMap<>();
    argumentMappings.put("uuid", uuid);

    List<Collection> result =
        retrieveList(getSqlSelectReducedFields(), innerQuery, argumentMappings, "ORDER BY idx ASC");
    return result;
  }

  @Override
  public String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    if (super.getColumnName(modelProperty) != null) {
      return super.getColumnName(modelProperty);
    }
    switch (modelProperty) {
      case "publicationEnd":
        return tableAlias + ".publication_end";
      case "publicationStart":
        return tableAlias + ".publication_start";
      default:
        return null;
    }
  }

  @Override
  public Collection getParent(UUID uuid) {
    String sqlAdditionalJoins =
        " INNER JOIN collection_collections cc ON "
            + tableAlias
            + ".uuid = cc.parent_collection_uuid";

    Filtering filtering =
        Filtering.builder()
            .add(
                FilterCriterion.nativeBuilder()
                    .withExpression("cc.child_collection_uuid")
                    .isEquals(uuid)
                    .build())
            .build();

    Collection result = retrieveOne(getSqlSelectReducedFields(), filtering, sqlAdditionalJoins);
    return result;
  }

  @Override
  public List<Collection> getParents(UUID uuid) {
    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT * FROM "
                + tableName
                + " AS "
                + tableAlias
                + " INNER JOIN collection_collections cc ON "
                + tableAlias
                + ".uuid = cc.parent_collection_uuid"
                + " WHERE cc.child_collection_uuid = :uuid");

    Map<String, Object> argumentMappings = new HashMap<>();
    argumentMappings.put("uuid", uuid);

    List<Collection> result =
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
            + " WHERE NOT EXISTS (SELECT FROM collection_collections WHERE child_collection_uuid = "
            + tableAlias
            + ".uuid)";
    List<Locale> result = dbi.withHandle(h -> h.createQuery(query).mapTo(Locale.class).list());
    return result;
  }

  @Override
  public boolean removeChild(UUID parentUuid, UUID childUuid) {
    if (parentUuid == null || childUuid == null) {
      return false;
    }
    final String sql =
        "DELETE FROM collection_collections WHERE parent_collection_uuid=:parentCollectionUuid AND child_collection_uuid=:childCollectionUuid";

    dbi.withHandle(
        h ->
            h.createUpdate(sql)
                .bind("parentCollectionUuid", parentUuid)
                .bind("childCollectionUuid", childUuid)
                .execute());
    return true;
  }

  @Override
  public boolean removeDigitalObject(UUID collectionUuid, UUID digitalObjectUuid) {
    if (collectionUuid != null && digitalObjectUuid != null) {
      // delete relation to collection

      final String sql =
          "DELETE FROM collection_digitalobjects WHERE collection_uuid=:collectionUuid AND digitalobject_uuid=:digitalObjectUuid";

      dbi.withHandle(
          h ->
              h.createUpdate(sql)
                  .bind("collectionUuid", collectionUuid)
                  .bind("digitalObjectUuid", digitalObjectUuid)
                  .execute());
      return true;
    }
    return false;
  }

  @Override
  public boolean removeDigitalObjectFromAllCollections(DigitalObject digitalObject) {
    if (digitalObject == null) {
      return false;
    }

    final String sql =
        "DELETE FROM collection_digitalobjects WHERE digitalobject_uuid=:digitalObjectUuid";

    dbi.withHandle(
        h -> h.createUpdate(sql).bind("digitalObjectUuid", digitalObject.getUuid()).execute());
    return true;
  }

  @Override
  public Collection saveWithParent(UUID childUuid, UUID parentUuid) {
    Integer nextSortIndex =
        retrieveNextSortIndexForParentChildren(
            dbi, "collection_collections", "parent_collection_uuid", parentUuid);

    dbi.withHandle(
        h ->
            h.createUpdate(
                    "INSERT INTO collection_collections(parent_collection_uuid, child_collection_uuid, sortindex)"
                        + " VALUES (:parent_collection_uuid, :child_collection_uuid, :sortindex)")
                .bind("parent_collection_uuid", parentUuid)
                .bind("child_collection_uuid", childUuid)
                .bind("sortindex", nextSortIndex)
                .execute());

    return getByUuid(childUuid);
  }

  @Override
  public boolean setDigitalObjects(UUID collectionUuid, List<DigitalObject> digitalObjects) {
    // as we store the whole list new: delete old entries
    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM collection_digitalobjects WHERE collection_uuid = :uuid")
                .bind("uuid", collectionUuid)
                .execute());

    if (digitalObjects != null) {
      // save relation to collection
      dbi.useHandle(
          handle -> {
            PreparedBatch preparedBatch =
                handle.prepareBatch(
                    "INSERT INTO collection_digitalobjects(collection_uuid, digitalobject_uuid, sortIndex) VALUES (:uuid, :digitalObjectUuid, :sortIndex)");
            for (DigitalObject digitalObject : digitalObjects) {
              preparedBatch
                  .bind("uuid", collectionUuid)
                  .bind("digitalObjectUuid", digitalObject.getUuid())
                  .bind("sortIndex", getIndex(digitalObjects, digitalObject))
                  .add();
            }
            preparedBatch.execute();
          });
      return true;
    }
    return false;
  }

  @Override
  public boolean updateChildrenOrder(UUID parentUuid, List<Collection> children) {
    if (parentUuid == null || children == null) {
      return false;
    }
    String query =
        "UPDATE collection_collections"
            + " SET sortindex = :idx"
            + " WHERE child_collection_uuid = :childUuid AND parent_collection_uuid = :parentUuid;";
    dbi.withHandle(
        h -> {
          PreparedBatch batch = h.prepareBatch(query);
          int idx = 0;
          for (Collection collection : children) {
            batch
                .bind("idx", idx++)
                .bind("childUuid", collection.getUuid())
                .bind("parentUuid", parentUuid)
                .add();
          }
          return batch.execute();
        });
    return true;
  }
}
