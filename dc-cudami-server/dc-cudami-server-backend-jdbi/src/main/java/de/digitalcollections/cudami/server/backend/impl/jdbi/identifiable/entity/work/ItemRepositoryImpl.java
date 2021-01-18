package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.work;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work.ItemRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifiableRepositoryImpl;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.entity.agent.Agent;
import de.digitalcollections.model.api.identifiable.entity.agent.Person;
import de.digitalcollections.model.api.identifiable.entity.work.Item;
import de.digitalcollections.model.api.identifiable.entity.work.Work;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.IdentifierImpl;
import de.digitalcollections.model.impl.identifiable.entity.DigitalObjectImpl;
import de.digitalcollections.model.impl.identifiable.entity.EntityImpl;
import de.digitalcollections.model.impl.identifiable.entity.agent.PersonImpl;
import de.digitalcollections.model.impl.identifiable.entity.work.ItemImpl;
import de.digitalcollections.model.impl.identifiable.entity.work.WorkImpl;
import de.digitalcollections.model.impl.identifiable.resource.ImageFileResourceImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ItemRepositoryImpl extends IdentifiableRepositoryImpl<Item> implements ItemRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(ItemRepositoryImpl.class);

  // select all details shown/needed in single object details page
  private static final String FIND_ONE_BASE_SQL =
      "SELECT i.uuid i_uuid, i.refid i_refId, i.label i_label, i.description i_description,"
          + " i.identifiable_type i_type, i.entity_type i_entityType,"
          + " i.created i_created, i.last_modified i_lastModified,"
          + " i.language i_language, i.publication_date i_publicationDate, i.publication_place i_publicationPlace, i.publisher i_publisher, i.version i_version,"
          + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
          + " file.uuid f_uuid, file.filename f_filename, file.mimetype f_mimetype, file.size_in_bytes f_size_in_bytes, file.uri f_uri"
          + " FROM items as i"
          + " LEFT JOIN identifiers as id on i.uuid = id.identifiable"
          + " LEFT JOIN fileresources_image as file on i.previewfileresource = file.uuid";

  // select only what is shown/needed in paged list (to avoid unnecessary payload/traffic):
  private static final String REDUCED_FIND_ONE_BASE_SQL =
      "SELECT i.uuid i_uuid, i.refid i_refId, i.label i_label, i.description i_description,"
          + " i.identifiable_type i_type, i.entity_type i_entityType,"
          + " i.created i_created, i.last_modified i_lastModified,"
          + " file.uuid f_uuid, file.filename f_filename, file.uri f_uri"
          + " FROM items as i"
          + " LEFT JOIN fileresources_image as file on i.previewfileresource = file.uuid";

  @Autowired
  public ItemRepositoryImpl(Jdbi dbi, IdentifierRepository identifierRepository) {
    super(dbi, identifierRepository);
  }

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM items";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOne().get());
    return count;
  }

  @Override
  public PageResponse<Item> find(PageRequest pageRequest) {
    StringBuilder query = new StringBuilder(REDUCED_FIND_ONE_BASE_SQL);
    addPageRequestParams(pageRequest, query);

    List<ItemImpl> result =
        new ArrayList(
            dbi.withHandle(
                h ->
                    h.createQuery(query.toString())
                        .registerRowMapper(BeanMapper.factory(ItemImpl.class, "i"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .reduceRows(
                            new LinkedHashMap<UUID, ItemImpl>(),
                            (map, rowView) -> {
                              ItemImpl item =
                                  map.computeIfAbsent(
                                      rowView.getColumn("i_uuid", UUID.class),
                                      uuid -> rowView.getRow(ItemImpl.class));
                              if (rowView.getColumn("f_uuid", String.class) != null) {
                                item.setPreviewImage(rowView.getRow(ImageFileResourceImpl.class));
                              }
                              return map;
                            })
                        .values()));

    long total = count();
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public PageResponse<Item> findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial) {
    // select only what is shown/needed in alphabetical sorted paged list:
    StringBuilder query =
        new StringBuilder(
            "SELECT i.uuid i_uuid, i.refid i_refId, i.label i_label, i.description i_description,"
                //      + " i.identifiable_type i_type, i.entity_type i_entityType,"
                //      + " i.created i_created, i.last_modified i_last_modified,"
                //      + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace
                // id_namespace, id.identifier id_id,"
                + " file.uuid f_uuid, file.uri f_uri, file.filename f_filename"
                + " FROM items as i"
                //      + " LEFT JOIN identifiers as id on i.uuid = id.identifiable"
                + " LEFT JOIN fileresources_image as file on i.previewfileresource = file.uuid"
                + " WHERE i.label ->> :language IS NOT null AND i.label ->> :language ILIKE :initial || '%'"
                + " ORDER BY i.label ->> :language");
    addPageRequestParams(pageRequest, query);

    List<Item> result =
        new ArrayList(
            dbi.withHandle(
                h ->
                    h.createQuery(query.toString())
                        .bind("language", language)
                        .bind("initial", initial)
                        .registerRowMapper(BeanMapper.factory(ItemImpl.class, "i"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .reduceRows(
                            new LinkedHashMap<UUID, Item>(),
                            (map, rowView) -> {
                              Item item =
                                  map.computeIfAbsent(
                                      rowView.getColumn("w_uuid", UUID.class),
                                      uuid -> rowView.getRow(ItemImpl.class));
                              if (rowView.getColumn("f_uuid", String.class) != null) {
                                item.setPreviewImage(rowView.getRow(ImageFileResourceImpl.class));
                              }
                              return map;
                            })
                        .values()));

    String countQuery =
        "SELECT count(*) FROM items as i"
            + " WHERE i.label ->> :language IS NOT null AND i.label ->> :language ILIKE :initial || '%'";
    long total =
        dbi.withHandle(
            h ->
                h.createQuery(countQuery)
                    .bind("language", language)
                    .bind("initial", initial)
                    .mapTo(Long.class)
                    .findOne()
                    .get());

    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public Item findOne(UUID uuid) {
    String query = FIND_ONE_BASE_SQL + " WHERE i.uuid = :uuid";

    ItemImpl result =
        dbi.withHandle(
                h ->
                    h.createQuery(query)
                        .bind("uuid", uuid)
                        .registerRowMapper(BeanMapper.factory(ItemImpl.class, "i"))
                        .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .reduceRows(
                            new LinkedHashMap<UUID, ItemImpl>(),
                            (map, rowView) -> {
                              ItemImpl item =
                                  map.computeIfAbsent(
                                      rowView.getColumn("i_uuid", UUID.class),
                                      fn -> {
                                        return rowView.getRow(ItemImpl.class);
                                      });

                              if (rowView.getColumn("f_uuid", UUID.class) != null) {
                                item.setPreviewImage(rowView.getRow(ImageFileResourceImpl.class));
                              }

                              if (rowView.getColumn("id_uuid", UUID.class) != null) {
                                IdentifierImpl identifier = rowView.getRow(IdentifierImpl.class);
                                item.addIdentifier(identifier);
                              }

                              return map;
                            }))
            .get(uuid);
    return result;
  }

  @Override
  public Item findOne(Identifier identifier) {
    if (identifier.getIdentifiable() != null) {
      return findOne(identifier.getIdentifiable());
    }

    String namespace = identifier.getNamespace();
    String identifierId = identifier.getId();

    String query = FIND_ONE_BASE_SQL + " WHERE id.identifier = :id AND id.namespace = :namespace";

    Optional<ItemImpl> result =
        dbi
            .withHandle(
                h ->
                    h.createQuery(query)
                        .bind("id", identifierId)
                        .bind("namespace", namespace)
                        .registerRowMapper(BeanMapper.factory(ItemImpl.class, "i"))
                        .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .reduceRows(
                            new LinkedHashMap<UUID, ItemImpl>(),
                            (map, rowView) -> {
                              ItemImpl item =
                                  map.computeIfAbsent(
                                      rowView.getColumn("i_uuid", UUID.class),
                                      fn -> {
                                        return rowView.getRow(ItemImpl.class);
                                      });

                              if (rowView.getColumn("f_uuid", UUID.class) != null) {
                                item.setPreviewImage(rowView.getRow(ImageFileResourceImpl.class));
                              }

                              if (rowView.getColumn("id_uuid", UUID.class) != null) {
                                IdentifierImpl dbIdentifier = rowView.getRow(IdentifierImpl.class);
                                item.addIdentifier(dbIdentifier);
                              }

                              return map;
                            }))
            .values()
            .stream()
            .findFirst();
    return result.orElse(null);
  }

  @Override
  public Item findOneByIdentifier(String namespace, String id) {
    return findOne(new IdentifierImpl(null, namespace, id));
  }

  @Override
  public Item save(Item item) {
    item.setUuid(UUID.randomUUID());
    item.setCreated(LocalDateTime.now());
    item.setLastModified(LocalDateTime.now());
    // refid is generated as serial, DO NOT SET!
    final UUID previewImageUuid =
        item.getPreviewImage() == null ? null : item.getPreviewImage().getUuid();

    String query =
        "INSERT INTO items("
            + "uuid, label, description, previewfileresource,"
            + " identifiable_type, entity_type,"
            + " created, last_modified,"
            + " language, publication_date, publication_place, publisher, version"
            + ") VALUES ("
            + ":uuid, :label::JSONB, :description::JSONB, :previewFileResource,"
            + " :type, :entityType,"
            + " :created, :lastModified,"
            + " :language, :publicationDate, :publicationPlace, :publisher, :version"
            + ")";

    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("previewFileResource", previewImageUuid)
                .bindBean(item)
                .execute());

    // save identifiers
    Set<Identifier> identifiers = item.getIdentifiers();
    saveIdentifiers(identifiers, item);

    Item result = findOne(item.getUuid());
    return result;
  }

  @Override
  public Item update(Item item) {
    item.setLastModified(LocalDateTime.now());
    // do not update/left out from statement (not changed since insert):
    // uuid, created, identifiable_type, entity_type, refid
    final UUID previewImageUuid =
        item.getPreviewImage() == null ? null : item.getPreviewImage().getUuid();

    String query =
        "UPDATE items SET"
            + " label=:label::JSONB, description=:description::JSONB, previewfileresource=:previewFileResource,"
            + " last_modified=:lastModified,"
            + " language=:language, publication_date=:publicationDate, publication_place=:publicationPlace, publisher=:publisher, version=:version"
            + " WHERE uuid=:uuid";

    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("previewFileResource", previewImageUuid)
                .bindBean(item)
                .execute());

    // save identifiers
    // as we store the whole list new: delete old entries
    deleteIdentifiers(item);
    Set<Identifier> identifiers = item.getIdentifiers();
    saveIdentifiers(identifiers, item);

    Item result = findOne(item.getUuid());
    return result;
  }

  @Override
  public Set<DigitalObject> getDigitalObjects(UUID itemUuid) {
    String query =
        "SELECT d.uuid d_uuid, d.label d_label, d.refid d_refId,"
            + " d.created d_created, d.last_modified d_lastModified,"
            + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
            + " file.uuid pf_uuid, file.filename pf_filename, file.mimetype pf_mimetype, file.size_in_bytes pf_size_in_bytes, file.uri pf_uri"
            + " FROM digitalobjects as d"
            + " LEFT JOIN identifiers as id on d.uuid = id.identifiable"
            + " LEFT JOIN fileresources_image as file on d.previewfileresource = file.uuid"
            + " LEFT JOIN item_digitalobjects as ido on d.uuid = ido.digitalobject_uuid"
            + " WHERE ido.item_uuid = :uuid"
            + " ORDER BY ido.sortIndex ASC";

    Set<DigitalObject> result =
        dbi.withHandle(
            h ->
                h
                    .createQuery(query)
                    .bind("uuid", itemUuid)
                    .registerRowMapper(BeanMapper.factory(DigitalObjectImpl.class, "d"))
                    .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "pf"))
                    .reduceRows(
                        new LinkedHashMap<UUID, DigitalObject>(),
                        (map, rowView) -> {
                          DigitalObject digitalObject =
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
                    .collect(Collectors.toSet()));
    return result;
  }

  @Override
  public Set<Work> getWorks(UUID itemUuid) {
    String query =
        "SELECT w.uuid w_uuid, w.label w_label, w.refid w_refId,"
            + " w.created w_created, w.last_modified w_lastModified,"
            + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
            + " file.uuid pf_uuid, file.filename pf_filename, file.mimetype pf_mimetype, file.size_in_bytes pf_size_in_bytes, file.uri pf_uri,"
            + " e.uuid e_uuid, e.label e_label, e.refid e_refId"
            + " FROM works as w"
            + " LEFT JOIN identifiers as id on w.uuid = id.identifiable"
            + " LEFT JOIN work_creators as wc on w.uuid = wc.work_uuid"
            + " LEFT JOIN entities as e on e.uuid = wc.agent_uuid"
            + " LEFT JOIN fileresources_image as file on w.previewfileresource = file.uuid"
            + " LEFT JOIN item_works as iw on w.uuid = iw.work_uuid"
            + " WHERE iw.item_uuid = :uuid"
            + " ORDER BY iw.sortIndex ASC";

    Set<Work> result =
        dbi.withHandle(
            h ->
                h
                    .createQuery(query)
                    .bind("uuid", itemUuid)
                    .registerRowMapper(BeanMapper.factory(WorkImpl.class, "w"))
                    .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "pf"))
                    .registerRowMapper(BeanMapper.factory(EntityImpl.class, "e"))
                    .reduceRows(
                        new LinkedHashMap<UUID, Work>(),
                        (map, rowView) -> {
                          Work work =
                              map.computeIfAbsent(
                                  rowView.getColumn("w_uuid", UUID.class),
                                  fn -> {
                                    return rowView.getRow(WorkImpl.class);
                                  });
                          if (rowView.getColumn("pf_uuid", UUID.class) != null) {
                            work.setPreviewImage(rowView.getRow(ImageFileResourceImpl.class));
                          }
                          if (rowView.getColumn("id_uuid", UUID.class) != null) {
                            IdentifierImpl dbIdentifier = rowView.getRow(IdentifierImpl.class);
                            work.addIdentifier(dbIdentifier);
                          }
                          if (rowView.getColumn("e_uuid", UUID.class) != null) {
                            EntityImpl agent = rowView.getRow(EntityImpl.class);
                            UUID agentUuid = agent.getUuid();
                            List<Agent> creators = work.getCreators();
                            boolean contained = false;
                            for (Agent creator : creators) {
                              if (agentUuid.equals(creator.getUuid())) {
                                contained = true;
                              }
                            }
                            if (!contained) {
                              // FIXME: not only persons! use entityType to disambiguate!
                              Person person = new PersonImpl();
                              person.setLabel(agent.getLabel());
                              person.setRefId(agent.getRefId());
                              person.setUuid(agent.getUuid());
                              work.getCreators().add(person);
                            }
                          }
                          return map;
                        })
                    .values()
                    .stream()
                    .collect(Collectors.toSet()));
    return result;
  }

  @Override
  public boolean addWork(UUID itemUuid, UUID workUuid) {
    Integer nextSortIndex =
        retrieveNextSortIndexForParentChildren(dbi, "item_works", "item_uuid", itemUuid);
    String query =
        "INSERT INTO item_works ("
            + "item_uuid, work_uuid, sortindex"
            + ") VALUES ("
            + ":itemUuid, :workUuid, :nextSortIndex"
            + ")";

    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("itemUuid", itemUuid)
                .bind("workUuid", workUuid)
                .bind("nextSortIndex", nextSortIndex)
                .execute());
    return true;
  }

  @Override
  public boolean addDigitalObject(UUID itemUuid, UUID digitalObjectUuid) {
    Integer nextSortIndex =
        retrieveNextSortIndexForParentChildren(dbi, "item_digitalobjects", "item_uuid", itemUuid);
    
    String query =
        "INSERT INTO item_digitalobjects ("
            + "item_uuid, digitalobject_uuid, sortindex"
            + ") VALUES ("
            + ":itemUuid, :digitalObjectUuid, :nextSortIndex"
            + ")";

    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("itemUuid", itemUuid)
                .bind("digitalObjectUuid", digitalObjectUuid)
                .bind("nextSortIndex", nextSortIndex)
                .execute());
    return true;
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
        return "i.created";
      case "lastModified":
        return "i.last_modified";
      case "refId":
        return "i.refid";
      default:
        return null;
    }
  }
}
