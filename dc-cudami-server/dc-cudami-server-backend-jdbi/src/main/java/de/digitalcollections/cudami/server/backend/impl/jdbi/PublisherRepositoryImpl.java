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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.jdbi.v3.core.result.RowView;
import org.springframework.stereotype.Repository;

@Repository
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
    String orderBy = getOrderBy(pageRequest.getSorting());

    StringBuilder sql =
        new StringBuilder(
            "SELECT "
                + getSqlFullFields()
                + ", l.id as l_id"
                + " FROM "
                + tableName
                + " "
                + tableAlias
                + " LEFT JOIN UNNEST(publ.location_uuids) l(id) ON true " // Required to preserve
                // order of
                // array items
                + " LEFT JOIN "
                + corporateBodyRepository.getTableName()
                + " "
                + corporateBodyRepository.getTableAlias()
                + " ON "
                + corporateBodyRepository.getTableAlias()
                + ".uuid="
                + tableAlias
                + ".agent_uuid"
                + " LEFT JOIN "
                + personRepository.getTableName()
                + " "
                + personRepository.getTableAlias()
                + " ON "
                + personRepository.getTableAlias()
                + ".uuid="
                + tableAlias
                + ".agent_uuid"
                + " LEFT JOIN "
                + humanSettlementRepository.getTableName()
                + " "
                + humanSettlementRepository.getTableAlias()
                + " ON "
                + humanSettlementRepository.getTableAlias()
                + ".uuid=l.id");

    Map argumentMappings = new HashMap<>();
    String executedSearchTerm = addSearchTerm(pageRequest, sql, argumentMappings);
    addFiltering(pageRequest, sql, argumentMappings);

    // Append default ordering to ensure deterministic paging results
    if (orderBy == null) {
      sql.append(" ORDER BY " + tableAlias + "_publisherPresentation");
    }

    addPageRequestParams(pageRequest, sql);
    final String query = sql.toString();

    List<Publisher> result =
        dbi.withHandle(
                (Handle handle) ->
                    handle
                        .createQuery(query)
                        .bindMap(argumentMappings)
                        .reduceRows(
                            (Map<UUID, Publisher> results, RowView rowView) -> {
                              fullFieldsReduceRowsBiFunction.apply(results, rowView);
                            }))
            .collect(Collectors.toList());

    StringBuilder countQuery =
        new StringBuilder("SELECT count(*) FROM " + tableName + " " + tableAlias);
    addSearchTerm(pageRequest, countQuery, argumentMappings);
    addFiltering(pageRequest, countQuery, argumentMappings);

    long total = retrieveCount(countQuery, argumentMappings);

    return new PageResponse<>(result, pageRequest, total, executedSearchTerm);
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
            + corporateBodyRepository.getTableName()
            + " "
            + corporateBodyRepository.getTableAlias()
            + " ON "
            + corporateBodyRepository.getTableAlias()
            + ".uuid="
            + tableAlias
            + ".agent_uuid"
            + " LEFT JOIN "
            + personRepository.getTableName()
            + " "
            + personRepository.getTableAlias()
            + " ON "
            + personRepository.getTableAlias()
            + ".uuid="
            + tableAlias
            + ".agent_uuid"
            + " LEFT JOIN "
            + humanSettlementRepository.getTableName()
            + " "
            + humanSettlementRepository.getTableAlias()
            + " ON "
            + humanSettlementRepository.getTableAlias()
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
                          .locations(new ArrayList<>())
                          .build();
                    });

        UUID corporateBodyUuid =
            rowView.getColumn(corporateBodyRepository.getMappingPrefix() + "_uuid", UUID.class);
        if (corporateBodyUuid != null) {
          publisher.setAgent(rowView.getRow(CorporateBody.class));
        }

        UUID personUuid =
            rowView.getColumn(personRepository.getMappingPrefix() + "_uuid", UUID.class);
        if (personUuid != null) {
          publisher.setAgent(rowView.getRow(Person.class));
        }

        UUID locationUuid =
            rowView.getColumn(humanSettlementRepository.getMappingPrefix() + "_uuid", UUID.class);
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
    return new ArrayList<>(Arrays.asList("created", "publisherPresentation", "lastModified"));
  }

  @Override
  public String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "created":
        return tableAlias + ".created";
      case "lastModified":
        return tableAlias + ".last_modified";
      case "uuid":
        return tableAlias + ".uuid";
      case "agent_uuid":
        return tableAlias + ".agent_uuid::UUID";
      case "location_uuids":
        return tableAlias + ".location_uuids::UUID[]";
      case "publisherPresentation":
        return tableAlias + ".publisherPresentation";
      default:
        return null;
    }
  }

  @Override
  protected String getUniqueField() {
    return "uuid";
  }

  @Override
  protected boolean supportsCaseSensitivityForProperty(String modelProperty) {
    switch (modelProperty) {
      case "publisherPresentation":
        return true;
      default:
        return false;
    }
  }
}
