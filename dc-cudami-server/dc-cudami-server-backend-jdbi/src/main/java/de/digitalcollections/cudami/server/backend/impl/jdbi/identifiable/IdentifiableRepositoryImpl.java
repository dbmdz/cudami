package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable;

import static de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifierRepositoryImpl.SQL_FULL_FIELDS_ID;
import static de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource.FileResourceMetadataRepositoryImpl.SQL_PREVIEW_IMAGE_FIELDS_PI;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.UniqueObjectRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.alias.UrlAliasRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource.ImageFileResourceRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.semantic.TagRepositoryImpl;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import de.digitalcollections.model.semantic.Tag;
import de.digitalcollections.model.text.LocalizedText;
import java.net.URI;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.jdbi.v3.core.result.RowView;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class IdentifiableRepositoryImpl<I extends Identifiable>
    extends UniqueObjectRepositoryImpl<I> implements IdentifiableRepository<I> {

  private static final Logger LOGGER = LoggerFactory.getLogger(IdentifiableRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "idf";
  public static final String TABLE_ALIAS = "i";
  public static final String TABLE_NAME = "identifiables";

  public String getSqlInsertFields() {
    return " uuid, created, description, identifiable_objecttype, identifiable_type, "
        + "label, last_modified, previewfileresource, preview_hints, split_label, tags_uuids";
  }

  /* Do not change order! Must match order in getSqlInsertFields!!! */
  public String getSqlInsertValues() {
    return " :uuid, :created, :description::JSONB, :identifiableObjectType, :type, "
        + ":label::JSONB, :lastModified, :previewFileResource, :previewImageRenderingHints::JSONB, :split_label::TEXT[], :tags_uuids::UUID[]";
  }

  public String getSqlSelectAllFields() {
    return getSqlSelectAllFields(tableAlias, mappingPrefix);
  }

  public String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    return getSqlSelectReducedFields(tableAlias, mappingPrefix);
  }

  protected String getSqlSelectAllFieldsJoins() {
    return "";
  }

  public String getSqlSelectReducedFields() {
    return getSqlSelectReducedFields(tableAlias, mappingPrefix);
  }

  public String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return " "
        + tableAlias
        + ".uuid "
        + mappingPrefix
        + "_uuid, "
        + tableAlias
        + ".created "
        + mappingPrefix
        + "_created, "
        + tableAlias
        + ".description "
        + mappingPrefix
        + "_description, "
        + tableAlias
        + ".identifiable_objecttype "
        + mappingPrefix
        + "_identifiableObjectType, "
        + tableAlias
        + ".identifiable_type "
        + mappingPrefix
        + "_type, "
        + tableAlias
        + ".label "
        + mappingPrefix
        + "_label, "
        + tableAlias
        + ".last_modified "
        + mappingPrefix
        + "_lastModified, "
        + tableAlias
        + ".preview_hints "
        + mappingPrefix
        + "_previewImageRenderingHints";
  }

  protected String getSqlSelectReducedFieldsJoins() {
    return "";
  }

  public String getSqlUpdateFieldValues() {
    // do not update/left out from statement (not changed since insert):
    // uuid, created, identifiable_type
    return " description=:description::JSONB, label=:label::JSONB, last_modified=:lastModified, previewfileresource=:previewFileResource, "
        + "preview_hints=:previewImageRenderingHints::JSONB, split_label=:split_label::TEXT[]"
        + ", tags_uuids=:tags_uuids::UUID[]";
  }

  /**
   * On insert or update these fields will be returned to be processed by {@link
   * #insertUpdateCallback(Identifiable, Map)}.
   *
   * @return modifiable list of fields, please do not return null
   */
  protected List<String> getReturnedFieldsOnInsertUpdate() {
    return new ArrayList<>(0);
  }

  /* BiFunction for reducing rows (related objects) of joins not already part of identifiable (Identifier, preview image ImageFileResource). */
  public BiConsumer<Map<UUID, I>, RowView> additionalReduceRowsBiConsumer = (map, rowView) -> {};
  public final BiConsumer<Map<UUID, I>, RowView> basicReduceRowsBiConsumer;
  public final BiConsumer<Map<UUID, I>, RowView> fullReduceRowsBiConsumer;
  protected final Class<? extends Identifiable> identifiableImplClass;

  @Autowired
  protected IdentifiableRepositoryImpl(Jdbi dbi, CudamiConfig cudamiConfig) {
    this(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        Identifiable.class,
        cudamiConfig.getOffsetForAlternativePaging());
  }

  protected IdentifiableRepositoryImpl(
      Jdbi dbi,
      String tableName,
      String tableAlias,
      String mappingPrefix,
      Class<? extends Identifiable> identifiableImplClass,
      int offsetForAlternativePaging) {
    this(
        dbi,
        tableName,
        tableAlias,
        mappingPrefix,
        identifiableImplClass,
        null,
        offsetForAlternativePaging);
  }

  protected IdentifiableRepositoryImpl(
      Jdbi dbi,
      String tableName,
      String tableAlias,
      String mappingPrefix,
      Class<? extends Identifiable> identifiableImplClass,
      BiConsumer<Map<UUID, I>, RowView> additionalReduceRowsBiConsumer,
      int offsetForAlternativePaging) {
    super(dbi, tableName, tableAlias, mappingPrefix, offsetForAlternativePaging);

    // register row mapper for given class and mapping prefix
    // (until now everywhere BeanMapper.factory... was used. If this changes, row mapper
    // registration may be moved back into each repository impl?)
    dbi.registerRowMapper(BeanMapper.factory(identifiableImplClass, mappingPrefix));
    dbi.registerRowMapper(
        BeanMapper.factory(UrlAlias.class, UrlAliasRepositoryImpl.MAPPING_PREFIX));

    // set basic reduce rows bifunction for reduced selects (lists, paging)
    // note: it turned out, that we also want identifiers and previewimage for reduced selects. So
    // currently there is no difference to full, except that we do not want tags in reduced selects.
    this.basicReduceRowsBiConsumer = createReduceRowsBiConsumer(true, true, false);

    // set full reduce rows bifunction for full selects (find one)
    this.fullReduceRowsBiConsumer = createReduceRowsBiConsumer(true, true, true);

    // for detailes select (only used in find one, not lists): if additional objects should be
    // "joined" into instance, set bi function for doing this:
    if (additionalReduceRowsBiConsumer != null) {
      this.additionalReduceRowsBiConsumer = additionalReduceRowsBiConsumer;
    }

    this.identifiableImplClass = identifiableImplClass;
  }

  protected String addCrossTablePageRequestParams(
      PageRequest pageRequest, StringBuilder innerQuery, final String crossTableAlias) {
    String orderBy = getOrderBy(pageRequest.getSorting());
    if (!StringUtils.hasText(orderBy)) {
      orderBy = "ORDER BY idx ASC";
      innerQuery.append(
          " ORDER BY "
              + crossTableAlias
              + ".sortindex"); // must be the column itself to use window functions
    }
    addPageRequestParams(pageRequest, innerQuery);
    return orderBy;
  }

  @Override
  public void addRelatedEntity(UUID identifiableUuid, UUID entityUuid) {
    Integer sortIndex =
        retrieveNextSortIndexForParentChildren(
            dbi, "rel_identifiable_entities", "identifiable_uuid", identifiableUuid);

    dbi.withHandle(
        h ->
            h.createUpdate(
                    "INSERT INTO rel_identifiable_entities(identifiable_uuid, entity_uuid, sortindex) VALUES (:identifiableUuid, :entityUuid, :sortindex)")
                .bind("identifiableUuid", identifiableUuid)
                .bind("entityUuid", entityUuid)
                .bind("sortindex", sortIndex)
                .execute());
  }

  @Override
  public void addRelatedFileresource(UUID identifiableUuid, UUID fileResourceUuid) {
    Integer sortIndex =
        retrieveNextSortIndexForParentChildren(
            dbi, "rel_identifiable_fileresources", "identifiable_uuid", identifiableUuid);

    dbi.withHandle(
        h ->
            h.createUpdate(
                    "INSERT INTO rel_identifiable_fileresources(identifiable_uuid, fileresource_uuid, sortindex) VALUES (:identifiableUuid, :fileresourceUuid, :sortindex)")
                .bind("identifiableUuid", identifiableUuid)
                .bind("fileresourceUuid", fileResourceUuid)
                .bind("sortindex", sortIndex)
                .execute());
  }

  @Override
  protected String addSearchTermMappings(String searchTerm, Map<String, Object> argumentMappings) {
    argumentMappings.put(
        SearchTermTemplates.ARRAY_CONTAINS.placeholder,
        IdentifiableRepository.splitToArray(searchTerm));
    return super.addSearchTermMappings(searchTerm, argumentMappings);
  }

  private BiConsumer<Map<UUID, I>, RowView> createReduceRowsBiConsumer(
      boolean withIdentifiers, boolean withPreviewImage, boolean withTags) {
    return (map, rowView) -> {
      I identifiable =
          map.computeIfAbsent(
              rowView.getColumn(mappingPrefix + "_uuid", UUID.class),
              fn -> {
                return (I) rowView.getRow(identifiableImplClass);
              });

      if (withPreviewImage && rowView.getColumn("pi_uuid", UUID.class) != null) {
        // see definition in FileResourceMetadataRepositoryimpl.SQL_PREVIEW_IMAGE_FIELDS_PI:
        // file.uuid pi_uuid, file.filename pi_filename, file.mimetype pi_mimeType,
        // file.uri pi_uri, file.http_base_url pi_httpBaseUrl

        // TODO workaround as long at is not possible to register two RowMappers for one type
        // but for different prefixes (unitl now the first takes precedence),
        // see discussion https://groups.google.com/g/jdbi/c/UhVygrtoH0U
        ImageFileResource previewImage = new ImageFileResource();
        previewImage.setUuid(rowView.getColumn("pi_uuid", UUID.class));
        previewImage.setFilename(rowView.getColumn("pi_filename", String.class));
        previewImage.setHttpBaseUrl(rowView.getColumn("pi_httpBaseUrl", URL.class));
        previewImage.setMimeType(rowView.getColumn("pi_mimeType", MimeType.class));
        previewImage.setUri(rowView.getColumn("pi_uri", URI.class));
        identifiable.setPreviewImage(previewImage);
      }
      if (withIdentifiers && rowView.getColumn("id_uuid", UUID.class) != null) {
        Identifier dbIdentifier = rowView.getRow(Identifier.class);
        identifiable.addIdentifier(dbIdentifier);
      }
      if (rowView.getColumn(UrlAliasRepositoryImpl.MAPPING_PREFIX + "_uuid", UUID.class) != null) {
        UrlAlias urlAlias = rowView.getRow(UrlAlias.class);
        UUID websiteUuid =
            rowView.getColumn(UrlAliasRepositoryImpl.WEBSITESALIAS + "_uuid", UUID.class);
        if (websiteUuid != null) {
          Website website =
              new Website(
                  rowView.getColumn(UrlAliasRepositoryImpl.WEBSITESALIAS + "_url", URL.class));
          website.setUuid(websiteUuid);
          website.setLabel(
              rowView.getColumn(
                  UrlAliasRepositoryImpl.WEBSITESALIAS + "_label", LocalizedText.class));
          urlAlias.setWebsite(website);
        }
        if (identifiable.getLocalizedUrlAliases() == null) {
          identifiable.setLocalizedUrlAliases(new LocalizedUrlAliases(urlAlias));
        } else if (!identifiable.getLocalizedUrlAliases().containsUrlAlias(urlAlias)) {
          identifiable.getLocalizedUrlAliases().add(urlAlias);
        }
      }
      if (withTags
          && rowView.getColumn(TagRepositoryImpl.MAPPING_PREFIX + "_uuid", UUID.class) != null) {
        Tag tag = rowView.getRow(Tag.class);
        if (tag != null) {
          identifiable.addTag(tag);
        }
      }

      extendReducedIdentifiable(identifiable, rowView);
    };
  }

  @Override
  public boolean delete(List<UUID> uuids) {
    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM " + tableName + " WHERE uuid in (<uuids>)")
                .bindList("uuids", uuids)
                .execute());
    return true;
  }

  /**
   * Extend the reduced Identifiable by the contents of the provided RowView
   *
   * @param identifiable the reduced Identifiable
   * @param rowView the rowView
   */
  protected void extendReducedIdentifiable(I identifiable, RowView rowView) {
    // do nothing by default
  }

  protected PageResponse<I> find(PageRequest pageRequest, Map<String, Object> argumentMappings) {
    String commonSql = " FROM " + tableName + " AS " + tableAlias;
    return find(pageRequest, commonSql, argumentMappings);
  }

  protected PageResponse<I> find(
      PageRequest pageRequest, String commonSql, Map<String, Object> argumentMappings) {
    if (argumentMappings == null) {
      argumentMappings = new HashMap<>(0);
    }
    StringBuilder commonSqlBuilder = new StringBuilder(commonSql);
    String executedSearchTerm = addSearchTerm(pageRequest, commonSqlBuilder, argumentMappings);
    addFiltering(pageRequest, commonSqlBuilder, argumentMappings);

    StringBuilder innerQuery = new StringBuilder("SELECT " + tableAlias + ".* " + commonSqlBuilder);
    addPageRequestParams(pageRequest, innerQuery);
    List<I> result =
        retrieveList(
            getSqlSelectReducedFields(),
            innerQuery,
            argumentMappings,
            getOrderBy(pageRequest.getSorting()));

    StringBuilder countQuery = new StringBuilder("SELECT count(*)" + commonSqlBuilder);
    long total = retrieveCount(countQuery, argumentMappings);

    return new PageResponse<>(result, pageRequest, total, executedSearchTerm);
  }

  protected PageResponse<I> find(PageRequest pageRequest, String commonSql) {
    return find(pageRequest, commonSql, null);
  }

  @Override
  public PageResponse<I> find(PageRequest pageRequest) {
    return find(pageRequest, (Map<String, Object>) null);
  }

  @Override
  @Deprecated
  /**
   * @deprecated use method with PageRequest signature instead
   */
  public PageResponse<I> findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial) {
    // add special filter
    Filtering filtering = pageRequest.getFiltering();
    if (filtering == null) {
      filtering = Filtering.builder().build();
      pageRequest.setFiltering(filtering);
    }

    Filtering initialFiltering =
        Filtering.builder()
            .add(
                FilterCriterion.builder()
                    .withExpression(tableAlias + ".label ->> :language")
                    .startsWith(":initial")
                    .build())
            .build();
    filtering.add(initialFiltering);

    // add special ordering
    Sorting sorting = pageRequest.getSorting();

    Sorting labelSorting =
        Sorting.builder()
            .order(
                Order.builder()
                    .property("label")
                    .subProperty(language)
                    .direction(Direction.ASC)
                    .build())
            .build();
    if (sorting == null) {
      sorting = labelSorting;
    } else {
      sorting.and(labelSorting);
    }
    pageRequest.setSorting(sorting);

    Map<String, Object> argumentMappings = new HashMap<>(2);
    argumentMappings.put("language", language);
    argumentMappings.put("initial", initial);

    return find(pageRequest, argumentMappings);
  }

  @Override
  public List<I> getAllFull() {
    return retrieveList(getSqlSelectAllFields(), null, null, null);
  }

  @Override
  public List<I> getAllReduced() {
    return retrieveList(getSqlSelectReducedFields(), null, null, null);
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    return new ArrayList<>(
        Arrays.asList("created", "identifiableObjectType", "label", "lastModified", "type"));
  }

  @Override
  public I getByIdentifier(Identifier identifier) {
    if (identifier.getIdentifiable() != null) {
      return getByUuid(identifier.getIdentifiable());
    }

    String namespace = identifier.getNamespace();
    String identifierId = identifier.getId();

    UUID identifiableUuid =
        dbi.withHandle(
            h ->
                h.createQuery(
                        """
            SELECT identifiable FROM identifiers
            WHERE namespace = ?
              AND identifier = ?;""")
                    .bind(0, namespace)
                    .bind(1, identifierId)
                    .mapTo(UUID.class)
                    .findOne()
                    .orElse(null));

    if (identifiableUuid == null) return null;

    return getByUuid(identifiableUuid);
  }

  @Override
  public I getByUuidAndFiltering(UUID uuid, Filtering filtering) {
    if (filtering == null) {
      filtering = Filtering.builder().build();
    }
    filtering.add(FilterCriterion.builder().withExpression("uuid").isEquals(uuid).build());

    I result = retrieveOne(getSqlSelectAllFields(), filtering, null);
    return result;
  }

  @Override
  public String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "created":
        return tableAlias + ".created";
      case "description":
        return tableAlias + ".description";
      case "identifiableObjectType":
        return tableAlias + ".identifiable_objecttype";
      case "label":
        return tableAlias + ".label";
      case "lastModified":
        return tableAlias + ".last_modified";
      case "previewImage":
        return tableAlias + ".previewfileresource";
      case "type":
        return tableAlias + ".identifiable_type";
      case "uuid":
        return tableAlias + ".uuid";
      default:
        return null;
    }
  }

  public int getIndex(List<? extends Identifiable> list, Identifiable identifiable) {
    int pos = -1;
    for (Identifiable idf : list) {
      pos += 1;
      if (idf.getUuid().equals(identifiable.getUuid())) {
        return pos;
      }
    }
    return -1;
  }

  public int getIndex(List<UUID> list, UUID uuid) {
    int pos = -1;
    for (UUID u : list) {
      pos += 1;
      if (u.equals(uuid)) {
        return pos;
      }
    }
    return -1;
  }

  @Override
  public List<Locale> getLanguages() {
    String query =
        "SELECT DISTINCT jsonb_object_keys("
            + tableAlias
            + ".label) as languages FROM "
            + tableName
            + " AS "
            + tableAlias;
    List<Locale> result = dbi.withHandle(h -> h.createQuery(query).mapTo(Locale.class).list());
    return result;
  }

  @Override
  public List<Entity> getRelatedEntities(UUID identifiableUuid) {
    String query =
        "SELECT * FROM entities e"
            + " INNER JOIN rel_identifiable_entities ref ON e.uuid=ref.entity_uuid"
            + " WHERE ref.identifiable_uuid = :identifiableUuid"
            + " ORDER BY ref.sortindex";

    List<Entity> list =
        dbi.withHandle(
            h ->
                h.createQuery(query)
                    .bind("identifiableUuid", identifiableUuid)
                    .mapToBean(Entity.class)
                    .list());
    return list;
  }

  @Override
  public List<FileResource> getRelatedFileResources(UUID identifiableUuid) {
    String query =
        "SELECT * FROM fileresources f"
            + " INNER JOIN rel_identifiable_fileresources ref ON f.uuid=ref.fileresource_uuid"
            + " WHERE ref.identifiable_uuid = :identifiableUuid"
            + " ORDER BY ref.sortindex";

    List<FileResource> result =
        dbi.withHandle(
            h ->
                h.createQuery(query)
                    .bind("identifiableUuid", identifiableUuid)
                    .mapToBean(FileResource.class)
                    .list());
    return result;
  }

  @Override
  protected List<String> getSearchTermTemplates(String tableAlias, String originalSearchTerm) {
    if (originalSearchTerm == null) {
      return Collections.EMPTY_LIST;
    }
    List<String> templates = new ArrayList<>(2);
    if (originalSearchTerm.matches("\".+\"")) {
      templates.add(SearchTermTemplates.JSONB_PATH.renderTemplate(tableAlias, "label", "**"));
    } else {
      templates.add(SearchTermTemplates.ARRAY_CONTAINS.renderTemplate(tableAlias, "split_label"));
    }
    templates.add(SearchTermTemplates.JSONB_PATH.renderTemplate(tableAlias, "description", "**"));
    return templates;
  }

  @Override
  protected String getUniqueField() {
    return "uuid";
  }

  /**
   * After save and update the returned fields (declared in {@link
   * #getReturnedFieldsOnInsertUpdate()}) can be processed here.
   *
   * @param identifiable the object that was passed to save/update
   * @param returnedFields returned fields in a map of column names to values
   */
  protected void insertUpdateCallback(I identifiable, Map<String, Object> returnedFields) {
    // can be implemented in derived classes
  }

  @Override
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

  public List<I> retrieveList(
      String fieldsSql,
      StringBuilder innerQuery,
      final Map<String, Object> argumentMappings,
      String orderBy) {
    return retrieveList(fieldsSql, null, innerQuery, argumentMappings, orderBy);
  }

  public List<I> retrieveList(
      String fieldsSql,
      String fieldsSqlAdditionalJoins,
      StringBuilder innerQuery,
      final Map<String, Object> argumentMappings,
      String orderBy) {
    final String sql =
        "SELECT "
            + fieldsSql
            + ","
            + SQL_FULL_FIELDS_ID
            + ","
            + SQL_PREVIEW_IMAGE_FIELDS_PI
            + ", "
            + UrlAliasRepositoryImpl.getSelectFields(true)
            + " FROM "
            + (innerQuery != null ? "(" + innerQuery + ")" : tableName)
            + " AS "
            + tableAlias
            + (StringUtils.hasText(fieldsSqlAdditionalJoins)
                ? " %s".formatted(fieldsSqlAdditionalJoins)
                : "")
            + (StringUtils.hasText(getSqlSelectReducedFieldsJoins())
                ? " %s ".formatted(getSqlSelectReducedFieldsJoins())
                : "")
            + " LEFT JOIN "
            + IdentifierRepositoryImpl.TABLE_NAME
            + " AS "
            + IdentifierRepositoryImpl.TABLE_ALIAS
            + " ON "
            + tableAlias
            + ".uuid = "
            + IdentifierRepositoryImpl.TABLE_ALIAS
            + ".identifiable"
            + " LEFT JOIN "
            + ImageFileResourceRepositoryImpl.TABLE_NAME
            + " AS file ON "
            + tableAlias
            + ".previewfileresource = file.uuid"
            + " LEFT JOIN "
            + UrlAliasRepositoryImpl.TABLE_NAME
            + " AS "
            + UrlAliasRepositoryImpl.TABLE_ALIAS
            + " ON "
            + tableAlias
            + ".uuid = "
            + UrlAliasRepositoryImpl.TABLE_ALIAS
            + ".target_uuid"
            + UrlAliasRepositoryImpl.WEBSITESJOIN
            + (orderBy != null && orderBy.matches("(?iu)^\\s*order by.+")
                ? " " + orderBy
                : (StringUtils.hasText(orderBy) ? " ORDER BY " + orderBy : ""));

    List<I> result =
        dbi.withHandle(
            (Handle handle) -> {
              //              handle.execute("SET cust.code=:customerID", "bav");
              // multitenancy, see
              // https://varun-verma.medium.com/isolate-multi-tenant-data-in-postgresql-db-using-row-level-security-rls-bdd3089d9337
              // https://aws.amazon.com/de/blogs/database/multi-tenant-data-isolation-with-postgresql-row-level-security/
              // https://www.postgresql.org/docs/current/ddl-rowsecurity.html
              // https://www.postgresql.org/docs/current/sql-createpolicy.html

              return handle
                  .createQuery(sql)
                  .bindMap(argumentMappings)
                  .reduceRows(basicReduceRowsBiConsumer)
                  .collect(Collectors.toList());
            });
    return result;
  }

  public I retrieveOne(String fieldsSql, Filtering filtering, String sqlAdditionalJoins) {
    Map<String, Object> argumentMappings = new HashMap<>(0);
    return retrieveOne(fieldsSql, filtering, sqlAdditionalJoins, argumentMappings);
  }

  public I retrieveOne(
      String fieldsSql,
      Filtering filtering,
      String sqlAdditionalJoins,
      Map<String, Object> argumentMappings) {
    return retrieveOne(fieldsSql, filtering, sqlAdditionalJoins, argumentMappings, null);
  }

  public I retrieveOne(
      String fieldsSql,
      Filtering filtering,
      String sqlAdditionalJoins,
      Map<String, Object> argumentMappings,
      String innerSelect) {
    StringBuilder sql =
        new StringBuilder(
            "SELECT"
                + fieldsSql
                + ","
                + IdentifierRepositoryImpl.SQL_FULL_FIELDS_ID
                + ","
                + ImageFileResourceRepositoryImpl.SQL_PREVIEW_IMAGE_FIELDS_PI
                + ", "
                + UrlAliasRepositoryImpl.getSelectFields(true)
                + ", "
                + TagRepositoryImpl.SQL_REDUCED_FIELDS_TAGS
                + " FROM "
                + (StringUtils.hasText(innerSelect) ? innerSelect : tableName)
                + " AS "
                + tableAlias
                + (StringUtils.hasText(sqlAdditionalJoins)
                    ? " %s".formatted(sqlAdditionalJoins)
                    : "")
                + (StringUtils.hasText(getSqlSelectAllFieldsJoins())
                    ? " %s".formatted(getSqlSelectAllFieldsJoins())
                    : "")
                + (StringUtils.hasText(getSqlSelectReducedFieldsJoins())
                    ? " %s".formatted(getSqlSelectReducedFieldsJoins())
                    : "")
                + " LEFT JOIN "
                + IdentifierRepositoryImpl.TABLE_NAME
                + " AS "
                + IdentifierRepositoryImpl.TABLE_ALIAS
                + " ON "
                + tableAlias
                + ".uuid = "
                + IdentifierRepositoryImpl.TABLE_ALIAS
                + ".identifiable"
                + " LEFT JOIN "
                + ImageFileResourceRepositoryImpl.TABLE_NAME
                + " AS file ON "
                + tableAlias
                + ".previewfileresource = file.uuid"
                + " LEFT JOIN "
                + UrlAliasRepositoryImpl.TABLE_NAME
                + " AS "
                + UrlAliasRepositoryImpl.TABLE_ALIAS
                + " ON "
                + tableAlias
                + ".uuid = "
                + UrlAliasRepositoryImpl.TABLE_ALIAS
                + ".target_uuid"
                + UrlAliasRepositoryImpl.WEBSITESJOIN
                + String.format(
                    " LEFT JOIN %1$s %2$s ON %2$s.uuid = ANY(%3$s.tags_uuids) ",
                    TagRepositoryImpl.TABLE_NAME, TagRepositoryImpl.TABLE_ALIAS, tableAlias));

    if (argumentMappings == null) {
      argumentMappings = new HashMap<>(0);
    }
    addFiltering(filtering, sql, argumentMappings);

    Map<String, Object> bindMap = Map.copyOf(argumentMappings);
    I result =
        dbi.withHandle(
                h ->
                    h.createQuery(sql.toString())
                        .bindMap(bindMap)
                        .reduceRows(
                            (Map<UUID, I> map, RowView rowView) -> {
                              fullReduceRowsBiConsumer.accept(map, rowView);
                              additionalReduceRowsBiConsumer.accept(map, rowView);
                            }))
            .findFirst()
            .orElse(null);
    return result;
  }

  private void execInsertUpdate(
      final String sql, I identifiable, final Map<String, Object> bindings, boolean withCallback) {
    // because of a significant difference in execution duration it makes sense to distinguish here
    if (withCallback) {
      Map<String, Object> returnedFields =
          dbi.withHandle(
              h ->
                  h.createQuery(sql)
                      .bindMap(bindings)
                      .bindBean(identifiable)
                      .mapToMap()
                      .findOne()
                      .orElse(Collections.emptyMap()));
      insertUpdateCallback(identifiable, returnedFields);
    } else {
      dbi.withHandle(h -> h.createUpdate(sql).bindMap(bindings).bindBean(identifiable).execute());
    }
  }

  @Override
  public void save(I identifiable, Map<String, Object> bindings) throws RepositoryException {
    save(identifiable, bindings, null);
  }

  public void save(
      I identifiable,
      Map<String, Object> bindings,
      BiFunction<String, Map<String, Object>, String> sqlModifier) {
    if (bindings == null) {
      bindings = new HashMap<>(0);
    }
    // add preview image uuid
    final UUID previewImageUuid =
        identifiable.getPreviewImage() == null ? null : identifiable.getPreviewImage().getUuid();
    bindings.put("previewFileResource", previewImageUuid);
    // split label
    bindings.put("split_label", splitToArray(identifiable.getLabel()));
    bindings.put("tags_uuids", extractUuids(identifiable.getTags()));
    if (identifiable.getUuid() == null) {
      // in case of fileresource the uuid is created on binary upload (before metadata save)
      // to make saving on storage using uuid is possible
      identifiable.setUuid(UUID.randomUUID());
    }
    if (identifiable.getCreated() == null) {
      identifiable.setCreated(LocalDateTime.now());
    }
    if (identifiable.getLastModified() == null) {
      identifiable.setLastModified(LocalDateTime.now());
    }
    boolean hasReturningStmt = !getReturnedFieldsOnInsertUpdate().isEmpty();
    String sql =
        "INSERT INTO "
            + tableName
            + "("
            + getSqlInsertFields()
            + ") VALUES ("
            + getSqlInsertValues()
            + ")"
            + (hasReturningStmt
                ? " RETURNING " + String.join(", ", getReturnedFieldsOnInsertUpdate())
                : "");
    if (sqlModifier != null) {
      sql = sqlModifier.apply(sql, bindings);
    }
    execInsertUpdate(sql, identifiable, bindings, hasReturningStmt);
  }

  @Override
  public List<Entity> setRelatedEntities(UUID identifiableUuid, List<Entity> entities) {
    // as we store the whole list new: delete old entries
    dbi.withHandle(
        h ->
            h.createUpdate(
                    "DELETE FROM rel_identifiable_entities WHERE identifiable_uuid = :identifiableUuid")
                .bind("identifiableUuid", identifiableUuid)
                .execute());

    if (entities != null) {
      // we assume that the entities are already saved...
      dbi.useHandle(
          handle -> {
            PreparedBatch preparedBatch =
                handle.prepareBatch(
                    "INSERT INTO rel_identifiable_entities(identifiable_uuid, entity_uuid, sortIndex) VALUES(:identifiableUuid, :entityUuid, :sortIndex)");
            for (Entity entity : entities) {
              preparedBatch
                  .bind("identifiableUuid", identifiableUuid)
                  .bind("entityUuid", entity.getUuid())
                  .bind("sortIndex", getIndex(entities, entity))
                  .add();
            }
            preparedBatch.execute();
          });
    }
    return getRelatedEntities(identifiableUuid);
  }

  @Override
  public List<FileResource> setRelatedFileResources(
      UUID identifiableUuid, List<FileResource> fileResources) {
    if (fileResources == null) {
      return null;
    }
    // as we store the whole list new: delete old entries
    dbi.withHandle(
        h ->
            h.createUpdate(
                    "DELETE FROM rel_identifiable_fileresources WHERE identifiable_uuid = :identifiableUuid")
                .bind("identifiableUuid", identifiableUuid)
                .execute());

    dbi.useHandle(
        handle -> {
          PreparedBatch preparedBatch =
              handle.prepareBatch(
                  "INSERT INTO rel_entity_fileresources(identifiable_uuid, fileresource_uuid, sortIndex) VALUES(:identifiableUuid, :fileResourceUuid, :sortIndex)");
          for (FileResource fileResource : fileResources) {
            preparedBatch
                .bind("identifiableUuid", identifiableUuid)
                .bind("fileResourceUuid", fileResource.getUuid())
                .bind("sortIndex", getIndex(fileResources, fileResource))
                .add();
          }
          preparedBatch.execute();
        });
    return getRelatedFileResources(identifiableUuid);
  }

  @Override
  protected boolean supportsCaseSensitivityForProperty(String modelProperty) {
    switch (modelProperty) {
      case "label":
        return true;
      default:
        return false;
    }
  }

  @Override
  public void update(I identifiable, Map<String, Object> bindings) throws RepositoryException {
    update(identifiable, bindings, null);
  }

  public void update(
      I identifiable,
      Map<String, Object> bindings,
      BiFunction<String, Map<String, Object>, String> sqlModifier) {
    if (bindings == null) {
      bindings = new HashMap<>(0);
    }
    final UUID previewImageUuid =
        identifiable.getPreviewImage() == null ? null : identifiable.getPreviewImage().getUuid();
    bindings.put("previewFileResource", previewImageUuid);
    // split label
    bindings.put("split_label", splitToArray(identifiable.getLabel()));
    bindings.put("tags_uuids", extractUuids(identifiable.getTags()));

    identifiable.setLastModified(LocalDateTime.now());
    // do not update/left out from statement (not changed since insert):
    // uuid, created, identifiable_type, identifiable_objecttype, refid

    boolean hasReturningStmt = !getReturnedFieldsOnInsertUpdate().isEmpty();
    String sql =
        "UPDATE "
            + tableName
            + " SET"
            + getSqlUpdateFieldValues()
            + " WHERE uuid=:uuid"
            + (hasReturningStmt
                ? " RETURNING " + String.join(", ", getReturnedFieldsOnInsertUpdate())
                : "");

    if (sqlModifier != null) {
      sql = sqlModifier.apply(sql, bindings);
    }
    execInsertUpdate(sql, identifiable, bindings, hasReturningStmt);
  }
}
