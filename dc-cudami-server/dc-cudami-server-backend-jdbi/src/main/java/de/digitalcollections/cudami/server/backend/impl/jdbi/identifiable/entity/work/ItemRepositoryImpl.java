package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.work;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work.ItemRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.DigitalObjectRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.EntityRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent.AgentRepositoryImpl;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.identifiable.entity.agent.Family;
import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.entity.item.Item;
import de.digitalcollections.model.identifiable.entity.manifestation.Manifestation;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.text.LocalizedText;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
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

  @Override
  public String getSqlInsertFields() {
    return super.getSqlInsertFields()
        + ", exemplifies_manifestation, manifestation, holder_uuids, part_of_item";
  }

  /* Do not change order! Must match order in getSqlInsertFields!!! */
  @Override
  public String getSqlInsertValues() {
    return super.getSqlInsertValues()
        + ", :exemplifiesManifestation, :manifestation?.uuid, :holder_uuids::UUID[], :partOfItem?.uuid";
  }

  @Override
  public String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    return getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + """
          , %1$s.exemplifies_manifestation %2$s_exemplifies_manifestation,
          poi.label poi_label
          """
            .formatted(tableAlias, mappingPrefix);
  }

  @Override
  protected String getSqlSelectAllFieldsJoins() {
    return super.getSqlSelectAllFieldsJoins()
        + """
        LEFT JOIN %1$s poi ON %2$s.part_of_item = poi.uuid
        """
            .formatted(tableName, tableAlias);
  }

  @Override
  public String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return super.getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".part_of_item "
        + mappingPrefix
        + "_part_of_item_uuid, "
        + tableAlias
        + ".manifestation "
        + mappingPrefix
        + "_manifestation_uuid, "
        + agentRepository.getSqlSelectReducedFields(
            "holdertable", AgentRepositoryImpl.MAPPING_PREFIX);
  }

  @Override
  protected String getSqlSelectReducedFieldsJoins() {
    return super.getSqlSelectReducedFieldsJoins()
        + """
        LEFT JOIN %1$s %2$s ON %2$s.uuid = ANY(%3$s.holder_uuids)
        """
            .formatted(AgentRepositoryImpl.TABLE_NAME, "holdertable", tableAlias);
  }

  @Override
  public String getSqlUpdateFieldValues() {
    return super.getSqlUpdateFieldValues()
        + ", exemplifies_manifestation=:exemplifiesManifestation, manifestation=:manifestation?.uuid, holder_uuids=:holder_uuids, part_of_item=:partOfItem?.uuid";
  }

  private final DigitalObjectRepositoryImpl digitalObjectRepositoryImpl;
  private final AgentRepositoryImpl agentRepository;

  @Autowired
  public ItemRepositoryImpl(
      Jdbi dbi,
      @Lazy DigitalObjectRepositoryImpl digitalObjectRepositoryImpl,
      @Lazy AgentRepositoryImpl agentRepository,
      CudamiConfig cudamiConfig) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        Item.class,
        ItemRepositoryImpl::additionalReduceRows,
        cudamiConfig.getOffsetForAlternativePaging());
    this.digitalObjectRepositoryImpl = digitalObjectRepositoryImpl;
    this.agentRepository = agentRepository;
  }

  private static void additionalReduceRows(Map<UUID, Item> map, RowView rowView) {
    // must not be null; otherwise something went wrong earlier
    Item item = map.get(rowView.getColumn(MAPPING_PREFIX + "_uuid", UUID.class));
    // the super item is created and filled with its UUID in extendReducedIdentifiable
    // if there is none then we will not do anything
    if (item.getPartOfItem() != null) {
      if (item.getPartOfItem().getLabel() != null) return;
      LocalizedText partOfItemLabel = rowView.getColumn("poi_label", LocalizedText.class);
      item.getPartOfItem().setLabel(partOfItemLabel);
    }
  }

  @Override
  protected void extendReducedIdentifiable(Item identifiable, RowView rowView) {
    super.extendReducedIdentifiable(identifiable, rowView);

    Agent holder = null;
    if (rowView.getColumn(AgentRepositoryImpl.MAPPING_PREFIX + "_uuid", UUID.class) != null) {
      holder = rowView.getRow(Agent.class);
      Agent exactholder =
          switch (holder.getIdentifiableObjectType()) {
            case CORPORATE_BODY -> DerivedAgentBuildHelper.build(holder, CorporateBody.class);
            case PERSON -> DerivedAgentBuildHelper.build(holder, Person.class);
            case FAMILY -> DerivedAgentBuildHelper.build(holder, Family.class);
            default -> null;
          };
      if (exactholder != null) {
        holder = exactholder;
      }
    }

    UUID partOfItemUuid = rowView.getColumn(MAPPING_PREFIX + "_part_of_item_uuid", UUID.class);
    UUID manifestationUuid = rowView.getColumn(MAPPING_PREFIX + "_manifestation_uuid", UUID.class);
    // holders
    if (identifiable.getHolders() == null) {
      identifiable.setHolders(new ArrayList<Agent>());
    }
    if (holder != null && !identifiable.getHolders().contains(holder)) {
      identifiable.getHolders().add(holder);
    }
    // partOfItem
    if (partOfItemUuid != null && identifiable.getPartOfItem() == null) {
      identifiable.setPartOfItem(Item.builder().uuid(partOfItemUuid).build());
    }
    // manifestation
    if (manifestationUuid != null && identifiable.getManifestation() == null) {
      identifiable.setManifestation(Manifestation.builder().uuid(manifestationUuid).build());
    }
  }

  @Override
  public PageResponse<DigitalObject> findDigitalObjects(UUID itemUuid, PageRequest pageRequest) {
    final String doTableAlias = digitalObjectRepositoryImpl.getTableAlias();
    final String doTableName = digitalObjectRepositoryImpl.getTableName();

    StringBuilder commonSql =
        new StringBuilder(
            " FROM "
                + doTableName
                + " AS "
                + doTableAlias
                + " WHERE "
                + doTableAlias
                + ".item_uuid = :uuid");
    Map<String, Object> argumentMappings = new HashMap<>();
    argumentMappings.put("uuid", itemUuid);

    String executedSearchTerm = addSearchTerm(pageRequest, commonSql, argumentMappings);
    Filtering filtering = pageRequest.getFiltering();
    // as filtering has other target object type (digitalobject) than this repository (item)
    // we have to rename filter field names to target table alias and column names:
    mapFilterExpressionsToOtherTableColumnNames(filtering, digitalObjectRepositoryImpl);
    addFiltering(pageRequest, commonSql, argumentMappings);

    StringBuilder innerQuery = new StringBuilder("SELECT * " + commonSql);
    addPageRequestParams(pageRequest, innerQuery);
    List<DigitalObject> result =
        digitalObjectRepositoryImpl.retrieveList(
            digitalObjectRepositoryImpl.getSqlSelectReducedFields(),
            innerQuery,
            argumentMappings,
            getOrderBy(pageRequest.getSorting()));

    StringBuilder countQuery = new StringBuilder("SELECT count(*)" + commonSql);
    long total = retrieveCount(countQuery, argumentMappings);

    return new PageResponse<>(result, pageRequest, total, executedSearchTerm);
  }

  @Override
  public String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "part_of_item.uuid":
        return tableAlias + ".part_of_item";
      default:
        return super.getColumnName(modelProperty);
    }
  }

  @Override
  public List<Locale> getLanguagesOfDigitalObjects(UUID uuid) {
    String doTableAlias = digitalObjectRepositoryImpl.getTableAlias();
    String doTableName = digitalObjectRepositoryImpl.getTableName();
    String sql =
        "SELECT DISTINCT jsonb_object_keys("
            + doTableAlias
            + ".label) as languages"
            + " FROM "
            + doTableName
            + " AS "
            + doTableAlias
            + String.format(" WHERE %s.item_uuid = :uuid;", doTableAlias);
    return this.dbi.withHandle(
        h -> h.createQuery(sql).bind("uuid", uuid).mapTo(Locale.class).list());
  }

  @Override
  public void save(Item item) throws RepositoryException {
    HashMap<String, Object> bindings = new HashMap<>();
    bindings.put("holder_uuids", extractUuids(item.getHolders()));
    super.save(item, bindings);
  }

  @Override
  public void update(Item item) throws RepositoryException {
    HashMap<String, Object> bindings = new HashMap<>();
    bindings.put("holder_uuids", extractUuids(item.getHolders()));
    super.update(item, bindings);
  }

  @Override
  public List<Item> getItemsForWork(UUID workUuid) {
    // TODO
    return null;
  }
}
