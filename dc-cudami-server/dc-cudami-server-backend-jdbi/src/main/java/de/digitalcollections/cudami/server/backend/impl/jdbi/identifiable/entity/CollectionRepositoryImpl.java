package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import static de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifierRepositoryImpl.SQL_FULL_IDENTIFIER_FIELDS_ID;
import static de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.DigitalObjectRepositoryImpl.SQL_REDUCED_DIGITALOBJECT_FIELDS_DO;
import static de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.DigitalObjectRepositoryImpl.mapRowToDigitalObject;
import static de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent.CorporateBodyRepositoryImpl.SQL_REDUCED_CORPORATEBODY_FIELDS_CB;
import static de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent.CorporateBodyRepositoryImpl.mapRowToCorporateBody;
import static de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource.FileResourceMetadataRepositoryImpl.SQL_PREVIEW_IMAGE_FIELDS_PI;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.CollectionRepository;
import de.digitalcollections.model.api.filter.FilterCriterion;
import de.digitalcollections.model.api.filter.Filtering;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.Node;
import de.digitalcollections.model.api.identifiable.entity.Collection;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.SearchPageRequest;
import de.digitalcollections.model.api.paging.SearchPageResponse;
import de.digitalcollections.model.api.view.BreadcrumbNavigation;
import de.digitalcollections.model.impl.identifiable.IdentifierImpl;
import de.digitalcollections.model.impl.identifiable.NodeImpl;
import de.digitalcollections.model.impl.identifiable.entity.CollectionImpl;
import de.digitalcollections.model.impl.identifiable.entity.DigitalObjectImpl;
import de.digitalcollections.model.impl.identifiable.entity.agent.CorporateBodyImpl;
import de.digitalcollections.model.impl.identifiable.resource.ImageFileResourceImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import de.digitalcollections.model.impl.paging.SearchPageResponseImpl;
import de.digitalcollections.model.impl.view.BreadcrumbNavigationImpl;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.jdbi.v3.core.result.RowView;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class CollectionRepositoryImpl extends EntityRepositoryImpl<Collection>
    implements CollectionRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(CollectionRepositoryImpl.class);

  public static final String SQL_REDUCED_COLLECTION_FIELDS_COL =
      " c.uuid col_uuid, c.refid col_refId, c.label col_label, c.description col_description,"
          + " c.identifiable_type col_type, c.entity_type col_entityType,"
          + " c.created col_created, c.last_modified col_lastModified,"
          + " c.publication_start col_publicationStart, c.publication_end col_publicationEnd,"
          + " c.preview_hints col_previewImageRenderingHints";

  public static final String SQL_FULL_COLLECTION_FIELDS_COL =
      SQL_REDUCED_COLLECTION_FIELDS_COL + ", c.text col_text";

  public static BiFunction<
          LinkedHashMap<UUID, Collection>, RowView, LinkedHashMap<UUID, Collection>>
      mapRowToCollection() {
    return mapRowToCollection(false);
  }

  public static BiFunction<
          LinkedHashMap<UUID, Collection>, RowView, LinkedHashMap<UUID, Collection>>
      mapRowToCollection(boolean withIdentifiers) {
    return (map, rowView) -> {
      Collection collection =
          map.computeIfAbsent(
              rowView.getColumn("col_uuid", UUID.class),
              fn -> {
                return rowView.getRow(CollectionImpl.class);
              });

      if (rowView.getColumn("pi_uuid", UUID.class) != null) {
        collection.setPreviewImage(rowView.getRow(ImageFileResourceImpl.class));
      }
      if (withIdentifiers && rowView.getColumn("id_uuid", UUID.class) != null) {
        IdentifierImpl dbIdentifier = rowView.getRow(IdentifierImpl.class);
        collection.addIdentifier(dbIdentifier);
      }
      return map;
    };
  }

  @Autowired
  public CollectionRepositoryImpl(Jdbi dbi, IdentifierRepository identifierRepository) {
    super(dbi, identifierRepository);
  }

  @Override
  public boolean addChildren(UUID parentUuid, List<Collection> children) {
    if (parentUuid == null || children == null) {
      return false;
    }
    Integer nextSortIndex =
        selectNextSortIndexForParentChildren(
            dbi, "collection_collections", "parent_collection_uuid", parentUuid);
    dbi.useHandle(
        handle -> {
          PreparedBatch preparedBatch =
              handle.prepareBatch(
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
      // get max sortIndex of existing
      Integer nextSortIndex =
          selectNextSortIndexForParentChildren(
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
  public long count() {
    final String sql = "SELECT count(*) FROM collections";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOne().get());
    return count;
  }

  @Override
  public PageResponse<Collection> find(PageRequest pageRequest) {
    StringBuilder innerQuery = new StringBuilder("SELECT * FROM collections AS c");
    addFiltering(pageRequest, innerQuery);
    addPageRequestParams(pageRequest, innerQuery);

    final String sql =
        "SELECT"
            + SQL_REDUCED_COLLECTION_FIELDS_COL
            + ","
            + SQL_PREVIEW_IMAGE_FIELDS_PI
            + " FROM ("
            + innerQuery
            + ") AS c"
            + " LEFT JOIN fileresources_image AS file ON c.previewfileresource = file.uuid";
    List<Collection> result =
        dbi
            .withHandle(
                h ->
                    h.createQuery(sql)
                        .registerRowMapper(BeanMapper.factory(CollectionImpl.class, "col"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "pi"))
                        .reduceRows(new LinkedHashMap<UUID, Collection>(), mapRowToCollection()))
            .values()
            .stream()
            .collect(Collectors.toList());

    StringBuilder sqlCount = new StringBuilder("SELECT count(*) FROM collections AS c");
    addFiltering(pageRequest, sqlCount);
    long total =
        dbi.withHandle(h -> h.createQuery(sqlCount.toString()).mapTo(Long.class).findOne().get());

    return new PageResponseImpl<>(result, pageRequest, total);
  }

  @Override
  public SearchPageResponse<Collection> find(SearchPageRequest searchPageRequest) {
    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT * FROM collections AS c"
                + " LEFT JOIN LATERAL jsonb_object_keys(c.label) l(keys) ON c.label IS NOT null"
                + " WHERE (c.label->>l.keys ILIKE '%' || :searchTerm || '%')");
    addFiltering(searchPageRequest, innerQuery);
    addPageRequestParams(searchPageRequest, innerQuery);

    final String sql =
        "SELECT"
            + SQL_REDUCED_COLLECTION_FIELDS_COL
            + ","
            + SQL_PREVIEW_IMAGE_FIELDS_PI
            + " FROM ("
            + innerQuery
            + ") AS c"
            + " LEFT JOIN fileresources_image AS file ON c.previewfileresource = file.uuid";

    List<Collection> result =
        dbi.withHandle(
            h ->
                h
                    .createQuery(sql)
                    .bind("searchTerm", searchPageRequest.getQuery())
                    .registerRowMapper(BeanMapper.factory(CollectionImpl.class, "col"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "pi"))
                    .reduceRows(new LinkedHashMap<UUID, Collection>(), mapRowToCollection())
                    .values()
                    .stream()
                    .collect(Collectors.toList()));

    StringBuilder countQuery =
        new StringBuilder(
            "SELECT count(*) FROM collections AS c"
                + " LEFT JOIN LATERAL jsonb_object_keys(c.label) l(keys) ON c.label IS NOT null"
                + " WHERE (c.label->>l.keys ILIKE '%' || :searchTerm || '%')");
    addFiltering(searchPageRequest, countQuery);
    long total =
        dbi.withHandle(
            h ->
                h.createQuery(countQuery.toString())
                    .bind("searchTerm", searchPageRequest.getQuery())
                    .mapTo(Long.class)
                    .findOne()
                    .get());

    return new SearchPageResponseImpl<>(result, searchPageRequest, total);
  }

  @Override
  public Collection findOne(UUID uuid) {
    return findOne(uuid, null);
  }

  @Override
  public Collection findOne(Identifier identifier) {
    if (identifier.getIdentifiable() != null) {
      return findOne(identifier.getIdentifiable());
    }

    String namespace = identifier.getNamespace();
    String identifierId = identifier.getId();

    String innerQuery =
        "SELECT * FROM collections AS c"
            + " LEFT JOIN identifiers AS id ON c.uuid = id.identifiable"
            + " WHERE id.identifier = :id AND id.namespace = :namespace";

    final String sql =
        "SELECT"
            + SQL_FULL_COLLECTION_FIELDS_COL
            + ","
            + SQL_FULL_IDENTIFIER_FIELDS_ID
            + ","
            + SQL_PREVIEW_IMAGE_FIELDS_PI
            + " FROM ("
            + innerQuery
            + ") AS c"
            + " LEFT JOIN identifiers AS id ON c.uuid = id.identifiable"
            + " LEFT JOIN fileresources_image AS file ON c.previewfileresource = file.uuid";

    Optional<Collection> result =
        dbi
            .withHandle(
                h ->
                    h.createQuery(sql)
                        .bind("id", identifierId)
                        .bind("namespace", namespace)
                        .registerRowMapper(BeanMapper.factory(CollectionImpl.class, "col"))
                        .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "pi"))
                        .reduceRows(
                            new LinkedHashMap<UUID, Collection>(), mapRowToCollection(true)))
            .values()
            .stream()
            .findFirst();

    Collection collection = result.orElse(null);
    if (collection != null) {
      // TODO could be replaced with another join in above query...
      collection.setChildren(getChildren(collection));
    }
    return collection;
  }

  @Override
  public Collection findOne(UUID uuid, Filtering filtering) {
    StringBuilder innerQuery =
        new StringBuilder("SELECT * FROM collections AS c" + " WHERE c.uuid = :uuid");
    addFiltering(filtering, innerQuery);

    final String sql =
        "SELECT"
            + SQL_FULL_COLLECTION_FIELDS_COL
            + ","
            + SQL_FULL_IDENTIFIER_FIELDS_ID
            + ","
            + SQL_PREVIEW_IMAGE_FIELDS_PI
            + " FROM ("
            + innerQuery
            + ") AS c"
            + " LEFT JOIN identifiers AS id ON c.uuid = id.identifiable"
            + " LEFT JOIN fileresources_image AS file ON c.previewfileresource = file.uuid";

    Collection result =
        dbi.withHandle(
                h ->
                    h.createQuery(sql)
                        .bind("uuid", uuid)
                        .registerRowMapper(BeanMapper.factory(CollectionImpl.class, "col"))
                        .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "pi"))
                        .reduceRows(
                            new LinkedHashMap<UUID, Collection>(), mapRowToCollection(true)))
            .get(uuid);

    if (result != null) {
      // TODO could be replaced with another join in above query...
      result.setChildren(getChildren(result));
    }
    return result;
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[] {
      "created", "label", "lastModified", "publicationEnd", "publicationStart", "refId"
    };
  }

  @Override
  public BreadcrumbNavigation getBreadcrumbNavigation(UUID nodeUuid) {

    List<NodeImpl> result =
        dbi.withHandle(
            h ->
                h.createQuery(
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
      result =
          dbi.withHandle(
              h ->
                  h.createQuery(
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
  public List<Collection> getChildren(Collection collection) {
    return CollectionRepository.super.getChildren(collection);
  }

  @Override
  public List<Collection> getChildren(UUID uuid) {
    String innerQuery =
        "SELECT * FROM collections AS c"
            + " INNER JOIN collection_collections cc ON c.uuid = cc.child_collection_uuid"
            + " WHERE cc.parent_collection_uuid = :uuid"
            + " ORDER BY cc.sortIndex ASC";

    final String sql =
        "SELECT"
            + SQL_REDUCED_COLLECTION_FIELDS_COL
            + ","
            + SQL_PREVIEW_IMAGE_FIELDS_PI
            + " FROM ("
            + innerQuery
            + ") AS c"
            + " LEFT JOIN fileresources_image AS file ON c.previewfileresource = file.uuid";

    List<Collection> result =
        dbi.withHandle(
            h ->
                h
                    .createQuery(sql)
                    .bind("uuid", uuid)
                    .registerRowMapper(BeanMapper.factory(CollectionImpl.class, "col"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "pi"))
                    .reduceRows(new LinkedHashMap<UUID, Collection>(), mapRowToCollection())
                    .values()
                    .stream()
                    .collect(Collectors.toList()));
    return result;
  }

  @Override
  public PageResponse<Collection> getChildren(UUID uuid, PageRequest pageRequest) {
    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT * FROM collections AS c"
                + " INNER JOIN collection_collections cc ON c.uuid = cc.child_collection_uuid"
                + " WHERE cc.parent_collection_uuid = :uuid");
    addFiltering(pageRequest, innerQuery);
    pageRequest.setSorting(null);
    innerQuery.append(" ORDER BY cc.sortIndex ASC");
    addPageRequestParams(pageRequest, innerQuery);

    final String sql =
        "SELECT"
            + SQL_REDUCED_COLLECTION_FIELDS_COL
            + ","
            + SQL_PREVIEW_IMAGE_FIELDS_PI
            + " FROM ("
            + innerQuery
            + ") AS c"
            + " LEFT JOIN fileresources_image AS file ON c.previewfileresource = file.uuid";

    List<Collection> result =
        dbi.withHandle(
            h ->
                h
                    .createQuery(sql)
                    .bind("uuid", uuid)
                    .registerRowMapper(BeanMapper.factory(CollectionImpl.class, "col"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "pi"))
                    .reduceRows(new LinkedHashMap<UUID, Collection>(), mapRowToCollection())
                    .values()
                    .stream()
                    .collect(Collectors.toList()));

    StringBuilder countQuery =
        new StringBuilder(
            "SELECT count(*) FROM collections AS c"
                + " INNER JOIN collection_collections cc ON c.uuid = cc.child_collection_uuid"
                + " WHERE cc.parent_collection_uuid = :uuid");
    addFiltering(pageRequest, countQuery);
    long total =
        dbi.withHandle(
            h ->
                h.createQuery(countQuery.toString())
                    .bind("uuid", uuid)
                    .mapTo(Long.class)
                    .findOne()
                    .get());

    return new PageResponseImpl<>(result, pageRequest, total);
  }

  @Override
  protected String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "created":
        return "c.created";
      case "label":
        return "c.label";
      case "lastModified":
        return "c.last_modified";
      case "publicationEnd":
        return "c.publication_end";
      case "publicationStart":
        return "c.publication_start";
      case "refId":
        return "c.refid";
      default:
        return null;
    }
  }

  @Override
  public PageResponse<DigitalObject> getDigitalObjects(
      UUID collectionUuid, PageRequest pageRequest) {
    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT * FROM digitalobjects as d"
                + " LEFT JOIN collection_digitalobjects AS cd ON d.uuid = cd.digitalobject_uuid"
                + " WHERE cd.collection_uuid = :uuid");
    addFiltering(pageRequest, innerQuery);
    pageRequest.setSorting(null);
    innerQuery.append(" ORDER BY cd.sortIndex ASC");
    addPageRequestParams(pageRequest, innerQuery);

    final String sql =
        "SELECT"
            + SQL_REDUCED_DIGITALOBJECT_FIELDS_DO
            + ","
            + SQL_FULL_IDENTIFIER_FIELDS_ID
            + ","
            + SQL_PREVIEW_IMAGE_FIELDS_PI
            + " FROM ("
            + innerQuery
            + ") AS d"
            + " LEFT JOIN identifiers AS id ON d.uuid = id.identifiable"
            + " LEFT JOIN fileresources_image AS file ON d.previewfileresource = file.uuid";

    List<DigitalObject> result =
        dbi
            .withHandle(
                h ->
                    h.createQuery(sql)
                        .bind("uuid", collectionUuid)
                        .registerRowMapper(BeanMapper.factory(DigitalObjectImpl.class, "do"))
                        .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "pi"))
                        .reduceRows(
                            new LinkedHashMap<UUID, DigitalObject>(), mapRowToDigitalObject(true)))
            .values()
            .stream()
            .collect(Collectors.toList());
    StringBuilder countQuery =
        new StringBuilder(
            "SELECT count(*) FROM digitalobjects AS d"
                + " LEFT JOIN collection_digitalobjects AS cd ON d.uuid = cd.digitalobject_uuid"
                + " WHERE cd.collection_uuid = :uuid");
    addFiltering(pageRequest, countQuery);
    long total =
        dbi.withHandle(
            h ->
                h.createQuery(countQuery.toString())
                    .bind("uuid", collectionUuid)
                    .mapTo(Long.class)
                    .findOne()
                    .get());
    return new PageResponseImpl<>(result, pageRequest, total);
  }

  @Override
  public Collection getParent(UUID uuid) {
    String innerQuery =
        "SELECT * FROM collections as c"
            + " INNER JOIN collection_collections cc ON c.uuid = cc.parent_collection_uuid"
            + " WHERE cc.child_collection_uuid = :uuid";

    final String sql =
        "SELECT"
            + SQL_REDUCED_COLLECTION_FIELDS_COL
            + ","
            + SQL_PREVIEW_IMAGE_FIELDS_PI
            + " FROM ("
            + innerQuery
            + ") AS c"
            + " LEFT JOIN fileresources_image AS file ON c.previewfileresource = file.uuid";

    Optional<Collection> result =
        dbi
            .withHandle(
                h ->
                    h.createQuery(sql)
                        .bind("uuid", uuid)
                        .registerRowMapper(BeanMapper.factory(CollectionImpl.class, "col"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "pi"))
                        .reduceRows(new LinkedHashMap<UUID, Collection>(), mapRowToCollection()))
            .values()
            .stream()
            .findFirst();
    return result.orElse(null);
  }

  @Override
  public List<Collection> getParents(UUID uuid) {
    String innerQuery =
        "SELECT * FROM collections as c"
            + " INNER JOIN collection_collections cc ON c.uuid = cc.parent_collection_uuid"
            + " WHERE cc.child_collection_uuid = :uuid";

    final String sql =
        "SELECT"
            + SQL_REDUCED_COLLECTION_FIELDS_COL
            + ","
            + SQL_PREVIEW_IMAGE_FIELDS_PI
            + " FROM ("
            + innerQuery
            + ") AS c"
            + " LEFT JOIN fileresources_image AS file ON c.previewfileresource = file.uuid";

    List<Collection> result =
        dbi
            .withHandle(
                h ->
                    h.createQuery(sql)
                        .bind("uuid", uuid)
                        .registerRowMapper(BeanMapper.factory(CollectionImpl.class, "col"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "pi"))
                        .reduceRows(new LinkedHashMap<UUID, Collection>(), mapRowToCollection()))
            .values()
            .stream()
            .collect(Collectors.toList());
    return result;
  }

  @Override
  public List<CorporateBody> getRelatedCorporateBodies(UUID uuid, Filtering filtering) {
    // We do a double join with "rel_entity_entities" because we have two different
    // predicates:
    // - one is fix ("is_part_of"): defines the relation between collection and project
    // - the other one is given as part of the parameter "filtering" for defining relation
    //   between corporatebody and project
    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT * FROM corporatebodies AS cb"
                + " LEFT JOIN rel_entity_entities AS r ON cb.uuid = r.object_uuid"
                + " LEFT JOIN rel_entity_entities AS rel ON r.subject_uuid = rel.subject_uuid"
                + " WHERE rel.object_uuid = :uuid"
                + " AND rel.predicate = 'is_part_of'");
    FilterCriterion predicate = filtering.getFilterCriterionFor("predicate");
    if (predicate != null) {
      String predicateFilter = String.format(" AND r.predicate = '%s'", predicate.getValue());
      innerQuery.append(predicateFilter);
    }

    final String sql =
        "SELECT"
            + SQL_REDUCED_CORPORATEBODY_FIELDS_CB
            + ","
            + SQL_PREVIEW_IMAGE_FIELDS_PI
            + " FROM ("
            + innerQuery
            + ") AS cb"
            + " LEFT JOIN fileresources_image AS file ON cb.previewfileresource = file.uuid";

    List<CorporateBody> result =
        dbi
            .withHandle(
                h ->
                    h.createQuery(sql)
                        .bind("uuid", uuid)
                        .registerRowMapper(BeanMapper.factory(CorporateBodyImpl.class, "cb"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "pi"))
                        .reduceRows(
                            new LinkedHashMap<UUID, CorporateBody>(), mapRowToCorporateBody()))
            .values()
            .stream()
            .collect(Collectors.toList());
    return result;
  }

  @Override
  public PageResponse<Collection> getTopCollections(PageRequest pageRequest) {
    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT * FROM collections AS c"
                + " WHERE NOT EXISTS (SELECT FROM collection_collections WHERE child_collection_uuid = c.uuid)");
    addFiltering(pageRequest, innerQuery);
    addPageRequestParams(pageRequest, innerQuery);

    final String sql =
        "SELECT"
            + SQL_REDUCED_COLLECTION_FIELDS_COL
            + ","
            + SQL_PREVIEW_IMAGE_FIELDS_PI
            + " FROM ("
            + innerQuery
            + ") AS c"
            + " LEFT JOIN fileresources_image AS file ON c.previewfileresource = file.uuid";

    List<Collection> result1 =
        dbi
            .withHandle(
                h ->
                    h.createQuery(sql)
                        .registerRowMapper(BeanMapper.factory(CollectionImpl.class, "col"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "pi"))
                        .reduceRows(new LinkedHashMap<UUID, Collection>(), mapRowToCollection()))
            .values()
            .stream()
            .collect(Collectors.toList());

    List<Collection> result = result1;

    StringBuilder countQuery =
        new StringBuilder(
            "SELECT count(*) FROM collections AS c"
                + " WHERE NOT EXISTS (SELECT FROM collection_collections WHERE child_collection_uuid = c.uuid)");
    addFiltering(pageRequest, countQuery);
    long total =
        dbi.withHandle(h -> h.createQuery(countQuery.toString()).mapTo(Long.class).findOne().get());

    return new PageResponseImpl<>(result, pageRequest, total);
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
  public Collection save(Collection collection) {
    collection.setUuid(UUID.randomUUID());
    collection.setCreated(LocalDateTime.now());
    collection.setLastModified(LocalDateTime.now());
    // refid is generated as serial, DO NOT SET!
    final UUID previewImageUuid =
        collection.getPreviewImage() == null ? null : collection.getPreviewImage().getUuid();

    final String sql =
        "INSERT INTO collections("
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
        h ->
            h.createUpdate(sql)
                .bind("previewFileResource", previewImageUuid)
                .bindBean(collection)
                .execute());

    // save identifiers
    Set<Identifier> identifiers = collection.getIdentifiers();
    saveIdentifiers(identifiers, collection);

    Collection result = findOne(collection.getUuid());
    return result;
  }

  @Override
  public boolean saveDigitalObjects(UUID collectionUuid, List<DigitalObject> digitalObjects) {
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
  public Collection saveWithParentCollection(Collection collection, UUID parentUuid) {
    final UUID childUuid =
        collection.getUuid() == null ? save(collection).getUuid() : collection.getUuid();
    Integer sortindex =
        selectNextSortIndexForParentChildren(
            dbi, "collection_collections", "parent_collection_uuid", parentUuid);
    dbi.withHandle(
        h ->
            h.createUpdate(
                    "INSERT INTO collection_collections(parent_collection_uuid, child_collection_uuid, sortindex)"
                        + " VALUES (:parent_collection_uuid, :child_collection_uuid, :sortindex)")
                .bind("parent_collection_uuid", parentUuid)
                .bind("child_collection_uuid", childUuid)
                .bind("sortindex", sortindex)
                .execute());

    return findOne(childUuid);
  }

  @Override
  public Collection update(Collection collection) {
    collection.setLastModified(LocalDateTime.now());
    // do not update/left out from statement (not changed since insert):
    // uuid, created, identifiable_type, entity_type, refid
    final UUID previewImageUuid =
        collection.getPreviewImage() == null ? null : collection.getPreviewImage().getUuid();

    final String sql =
        "UPDATE collections SET"
            + " label=:label::JSONB, description=:description::JSONB,"
            + " previewfileresource=:previewFileResource, preview_hints=:previewImageRenderingHints::JSONB,"
            + " last_modified=:lastModified,"
            + " text=:text::JSONB, publication_start=:publicationStart, publication_end=:publicationEnd"
            + " WHERE uuid=:uuid";

    dbi.withHandle(
        h ->
            h.createUpdate(sql)
                .bind("previewFileResource", previewImageUuid)
                .bindBean(collection)
                .execute());

    // save identifiers
    // as we store the whole list new: delete old entries
    deleteIdentifiers(collection);
    Set<Identifier> identifiers = collection.getIdentifiers();
    saveIdentifiers(identifiers, collection);

    Collection result = findOne(collection.getUuid());
    return result;
  }
}
