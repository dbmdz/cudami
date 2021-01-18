package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.CollectionRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent.CorporateBodyRepositoryImpl;
import de.digitalcollections.model.api.filter.FilterCriterion;
import de.digitalcollections.model.api.filter.Filtering;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.Node;
import de.digitalcollections.model.api.identifiable.entity.Collection;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.view.BreadcrumbNavigation;
import de.digitalcollections.model.impl.identifiable.NodeImpl;
import de.digitalcollections.model.impl.identifiable.entity.CollectionImpl;
import de.digitalcollections.model.impl.identifiable.entity.agent.CorporateBodyImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import de.digitalcollections.model.impl.view.BreadcrumbNavigationImpl;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class CollectionRepositoryImpl extends EntityRepositoryImpl<CollectionImpl>
        implements CollectionRepository<CollectionImpl> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CollectionRepositoryImpl.class);

  public static final String SQL_REDUCED_FIELDS_COL
          = " c.uuid col_uuid, c.refid col_refId, c.label col_label, c.description col_description,"
          + " c.identifiable_type col_type, c.entity_type col_entityType,"
          + " c.created col_created, c.last_modified col_lastModified,"
          + " c.publication_start col_publicationStart, c.publication_end col_publicationEnd,"
          + " c.preview_hints col_previewImageRenderingHints";

  public static final String SQL_FULL_FIELDS_COL = SQL_REDUCED_FIELDS_COL + ", c.text col_text";

  public static final String TABLE_NAME = "collections";

  private final CorporateBodyRepositoryImpl corporateBodyRepositoryImpl;
  private final DigitalObjectRepositoryImpl digitalObjectRepositoryImpl;

  @Autowired
  public CollectionRepositoryImpl(
          Jdbi dbi,
          IdentifierRepository identifierRepository,
          DigitalObjectRepositoryImpl digitalObjectRepositoryImpl,
          CorporateBodyRepositoryImpl corporateBodyRepositoryImpl) {
    super(
            dbi,
            identifierRepository,
            TABLE_NAME,
            "c",
            "col",
            CollectionImpl.class,
            SQL_REDUCED_FIELDS_COL,
            SQL_FULL_FIELDS_COL);
    this.corporateBodyRepositoryImpl = corporateBodyRepositoryImpl;
    this.digitalObjectRepositoryImpl = digitalObjectRepositoryImpl;
  }

  @Override
  public boolean addChildren(UUID parentUuid, List<CollectionImpl> children) {
    if (parentUuid == null || children == null) {
      return false;
    }
    Integer nextSortIndex = retrieveNextSortIndexForParentChildren(
            dbi, "collection_collections", "parent_collection_uuid", parentUuid);

    dbi.useHandle(
            handle -> {
              PreparedBatch preparedBatch
              = handle.prepareBatch(
                      "INSERT INTO collection_collections(parent_collection_uuid, child_collection_uuid, sortIndex)"
                      + " VALUES (:parentCollectionUuid, :childCollectionUuid, :sortIndex) ON CONFLICT (parent_collection_uuid, child_collection_uuid) DO NOTHING");
              children.forEach(
                      child -> {
                        preparedBatch
                                .bind("parentCollectionUuid", parentUuid)
                                .bind("childCollectionUuid", child.getUuid())
                                .bind("sortIndex", nextSortIndex + getIndex(children, child))
                                .add();
                      });
              preparedBatch.execute();
            });
    return true;
  }

  @Override
  public boolean addDigitalObjects(UUID collectionUuid, List<DigitalObject> digitalObjects) {
    if (collectionUuid != null && digitalObjects != null) {
      Integer nextSortIndex = retrieveNextSortIndexForParentChildren(
              dbi, "collection_digitalobjects", "collection_uuid", collectionUuid);

      // save relation to collection
      dbi.useHandle(
              handle -> {
                PreparedBatch preparedBatch
                = handle.prepareBatch(
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
  public CollectionImpl findOne(UUID uuid, Filtering filtering) {
    CollectionImpl collection = super.findOne(uuid, filtering);

    if (collection != null) {
      // TODO could be replaced with another join in above query...
      collection.setChildren(
              Stream.ofNullable(getChildren(collection))
                      .map(Collection.class::cast)
                      .collect(Collectors.toList()));
    }
    return collection;
  }

  @Override
  public CollectionImpl findOne(Identifier identifier) {
    CollectionImpl collection = super.findOne(identifier);

    if (collection != null) {
      // TODO could be replaced with another join in above query...
      collection.setChildren(
              Stream.ofNullable(getChildren(collection))
                      .map(Collection.class::cast)
                      .collect(Collectors.toList()));
    }
    return collection;
  }

  @Override
  public CollectionImpl findOneByRefId(long refId) {
    CollectionImpl collection = super.findOneByRefId(refId);

    if (collection != null) {
      // TODO could be replaced with another join in above query...
      collection.setChildren(
              Stream.ofNullable(getChildren(collection))
                      .map(Collection.class::cast)
                      .collect(Collectors.toList()));
    }
    return collection;
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[]{
      "created", "label", "lastModified", "publicationEnd", "publicationStart", "refId"
    };
  }

  @Override
  public BreadcrumbNavigation getBreadcrumbNavigation(UUID nodeUuid) {

    List<NodeImpl> result
            = dbi.withHandle(
                    h
                    -> h.createQuery(
                            "WITH recursive breadcrumb (uuid,label,parent_uuid,depth)"
                            + " AS ("
                            + "        SELECT c.uuid AS uuid, c.label AS label, c.refid c_refId, cc.parent_collection_uuid AS parent_uuid, 99 AS depth"
                            + "        FROM collections c, collection_collections cc"
                            + "        WHERE uuid= :uuid and cc.child_collection_uuid = c.uuid"
                            + ""
                            + "        UNION ALL"
                            + "        SELECT c.uuid AS uuid, c.label AS label, c.refid c_refId, cc.parent_collection_uuid AS parent_uuid, depth-1 AS depth"
                            + "        FROM collections c,"
                            + "             collection_collections cc,"
                            + "             breadcrumb b"
                            + "        WHERE b.uuid = cc.child_collection_uuid AND cc.parent_collection_uuid = c.uuid AND cc.parent_collection_uuid IS NOT null"
                            + "    )"
                            + " SELECT * FROM breadcrumb"
                            + " ORDER BY depth ASC")
                            .bind("uuid", nodeUuid)
                            .registerRowMapper(BeanMapper.factory(NodeImpl.class))
                            .mapTo(NodeImpl.class)
                            .list());

    if (result.isEmpty()) {
      // Special case: If we are on a top level collection, we have no parent, so
      // we must construct a breadcrumb more or less manually
      result
              = dbi.withHandle(
                      h
                      -> h.createQuery(
                              "SELECT c.uuid AS uuid, c.label AS label"
                              + "        FROM collections c"
                              + "        WHERE uuid= :uuid")
                              .bind("uuid", nodeUuid)
                              .registerRowMapper(BeanMapper.factory(NodeImpl.class))
                              .mapTo(NodeImpl.class)
                              .list());
    }

    List<Node> nodes = result.stream().map(s -> (Node) s).collect(Collectors.toList());
    return new BreadcrumbNavigationImpl(nodes);
  }

  @Override
  public List<CollectionImpl> getChildren(CollectionImpl collection) {
    return CollectionRepository.super.getChildren(collection);
  }

  @Override
  public List<CollectionImpl> getChildren(UUID uuid) {
    StringBuilder innerQuery
            = new StringBuilder("SELECT * FROM "
                    + tableName
                    + " AS "
                    + tableAlias
                    + " INNER JOIN collection_collections cc ON "
                    + tableAlias
                    + ".uuid = cc.child_collection_uuid"
                    + " WHERE cc.parent_collection_uuid = :uuid"
                    + " ORDER BY cc.sortIndex ASC");

    List<CollectionImpl> result = retrieveList(reducedFieldsSql, innerQuery, Map.of("uuid", uuid));
    return result;
  }

  @Override
  public PageResponse<CollectionImpl> getChildren(UUID uuid, PageRequest pageRequest) {
    String commonSql = " FROM "
            + tableName
            + " AS "
            + tableAlias
            + " INNER JOIN collection_collections cc ON "
            + tableAlias
            + ".uuid = cc.child_collection_uuid"
            + " WHERE cc.parent_collection_uuid = :uuid";

    StringBuilder innerQuery = new StringBuilder("SELECT *" + commonSql);
    addFiltering(pageRequest, innerQuery);
    pageRequest.setSorting(null);
    innerQuery.append(" ORDER BY cc.sortIndex ASC");
    addPageRequestParams(pageRequest, innerQuery);

    List<CollectionImpl> result = retrieveList(reducedFieldsSql, innerQuery, Map.of("uuid", uuid));

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
      case "label":
        return tableAlias + ".label";
      case "lastModified":
        return tableAlias + ".last_modified";
      case "publicationEnd":
        return tableAlias + ".publication_end";
      case "publicationStart":
        return tableAlias + ".publication_start";
      case "refId":
        return tableAlias + ".refid";
      default:
        return null;
    }
  }

  @Override
  public PageResponse<DigitalObject> getDigitalObjects(
          UUID collectionUuid, PageRequest pageRequest) {
    final String doTableAlias = digitalObjectRepositoryImpl.getTableAlias();
    final String doTableName = digitalObjectRepositoryImpl.getTableName();

    String commonSql = " FROM "
            + doTableName
            + " AS "
            + doTableAlias
            + " LEFT JOIN collection_digitalobjects AS cd ON "
            + doTableAlias
            + ".uuid = cd.digitalobject_uuid"
            + " WHERE cd.collection_uuid = :uuid";

    StringBuilder innerQuery = new StringBuilder("SELECT *" + commonSql);
    addFiltering(pageRequest, innerQuery);
    pageRequest.setSorting(null);
    innerQuery.append(" ORDER BY cd.sortIndex ASC");
    addPageRequestParams(pageRequest, innerQuery);

    List<DigitalObject> result
            = digitalObjectRepositoryImpl
                    .retrieveList(
                            DigitalObjectRepositoryImpl.SQL_REDUCED_FIELDS_DO,
                            innerQuery,
                            Map.of("uuid", collectionUuid))
                    .stream()
                    .map(DigitalObject.class::cast)
                    .collect(Collectors.toList());

    StringBuilder countQuery = new StringBuilder("SELECT count(*)" + commonSql);
    addFiltering(pageRequest, countQuery);
    long total = retrieveCount(countQuery, Map.of("uuid", collectionUuid));

    return new PageResponseImpl<>(result, pageRequest, total);
  }

  @Override
  public CollectionImpl getParent(UUID uuid) {
    StringBuilder innerQuery = new StringBuilder("SELECT * FROM "
            + tableName
            + " AS "
            + tableAlias
            + " INNER JOIN collection_collections cc ON "
            + tableAlias
            + ".uuid = cc.parent_collection_uuid"
            + " WHERE cc.child_collection_uuid = :uuid");
    CollectionImpl result = retrieveOne(reducedFieldsSql, innerQuery, Map.of("uuid", uuid));

    return result;
  }

  @Override
  public List<CollectionImpl> getParents(UUID uuid) {
    StringBuilder innerQuery
            = new StringBuilder("SELECT * FROM "
                    + tableName
                    + " AS "
                    + tableAlias
                    + " INNER JOIN collection_collections cc ON "
                    + tableAlias
                    + ".uuid = cc.parent_collection_uuid"
                    + " WHERE cc.child_collection_uuid = :uuid");

    List<CollectionImpl> result = retrieveList(reducedFieldsSql, innerQuery, Map.of("uuid", uuid));
    return result;
  }

  @Override
  public List<CorporateBody> getRelatedCorporateBodies(UUID uuid, Filtering filtering) {
    final String cbTableAlias = corporateBodyRepositoryImpl.getTableAlias();
    final String cbTableName = corporateBodyRepositoryImpl.getTableName();

    // We do a double join with "rel_entity_entities" because we have two different
    // predicates:
    // - one is fix ("is_part_of"): defines the relation between collection and project
    // - the other one is given as part of the parameter "filtering" for defining relation
    //   between corporatebody and project
    StringBuilder innerQuery
            = new StringBuilder(
                    "SELECT * FROM " + cbTableName + " AS " + cbTableAlias
                    + " LEFT JOIN rel_entity_entities AS r ON " + cbTableAlias + ".uuid = r.object_uuid"
                    + " LEFT JOIN rel_entity_entities AS rel ON r.subject_uuid = rel.subject_uuid"
                    + " WHERE rel.object_uuid = :uuid"
                    + " AND rel.predicate = 'is_part_of'");
    FilterCriterion predicate = filtering.getFilterCriterionFor("predicate");
    if (predicate != null) {
      String predicateFilter = String.format(" AND r.predicate = '%s'", predicate.getValue());
      innerQuery.append(predicateFilter);
    }

    List<CorporateBodyImpl> result
            = corporateBodyRepositoryImpl.retrieveList(corporateBodyRepositoryImpl.getReducedFieldsSql(), innerQuery, Map.of("uuid", uuid));

    return result.stream().map(CorporateBody.class::cast).collect(Collectors.toList());
  }

  @Override
  public PageResponse<CollectionImpl> getTopCollections(PageRequest pageRequest) {
    String commonSql
            = " FROM " + tableName + " AS " + tableAlias
            + " WHERE NOT EXISTS (SELECT FROM collection_collections WHERE child_collection_uuid = " + tableAlias + ".uuid)";
    return find(pageRequest, commonSql, null);
  }

  @Override
  public List<Locale> getTopCollectionsLanguages() {
    String query =
        "SELECT DISTINCT languages"
            + " FROM collections as c, jsonb_object_keys(c.label) as languages"
            + " WHERE NOT EXISTS (SELECT FROM collection_collections WHERE child_collection_uuid = c.uuid)";
    List<Locale> result = dbi.withHandle(h -> h.createQuery(query).mapTo(Locale.class).list());
    return result;
  }

  @Override
  public boolean removeChild(UUID parentUuid, UUID childUuid) {
    if (parentUuid == null || childUuid == null) {
      return false;
    }
    final String sql
            = "DELETE FROM collection_collections WHERE parent_collection_uuid=:parentCollectionUuid AND child_collection_uuid=:childCollectionUuid";

    dbi.withHandle(
            h
            -> h.createUpdate(sql)
                    .bind("parentCollectionUuid", parentUuid)
                    .bind("childCollectionUuid", childUuid)
                    .execute());
    return true;
  }

  @Override
  public boolean removeDigitalObject(UUID collectionUuid, UUID digitalObjectUuid) {
    if (collectionUuid != null && digitalObjectUuid != null) {
      // delete relation to collection

      final String sql
              = "DELETE FROM collection_digitalobjects WHERE collection_uuid=:collectionUuid AND digitalobject_uuid=:digitalObjectUuid";

      dbi.withHandle(
              h
              -> h.createUpdate(sql)
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

    final String sql
            = "DELETE FROM collection_digitalobjects WHERE digitalobject_uuid=:digitalObjectUuid";

    dbi.withHandle(
            h -> h.createUpdate(sql).bind("digitalObjectUuid", digitalObject.getUuid()).execute());
    return true;
  }

  @Override
  public CollectionImpl save(CollectionImpl collection) {
    collection.setUuid(UUID.randomUUID());
    collection.setCreated(LocalDateTime.now());
    collection.setLastModified(LocalDateTime.now());
    // refid is generated as serial, DO NOT SET!
    final UUID previewImageUuid
            = collection.getPreviewImage() == null ? null : collection.getPreviewImage().getUuid();

    final String sql
            = "INSERT INTO "
            + tableName
            + "("
            + "uuid, label, description, previewfileresource, preview_hints,"
            + " identifiable_type, entity_type,"
            + " created, last_modified,"
            + " text, publication_start, publication_end"
            + ") VALUES ("
            + ":uuid, :label::JSONB, :description::JSONB, :previewFileResource, :previewImageRenderingHints::JSONB,"
            + " :type, :entityType,"
            + " :created, :lastModified,"
            + " :text::JSONB, :publicationStart, :publicationEnd"
            + ")";

    dbi.withHandle(
            h
            -> h.createUpdate(sql)
                    .bind("previewFileResource", previewImageUuid)
                    .bindBean(collection)
                    .execute());

    // save identifiers
    Set<Identifier> identifiers = collection.getIdentifiers();
    saveIdentifiers(identifiers, collection);

    CollectionImpl result = findOne(collection.getUuid());
    return result;
  }

  @Override
  public boolean saveDigitalObjects(UUID collectionUuid, List<DigitalObject> digitalObjects) {
    // as we store the whole list new: delete old entries
    dbi.withHandle(
            h
            -> h.createUpdate("DELETE FROM collection_digitalobjects WHERE collection_uuid = :uuid")
                    .bind("uuid", collectionUuid)
                    .execute());

    if (digitalObjects != null) {
      // save relation to collection
      dbi.useHandle(
              handle -> {
                PreparedBatch preparedBatch
                = handle.prepareBatch(
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
  public CollectionImpl saveWithParentCollection(CollectionImpl collection, UUID parentUuid) {
    final UUID childUuid
            = collection.getUuid() == null ? save(collection).getUuid() : collection.getUuid();

    Integer nextSortIndex = retrieveNextSortIndexForParentChildren(
            dbi, "collection_collections", "parent_collection_uuid", parentUuid);

    dbi.withHandle(
            h
            -> h.createUpdate(
                    "INSERT INTO collection_collections(parent_collection_uuid, child_collection_uuid, sortindex)"
                    + " VALUES (:parent_collection_uuid, :child_collection_uuid, :sortindex)")
                    .bind("parent_collection_uuid", parentUuid)
                    .bind("child_collection_uuid", childUuid)
                    .bind("sortindex", nextSortIndex)
                    .execute());

    return findOne(childUuid);
  }

  @Override
  public CollectionImpl update(CollectionImpl collection) {
    collection.setLastModified(LocalDateTime.now());
    // do not update/left out from statement (not changed since insert):
    // uuid, created, identifiable_type, entity_type, refid
    final UUID previewImageUuid
            = collection.getPreviewImage() == null ? null : collection.getPreviewImage().getUuid();

    final String sql
            = "UPDATE "
            + tableName
            + " SET"
            + " label=:label::JSONB, description=:description::JSONB,"
            + " previewfileresource=:previewFileResource, preview_hints=:previewImageRenderingHints::JSONB,"
            + " last_modified=:lastModified,"
            + " text=:text::JSONB, publication_start=:publicationStart, publication_end=:publicationEnd"
            + " WHERE uuid=:uuid";

    dbi.withHandle(
            h
            -> h.createUpdate(sql)
                    .bind("previewFileResource", previewImageUuid)
                    .bindBean(collection)
                    .execute());

    // save identifiers
    // as we store the whole list new: delete old entries
    identifierRepository.deleteByIdentifiable(collection);
    Set<Identifier> identifiers = collection.getIdentifiers();
    saveIdentifiers(identifiers, collection);

    CollectionImpl result = findOne(collection.getUuid());
    return result;
  }
}
