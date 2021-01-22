package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.work;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work.ItemRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.DigitalObjectRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.EntityRepositoryImpl;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.entity.work.Item;
import de.digitalcollections.model.api.identifiable.entity.work.Work;
import de.digitalcollections.model.impl.identifiable.entity.work.ItemImpl;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ItemRepositoryImpl extends EntityRepositoryImpl<Item> implements ItemRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(ItemRepositoryImpl.class);
  public static final String MAPPING_PREFIX = "it";

  public static final String SQL_REDUCED_FIELDS_IT =
      " i.uuid it_uuid, i.refid it_refId, i.label it_label, i.description it_description,"
          + " i.identifiable_type it_type, i.entity_type it_entityType,"
          + " i.created it_created, i.last_modified it_lastModified";

  public static final String SQL_FULL_FIELDS_IT =
      SQL_REDUCED_FIELDS_IT
          + ", i.language it_language, i.publication_date it_publicationDate, i.publication_place it_publicationPlace, i.publisher it_publisher, i.version it_version";

  public static final String TABLE_ALIAS = "i";
  public static final String TABLE_NAME = "items";

  private final DigitalObjectRepositoryImpl digitalObjectRepositoryImpl;
  private final WorkRepositoryImpl workRepositoryImpl;

  @Autowired
  public ItemRepositoryImpl(
      Jdbi dbi,
      IdentifierRepository identifierRepository,
      DigitalObjectRepositoryImpl digitalObjectRepositoryImpl,
      WorkRepositoryImpl workRepositoryImpl) {
    super(
        dbi,
        identifierRepository,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        ItemImpl.class,
        SQL_REDUCED_FIELDS_IT,
        SQL_FULL_FIELDS_IT);
    this.digitalObjectRepositoryImpl = digitalObjectRepositoryImpl;
    this.workRepositoryImpl = workRepositoryImpl;
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
        return tableAlias + ".created";
      case "lastModified":
        return tableAlias + ".last_modified";
      case "refId":
        return tableAlias + ".refid";
      default:
        return null;
    }
  }

  @Override
  public Set<DigitalObject> getDigitalObjects(UUID itemUuid) {
    final String doTableAlias = digitalObjectRepositoryImpl.getTableAlias();
    final String doTableName = digitalObjectRepositoryImpl.getTableName();

    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT * FROM "
                + doTableName
                + " AS "
                + doTableAlias
                + " LEFT JOIN item_digitalobjects AS ido ON "
                + doTableAlias
                + ".uuid = ido.digitalobject_uuid"
                + " WHERE ido.item_uuid = :uuid"
                + " ORDER BY ido.sortIndex ASC");

    List<DigitalObject> result =
        digitalObjectRepositoryImpl.retrieveList(
            DigitalObjectRepositoryImpl.SQL_REDUCED_FIELDS_DO,
            innerQuery,
            Map.of("uuid", itemUuid));
    return result.stream().collect(Collectors.toSet());
  }

  @Override
  public Set<Work> getWorks(UUID itemUuid) {
    final String wTableAlias = workRepositoryImpl.getTableAlias();
    final String wTableName = workRepositoryImpl.getTableName();

    // Note: if getting list of all participating persons to work is wanted,
    // this code fragment may help as entry point:
    /*
    " e.uuid e_uuid, e.label e_label, e.refid e_refId"
    + LEFT JOIN work_creators as wc on w.uuid = wc.work_uuid
    " LEFT JOIN entities as e on e.uuid = wc.agent_uuid"
    .registerRowMapper(BeanMapper.factory(EntityImpl.class, "e"))
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
     */
    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT * FROM "
                + wTableName
                + " AS "
                + wTableAlias
                + " LEFT JOIN item_works AS iw ON "
                + wTableAlias
                + ".uuid = iw.work_uuid"
                + " WHERE iw.item_uuid = :uuid"
                + " ORDER BY iw.sortIndex ASC");

    List<Work> result =
        workRepositoryImpl.retrieveList(
            WorkRepositoryImpl.SQL_REDUCED_FIELDS_WO, innerQuery, Map.of("uuid", itemUuid));
    return result.stream().collect(Collectors.toSet());
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
        "INSERT INTO "
            + tableName
            + "("
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
        "UPDATE "
            + tableName
            + " SET"
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
    identifierRepository.deleteByIdentifiable(item);
    Set<Identifier> identifiers = item.getIdentifiers();
    saveIdentifiers(identifiers, item);

    Item result = findOne(item.getUuid());
    return result;
  }
}
