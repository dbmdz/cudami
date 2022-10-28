package de.digitalcollections.cudami.server.backend.impl.jdbi;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.PublisherRepository;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent.CorporateBodyRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent.PersonRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.geo.location.HumanSettlementRepositoryImpl;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.identifiable.entity.geo.location.HumanSettlement;
import de.digitalcollections.model.identifiable.entity.work.Publisher;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.jdbi.v3.core.result.RowView;

public class PublisherRepositoryImpl extends JdbiRepositoryImpl implements PublisherRepository {

  private CorporateBodyRepositoryImpl corporateBodyRepository;
  private PersonRepositoryImpl personRepository;
  private HumanSettlementRepositoryImpl humanSettlementRepository;

  public static final String TABLE_NAME = "publishers";
  public static final String TABLE_ALIAS = "publ";
  public static final String MAPPING_PREFIX = "publ";

  public static final String SQL_INSERT_FIELDS =
      " uuid, agent_uuid, location_uuids, publisherPresentation, created, last_modified";

  public static final String SQL_INSERT_VALUES =
      " :uuid, :agent?.uuid, :location_uuids::UUID[], :publisherPresentation, :created, :lastModified";
  public static final String SQL_REDUCED_FIELDS_PUBLISHERS =
      String.format(
          " %1$s.uuid as %2$s_uuid, %1$s.agent_uuid as %2$s_agent_uuid, %1$s.location_uuids as %2$s_location_uuids"
              + ", %1$s.publisherPresentation as %2$s_publisherPresentation"
              + ", %1$s.created as %2$s_created, %1$s.last_modified as %2$s_last_modified",
          TABLE_ALIAS, MAPPING_PREFIX);

  public PublisherRepositoryImpl(
      Jdbi dbi,
      CudamiConfig cudamiConfig,
      CorporateBodyRepositoryImpl corporateBodyRepository,
      PersonRepositoryImpl personRepository,
      HumanSettlementRepositoryImpl humanSettlementRepository) {
    super(
        dbi, TABLE_NAME, TABLE_ALIAS, MAPPING_PREFIX, cudamiConfig.getOffsetForAlternativePaging());
    this.corporateBodyRepository = corporateBodyRepository;
    this.personRepository = personRepository;
    this.humanSettlementRepository = humanSettlementRepository;
    this.dbi.registerRowMapper(BeanMapper.factory(Publisher.class, MAPPING_PREFIX));
  }

  @Override
  public PageResponse<Publisher> find(PageRequest pageRequest) throws RepositoryException {
    return null;
  }

  @Override
  public Publisher getByUuid(UUID uuid) throws RepositoryException {
    final String sql =
        "SELECT "
            + getSqlFullFields()
            + " FROM "
            + tableName
            + " "
            + tableAlias
            + " LEFT JOIN "
            + corporateBodyRepository.tableName
            + " "
            + corporateBodyRepository.tableAlias
            + " ON "
            + corporateBodyRepository.tableAlias
            + ".uuid="
            + tableAlias
            + ".agent_uuid"
            + " LEFT JOIN "
            + personRepository.tableName
            + " "
            + personRepository.tableAlias
            + " ON "
            + personRepository.tableAlias
            + ".uuid="
            + tableAlias
            + ".agent_uuid"
            + " LEFT JOIN "
            + humanSettlementRepository.tableName
            + " "
            + humanSettlementRepository.tableAlias
            + " ON "
            + humanSettlementRepository.tableAlias
            + ".uuid="
            + " ANY("
            + tableAlias
            + ".location_uuids"
            + ")"
            + " WHERE "
            + tableAlias
            + ".uuid = :publ_uuid";

    return dbi.withHandle(
            h ->
                h.createQuery(sql)
                    .bind("publ_uuid", uuid)
                    .reduceRows(
                        (Map<UUID, Publisher> results, RowView rowView) -> {
                          fullFieldsReduceRowsBiFunction.apply(results, rowView);
                        }))
        .findFirst()
        .orElse(null);
  }

