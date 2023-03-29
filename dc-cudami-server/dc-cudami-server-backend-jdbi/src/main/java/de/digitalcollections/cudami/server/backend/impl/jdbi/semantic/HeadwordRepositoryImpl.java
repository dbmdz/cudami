package de.digitalcollections.cudami.server.backend.impl.jdbi.semantic;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.semantic.HeadwordRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.UniqueObjectRepositoryImpl;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class HeadwordRepositoryImpl extends UniqueObjectRepositoryImpl<Headword>
    implements HeadwordRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(HeadwordRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "hw";
  public static final String TABLE_ALIAS = "hw";
  public static final String TABLE_NAME = "headwords";

  public HeadwordRepositoryImpl(Jdbi dbi, CudamiConfig cudamiConfig) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        Headword.class,
        cudamiConfig.getOffsetForAlternativePaging());
  }

  @Override
  public void addRelatedEntity(UUID headwordUuid, UUID entityUuid) throws RepositoryException {
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
  public void addRelatedFileresource(UUID headwordUuid, UUID fileResourceUuid)
      throws RepositoryException {
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
  public Headword create() throws RepositoryException {
    return new Headword();
  }

  @Override
  public int deleteByUuids(List<UUID> uuids) throws RepositoryException {
    // delete related data
    uuids.stream()
        .forEach(
            (u) -> {
              try {
                deleteRelatedEntities(u);
              } catch (RepositoryException e) {
                LOGGER.error("Can not delete related entities of headword " + u, e);
              }
              try {
                deleteRelatedFileresources(u);
              } catch (RepositoryException e) {
                LOGGER.error("Can not delete related fileresources of headword " + u, e);
              }
            });

    return super.deleteByUuids(uuids);
  }

  @Override
  public void deleteByLabelAndLocale(String label, Locale locale) throws RepositoryException {
    dbi.withHandle(
        h ->
            h.createUpdate(
                    "DELETE FROM " + tableName + " WHERE label = :label AND locale = :locale")
                .bind("label", label)
                .bind("locale", locale)
                .execute());
  }

  @Override
  public void deleteRelatedEntities(UUID headwordUuid) throws RepositoryException {
    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM headword_entities WHERE headword_uuid = :uuid")
                .bind("uuid", headwordUuid)
                .execute());
  }

  @Override
  public void deleteRelatedFileresources(UUID headwordUuid) throws RepositoryException {
    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM headword_fileresources WHERE headword_uuid = :uuid")
                .bind("uuid", headwordUuid)
                .execute());
  }

  @Override
  public BucketObjectsResponse<Headword> find(BucketObjectsRequest<Headword> bucketObjectsRequest)
      throws RepositoryException {
    Map<String, Object> argumentMappings = new HashMap<>(0);

    // bucket
    Bucket<Headword> bucket = bucketObjectsRequest.getBucket();
    UUID startUuid = bucket.getStartObject().getUuid();
    UUID endUuid = bucket.getEndObject().getUuid();
    argumentMappings.put("startUuid", startUuid);
    argumentMappings.put("endUuid", endUuid);

    String baseQuery =
        "WITH"
            + " headwords_list AS (SELECT row_number() OVER (ORDER BY label) as num, uuid, label FROM "
            + tableName
            + "),"
            + " hws AS (SELECT * FROM headwords_list WHERE num <@ int8range((SELECT num FROM headwords_list WHERE uuid = :startUuid), (SELECT num FROM headwords_list WHERE uuid = :endUuid), '[]'))";

    // query data
    StringBuilder dataQuery = new StringBuilder(baseQuery);
    dataQuery.append(" SELECT uuid, label FROM hws");
    // paging
    int pageSize = bucketObjectsRequest.getPageSize();
    if (pageSize > 0) {
      dataQuery.append(" ").append("LIMIT").append(" ").append(pageSize);
    }
    int offset = bucketObjectsRequest.getOffset();
    if (offset >= 0) {
      dataQuery.append(" ").append("OFFSET").append(" ").append(offset);
    }
    List<Headword> content =
        dbi.withHandle(
            (Handle handle) -> {
              return handle
                  .createQuery(dataQuery.toString())
                  .bindMap(argumentMappings)
                  .mapToBean(Headword.class)
                  .list();
            });

    // query total count
    StringBuilder countQuery = new StringBuilder(baseQuery);
    countQuery.append(" SELECT count(*) FROM hws");
    long total =
        dbi.withHandle(
            h ->
                h.createQuery(countQuery.toString())
                    .bindMap(argumentMappings)
                    .mapTo(Long.class)
                    .findOne()
                    .get());

    BucketObjectsResponse<Headword> bucketObjectsResponse =
        new BucketObjectsResponse<>(bucketObjectsRequest, content, total);
    return bucketObjectsResponse;
  }

  @Override
  public BucketsResponse<Headword> find(BucketsRequest<Headword> bucketsRequest)
      throws RepositoryException {
    Map<String, Object> argumentMappings = new HashMap<>(0);

    int numberOfBuckets = bucketsRequest.getNumberOfBuckets();
    argumentMappings.put("numberOfBuckets", numberOfBuckets);

    /*
     * - get an alphabetically sorted (ASC: A-Z) and numbered list, - divide it in
     * e.g. 100 (numberOfBuckets) same sized sublists (buckets), - get first and
     * last result of each bucket (lower and upper border)
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
              + " headwords_list AS (SELECT row_number() OVER (ORDER BY label) AS num, uuid, label FROM "
              + tableName
              + "),"
              + " hws AS (SELECT * FROM headwords_list WHERE num <@ int8range((SELECT num FROM headwords_list WHERE uuid = :startUuid), (SELECT num FROM headwords_list WHERE uuid = :endUuid), '[]')),");
    } else {
      sqlQuery.append(
          "WITH"
              + " hws AS (SELECT row_number() OVER (ORDER BY label) AS num, uuid, label FROM "
              + tableName
              + "),");
    }
    sqlQuery.append(
        " buckets AS (SELECT num, uuid, label, ntile(:numberOfBuckets) OVER (ORDER BY label ASC) AS tile_number FROM hws),"
            + " buckets_borders_nums AS (SELECT min(num) AS minNum, max(num) AS maxNum, tile_number FROM buckets GROUP BY tile_number ORDER BY tile_number)"
            + " SELECT num, uuid, label, tile_number FROM buckets"
            + " WHERE num IN (SELECT minNum FROM buckets_borders_nums UNION SELECT maxNum FROM buckets_borders_nums)");

    List<Map<String, Object>> rows =
        dbi.withHandle(
            (Handle handle) -> {
              return handle
                  .createQuery(sqlQuery.toString())
                  .bindMap(argumentMappings)
                  .mapToMap()
                  .list();
            });

    // analyze the pairs with same ntile id and create lower and upper border using
    // their values
    // create a bucket java instance for each pair of this lower and upper borders
    List<Bucket<Headword>> content = new ArrayList<>(0);
    for (int i = 0; i < rows.size(); i += 2) {
      Map<String, Object> lowerBorder = rows.get(i);
      Map<String, Object> upperBorder;
      if ((i + 1) < rows.size()) {
        upperBorder = rows.get(i + 1);
      } else {
        upperBorder = lowerBorder;
      }

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
  public List<Headword> find(String label, Locale locale) throws RepositoryException {
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
  public List<Headword> findByLabel(String label) throws RepositoryException {
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
    throw new UnsupportedOperationException(); // TODO: not yet implemented
  }

  @Override
  public PageResponse<Entity> findRelatedEntities(UUID headwordUuid, PageRequest pageRequest) {
    throw new UnsupportedOperationException(); // TODO: not yet implemented
  }

  @Override
  public PageResponse<FileResource> findRelatedFileResources(
      UUID headwordUuid, PageRequest pageRequest) {
    throw new UnsupportedOperationException(); // TODO: not yet implemented
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    List<String> allowedOrderByFields = super.getAllowedOrderByFields();
    allowedOrderByFields.addAll(Arrays.asList("label", "locale"));
    return allowedOrderByFields;
  }

  @Override
  public Headword getByLabelAndLocale(String label, Locale locale) {
    // basic query
    StringBuilder sqlQuery =
        new StringBuilder(
            "SELECT " + getSqlSelectAllFields() + " FROM " + tableName + " AS " + tableAlias);

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
      case "label":
        return tableAlias + ".label";
      case "locale":
        return tableAlias + ".locale";
      default:
        return super.getColumnName(modelProperty);
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
    throw new UnsupportedOperationException(); // TODO: not yet implemented
  }

  @Override
  public List<Entity> getRelatedEntities(UUID headwordUuid) {
    throw new UnsupportedOperationException(); // TODO: not yet implemented
  }

  @Override
  public List<FileResource> getRelatedFileResources(UUID headwordUuid) {
    throw new UnsupportedOperationException(); // TODO: not yet implemented
  }

  @Override
  protected List<String> getSearchTermTemplates(String tableAlias, String originalSearchTerm) {
    return List.of(SearchTermTemplates.ILIKE_STARTS_WITH.renderTemplate(tableAlias, "label"));
  }

  @Override
  protected String getSqlInsertFields() {
    return super.getSqlInsertFields() + ", label, locale";
  }

  @Override
  protected String getSqlInsertValues() {
    return super.getSqlInsertValues() + ", :label, :locale";
  }

  @Override
  public String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    return getSqlSelectReducedFields(tableAlias, mappingPrefix);
  }

  @Override
  protected String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return super.getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".label "
        + mappingPrefix
        + "_label, "
        + tableAlias
        + ".locale "
        + mappingPrefix
        + "_locale";
  }

  @Override
  public String getSqlUpdateFieldValues() {
    return super.getSqlUpdateFieldValues() + ", label=:label, locale=:locale";
  }

  @Override
  public List<Entity> setRelatedEntities(UUID headwordUuid, List<Entity> entities) {
    throw new UnsupportedOperationException(); // TODO: not yet implemented
  }

  @Override
  public List<FileResource> setRelatedFileResources(
      UUID headwordUuid, List<FileResource> fileResources) {
    throw new UnsupportedOperationException(); // TODO: not yet implemented
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
}
