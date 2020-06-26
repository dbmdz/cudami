package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.CollectionRepository;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.entity.Collection;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.IdentifierImpl;
import de.digitalcollections.model.impl.identifiable.entity.CollectionImpl;
import de.digitalcollections.model.impl.identifiable.resource.ImageFileResourceImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
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
          + " c.text c_text, c.preview_hints c_previewImageRenderingHints,"
          + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
          + " file.uuid f_uuid, file.filename f_filename, file.mimetype f_mimeType, file.size_in_bytes f_sizeInBytes, file.uri f_uri, file.iiif_base_url f_iiifBaseUrl"
          + " FROM collections as c"
          + " LEFT JOIN identifiers as id on c.uuid = id.identifiable"
          + " LEFT JOIN fileresources_image as file on c.previewfileresource = file.uuid";

  // select only what is shown/needed in paged list (commented some additional available fields
  // not needed in overview list to avoid unnecessary payload/traffic):
  private static final String REDUCED_FIND_ONE_BASE_SQL =
      "SELECT c.uuid c_uuid, c.refid c_refId, c.label c_label, c.description c_description,"
          + " c.identifiable_type c_type, c.entity_type c_entityType,"
          + " c.created c_created, c.last_modified c_lastModified,"
          + " c.preview_hints c_previewImageRenderingHints,"
          + " file.uuid f_uuid, file.filename f_filename, file.mimetype f_mimeType, file.size_in_bytes f_sizeInBytes, file.uri f_uri, file.iiif_base_url f_iiifBaseUrl"
          + " FROM collections as c"
          + " LEFT JOIN fileresources_image as file on c.previewfileresource = file.uuid";

  private static final String BASE_CHILDREN_QUERY =
      REDUCED_FIND_ONE_BASE_SQL
          + " INNER JOIN collection_collections cc ON c.uuid = cc.child_collection_uuid"
          + " WHERE cc.parent_collection_uuid = :uuid";

  private static final String BASE_TOP_QUERY =
      REDUCED_FIND_ONE_BASE_SQL
          + " WHERE NOT EXISTS (SELECT FROM collection_collections WHERE child_collection_uuid = c.uuid)";

  /*
    SELECT ip
  FROM   login_log l
  WHERE  NOT EXISTS (
     SELECT  -- SELECT list mostly irrelevant; can just be empty in Postgres
     FROM   ip_location
     WHERE  ip = l.ip
     );
     */

  @Autowired
  public CollectionRepositoryImpl(Jdbi dbi, IdentifierRepository identifierRepository) {
    super(dbi, identifierRepository);
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
            + " text"
            + ") VALUES ("
            + ":uuid, :label::JSONB, :description::JSONB, :previewFileResource, :previewImageRenderingHints::JSONB,"
            + " :type, :entityType,"
            + " :created, :lastModified,"
            + " :text::JSONB"
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
            + " text=:text::JSONB"
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
    return new String[] {"created", "lastModified", "refId"};
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
      case "refId":
        return "c.refid";
      default:
        return null;
    }
  }

  @Override
  public List<Collection> getChildren(Collection collection) {
    return CollectionRepository.super.getChildren(collection);
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
  public PageResponse<Collection> getRootCollections(PageRequest pageRequest) {
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
}
