package de.digitalcollections.cudami.server.backend.impl.jdbi.semantic;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.semantic.HeadwordRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.JdbiRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.SearchTermTemplates;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import de.digitalcollections.model.semantic.Headword;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class HeadwordRepositoryImpl extends JdbiRepositoryImpl implements HeadwordRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(HeadwordRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "hw";
  public static final String SQL_INSERT_FIELDS = " uuid, created, label, locale, last_modified";
  public static final String SQL_INSERT_VALUES = " :uuid, :created, :label, :locale, :lastModified";
  public static final String SQL_REDUCED_FIELDS_HW =
      " hw.uuid, hw.label, hw.locale, hw.created, hw.last_modified";
  public static final String SQL_FULL_FIELDS_HW = SQL_REDUCED_FIELDS_HW;
  public static final String TABLE_ALIAS = "hw";
  public static final String TABLE_NAME = "headwords";

  @Autowired
  public HeadwordRepositoryImpl(Jdbi dbi, CudamiConfig cudamiConfig) {
    super(
        dbi, TABLE_NAME, TABLE_ALIAS, MAPPING_PREFIX, cudamiConfig.getOffsetForAlternativePaging());
  }

  @Override
  public void addRelatedEntity(UUID headwordUuid, UUID entityUuid) {
    Integer sortIndex =
        retrieveNextSortIndexForParentChildren(
            dbi, "headword_entities", "headword_uuid", headwordUuid);

    dbi.withHandle(
        h ->
            h.createUpdate(
                    "INSERT INTO headword_entities(headword_uuid, entity_uuid, sortindex) VALUES (:headwordUuid, :entityUuid, :sortindex)")
                .bind("headwordUuid", headwordUuid)
                .bind("entityUuid", entityUuid)
                .bind("sortindex", sortIndex)
                .execute());
  }

  @Override
  public void addRelatedFileresource(UUID headwordUuid, UUID fileResourceUuid) {
    Integer sortIndex =
        retrieveNextSortIndexForParentChildren(
            dbi, "headword_fileresources", "headword_uuid", headwordUuid);

    dbi.withHandle(
        h ->
            h.createUpdate(
                    "INSERT INTO headword_fileresources(headword_uuid, fileresource_uuid, sortindex) VALUES (:headwordUuid, :fileresourceUuid, :sortindex)")
                .bind("headwordUuid", headwordUuid)
                .bind("fileresourceUuid", fileResourceUuid)
                .bind("sortindex", sortIndex)
                .execute());
  }

  @Override
  public void delete(String label, Locale locale) {
    dbi.withHandle(
        h ->
            h.createUpdate(
                    "DELETE FROM " + tableName + " WHERE label = :label AND locale = :locale")
                .bind("label", label)
                .bind("locale", locale)
                .execute());
  }

  @Override
  public void delete(UUID uuid) {
    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM " + tableName + " WHERE uuid = :uuid")
                .bind("uuid", uuid)
                .execute());
  }

  @Override
  public boolean delete(List<UUID> uuids) {
    // delete related data
    uuids.stream()
        .forEach(
            (u) -> {
              deleteRelatedEntities(u);
              deleteRelatedFileresources(u);
            });

    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM " + tableName + " WHERE uuid in (<uuids>)")
                .bindList("uuids", uuids)
                .execute());
    return true;
  }

  @Override
  public void deleteRelatedEntities(UUID headwordUuid) {
    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM headword_entities WHERE headword_uuid = :uuid")
                .bind("uuid", headwordUuid)
                .execute());
  }

  @Override
  public void deleteRelatedFileresources(UUID headwordUuid) {
    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM headword_fileresources WHERE headword_uuid = :uuid")
                .bind("uuid", headwordUuid)
                .execute());
  }

  @Override
  public PageResponse<Headword> find(PageRequest pageRequest) {
    StringBuilder commonSql = new StringBuilder(" FROM " + tableName + " AS " + tableAlias);
    Map<String, Object> argumentMappings = new HashMap<>(0);
    String executedSearchTerm = addSearchTerm(pageRequest, commonSql, argumentMappings);
    addFiltering(pageRequest, commonSql, argumentMappings);

    StringBuilder innerQuery = new StringBuilder("SELECT " + tableAlias + ".* " + commonSql);
    addPageRequestParams(pageRequest, innerQuery);
    List<Headword> result =
        retrieveList(
            SQL_REDUCED_FIELDS_HW,
            innerQuery,
            argumentMappings,
            getOrderBy(pageRequest.getSorting()));

    StringBuilder countQuery =
        new StringBuilder("SELECT count(" + tableAlias + ".uuid)" + commonSql);
    long total = retrieveCount(countQuery, argumentMappings);

    return new PageResponse<>(result, pageRequest, total, executedSearchTerm);
  }

  @Override
  public List<Headword> find(String label, Locale locale) {
    Filtering filtering =
        Filtering.builder()
            .add(FilterCriterion.builder().withExpression("label").isEquals(label).build())
            .add(FilterCriterion.builder().withExpression("locale").isEquals(locale).build())
            .build();

    // TODO make PageRequest able to return all items (one page)
    PageRequest request =
        new PageRequest(
            0,
            10000,
            Sorting.builder().order(new Order(Direction.ASC, "label")).build(),
            filtering);
    PageResponse<Headword> response = find(request);
    return response.getContent();
  }

  @Override
  public List<Headword> findByLabel(String label) {
    Filtering filtering =
        Filtering.builder()
            .add(FilterCriterion.builder().withExpression("label").isEquals(label).build())
            .build();
    // FIXME make PageRequest able to return all items (one page)
    PageRequest request =
        new PageRequest(
            0,
            10000,
            Sorting.builder().order(new Order(Direction.ASC, "label")).build(),
            filtering);
    PageResponse<Headword> response = find(request);
    return response.getContent();
  }

  @Override
  public Headword findByLabelAndLocale(String label, Locale locale) {
    // basic query
    StringBuilder sqlQuery =
        new StringBuilder(
            "SELECT " + SQL_FULL_FIELDS_HW + " FROM " + tableName + " AS " + tableAlias);

    // add filtering
    Filtering filtering =
        Filtering.builder()
            .add(FilterCriterion.builder().withExpression("label").isEquals(label).build())
            .add(FilterCriterion.builder().withExpression("locale").isEquals(locale).build())
            .build();
    Map<String, Object> argumentMappings = new HashMap<>();
    addFiltering(filtering, sqlQuery, argumentMappings);

    // get it
    Map<String, Object> bindMap = Map.copyOf(argumentMappings);
    Optional<Headword> result =
        dbi.withHandle(
            h ->
                h.createQuery(sqlQuery.toString())
                    .bindMap(bindMap)
                    .mapToBean(Headword.class)
                    .findFirst());
    return result.orElse(null);
  }

  @Override
  public PageResponse<Headword> findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial) {
    throw new UnsupportedOperationException(
        "Not supported yet."); // To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public Headword findByUuidAndFiltering(UUID uuid, Filtering filtering) {
    // basic query
    StringBuilder sqlQuery =
        new StringBuilder(
            "SELECT " + SQL_FULL_FIELDS_HW + " FROM " + tableName + " AS " + tableAlias);

    // add filtering
    if (filtering == null) {
      filtering = Filtering.builder().build();
    }
    filtering.add(FilterCriterion.builder().withExpression("uuid").isEquals(uuid).build());
    Map<String, Object> argumentMappings = new HashMap<>();
    addFiltering(filtering, sqlQuery, argumentMappings);

    // get it
    Map<String, Object> bindMap = Map.copyOf(argumentMappings);
    Optional<Headword> result =
        dbi.withHandle(
            h ->
                h.createQuery(sqlQuery.toString())
                    .bindMap(bindMap)
                    .mapToBean(Headword.class)
                    .findFirst());
    return result.orElse(null);
  }

  @Override
  public PageResponse<Entity> findRelatedEntities(UUID headwordUuid, PageRequest pageRequest) {
    throw new UnsupportedOperationException(
        "Not supported yet."); // To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public PageResponse<FileResource> findRelatedFileResources(
      UUID headwordUuid, PageRequest pageRequest) {
    throw new UnsupportedOperationException(
        "Not supported yet."); // To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public List<Headword> getAll() {
    return retrieveList(SQL_REDUCED_FIELDS_HW, null, null);
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    return new ArrayList<>(Arrays.asList("created", "label", "lastModified"));
  }

  @Override
  public String getColumnName(String modelProperty) {
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
      case "locale":
        return tableAlias + ".locale";
      case "uuid":
        return tableAlias + ".uuid";
      default:
        return null;
    }
  }

  @Override
  public List<Locale> getLanguages() {
    String query = "SELECT DISTINCT locale FROM " + tableName;
    List<Locale> result = dbi.withHandle(h -> h.createQuery(query).mapTo(Locale.class).list());
    return result;
  }

  @Override
  public List<Headword> getRandom(int count) {
    throw new UnsupportedOperationException(
        "Not supported yet."); // To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public List<Entity> getRelatedEntities(UUID headwordUuid) {
    throw new UnsupportedOperationException(
        "Not supported yet."); // To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public List<FileResource> getRelatedFileResources(UUID headwordUuid) {
    throw new UnsupportedOperationException(
        "Not supported yet."); // To change body of generated methods, choose Tools | Templates.
  }

  @Override
  protected List<String> getSearchTermTemplates(String tableAlias) {
    return List.of(SearchTermTemplates.ILIKE_STARTS_WITH.renderTemplate(tableAlias, "label"));
  }

  @Override
  protected String getUniqueField() {
    return "uuid";
  }

  public long retrieveCount(StringBuilder sqlCount, final Map<String, Object> argumentMappings) {
    long total =
        dbi.withHandle(
            h ->
                h.createQuery(sqlCount.toString())
                    .bindMap(argumentMappings)
                    .mapTo(Long.class)
                    .findOne()
                    .get());
    return total;
  }

  public List<Headword> retrieveList(String fieldsSql, StringBuilder innerQuery, String orderBy) {
    final String sql =
        "SELECT "
            + fieldsSql
            + " FROM "
            + (innerQuery != null ? "(" + innerQuery + ")" : tableName)
            + " AS "
            + tableAlias
            + (orderBy != null ? " " + orderBy : "");

    List<Headword> result =
        dbi.withHandle(
            (Handle handle) -> {
              return handle.createQuery(sql).mapToBean(Headword.class).collect(Collectors.toList());
            });
    return result;
  }

  private List<Headword> retrieveList(
      String fieldsSql,
      StringBuilder innerQuery,
      Map<String, Object> argumentMappings,
      String orderBy) {
    final String sql =
        "SELECT "
            + fieldsSql
            + " FROM "
            + (innerQuery != null ? "(" + innerQuery + ")" : tableName)
            + " AS "
            + tableAlias
            + (orderBy != null ? " " + orderBy : "");

    List<Headword> result =
        dbi.withHandle(
            (Handle handle) -> {
              return handle
                  .createQuery(sql)
                  .bindMap(argumentMappings)
                  .mapToBean(Headword.class)
                  .collect(Collectors.toList());
            });
    return result;
  }

  @Override
  public Headword save(Headword headword) {
    if (headword.getUuid() == null) {
      headword.setUuid(UUID.randomUUID());
    }
    headword.setCreated(LocalDateTime.now());
    headword.setLastModified(LocalDateTime.now());

    final String sql =
        "INSERT INTO "
            + tableName
            + "("
            + SQL_INSERT_FIELDS
            + ") VALUES ("
            + SQL_INSERT_VALUES
            + ")";

    dbi.withHandle(h -> h.createUpdate(sql).bindBean(headword).execute());

    return headword;
  }

  @Override
  public List<Entity> setRelatedEntities(UUID headwordUuid, List<Entity> entities) {
    throw new UnsupportedOperationException(
        "Not supported yet."); // To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public List<FileResource> setRelatedFileResources(
      UUID headwordUuid, List<FileResource> fileResources) {
    throw new UnsupportedOperationException(
        "Not supported yet."); // To change body of generated methods, choose Tools | Templates.
  }

  @Override
  protected boolean supportsCaseSensitivityForProperty(String modelProperty) {
    switch (modelProperty) {
      case "label":
      case "locale":
        return true;
      default:
        return false;
    }
  }

  @Override
  public Headword update(Headword headword) {
    throw new UnsupportedOperationException(
        "Not supported yet."); // To change body of generated methods, choose Tools | Templates.
  }
}
