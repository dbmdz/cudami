package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.alias;

import static de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias.UrlAliasRepository.grabLanguage;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias.UrlAliasRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.UniqueObjectRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.WebsiteRepositoryImpl;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.IdentifiableObjectType;
import de.digitalcollections.model.identifiable.IdentifiableType;
import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Sorting;
import de.digitalcollections.model.text.LocalizedText;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.JdbiException;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.jdbi.v3.core.result.RowView;
import org.jdbi.v3.core.statement.StatementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class UrlAliasRepositoryImpl extends UniqueObjectRepositoryImpl<UrlAlias>
    implements UrlAliasRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(UrlAliasRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "ua";
  public static final String TABLE_ALIAS = "ua";
  public static final String TABLE_NAME = "url_aliases";

  public UrlAliasRepositoryImpl(Jdbi dbi, CudamiConfig cudamiConfig) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        UrlAlias.class,
        cudamiConfig.getOffsetForAlternativePaging());
    dbi.registerRowMapper(BeanMapper.factory(UrlAlias.class, MAPPING_PREFIX));
  }

  @Override
  public UrlAlias create() throws RepositoryException {
    return new UrlAlias();
  }

  @Override
  protected BiConsumer<Map<UUID, UrlAlias>, RowView> createAdditionalReduceRowsBiConsumer() {
    return (map, rowView) -> {
      // object should be already in map, as we here just add additional data
      UrlAlias urlAlias = map.get(rowView.getColumn(mappingPrefix + "_uuid", UUID.class));

      /*
       + tableAlias + ".target_identifiable_objecttype target_identifiableObjectType, "
       + tableAlias + ".target_identifiable_type target_identifiableType, "
       + tableAlias + ".target_uuid target_uuid, "
      */
      if (rowView.getColumn("target_uuid", UUID.class) != null) {
        UUID targetUuid = rowView.getColumn("target_uuid", UUID.class);
        IdentifiableType targetIdentifiableType =
            rowView.getColumn("target_identifiableType", IdentifiableType.class);
        IdentifiableObjectType targetIdentifiableObjectType =
            rowView.getColumn("target_identifiableObjectType", IdentifiableObjectType.class);

        Identifiable target = new Identifiable();
        target.setUuid(targetUuid);
        target.setType(targetIdentifiableType);
        target.setIdentifiableObjectType(targetIdentifiableObjectType);
        urlAlias.setTarget(target);
      }

      /*
       + WebsiteRepositoryImpl.TABLE_ALIAS + ".uuid " + WebsiteRepositoryImpl.MAPPING_PREFIX + "_uuid, "
       + WebsiteRepositoryImpl.TABLE_ALIAS + ".label " + WebsiteRepositoryImpl.MAPPING_PREFIX + "_label, "
       + WebsiteRepositoryImpl.TABLE_ALIAS + ".url " + WebsiteRepositoryImpl.MAPPING_PREFIX + "_url";
      */
      if (rowView.getColumn(WebsiteRepositoryImpl.MAPPING_PREFIX + "_uuid", UUID.class) != null) {
        UUID websiteUuid =
            rowView.getColumn(WebsiteRepositoryImpl.MAPPING_PREFIX + "_uuid", UUID.class);
        LocalizedText websiteLabel =
            rowView.getColumn(WebsiteRepositoryImpl.MAPPING_PREFIX + "_label", LocalizedText.class);

        Website website = new Website();
        website.setUuid(websiteUuid);
        website.setLabel(websiteLabel);
        urlAlias.setWebsite(website);
      }
    };
  }

  @Override
  public boolean deleteByIdentifiable(UUID identifiableUuid, boolean force)
      throws RepositoryException {
    LocalizedUrlAliases urlAliases = getByIdentifiable(identifiableUuid);
    return delete(
            urlAliases.flatten().stream()
                .filter(ua -> force || ua.getLastPublished() == null)
                .map(ua -> UrlAlias.builder().uuid(ua.getUuid()).build())
                .collect(Collectors.toSet()))
        > 0;
  }

  // FIXME: use standard find method with filtering set before (not searchTerm
  // anymore!)!
  // @Override
  // public PageResponse<LocalizedUrlAliases> find(PageRequest pageRequest)
  // throws RepositoryException {
  // StringBuilder commonSql =
  // new StringBuilder(" FROM " + tableName + " AS " + tableAlias + WEBSITESJOIN);
  //
  // FilterCriterion slug =
  // StringUtils.hasText(pageRequest.getSearchTerm())
  // ? FilterCriterion.builder()
  // .withExpression("slug")
  // .contains(pageRequest.getSearchTerm())
  // .build()
  // : null;
  //
  // Filtering filtering = pageRequest.getFiltering();
  // if (filtering == null) {
  // filtering = Filtering.builder().add(slug).build();
  // } else {
  // filtering.add(slug);
  // }
  // Map<String, Object> bindings = new HashMap<>(0);
  // addFiltering(filtering, commonSql, bindings);
  //
  // long count;
  // try {
  // count =
  // dbi.withHandle(
  // h ->
  // h.createQuery("SELECT count(*) " + commonSql.toString())
  // .bindMap(bindings)
  // .mapTo(Long.class)
  // .findOne()
  // .orElse(0L));
  // } catch (StatementException e) {
  // String detailMessage = e.getCause() != null ? e.getCause().getMessage() :
  // e.getMessage();
  // throw new RepositoryException(
  // String.format("The SQL statement is defective: %s", detailMessage), e);
  // } catch (JdbiException e) {
  // throw new RepositoryException(e);
  // }
  //
  // if (!pageRequest.hasSorting()) {
  // pageRequest.setSorting(new Sorting("slug"));
  // }
  // commonSql.insert(0, String.format("SELECT %s ", getSelectFields(true)));
  // addPagingAndSorting(pageRequest, commonSql);
  //
  // try {
  // UrlAlias[] resultset =
  // dbi.withHandle(
  // h ->
  // h.createQuery(commonSql.toString())
  // .bindMap(bindings)
  // .reduceRows(this::mapRowToUrlAlias)
  // .toArray(UrlAlias[]::new));
  // return new PageResponse<>(List.of(new LocalizedUrlAliases(resultset)),
  // pageRequest,
  // count);
  // } catch (StatementException e) {
  // String detailMessage = e.getCause() != null ? e.getCause().getMessage() :
  // e.getMessage();
  // throw new RepositoryException(
  // String.format("The SQL statement is defective: %s", detailMessage), e);
  // } catch (JdbiException e) {
  // throw new RepositoryException(e);
  // }
  // }

  private UUID extractWebsiteUuid(UrlAlias urlAlias) {
    if (urlAlias == null) {
      return null;
    }
    return urlAlias.getWebsite() != null ? urlAlias.getWebsite().getUuid() : null;
  }

  @Override
  public LocalizedUrlAliases findAllPrimaryLinks(String slug) throws RepositoryException {
    if (!StringUtils.hasText(slug)) {
      return new LocalizedUrlAliases();
    }
    return findMainLinks(false, null, slug, false);
  }

  @Override
  public PageResponse<LocalizedUrlAliases> findLocalizedUrlAliases(PageRequest pageRequest)
      throws RepositoryException {
    StringBuilder commonSql =
        new StringBuilder(" FROM " + tableName + " AS " + tableAlias + WEBSITESJOIN);

    FilterCriterion slug =
        StringUtils.hasText(pageRequest.getSearchTerm())
            ? FilterCriterion.builder()
                .withExpression("slug")
                .contains(pageRequest.getSearchTerm())
                .build()
            : null;

    Filtering filtering = pageRequest.getFiltering();
    if (filtering == null) {
      filtering = Filtering.builder().add(slug).build();
    } else {
      filtering.add(slug);
    }
    Map<String, Object> bindings = new HashMap<>(0);
    addFiltering(filtering, commonSql, bindings);

    long count;
    try {
      count =
          dbi.withHandle(
              h ->
                  h.createQuery("SELECT count(*) " + commonSql.toString())
                      .bindMap(bindings)
                      .mapTo(Long.class)
                      .findOne()
                      .orElse(0L));
    } catch (StatementException e) {
      String detailMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
      throw new RepositoryException(
          String.format("The SQL statement is defective: %s", detailMessage), e);
    } catch (JdbiException e) {
      throw new RepositoryException(e);
    }

    if (!pageRequest.hasSorting()) {
      pageRequest.setSorting(new Sorting("slug"));
    }
    commonSql.insert(0, String.format("SELECT %s ", getSelectFields(true)));
    addPagingAndSorting(pageRequest, commonSql);

    try {
      UrlAlias[] resultset =
          dbi.withHandle(
              h ->
                  h.createQuery(commonSql.toString())
                      .bindMap(bindings)
                      .reduceRows(this::mapRowToUrlAlias)
                      .toArray(UrlAlias[]::new));
      return new PageResponse<>(List.of(new LocalizedUrlAliases(resultset)), pageRequest, count);
    } catch (StatementException e) {
      String detailMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
      throw new RepositoryException(
          String.format("The SQL statement is defective: %s", detailMessage), e);
    } catch (JdbiException e) {
      throw new RepositoryException(e);
    }
  }

  private LocalizedUrlAliases findMainLinks(
      boolean useWebsite, UUID websiteUuid, String slug, boolean considerLanguage)
      throws RepositoryException {
    StringBuilder innerSel =
        new StringBuilder(
            String.format(
                "(SELECT %2$s.target_uuid, %2$s.target_language FROM %1$s AS %2$s ",
                tableName, tableAlias));
    Filtering innerFiltering =
        Filtering.builder()
            .add(FilterCriterion.builder().withExpression("slug").isEquals(slug).build())
            .build();
    if (useWebsite) {
      innerFiltering.add(
          FilterCriterion.builder().withExpression("websiteUuid").isEquals(websiteUuid).build());
    }
    Map<String, Object> bindings = new HashMap<>(0);
    addFiltering(innerFiltering, innerSel, bindings);
    innerSel.append(")");
    StringBuilder sql =
        new StringBuilder(
            "WITH target (uuid, language) AS "
                + innerSel.toString()
                + " SELECT "
                + getSelectFields(true)
                + " FROM "
                + tableName
                + " AS "
                + tableAlias
                + WEBSITESJOIN
                + " WHERE "
                + tableAlias
                + ".target_uuid IN (SELECT uuid FROM target)");
    if (considerLanguage) {
      sql.append(" AND " + tableAlias + ".target_language IN (SELECT language FROM target)");
    }
    Filtering outerFiltering =
        Filtering.builder()
            .add(FilterCriterion.builder().withExpression("primary").isEquals(true).build())
            .build();
    if (useWebsite) {
      outerFiltering.add(
          FilterCriterion.builder().withExpression("websiteUuid").isEquals(websiteUuid).build());
    }
    addFiltering(outerFiltering, sql, bindings);
    try {
      UrlAlias[] resultset =
          dbi.withHandle(
              h ->
                  h.createQuery(sql.toString())
                      .bindMap(bindings)
                      .reduceRows(this::mapRowToUrlAlias)
                      .toArray(UrlAlias[]::new));
      return new LocalizedUrlAliases(resultset);
    } catch (StatementException e) {
      String detailMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
      throw new RepositoryException(
          String.format("The SQL statement is defective: %s", detailMessage), e);
    } catch (JdbiException e) {
      throw new RepositoryException(e);
    }
  }

  @Override
  public LocalizedUrlAliases findPrimaryLinksForWebsite(
      UUID websiteUuid, String slug, boolean considerLanguage) throws RepositoryException {
    if (!StringUtils.hasText(slug)) {
      return new LocalizedUrlAliases();
    }
    return findMainLinks(true, websiteUuid, slug, considerLanguage);
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    List<String> allowedOrderByFields = super.getAllowedOrderByFields();
    allowedOrderByFields.addAll(
        Arrays.asList("lastPublished", "\"primary\"", "slug", "targetLanguage"));
    return allowedOrderByFields;
  }

  @Override
  public LocalizedUrlAliases getByIdentifiable(UUID uuid) throws RepositoryException {
    if (uuid == null) {
      return new LocalizedUrlAliases();
    }
    StringBuilder sql =
        new StringBuilder(
            "SELECT "
                + getSelectFields(true)
                + " FROM "
                + tableName
                + " AS "
                + tableAlias
                + WEBSITESJOIN);
    Map<String, Object> bindings = new HashMap<>();
    Filtering target =
        Filtering.builder()
            .add(FilterCriterion.builder().withExpression("targetUuid").isEquals(uuid).build())
            .build();
    addFiltering(target, sql, bindings);
    try {
      UrlAlias[] resultset =
          dbi.withHandle(
              h ->
                  h.createQuery(sql.toString())
                      .bindMap(bindings)
                      .reduceRows(this::mapRowToUrlAlias)
                      .toArray(UrlAlias[]::new));
      return new LocalizedUrlAliases(resultset);
    } catch (StatementException e) {
      String detailMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
      throw new RepositoryException(
          String.format("The SQL statement is defective: %s", detailMessage), e);
    } catch (JdbiException e) {
      throw new RepositoryException(e);
    }
  }

  @Override
  public String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "lastPublished":
        return tableAlias + ".last_published";
      case "primary":
        return tableAlias + ".primary";
      case "slug":
        return tableAlias + ".slug";
      case "targetLanguage":
        return tableAlias + ".target_language";
      case "websiteUuid":
        return tableAlias + ".website_uuid";
      default:
        return super.getColumnName(modelProperty);
    }
  }

  @Override
  public List<UrlAlias> getRandom(int count) throws RepositoryException {
    throw new UnsupportedOperationException(); // TODO: not yet implemented
  }

  private Map<String, Object> getSpecialBindings(UrlAlias urlAlias) {
    Map<String, Object> bindings = new HashMap<>();
    bindings.put("targetIdentifiableObjectType", urlAlias.getTarget().getIdentifiableObjectType());
    bindings.put("targetIdentifiableType", urlAlias.getTarget().getType());
    bindings.put("targetUuid", urlAlias.getTarget().getUuid());
    bindings.put("websiteUuid", extractWebsiteUuid(urlAlias));
    bindings.put("targetLanguage", grabLanguage(urlAlias.getTargetLanguage()));
    return bindings;
  }

  @Override
  protected String getSqlInsertFields() {
    return super.getSqlInsertFields()
        + ", last_published, \"primary\", slug, target_identifiable_objecttype, target_identifiable_type, target_language, target_uuid, website_uuid";
  }

  @Override
  protected String getSqlInsertValues() {
    return super.getSqlInsertValues()
        + ", :lastPublished, :primary, :slug, :targetIdentifiableObjectType, :targetIdentifiableType, :targetLanguage, :targetUuid, :websiteUuid";
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
        + ".last_published "
        + mappingPrefix
        + "_lastPublished, "
        + tableAlias
        + ".primary "
        + mappingPrefix
        + "_primary, "
        + tableAlias
        + ".slug "
        + mappingPrefix
        + "_slug, "
        + tableAlias
        + ".target_language "
        + mappingPrefix
        + "_targetLanguage, "

        // target object fields without registered mappingPrefix
        // because they can not be mapped automatically by JDBI to UrlAlias fields - see
        // mapRowToUrlAlias (use unregistered mapping-prefix "target_" to make it easier to read)
        + tableAlias
        + ".target_identifiable_objecttype target_identifiableObjectType, "
        + tableAlias
        + ".target_identifiable_type target_identifiableType, "
        + tableAlias
        + ".target_uuid target_uuid, "

        // joined website fields
        + WebsiteRepositoryImpl.TABLE_ALIAS
        + ".uuid "
        + WebsiteRepositoryImpl.MAPPING_PREFIX
        + "_uuid, "
        + WebsiteRepositoryImpl.TABLE_ALIAS
        + ".label "
        + WebsiteRepositoryImpl.MAPPING_PREFIX
        + "_label, "
        + WebsiteRepositoryImpl.TABLE_ALIAS
        + ".url "
        + WebsiteRepositoryImpl.MAPPING_PREFIX
        + "_url";
  }

  @Override
  protected String getSqlSelectReducedFieldsJoins() {
    return super.getSqlSelectReducedFieldsJoins()
        + " LEFT JOIN "
        + WebsiteRepositoryImpl.TABLE_NAME
        + " AS "
        + WebsiteRepositoryImpl.TABLE_ALIAS
        + " ON "
        + WebsiteRepositoryImpl.TABLE_ALIAS
        + ".uuid = "
        + tableAlias
        + ".website_uuid";
  }

  @Override
  public String getSqlUpdateFieldValues() {
    return super.getSqlUpdateFieldValues()
        + ", last_published=:lastPublished, \"primary\"=:primary, slug=:slug, target_identifiable_objecttype=:targetIdentifiableObjectType, target_identifiable_type=:targetIdentifiableType, target_language=:targetLanguage, target_uuid=:targetUuid, website_uuid=:websiteUuid";
  }

  @Override
  public boolean hasUrlAlias(String slug, UUID websiteUuid, Locale targetLanguage)
      throws RepositoryException {
    if (!StringUtils.hasText(slug)) {
      throw new RepositoryException(
          "UrlAliasRepository.hasUrlAlias: Parameter 'slug' must not be null or empty.");
    }
    StringBuilder sql = new StringBuilder("SELECT uuid FROM " + tableName + " AS " + tableAlias);
    Filtering filtering =
        Filtering.builder()
            .add(
                FilterCriterion.builder()
                    .withExpression("websiteUuid")
                    .isEquals(websiteUuid)
                    .build())
            .build();
    filtering.add(
        FilterCriterion.builder()
            .withExpression("targetLanguage")
            .isEquals(grabLanguage(targetLanguage))
            .build());
    filtering.add(FilterCriterion.builder().withExpression("slug").isEquals(slug).build());
    Map<String, Object> bindings = new HashMap<>();
    addFiltering(filtering, sql, bindings);
    try {
      return 0
          < dbi.withHandle(
              h ->
                  h.createQuery(sql.toString())
                      .bindMap(bindings)
                      .reduceRows(0, (count, row) -> ++count));
    } catch (StatementException e) {
      String detailMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
      throw new RepositoryException(
          String.format("The SQL statement is defective: %s", detailMessage), e);
    } catch (JdbiException e) {
      throw new RepositoryException(e);
    }
  }

  @Override
  public void save(UrlAlias urlAlias) throws RepositoryException {
    Map<String, Object> bindings = getSpecialBindings(urlAlias);
    super.save(urlAlias, bindings);
  }

  @Override
  protected boolean supportsCaseSensitivityForProperty(String modelProperty) {
    return false;
  }

  @Override
  public void update(UrlAlias urlAlias) throws RepositoryException {
    Map<String, Object> bindings = getSpecialBindings(urlAlias);
    super.update(urlAlias, bindings);
  }
}