  BiFunction<Map<UUID, Publisher>, RowView, Map<UUID, Publisher>> fullFieldsReduceRowsBiFunction =
      (results, rowView) -> {
        Publisher publisher =
            (Publisher)
                results.computeIfAbsent(
                    rowView.getColumn(mappingPrefix + "_uuid", UUID.class),
                    fn -> {
                      return Publisher.builder()
                          .uuid(rowView.getColumn(mappingPrefix + "_uuid", UUID.class))
                          .build();
                    });

        UUID corporateBodyUuid =
            rowView.getColumn(corporateBodyRepository.mappingPrefix + "_uuid", UUID.class);
        if (corporateBodyUuid != null) {
          publisher.setAgent(rowView.getRow(CorporateBody.class));
        }

        UUID personUuid = rowView.getColumn(personRepository.mappingPrefix + "_uuid", UUID.class);
        if (personUuid != null) {
          publisher.setAgent(rowView.getRow(Person.class));
        }

        UUID locationUuid =
            rowView.getColumn(humanSettlementRepository.mappingPrefix + "_uuid", UUID.class);
        if (locationUuid != null) {
          publisher.addLocation(rowView.getRow(HumanSettlement.class));
        }

        publisher.setPublisherPresentation(
            rowView.getColumn(mappingPrefix + "_publisherPresentation", String.class));
        publisher.setCreated(rowView.getColumn(mappingPrefix + "_created", LocalDateTime.class));
        publisher.setLastModified(
            rowView.getColumn(mappingPrefix + "_last_modified", LocalDateTime.class));
        return results;
      };

  public String getSqlFullFields() {
    return SQL_REDUCED_FIELDS_PUBLISHERS
        + ", "
        + corporateBodyRepository.getSqlSelectReducedFields(
            CorporateBodyRepositoryImpl.TABLE_ALIAS, CorporateBodyRepositoryImpl.MAPPING_PREFIX)
        + ", "
        + personRepository.getSqlSelectReducedFields(
            PersonRepositoryImpl.TABLE_ALIAS, PersonRepositoryImpl.MAPPING_PREFIX)
        + ", "
        + humanSettlementRepository.getSqlSelectReducedFields(
            HumanSettlementRepositoryImpl.TABLE_ALIAS,
            HumanSettlementRepositoryImpl.MAPPING_PREFIX);
  }

  @Override
  public Publisher save(Publisher publisher) throws RepositoryException {
    publisher.setUuid(UUID.randomUUID());
    publisher.setCreated(LocalDateTime.now());
    publisher.setLastModified(LocalDateTime.now());

    HashMap<String, Object> bindings = new HashMap<>();
    bindings.put("location_uuids", extractUuids(publisher.getLocations()));

    final String sql =
        "INSERT INTO "
            + tableName
            + "("
            + SQL_INSERT_FIELDS
            + ")"
            + " VALUES ("
            + SQL_INSERT_VALUES
            + ") RETURNING *";

    Publisher savedPublisher =
        dbi.withHandle(
            h ->
                h.createQuery(sql)
                    .bindMap(bindings)
                    .bindBean(publisher)
                    .mapToBean(Publisher.class)
                    .findOne()
                    .orElse(null));

    if (savedPublisher == null) {
      throw new RepositoryException(
          "Could not successfully save publisher=" + publisher + "; got null as saved publisher");
    }

    publisher.setUuid(savedPublisher.getUuid());
    return publisher;
  }

  @Override
  public Publisher update(Publisher publisher) throws RepositoryException {
    publisher.setLastModified(LocalDateTime.now());

    HashMap<String, Object> bindings = new HashMap<>();
    bindings.put("location_uuids", extractUuids(publisher.getLocations()));

    final String sql =
        "UPDATE "
            + tableName
            + " SET "
            + " agent_uuid=:agent?.uuid, location_uuids=:location_uuids::UUID[]"
            + ", publisherPresentation=:publisherPresentation, created=:created, last_modified=:lastModified"
            + " WHERE uuid=:uuid RETURNING *";

    Publisher updated =
        dbi.withHandle(
            h ->
                h.createQuery(sql)
                    .bindMap(bindings)
                    .bindBean(publisher)
                    .mapToBean(Publisher.class)
                    .findOne()
                    .orElse(null));

    return getByUuid(updated.getUuid());
  }

  @Override
  public int deleteByUuid(UUID uuid) throws RepositoryException {
    return dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM " + tableName + " WHERE uuid=:uuid")
                .bind("uuid", uuid)
                .execute());
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    return null;
  }

  @Override
  public String getColumnName(String modelProperty) {
    return null;
  }

  @Override
  protected String getUniqueField() {
    return null;
  }

  @Override
  protected boolean supportsCaseSensitivityForProperty(String modelProperty) {
    return false;
  }
}
