package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable;

import static de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifierRepositoryImpl.SQL_FULL_FIELDS_ID;
import static de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource.FileResourceMetadataRepositoryImpl.SQL_PREVIEW_IMAGE_FIELDS_PI;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.JdbiRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.alias.UrlAliasRepositoryImpl;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.filter.FilterValuePlaceholder;
import de.digitalcollections.model.filter.Filtering;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.paging.Direction;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import de.digitalcollections.model.paging.Sorting;
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
import java.util.Set;
import java.util.UUID;
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
public class IdentifiableRepositoryImpl<I extends Identifiable> extends JdbiRepositoryImpl
    implements IdentifiableRepository<I> {

  private static final Logger LOGGER = LoggerFactory.getLogger(IdentifiableRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "idf";
  public static final String TABLE_ALIAS = "i";
  public static final String TABLE_NAME = "identifiables";

  public static String getSqlInsertFields() {
    return " uuid, created, description, identifiable_type, label, last_modified, previewfileresource, preview_hints";
  }

  /* Do not change order! Must match order in getSqlInsertFields!!! */
  public static String getSqlInsertValues() {
    return " :uuid, :created, :description::JSONB, :type, :label::JSONB, :lastModified, :previewFileResource, :previewImageRenderingHints::JSONB";
  }

  public static String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    return getSqlSelectReducedFields(tableAlias, mappingPrefix);
  }

  public static String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
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

  public static String getSqlUpdateFieldValues() {
    // do not update/left out from statement (not changed since insert):
    // uuid, created, identifiable_type
    return " description=:description::JSONB, label=:label::JSONB, last_modified=:lastModified, previewfileresource=:previewFileResource, preview_hints=:previewImageRenderingHints::JSONB";
  }

  /* BiFunction for reducing rows (related objects) of joins not already part of identifiable (Identifier, preview image ImageFileResource). */
  public BiFunction<Map<UUID, I>, RowView, Map<UUID, I>> additionalReduceRowsBiFunction =
      (map, rowView) -> {
        return map;
      };
  public final BiFunction<Map<UUID, I>, RowView, Map<UUID, I>> basicReduceRowsBiFunction;
  public final BiFunction<Map<UUID, I>, RowView, Map<UUID, I>> fullReduceRowsBiFunction;
  protected final Class identifiableImplClass;
  protected final IdentifierRepository identifierRepository;
  private final String sqlInsertFields;
  private final String sqlInsertValues;
  protected String sqlSelectAllFields;
  protected final String sqlSelectAllFieldsJoins;
  protected String sqlSelectReducedFields;
  private final String sqlUpdateFieldValues;

  @Autowired
  protected IdentifiableRepositoryImpl(Jdbi dbi, IdentifierRepository identifierRepository) {
    this(
        dbi,
        identifierRepository,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        Identifiable.class,
        getSqlSelectAllFields(TABLE_ALIAS, MAPPING_PREFIX),
        getSqlSelectReducedFields(TABLE_ALIAS, MAPPING_PREFIX),
        getSqlInsertFields(),
        getSqlInsertValues(),
        getSqlUpdateFieldValues());
  }

  protected IdentifiableRepositoryImpl(
      Jdbi dbi,
      IdentifierRepository identifierRepository,
      String tableName,
      String tableAlias,
      String mappingPrefix,
      Class identifiableImplClass,
      String sqlSelectAllFields,
      String sqlSelectReducedFields,
      String sqlInsertFields,
      String sqlInsertValues,
      String sqlUpdateFieldValues) {
    this(
        dbi,
        identifierRepository,
        tableName,
        tableAlias,
        mappingPrefix,
        identifiableImplClass,
        sqlSelectAllFields,
        sqlSelectReducedFields,
        sqlInsertFields,
        sqlInsertValues,
        sqlUpdateFieldValues,
        null);
  }

  protected IdentifiableRepositoryImpl(
      Jdbi dbi,
      IdentifierRepository identifierRepository,
      String tableName,
      String tableAlias,
      String mappingPrefix,
      Class identifiableImplClass,
      String sqlSelectAllFields,
      String sqlSelectReducedFields,
      String sqlInsertFields,
      String sqlInsertValues,
      String sqlUpdateFieldValues,
      String sqlSelectAllFieldsJoins) {
    this(
        dbi,
        identifierRepository,
        tableName,
        tableAlias,
        mappingPrefix,
        identifiableImplClass,
        sqlSelectAllFields,
        sqlSelectReducedFields,
        sqlInsertFields,
        sqlInsertValues,
        sqlUpdateFieldValues,
        sqlSelectAllFieldsJoins,
        null);
  }

  protected IdentifiableRepositoryImpl(
      Jdbi dbi,
      IdentifierRepository identifierRepository,
      String tableName,
      String tableAlias,
      String mappingPrefix,
      Class identifiableImplClass,
      String sqlSelectAllFields,
      String sqlSelectReducedFields,
      String sqlInsertFields,
      String sqlInsertValues,
      String sqlUpdateFieldValues,
      String sqlSelectAllFieldsJoins,
      BiFunction<Map<UUID, I>, RowView, Map<UUID, I>> additionalReduceRowsBiFunction) {
    super(dbi, tableName, tableAlias, mappingPrefix);

    // register row mapper for given class and mapping prefix
    // (until now everywhere BeanMapper.factory... was used. If this changes, row mapper
    // registration may be moved back into each repository impl?)
    dbi.registerRowMapper(BeanMapper.factory(identifiableImplClass, mappingPrefix));
    dbi.registerRowMapper(
        BeanMapper.factory(UrlAlias.class, UrlAliasRepositoryImpl.MAPPING_PREFIX));

    // set basic reduce rows bifunction for reduced selects (lists, paging)
    // note: it turned out, that we also want identifiers and previewimage for reduced selects. So
    // currently there is no difference to full.
    this.basicReduceRowsBiFunction = createReduceRowsBiFunction(true, true);

    // set full reduce rows bifunction for full selects (find one)
    this.fullReduceRowsBiFunction = createReduceRowsBiFunction(true, true);

    // for detailes select (only used in find one, not lists): if additional objects should be
    // "joined" into instance, set bi function for doing this:
    if (additionalReduceRowsBiFunction != null) {
      this.additionalReduceRowsBiFunction = additionalReduceRowsBiFunction;
    }

    this.identifiableImplClass = identifiableImplClass;
    this.identifierRepository = identifierRepository;
    this.sqlInsertFields = sqlInsertFields;
    this.sqlInsertValues = sqlInsertValues;
    this.sqlSelectAllFields = sqlSelectAllFields;
    this.sqlSelectAllFieldsJoins = sqlSelectAllFieldsJoins;
    this.sqlSelectReducedFields = sqlSelectReducedFields;
    this.sqlUpdateFieldValues = sqlUpdateFieldValues;
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

  private BiFunction<Map<UUID, I>, RowView, Map<UUID, I>> createReduceRowsBiFunction(
      boolean withIdentifiers, boolean withPreviewImage) {
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
        Website website =
            new Website(
                rowView.getColumn(UrlAliasRepositoryImpl.WEBSITESALIAS + "_url", URL.class));
        website.setUuid(
            rowView.getColumn(UrlAliasRepositoryImpl.WEBSITESALIAS + "_uuid", UUID.class));
        website.setLabel(
            rowView.getColumn(
                UrlAliasRepositoryImpl.WEBSITESALIAS + "_label", LocalizedText.class));
        urlAlias.setWebsite(website);
        if (identifiable.getLocalizedUrlAliases() == null) {
          identifiable.setLocalizedUrlAliases(new LocalizedUrlAliases(urlAlias));
        } else {
          identifiable.getLocalizedUrlAliases().add(urlAlias);
        }
      }
      return map;
    };
  }

  @Override
  public boolean delete(List<UUID> uuids) {
    // delete related data
    uuids.stream()
        .forEach(
            (u) -> {
              deleteIdentifiers(u);
            });

    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM " + tableName + " WHERE uuid in (<uuids>)")
                .bindList("uuids", uuids)
                .execute());
    return true;
  }

  @Override
  public boolean deleteIdentifiers(UUID identifiableUuid) {
    I identifiable = findOne(identifiableUuid);
    if (identifiable == null) {
      return false;
    }

    identifierRepository.delete(
        identifiable.getIdentifiers().stream()
            .map(Identifier::getUuid)
            .collect(Collectors.toList()));

    return true;
  }

  /**
   * Escape characters that must not appear in jsonpath inner strings.
   *
   * <p>This method should always be used to clean up strings, e.g. search terms, that are intended
   * to appear in an jsonpath inner string, i.e. between double quotes. If the inserted term
   * contains double quotes then the jsonpath breaks. Hence we remove double quotes at start and end
   * of the provided string (they do not have any meaning for the search at all) and escape the
   * remaining ones with a backslash.
   *
   * @param term, can be null
   * @return term with forbidden characters removed or escaped
   */
  protected String escapeTermForJsonpath(String term) {
    if (term == null) {
      return null;
    }
    if (term.startsWith("\"") && term.endsWith("\"")) {
      // 1st step: remove useless surrounding quotes
      term = term.replaceAll("^\"(.+)\"$", "$1");
    }
    if (term.contains("\"")) {
      // 2nd step: escape remaining double quotes; yes, looks ugly...
      term = term.replaceAll("\"", "\\\\\"");
    }
    return term;
  }

  @Override
  public PageResponse<I> find(PageRequest pageRequest) {
    return find(pageRequest, null, null);
  }

  protected PageResponse<I> find(
      PageRequest pageRequest, String commonSql, Map<String, Object> argumentMappings) {
    if (commonSql == null) {
      commonSql = " FROM " + tableName + " AS " + tableAlias;
    }
    StringBuilder innerQuery = new StringBuilder("SELECT *" + commonSql);
    addFiltering(pageRequest, innerQuery);
    addPageRequestParams(pageRequest, innerQuery);

    String orderBy = getOrderBy(pageRequest.getSorting());
    if (StringUtils.hasText(orderBy)) {
      orderBy = " ORDER BY " + orderBy;
    }
    List<I> result = retrieveList(sqlSelectReducedFields, innerQuery, argumentMappings, orderBy);

    StringBuilder sqlCount = new StringBuilder("SELECT count(*)" + commonSql);
    addFiltering(pageRequest, sqlCount);
    long total = retrieveCount(sqlCount, argumentMappings);

    return new PageResponse<>(result, pageRequest, total);
  }

  @Override
  public SearchPageResponse<I> find(SearchPageRequest searchPageRequest) {
    String commonSql = " FROM " + tableName + " AS " + tableAlias;
    String searchTerm = searchPageRequest.getQuery();
    if (!StringUtils.hasText(searchTerm)) {
      return find(searchPageRequest, commonSql, Collections.EMPTY_MAP);
    }

    commonSql += " WHERE " + getCommonSearchSql(tableAlias);
    return find(
        searchPageRequest, commonSql, Map.of("searchTerm", this.escapeTermForJsonpath(searchTerm)));
  }

  protected SearchPageResponse<I> find(
      SearchPageRequest searchPageRequest, String commonSql, Map<String, Object> argumentMappings) {
    StringBuilder innerQuery = new StringBuilder("SELECT " + tableAlias + ".*" + commonSql);
    addFiltering(searchPageRequest, innerQuery);
    addPageRequestParams(searchPageRequest, innerQuery);
    String orderBy = getOrderBy(searchPageRequest.getSorting());
    if (StringUtils.hasText(orderBy)) {
      orderBy = " ORDER BY " + orderBy;
    }
    List<I> result = retrieveList(sqlSelectReducedFields, innerQuery, argumentMappings, orderBy);

    StringBuilder countQuery =
        new StringBuilder("SELECT count(" + tableAlias + ".uuid)" + commonSql);
    addFiltering(searchPageRequest, countQuery);
    long total = retrieveCount(countQuery, argumentMappings);

    return new SearchPageResponse<>(result, searchPageRequest, total);
  }

  @Override
  public List<I> findAllFull() {
    return retrieveList(sqlSelectAllFields, null, null, null);
  }

  @Override
  public List<I> findAllReduced() {
    return retrieveList(sqlSelectReducedFields, null, null, null);
  }

  @Override
  @Deprecated
  /** @deprecated use method with SearchPageRequest signature instead */
  public PageResponse<I> findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial) {
    // add special filter
    Filtering filtering = pageRequest.getFiltering();
    if (filtering == null) {
      filtering = Filtering.defaultBuilder().build();
      pageRequest.setFiltering(filtering);
    }
    // TODO: test if binding works (because of single quotes done by filter expandion) or we have to
    // put here values direktly, not passing Map.of....
    Filtering initialFiltering =
        Filtering.defaultBuilder()
            .filter(tableAlias + ".label ->> :language")
            .startsWith(":initial")
            .build();
    filtering.add(initialFiltering);

    // add special ordering
    Sorting sorting = pageRequest.getSorting();
    if (sorting == null) {
      sorting = Sorting.defaultBuilder().build();
      pageRequest.setSorting(sorting);
    }
    Sorting.defaultBuilder()
        .order(
            Order.defaultBuilder()
                .property("label")
                .subProperty(language)
                .direction(Direction.ASC)
                .build());
    sorting.and(sorting);

    return this.find(pageRequest, null, Map.of("language", language, "initial", initial));
  }

  @Override
  public I findOne(UUID uuid, Filtering filtering) {
    if (filtering == null) {
      filtering = Filtering.defaultBuilder().build();
    }
    filtering.add(
        Filtering.defaultBuilder()
            .filter("uuid")
            .isEquals(new FilterValuePlaceholder(":uuid"))
            .build());

    I result =
        retrieveOne(sqlSelectAllFields, sqlSelectAllFieldsJoins, filtering, Map.of("uuid", uuid));

    return result;
  }

  @Override
  public I findOne(Identifier identifier) {
    if (identifier.getIdentifiable() != null) {
      return findOne(identifier.getIdentifiable());
    }

    String namespace = identifier.getNamespace();
    String identifierId = identifier.getId();

    Filtering filtering =
        Filtering.defaultBuilder()
            .filter("id.identifier")
            .isEquals(new FilterValuePlaceholder(":id"))
            .filter("id.namespace")
            .isEquals(new FilterValuePlaceholder(":namespace"))
            .build();

    I result =
        retrieveOne(
            sqlSelectAllFields,
            sqlSelectAllFieldsJoins,
            filtering,
            Map.of("id", identifierId, "namespace", namespace));

    return result;
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    return new ArrayList<>(Arrays.asList("created", "label", "lastModified", "type"));
  }

  @Override
  protected String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "created":
        return tableAlias + ".created";
      case "description":
        return tableAlias + ".description";
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

  protected String getCommonSearchSql(String tblAlias) {
    // FYI: [JSON Path
    // Functions](https://www.postgresql.org/docs/12/functions-json.html#FUNCTIONS-SQLJSON-PATH)
    // and [Data type](https://www.postgresql.org/docs/12/datatype-json.html#DATATYPE-JSONPATH)
    return "("
        + "jsonb_path_exists("
        + tblAlias
        // To insert `:searchTerm` into the `jsonpath` we must split it up;
        // the cast is necessary otherwise Postgres does not recognise it as `jsonpath` (that is
        // just a string practically).
        // Finds (case insensitively) labels that contain the search term, see `like_regex`
        // example in
        // https://www.postgresql.org/docs/12/functions-json.html#FUNCTIONS-SQLJSON-PATH
        + ".label, ('$.* ? (@ like_regex \"' || :searchTerm || '\" flag \"iq\")')::jsonpath)"
        + " OR "
        + "jsonb_path_exists("
        + tblAlias
        + ".description, ('$.* ? (@ like_regex \"' || :searchTerm || '\" flag \"iq\")')::jsonpath))";
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
        "SELECT DISTINCT languages FROM "
            + tableName
            + " AS "
            + tableAlias
            + ", jsonb_object_keys("
            + tableAlias
            + ".label) AS languages";
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
                    .map(Entity.class::cast)
                    .list());
    return list;
  }

  @Override
  public List<FileResource> getRelatedFileResources(UUID identifiableUuid) {
    String query =
        "SELECT * FROM fileresources f"
            + " INNER JOIN rel_identifiable_fileresources ref ON f.uuid=ref.fileresource_uuid"
            + " WHERE ref.identifiableUuid = :identifiableUuid"
            + " ORDER BY ref.sortindex";

    List<FileResource> result =
        dbi.withHandle(
            h ->
                h.createQuery(query)
                    .bind("identifiableUuid", identifiableUuid)
                    .mapToBean(FileResource.class)
                    .map(FileResource.class::cast)
                    .list());
    return result;
  }

  public String getSqlSelectAllFields() {
    return sqlSelectAllFields;
  }

  public String getSqlSelectReducedFields() {
    return sqlSelectReducedFields;
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

  public List<I> retrieveList(
      String fieldsSql,
      StringBuilder innerQuery,
      final Map<String, Object> argumentMappings,
      String orderBy) {
    final String urlAliasName = UrlAliasRepositoryImpl.TABLE_NAME;
    final String urlAliasAlias = UrlAliasRepositoryImpl.TABLE_ALIAS;
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
            + (sqlSelectAllFieldsJoins != null ? sqlSelectAllFieldsJoins : "")
            + " LEFT JOIN identifiers AS id ON "
            + tableAlias
            + ".uuid = id.identifiable"
            + " LEFT JOIN fileresources_image AS file ON "
            + tableAlias
            + ".previewfileresource = file.uuid"
            + " LEFT JOIN "
            + urlAliasName
            + " AS "
            + urlAliasAlias
            + " ON "
            + this.tableAlias
            + ".uuid = "
            + urlAliasAlias
            + ".target_uuid"
            + UrlAliasRepositoryImpl.WEBSITESJOIN
            + (orderBy != null ? " " + orderBy : "");

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
                  .reduceRows(
                      (Map<UUID, I> map, RowView rowView) -> {
                        basicReduceRowsBiFunction.apply(map, rowView);
                      })
                  .collect(Collectors.toList());
            });
    return result;
  }

  protected Integer retrieveNextSortIndexForParentChildren(
      Jdbi dbi, String tableName, String columNameParentUuid, UUID parentUuid) {
    // first child: max gets no results (= null)):
    Integer sortIndex =
        dbi.withHandle(
            (Handle h) ->
                h.createQuery(
                        "SELECT MAX(sortIndex) + 1 FROM "
                            + tableName
                            + " WHERE "
                            + columNameParentUuid
                            + " = :parent_uuid")
                    .bind("parent_uuid", parentUuid)
                    .mapTo(Integer.class)
                    .findOne()
                    .orElse(null));
    if (sortIndex == null) {
      return 0;
    }
    return sortIndex;
  }

  public I retrieveOne(
      String fieldsSql,
      String sqlSelectAllFieldsJoins,
      Filtering filtering,
      final Map<String, Object> argumentMappings) {
    final String urlAliasName = UrlAliasRepositoryImpl.TABLE_NAME;
    final String urlAliasAlias = UrlAliasRepositoryImpl.TABLE_ALIAS;
    StringBuilder sql =
        new StringBuilder(
            "SELECT"
                + fieldsSql
                + ","
                + SQL_FULL_FIELDS_ID
                + ","
                + SQL_PREVIEW_IMAGE_FIELDS_PI
                + ", "
                + UrlAliasRepositoryImpl.getSelectFields(true)
                + " FROM "
                + tableName
                + " AS "
                + tableAlias
                + (sqlSelectAllFieldsJoins != null ? sqlSelectAllFieldsJoins : "")
                + " LEFT JOIN identifiers AS id ON "
                + tableAlias
                + ".uuid = id.identifiable"
                + " LEFT JOIN fileresources_image AS file ON "
                + tableAlias
                + ".previewfileresource = file.uuid"
                + " LEFT JOIN "
                + urlAliasName
                + " AS "
                + urlAliasAlias
                + " ON "
                + this.tableAlias
                + ".uuid = "
                + urlAliasAlias
                + ".target_uuid"
                + UrlAliasRepositoryImpl.WEBSITESJOIN);
    addFiltering(filtering, sql);

    I result =
        dbi.withHandle(
                h ->
                    h.createQuery(sql.toString())
                        .bindMap(argumentMappings)
                        .reduceRows(
                            (Map<UUID, I> map, RowView rowView) -> {
                              fullReduceRowsBiFunction.apply(map, rowView);
                              additionalReduceRowsBiFunction.apply(map, rowView);
                            }))
            .findFirst()
            .orElse(null);
    return result;
  }

  @Override
  public I save(I identifiable, Map<String, Object> bindings) {
    if (bindings == null) {
      bindings = new HashMap<>();
    }
    // add preview image uuid
    final UUID previewImageUuid =
        identifiable.getPreviewImage() == null ? null : identifiable.getPreviewImage().getUuid();
    bindings.put("previewFileResource", previewImageUuid);
    final Map<String, Object> finalBindings = new HashMap<>(bindings);

    if (identifiable.getUuid() == null) {
      // in case of fileresource the uuid is created on binary upload (before metadata save)
      // to make saving on storage using uuid is possible
      identifiable.setUuid(UUID.randomUUID());
    }
    identifiable.setCreated(LocalDateTime.now());
    identifiable.setLastModified(LocalDateTime.now());

    final String sql =
        "INSERT INTO " + tableName + "(" + sqlInsertFields + ") VALUES (" + sqlInsertValues + ")";

    dbi.withHandle(
        h -> h.createUpdate(sql).bindMap(finalBindings).bindBean(identifiable).execute());

    // save identifiers
    Set<Identifier> identifiers = identifiable.getIdentifiers();
    saveIdentifiers(identifiers, identifiable);

    return identifiable;
  }

  public void saveIdentifiers(Set<Identifier> identifiers, Identifiable identifiable) {
    // we assume that identifiers (unique to object) are new (existing ones were deleted before
    // (e.g. see update))
    if (identifiers != null) {
      for (Identifier identifier : identifiers) {
        identifier.setIdentifiable(identifiable.getUuid());
        identifierRepository.save(identifier);
      }
    }
  }

  @Override
  public List<Entity> saveRelatedEntities(UUID identifiableUuid, List<Entity> entities) {
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
  public List<FileResource> saveRelatedFileResources(
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
  public I update(I identifiable, Map<String, Object> bindings) {
    if (bindings == null) {
      bindings = new HashMap<>();
    }
    final UUID previewImageUuid =
        identifiable.getPreviewImage() == null ? null : identifiable.getPreviewImage().getUuid();
    bindings.put("previewFileResource", previewImageUuid);
    final Map<String, Object> finalBindings = new HashMap<>(bindings);

    identifiable.setLastModified(LocalDateTime.now());
    // do not update/left out from statement (not changed since insert):
    // uuid, created, identifiable_type, entity_type, refid

    final String sql = "UPDATE " + tableName + " SET" + sqlUpdateFieldValues + " WHERE uuid=:uuid";

    dbi.withHandle(
        h -> h.createUpdate(sql).bindMap(finalBindings).bindBean(identifiable).execute());

    // save identifiers
    // as we store the whole list new: delete old entries
    identifierRepository.deleteByIdentifiable(identifiable);
    Set<Identifier> identifiers = identifiable.getIdentifiers();
    saveIdentifiers(identifiers, identifiable);

    return identifiable;
  }
}
