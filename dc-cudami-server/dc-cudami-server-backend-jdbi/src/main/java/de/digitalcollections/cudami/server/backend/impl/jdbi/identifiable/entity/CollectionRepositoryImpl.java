package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.CollectionRepository;
import de.digitalcollections.model.api.filter.FilterCriterion;
import de.digitalcollections.model.api.filter.Filtering;
import de.digitalcollections.model.api.filter.enums.FilterOperation;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.Node;
import de.digitalcollections.model.api.identifiable.entity.Collection;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.SearchPageRequest;
import de.digitalcollections.model.api.paging.SearchPageResponse;
import de.digitalcollections.model.api.view.BreadcrumbNavigation;
import de.digitalcollections.model.impl.identifiable.IdentifierImpl;
import de.digitalcollections.model.impl.identifiable.NodeImpl;
import de.digitalcollections.model.impl.identifiable.entity.CollectionImpl;
import de.digitalcollections.model.impl.identifiable.entity.DigitalObjectImpl;
import de.digitalcollections.model.impl.identifiable.resource.ImageFileResourceImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import de.digitalcollections.model.impl.paging.SearchPageResponseImpl;
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
public class CollectionRepositoryImpl extends EntityRepositoryImpl<Collection>
    implements CollectionRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(CollectionRepositoryImpl.class);

  // select all details shown/needed in single object details page
  private static final String FIND_ONE_BASE_SQL =
      "SELECT c.uuid c_uuid, c.refid c_refId, c.label c_label, c.description c_description,"
          + " c.identifiable_type c_type, c.entity_type c_entityType,"
          + " c.created c_created, c.last_modified c_lastModified,"
          + " c.text c_text, c.publication_start c_publicationStart, c.publication_end c_publicationEnd,"
          + " c.preview_hints c_previewImageRenderingHints,"
          + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
          + " file.uuid f_uuid, file.filename f_filename, file.mimetype f_mimeType, file.size_in_bytes f_sizeInBytes, file.uri f_uri, file.http_base_url f_httpBaseUrl"
          + " FROM collections as c"
          + " LEFT JOIN identifiers as id on c.uuid = id.identifiable"
          + " LEFT JOIN fileresources_image as file on c.previewfileresource = file.uuid";

  // select only what is shown/needed in paged list (commented some additional available fields
  // not needed in overview list to avoid unnecessary payload/traffic):
  private static final String REDUCED_FIND_ONE_BASE_SQL =
      "SELECT c.uuid c_uuid, c.refid c_refId, c.label c_label, c.description c_description,"
          + " c.identifiable_type c_type, c.entity_type c_entityType,"
          + " c.created c_created, c.last_modified c_lastModified,"
          + " c.publication_start c_publicationStart, c.publication_end c_publicationEnd,"
          + " c.preview_hints c_previewImageRenderingHints,"
          + " file.uuid f_uuid, file.filename f_filename, file.mimetype f_mimeType, file.size_in_bytes f_sizeInBytes, file.uri f_uri, file.http_base_url f_httpBaseUrl"
          + " FROM collections as c"
          + " LEFT JOIN fileresources_image as file on c.previewfileresource = file.uuid";

  private static final String BASE_CHILDREN_QUERY =
      REDUCED_FIND_ONE_BASE_SQL
          + " INNER JOIN collection_collections cc ON c.uuid = cc.child_collection_uuid"
          + " WHERE cc.parent_collection_uuid = :uuid";

  private static final String BASE_TOP_QUERY =
      REDUCED_FIND_ONE_BASE_SQL
          + " WHERE NOT EXISTS (SELECT FROM collection_collections WHERE child_collection_uuid = c.uuid)";

  private static final String BREADCRUMB_QUERY =
      "WITH recursive breadcrumb (uuid,label,parent_uuid,depth)"
          + " AS ("
          + "        SELECT c.uuid as uuid, c.label as label, cc.parent_collection_uuid as parent_uuid,99 as depth"
          + "        FROM collections c, collection_collections cc"
          + "        WHERE uuid= :uuid and cc.child_collection_uuid = c.uuid"
          + ""
          + "        UNION ALL"
          + "        SELECT c.uuid as uuid, c.label as label, cc.parent_collection_uuid as parent_uuid, depth-1 as depth"
          + "        FROM collections c,"
          + "             collection_collections cc,"
          + "             breadcrumb b"
          + "        WHERE b.uuid = cc.child_collection_uuid and cc.parent_collection_uuid = c.uuid AND cc.parent_collection_uuid is not null"
          + "    )"
          + " SELECT * from breadcrumb"
          + " ORDER BY depth ASC";

  private static final String BREADCRUMB_WITHOUT_PARENT_QUERY =
      "SELECT c.uuid as uuid, c.label as label"
          + "        FROM collections c"
          + "        WHERE uuid= :uuid";

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
          for (Collection child : children) {
            preparedBatch
                .bind("parentCollectionUuid", parentUuid)
                .bind("childCollectionUuid", child.getUuid())
                .bind("sortIndex", nextSortIndex + getIndex(children, child))
                .add();
          }
          preparedBatch.execute();
        });
    return true;
  }

  @Override
  public List<Collection> getChildren(Collection collection) {
    return CollectionRepository.super.getChildren(collection);
  }

  @Override
  public boolean removeChild(UUID parentUuid, UUID childUuid) {
    if (parentUuid == null || childUuid == null) {
      return false;
    }
    String query =
        "DELETE FROM collection_collections WHERE parent_collection_uuid=:parentCollectionUuid AND child_collection_uuid=:childCollectionUuid";

    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("parentCollectionUuid", parentUuid)
                .bind("childCollectionUuid", childUuid)
                .execute());
    return true;
  }

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM collections";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOne().get());
    return count;
  }

  @Override
  public PageResponse<Collection> find(PageRequest pageRequest) {
    StringBuilder query = new StringBuilder(REDUCED_FIND_ONE_BASE_SQL);
    addPageRequestParams(pageRequest, query);

    List<CollectionImpl> result =
        new ArrayList(
            dbi.withHandle(
                h ->
                    h.createQuery(query.toString())
                        .registerRowMapper(BeanMapper.factory(CollectionImpl.class, "c"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .reduceRows(
                            new LinkedHashMap<UUID, CollectionImpl>(),
                            (map, rowView) -> {
                              CollectionImpl collection =
                                  map.computeIfAbsent(
                                      rowView.getColumn("c_uuid", UUID.class),
                                      fn -> {
                                        return rowView.getRow(CollectionImpl.class);
                                      });

                              if (rowView.getColumn("f_uuid", UUID.class) != null) {
                                collection.setPreviewImage(
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
  public SearchPageResponse<Collection> find(SearchPageRequest searchPageRequest) {
    // select only what is shown/needed in paged result list:
    StringBuilder query =
        new StringBuilder(
            "SELECT c.uuid c_uuid, c.refid c_refId, c.label c_label, c.description c_description,"
                + " c.entity_type c_entityType,"
                + " file.uuid f_uuid, file.filename f_filename, file.mimetype f_mimeType, file.size_in_bytes f_sizeInBytes, file.uri f_uri, file.http_base_url f_httpBaseUrl"
                + " FROM collections as c"
                + " LEFT JOIN fileresources_image as file on c.previewfileresource = file.uuid"
                + " LEFT JOIN LATERAL jsonb_object_keys(c.label) l(keys) on c.label is not null"
                + " LEFT JOIN LATERAL jsonb_object_keys(c.description) n(keys) on c.description is not null"
                + " WHERE (c.label->>l.keys ilike '%' || :searchTerm || '%'"
                + " OR c.description->>n.keys ilike '%' || :searchTerm || '%')");
    addPageRequestParams(searchPageRequest, query);

    List<Collection> result =
        new ArrayList(
            dbi.withHandle(
                h ->
                    h.createQuery(query.toString())
                        .bind("searchTerm", searchPageRequest.getQuery())
                        .registerRowMapper(BeanMapper.factory(CollectionImpl.class, "c"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .reduceRows(
                            new LinkedHashMap<UUID, Collection>(),
                            (map, rowView) -> {
                              Collection collection =
                                  map.computeIfAbsent(
                                      rowView.getColumn("c_uuid", UUID.class),
                                      uuid -> rowView.getRow(CollectionImpl.class));
                              if (rowView.getColumn("f_uuid", String.class) != null) {
                                collection.setPreviewImage(
                                    rowView.getRow(ImageFileResourceImpl.class));
                              }
                              return map;
                            })
                        .values()));

    String countQuery =
        "SELECT count(*) FROM collections as c"
            + " LEFT JOIN LATERAL jsonb_object_keys(c.label) l(keys) on c.label is not null"
            + " LEFT JOIN LATERAL jsonb_object_keys(c.description) n(keys) on c.description is not null"
            + " WHERE (c.label->>l.keys ilike '%' || :searchTerm || '%'"
            + " OR c.description->>n.keys ilike '%' || :searchTerm || '%')";
    long total =
        dbi.withHandle(
            h ->
                h.createQuery(countQuery)
                    .bind("searchTerm", searchPageRequest.getQuery())
                    .mapTo(Long.class)
                    .findOne()
                    .get());

    return new SearchPageResponseImpl(result, searchPageRequest, total);
  }

  @Override
  public Collection findOne(UUID uuid) {
    String query = FIND_ONE_BASE_SQL + " WHERE c.uuid = :uuid";

    CollectionImpl result =
        dbi.withHandle(
                h ->
                    h.createQuery(query)
                        .bind("uuid", uuid)
                        .registerRowMapper(BeanMapper.factory(CollectionImpl.class, "c"))
                        .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .reduceRows(
                            new LinkedHashMap<UUID, CollectionImpl>(),
                            (map, rowView) -> {
                              CollectionImpl collection =
                                  map.computeIfAbsent(
                                      rowView.getColumn("c_uuid", UUID.class),
                                      fn -> {
                                        return rowView.getRow(CollectionImpl.class);
                                      });

                              if (rowView.getColumn("f_uuid", UUID.class) != null) {
                                collection.setPreviewImage(
                                    rowView.getRow(ImageFileResourceImpl.class));
                              }

                              if (rowView.getColumn("id_uuid", UUID.class) != null) {
                                IdentifierImpl identifier = rowView.getRow(IdentifierImpl.class);
                                collection.addIdentifier(identifier);
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
  public Collection findOne(Identifier identifier) {
    if (identifier.getIdentifiable() != null) {
      return findOne(identifier.getIdentifiable());
    }

    String namespace = identifier.getNamespace();
    String identifierId = identifier.getId();

    String query = FIND_ONE_BASE_SQL + " WHERE id.identifier = :id AND id.namespace = :namespace";

    Optional<CollectionImpl> result =
        dbi
            .withHandle(
                h ->
                    h.createQuery(query)
                        .bind("id", identifierId)
                        .bind("namespace", namespace)
                        .registerRowMapper(BeanMapper.factory(CollectionImpl.class, "c"))
                        .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .reduceRows(
                            new LinkedHashMap<UUID, CollectionImpl>(),
                            (map, rowView) -> {
                              CollectionImpl collection =
                                  map.computeIfAbsent(
                                      rowView.getColumn("c_uuid", UUID.class),
                                      fn -> {
                                        return rowView.getRow(CollectionImpl.class);
                                      });

                              if (rowView.getColumn("f_uuid", UUID.class) != null) {
                                collection.setPreviewImage(
                                    rowView.getRow(ImageFileResourceImpl.class));
                              }

                              if (rowView.getColumn("id_uuid", UUID.class) != null) {
                                IdentifierImpl dbIdentifier = rowView.getRow(IdentifierImpl.class);
                                collection.addIdentifier(dbIdentifier);
                              }

                              return map;
                            }))
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
  public PageResponse<DigitalObject> getDigitalObjects(
      UUID collectionUuid, PageRequest pageRequest) {
    final String baseQuery =
        "SELECT d.uuid d_uuid, d.label d_label, d.refid d_refId,"
            + " d.created d_created, d.last_modified d_lastModified,"
            + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
            + " file.uuid pf_uuid, file.filename pf_filename, file.mimetype pf_mimeType, file.size_in_bytes pf_sizeInBytes, file.uri pf_uri, file.http_base_url pf_httpBaseUrl"
            + " FROM digitalobjects as d"
            + " LEFT JOIN identifiers as id on d.uuid = id.identifiable"
            + " LEFT JOIN fileresources_image as file on d.previewfileresource = file.uuid"
            + " LEFT JOIN collection_digitalobjects as cd on d.uuid = cd.digitalobject_uuid"
            + " WHERE cd.collection_uuid = :uuid"
            + " ORDER BY cd.sortIndex ASC";
    StringBuilder query = new StringBuilder(baseQuery);

    // we add fix sorting in above query; otherwise we get in conflict with allowed sorting
    // and column names of this repository (it is for collections, not sublists of
    // digitalobjects...)
    pageRequest.setSorting(null);
    addPageRequestParams(pageRequest, query);

    List<DigitalObject> result =
        dbi.withHandle(
            h ->
                h
                    .createQuery(query.toString())
                    .bind("uuid", collectionUuid)
                    .registerRowMapper(BeanMapper.factory(DigitalObjectImpl.class, "d"))
                    .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "pf"))
                    .reduceRows(
                        new LinkedHashMap<UUID, DigitalObjectImpl>(),
                        (map, rowView) -> {
                          DigitalObjectImpl digitalObject =
                              map.computeIfAbsent(
                                  rowView.getColumn("d_uuid", UUID.class),
                                  fn -> {
                                    return rowView.getRow(DigitalObjectImpl.class);
                                  });

                          if (rowView.getColumn("pf_uuid", UUID.class) != null) {
                            digitalObject.setPreviewImage(
                                rowView.getRow(ImageFileResourceImpl.class));
                          }

                          if (rowView.getColumn("id_uuid", UUID.class) != null) {
                            IdentifierImpl dbIdentifier = rowView.getRow(IdentifierImpl.class);
                            digitalObject.addIdentifier(dbIdentifier);
                          }
                          return map;
                        })
                    .values()
                    .stream()
                    .map(DigitalObject.class::cast)
                    .collect(Collectors.toList()));
    String countQuery =
        "SELECT count(*) FROM digitalobjects as d"
            + " LEFT JOIN collection_digitalobjects as cd on d.uuid = cd.digitalobject_uuid"
            + " WHERE cd.collection_uuid = :uuid";
    long total =
        dbi.withHandle(
            h ->
                h.createQuery(countQuery)
                    .bind("uuid", collectionUuid)
                    .mapTo(Long.class)
                    .findOne()
                    .get());
    PageResponse<DigitalObject> pageResponse = new PageResponseImpl<>(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public Collection save(Collection collection) {
    collection.setUuid(UUID.randomUUID());
    collection.setCreated(LocalDateTime.now());
    collection.setLastModified(LocalDateTime.now());
    // refid is generated as serial, DO NOT SET!
    final UUID previewImageUuid =
        collection.getPreviewImage() == null ? null : collection.getPreviewImage().getUuid();

    String query =
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
            h.createUpdate(query)
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
            for (DigitalObject digitalObject : digitalObjects) {
              preparedBatch
                  .bind("uuid", collectionUuid)
                  .bind("digitalObjectUuid", digitalObject.getUuid())
                  .bind("sortIndex", nextSortIndex + getIndex(digitalObjects, digitalObject))
                  .add();
            }
            preparedBatch.execute();
          });
      return true;
    }
    return false;
  }

  @Override
  public Collection update(Collection collection) {
    collection.setLastModified(LocalDateTime.now());
    // do not update/left out from statement (not changed since insert):
    // uuid, created, identifiable_type, entity_type, refid
    final UUID previewImageUuid =
        collection.getPreviewImage() == null ? null : collection.getPreviewImage().getUuid();

    String query =
        "UPDATE collections SET"
            + " label=:label::JSONB, description=:description::JSONB,"
            + " previewfileresource=:previewFileResource, preview_hints=:previewImageRenderingHints::JSONB,"
            + " last_modified=:lastModified,"
            + " text=:text::JSONB, publication_start=:publicationStart, publication_end=:publicationEnd"
            + " WHERE uuid=:uuid";

    dbi.withHandle(
        h ->
            h.createUpdate(query)
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

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[] {"created", "lastModified", "publicationEnd", "publicationStart", "refId"};
  }

  @Override
  protected String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "created":
        return "c.created";
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
  public Collection getParent(UUID uuid) {
    String query =
        REDUCED_FIND_ONE_BASE_SQL
            + " INNER JOIN collection_collections cc ON c.uuid = cc.parent_collection_uuid"
            + " WHERE cc.child_collection_uuid = :uuid";

    Optional<CollectionImpl> result =
        dbi
            .withHandle(
                h ->
                    h.createQuery(query)
                        .bind("uuid", uuid)
                        .registerRowMapper(BeanMapper.factory(CollectionImpl.class, "c"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .reduceRows(
                            new LinkedHashMap<UUID, CollectionImpl>(),
                            (map, rowView) -> {
                              CollectionImpl parent =
                                  map.computeIfAbsent(
                                      rowView.getColumn("c_uuid", UUID.class),
                                      fn -> {
                                        return rowView.getRow(CollectionImpl.class);
                                      });

                              if (rowView.getColumn("f_uuid", UUID.class) != null) {
                                parent.setPreviewImage(rowView.getRow(ImageFileResourceImpl.class));
                              }
                              return map;
                            }))
            .values()
            .stream()
            .findFirst();
    return result.orElse(null);
  }

  @Override
  public List<Collection> getChildren(UUID uuid) {
    // minimal data required (= identifiable fields) for creating text links/teasers in a list
    String query = BASE_CHILDREN_QUERY + " ORDER BY cc.sortIndex ASC";

    List<Collection> result =
        new ArrayList(
            dbi.withHandle(
                h ->
                    h.createQuery(query)
                        .bind("uuid", uuid)
                        .registerRowMapper(BeanMapper.factory(CollectionImpl.class, "c"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .reduceRows(
                            new LinkedHashMap<UUID, CollectionImpl>(),
                            (map, rowView) -> {
                              CollectionImpl collection =
                                  map.computeIfAbsent(
                                      rowView.getColumn("c_uuid", UUID.class),
                                      fn -> {
                                        return rowView.getRow(CollectionImpl.class);
                                      });

                              if (rowView.getColumn("f_uuid", UUID.class) != null) {
                                collection.setPreviewImage(
                                    rowView.getRow(ImageFileResourceImpl.class));
                              }
                              return map;
                            })
                        .values()));
    return result;
  }

  @Override
  public PageResponse<Collection> getChildren(UUID uuid, PageRequest pageRequest) {
    // minimal data required (= identifiable fields) for creating text links/teasers in a list
    StringBuilder query = new StringBuilder(BASE_CHILDREN_QUERY);

    // handle optional filtering params
    Filtering filtering = pageRequest.getFiltering();
    if (filtering != null) {
      // handle publication start criteria
      FilterCriterion fc = (FilterCriterion) filtering.getFilterCriterionFor("publicationStart");
      if (fc != null) {
        query.append(" AND ").append(getWhereClause(fc));
      }

      // handle publication end criteria
      fc = (FilterCriterion) filtering.getFilterCriterionFor("publicationEnd");
      if (fc != null) {
        if (fc.getOperation() == FilterOperation.GREATER_THAN_OR_EQUAL_TO) {
          query
              .append(" AND (")
              .append(getWhereClause(fc))
              .append(" OR ")
              .append(getColumnName("publicationEnd"))
              .append(" IS NULL")
              .append(")");
        } else {
          query.append(" AND ").append(getWhereClause(fc));
        }
      }
    }
    query.append(" ORDER BY cc.sortIndex ASC");
    pageRequest.setSorting(null);
    addPageRequestParams(pageRequest, query);
    List<Collection> result =
        new ArrayList(
            dbi.withHandle(
                h ->
                    h.createQuery(query.toString())
                        .bind("uuid", uuid)
                        .registerRowMapper(BeanMapper.factory(CollectionImpl.class, "c"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .reduceRows(
                            new LinkedHashMap<UUID, CollectionImpl>(),
                            (map, rowView) -> {
                              CollectionImpl collection =
                                  map.computeIfAbsent(
                                      rowView.getColumn("c_uuid", UUID.class),
                                      fn -> {
                                        return rowView.getRow(CollectionImpl.class);
                                      });

                              if (rowView.getColumn("f_uuid", UUID.class) != null) {
                                collection.setPreviewImage(
                                    rowView.getRow(ImageFileResourceImpl.class));
                              }
                              return map;
                            })
                        .values()));
    String sql =
        "SELECT count(*) FROM collections as c"
            + " INNER JOIN collection_collections cc ON c.uuid = cc.child_collection_uuid"
            + " WHERE cc.parent_collection_uuid = :uuid";
    long total =
        dbi.withHandle(
            h -> h.createQuery(sql).bind("uuid", uuid).mapTo(Long.class).findOne().get());
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public PageResponse<Collection> getTopCollections(PageRequest pageRequest) {
    // minimal data required (= identifiable fields) for creating text links/teasers in a list
    StringBuilder query = new StringBuilder(BASE_TOP_QUERY);
    addPageRequestParams(pageRequest, query);
    List<Collection> result =
        new ArrayList(
            dbi.withHandle(
                h ->
                    h.createQuery(query.toString())
                        .registerRowMapper(BeanMapper.factory(CollectionImpl.class, "c"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .reduceRows(
                            new LinkedHashMap<UUID, CollectionImpl>(),
                            (map, rowView) -> {
                              CollectionImpl collection =
                                  map.computeIfAbsent(
                                      rowView.getColumn("c_uuid", UUID.class),
                                      fn -> {
                                        return rowView.getRow(CollectionImpl.class);
                                      });

                              if (rowView.getColumn("f_uuid", UUID.class) != null) {
                                collection.setPreviewImage(
                                    rowView.getRow(ImageFileResourceImpl.class));
                              }
                              return map;
                            })
                        .values()));
    String sql =
        "SELECT count(*) FROM collections as c"
            + " WHERE NOT EXISTS (SELECT FROM collection_collections WHERE child_collection_uuid = c.uuid)";
    long total = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOne().get());
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
    return pageResponse;
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
      // Special case: If we are on a top level collection, we have no parent, so
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
  public boolean removeDigitalObject(UUID collectionUuid, UUID digitalObjectUuid) {
    if (collectionUuid != null && digitalObjectUuid != null) {
      // delete relation to collection

      String query =
          "DELETE FROM collection_digitalobjects WHERE collection_uuid=:collectionUuid AND digitalobject_uuid=:digitalObjectUuid";

      dbi.withHandle(
          h ->
              h.createUpdate(query)
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

    String query =
        "DELETE FROM collection_digitalobjects WHERE digitalobject_uuid=:digitalObjectUuid";

    dbi.withHandle(
        h -> h.createUpdate(query).bind("digitalObjectUuid", digitalObject.getUuid()).execute());
    return true;
  }
}
