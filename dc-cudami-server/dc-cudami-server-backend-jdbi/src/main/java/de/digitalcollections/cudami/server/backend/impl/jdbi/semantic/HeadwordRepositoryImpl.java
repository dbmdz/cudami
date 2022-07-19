package de.digitalcollections.cudami.server.backend.impl.jdbi.semantic;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.semantic.HeadwordRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.JdbiRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.SearchTermTemplates;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.list.buckets.Bucket;
import de.digitalcollections.model.list.buckets.BucketObjectsRequest;
import de.digitalcollections.model.list.buckets.BucketObjectsResponse;
import de.digitalcollections.model.list.buckets.BucketsRequest;
import de.digitalcollections.model.list.buckets.BucketsResponse;
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
  public BucketsResponse<Headword> find(BucketsRequest<Headword> bucketsRequest) {
    Map<String, Object> argumentMappings = new HashMap<>(0);

    int numberOfBuckets = bucketsRequest.getNumberOfBuckets();
    argumentMappings.put("numberOfBuckets", numberOfBuckets);

    /*
    - get an alphabetically sorted (ASC: A-Z) and numbered list,
    - divide it in e.g. 100 (numberOfBuckets) same sized sublists (buckets),
    - get first and last result of each bucket (lower and upper border)
     */
    StringBuilder sqlQuery = new StringBuilder(0);
    Bucket<Headword> parentBucket = bucketsRequest.getParentBucket();
    if (parentBucket != null) {
      UUID startUuid = parentBucket.getStartObject().getUuid();
      UUID endUuid = parentBucket.getEndObject().getUuid();
      argumentMappings.put("startUuid", startUuid);
      argumentMappings.put("endUuid", endUuid);

      sqlQuery.append(
          "WITH"
              + " headwords_list AS (SELECT ROW_NUMBER() OVER (ORDER BY label) as num, uuid, label FROM "
              + tableName
              + "),"
              + " hws AS (SELECT * FROM headwords_list WHERE num between (select num from headwords_list where uuid = :startUuid) AND (select num from headwords_list where uuid = :endUuid)),");
    } else {
      sqlQuery.append(
          "WITH"
              + " hws AS (SELECT ROW_NUMBER() OVER (ORDER BY label) as num, uuid, label FROM "
              + tableName
              + "),");
    }
    sqlQuery.append(
        " buckets AS (SELECT num, uuid, label, ntile(:numberOfBuckets) OVER (ORDER BY label ASC) FROM hws),"
            + " buckets_borders_nums AS (SELECT min(num) AS minNum, max(num) AS maxNum, ntile FROM buckets GROUP BY ntile ORDER BY ntile)"
            + " SELECT num, uuid, label, ntile FROM buckets"
            + " WHERE num IN ((SELECT minNum FROM buckets_borders_nums) UNION (SELECT maxNum FROM buckets_borders_nums))");

    List<Map<String, Object>> rows =
        dbi.withHandle(
            (Handle handle) -> {
              return handle
                  .createQuery(sqlQuery.toString())
                  .bindMap(argumentMappings)
                  .mapToMap()
                  .list();
            });

    // analyze the pairs with same ntile id and create lower and upper border using their values
    // create a bucket java instance for each pair of this lower and upper borders
    List<Bucket<Headword>> content = new ArrayList<>(0);
    for (int i = 0; i < rows.size(); i += 2) {
      Map<String, Object> lowerBorder = rows.get(i);
      Map<String, Object> upperBorder = rows.get(i + 1);

      Headword lowerHeadword = new Headword();
      lowerHeadword.setUuid((UUID) lowerBorder.get("uuid"));
      lowerHeadword.setLabel((String) lowerBorder.get("label"));

      Headword upperHeadword = new Headword();
      upperHeadword.setUuid((UUID) upperBorder.get("uuid"));
      upperHeadword.setLabel((String) upperBorder.get("label"));

      Bucket<Headword> bucket = new Bucket<>(lowerHeadword, upperHeadword);
      content.add(bucket);
    }

    BucketsResponse<Headword> bucketsResponse = new BucketsResponse<>(bucketsRequest, content);
    return bucketsResponse;
  }

  @Override
  public BucketObjectsResponse<Headword> find(BucketObjectsRequest<Headword> bucketObjectsRequest) {
    StringBuilder sqlQuery = new StringBuilder(0);
    Map<String, Object> argumentMappings = new HashMap<>(0);

    // bucket
    Bucket<Headword> bucket = bucketObjectsRequest.getBucket();
    UUID startUuid = bucket.getStartObject().getUuid();
    UUID endUuid = bucket.getEndObject().getUuid();
    argumentMappings.put("startUuid", startUuid);
    argumentMappings.put("endUuid", endUuid);

    sqlQuery.append(
        "WITH"
            + " headwords_list AS (SELECT ROW_NUMBER() OVER (ORDER BY label) as num, uuid, label FROM "
            + tableName
            + "),"
            + " hws AS (SELECT * FROM headwords_list WHERE num between (select num from headwords_list where uuid = :startUuid) AND (select num from headwords_list where uuid = :endUuid))"
            + " SELECT uuid, label FROM hws");

    // paging
    int pageSize = bucketObjectsRequest.getPageSize();
    if (pageSize > 0) {
      sqlQuery.append(" ").append("LIMIT").append(" ").append(pageSize);
    }
    int offset = bucketObjectsRequest.getOffset();
    if (offset >= 0) {
      sqlQuery.append(" ").append("OFFSET").append(" ").append(offset);
    }

    List<Headword> content =
        dbi.withHandle(
            (Handle handle) -> {
              return handle
                  .createQuery(sqlQuery.toString())
                  .bindMap(argumentMappings)
                  .mapToBean(Headword.class)
                  .list();
            });

    BucketObjectsResponse<Headword> bucketObjectsResponse =
        new BucketObjectsResponse<>(bucketObjectsRequest, content);
    return bucketObjectsResponse;
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
  public Headword getByLabelAndLocale(String label, Locale locale) {
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
  protected List<String> getSearchTermTemplates(String tableAlias, String originalSearchTerm) {
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
