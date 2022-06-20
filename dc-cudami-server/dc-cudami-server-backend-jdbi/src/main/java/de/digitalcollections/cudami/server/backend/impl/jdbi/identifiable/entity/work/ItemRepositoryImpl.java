package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.work;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work.ItemRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.DigitalObjectRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.EntityRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent.AgentRepositoryImpl;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.identifiable.entity.work.Item;
import de.digitalcollections.model.identifiable.entity.work.Work;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.result.RowView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

@Repository
public class ItemRepositoryImpl extends EntityRepositoryImpl<Item> implements ItemRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(ItemRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "it";
  public static final String TABLE_ALIAS = "i";
  public static final String TABLE_NAME = "items";

  public static String getSqlInsertFields() {
    return EntityRepositoryImpl.getSqlInsertFields()
        + ", language, publication_date, publication_place, publisher, version, "
        + "exemplifies_manifestation, manifestation, holder_uuids, part_of_item";
  }

  /* Do not change order! Must match order in getSqlInsertFields!!! */
  public static String getSqlInsertValues() {
    return EntityRepositoryImpl.getSqlInsertValues()
        + ", :language, :publicationDate, :publicationPlace, :publisher, :version, "
        + ":exemplifiesManifestation, :manifestation, :holder_uuids::UUID[], :partOfItem";
  }

  public static String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    return getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".language "
        + mappingPrefix
        + "_language, "
        + tableAlias
        + ".publication_date "
        + mappingPrefix
        + "_publicationDate, "
        + tableAlias
        + ".publication_place "
        + mappingPrefix
        + "_publicationPlace, "
        + tableAlias
        + ".publisher "
        + mappingPrefix
        + "_publisher, "
        + tableAlias
        + ".version "
        + mappingPrefix
        + "_version, "
        + tableAlias
        + ".exemplifies_manifestation "
        + mappingPrefix
        + "_exemplifies_manifestation, "
        + tableAlias
        + ".manifestation "
        + mappingPrefix
        + "_manifestation, "
        + tableAlias
        + ".part_of_item "
        + mappingPrefix
        + "_part_of_item, "
        + AgentRepositoryImpl.getSqlSelectReducedFields(
            "holdertable", AgentRepositoryImpl.MAPPING_PREFIX);
  }

  public static final String SQL_SELECT_ALL_FIELDS_JOINS =
      String.format(
          " LEFT JOIN %1$s %2$s ON %2$s.uuid = ANY(%3$s.holder_uuids) ",
          AgentRepositoryImpl.TABLE_NAME, "holdertable", TABLE_ALIAS);

  public static String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return EntityRepositoryImpl.getSqlSelectReducedFields(tableAlias, mappingPrefix);
  }

  public static String getSqlUpdateFieldValues() {
    return EntityRepositoryImpl.getSqlUpdateFieldValues()
        + ", language=:language, publication_date=:publicationDate, publication_place=:publicationPlace, publisher=:publisher, version=:version, "
        + "exemplifies_manifestation=:exemplifiesManifestation, manifestation=:manifestation, holder_uuids=:holder_uuids, part_of_item=:partOfItem";
  }

  private final DigitalObjectRepositoryImpl digitalObjectRepositoryImpl;
  private final WorkRepositoryImpl workRepositoryImpl;

  private static final BiFunction<Map<UUID, Item>, RowView, Map<UUID, Item>>
      ADDITIONAL_REDUCE_ROWS_BIFUNCTION =
          (map, rowView) -> {
            UUID itemUuid = rowView.getColumn(MAPPING_PREFIX + "_uuid", UUID.class);
            Item item = map.get(itemUuid);
            Agent holder = rowView.getRow(Agent.class);
            if (item.getHolders() == null) {
              item.setHolders(new ArrayList<Agent>());
            }
            if (holder != null && !item.getHolders().contains(holder)) {
              item.getHolders().add(holder);
            }
            return map;
          };

  @Autowired
  public ItemRepositoryImpl(
      Jdbi dbi,
      @Lazy DigitalObjectRepositoryImpl digitalObjectRepositoryImpl,
      @Lazy WorkRepositoryImpl workRepositoryImpl,
      CudamiConfig cudamiConfig) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        Item.class,
        getSqlSelectAllFields(TABLE_ALIAS, MAPPING_PREFIX),
        getSqlSelectReducedFields(TABLE_ALIAS, MAPPING_PREFIX),
        getSqlInsertFields(),
        getSqlInsertValues(),
        getSqlUpdateFieldValues(),
        SQL_SELECT_ALL_FIELDS_JOINS,
        ADDITIONAL_REDUCE_ROWS_BIFUNCTION,
        cudamiConfig.getOffsetForAlternativePaging());
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

  private UUID[] extractHolderUuids(Item item) {
    if (item == null || item.getHolders() == null || item.getHolders().isEmpty()) {
      return null;
    }
    return item.getHolders().stream()
        .collect(
            ArrayList<UUID>::new,
            (result, holder) -> result.add(holder.getUuid()),
            ArrayList::addAll)
        .toArray(new UUID[1]);
  }

  @Override
  public Set<DigitalObject> getDigitalObjects(UUID itemUuid) {
    final String doTableAlias = digitalObjectRepositoryImpl.getTableAlias();
    final String doTableName = digitalObjectRepositoryImpl.getTableName();

    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT ido.sortindex AS idx, * FROM "
                + doTableName
                + " AS "
                + doTableAlias
                + " LEFT JOIN item_digitalobjects AS ido ON "
                + doTableAlias
                + ".uuid = ido.digitalobject_uuid"
                + " WHERE ido.item_uuid = :uuid"
                + " ORDER BY ido.sortindex ASC");
    Map<String, Object> argumentMappings = new HashMap<>();
    argumentMappings.put("uuid", itemUuid);
    List<DigitalObject> result =
        digitalObjectRepositoryImpl.retrieveList(
            digitalObjectRepositoryImpl.getSqlSelectReducedFields(),
            innerQuery,
            argumentMappings,
            "ORDER BY idx ASC");
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
            "SELECT iw.sortindex AS idx, * FROM "
                + wTableName
                + " AS "
                + wTableAlias
                + " LEFT JOIN item_works AS iw ON "
                + wTableAlias
                + ".uuid = iw.work_uuid"
                + " WHERE iw.item_uuid = :uuid"
                + " ORDER BY iw.sortindex ASC");
    Map<String, Object> argumentMappings = new HashMap<>();
    argumentMappings.put("uuid", itemUuid);
    List<Work> result =
        workRepositoryImpl.retrieveList(
            workRepositoryImpl.getSqlSelectReducedFields(),
            innerQuery,
            argumentMappings,
            "ORDER BY idx ASC");
    return result.stream()
        .map(
            w -> {
              List<Agent> creators = workRepositoryImpl.getCreators(w.getUuid());
              w.setCreators(creators);
              return w;
            })
        .collect(Collectors.toSet());
  }

  @Override
  public Item save(Item item) {
    HashMap<String, Object> bindings = new HashMap<>();
    bindings.put("holder_uuids", extractHolderUuids(item));
    super.save(item, bindings);
    Item result = getByUuid(item.getUuid());
    return result;
  }

  @Override
  public Item update(Item item) {
    HashMap<String, Object> bindings = new HashMap<>();
    bindings.put("holder_uuids", extractHolderUuids(item));
    super.update(item, bindings);
    Item result = getByUuid(item.getUuid());
    return result;
  }
}
