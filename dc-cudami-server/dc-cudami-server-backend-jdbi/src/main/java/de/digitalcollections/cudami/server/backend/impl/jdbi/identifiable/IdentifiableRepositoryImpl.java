package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable;

import static de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource.FileResourceMetadataRepositoryImpl.SQL_PREVIEW_IMAGE_FIELDS_PI;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.UniqueObjectRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.alias.UrlAliasRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.semantic.SubjectRepositoryImpl;
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
import de.digitalcollections.model.semantic.Subject;
import de.digitalcollections.model.semantic.Tag;
import de.digitalcollections.model.text.LocalizedText;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
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

  @Autowired protected IdentifierRepositoryImpl identifierRepositoryImpl;

  @Override
  protected String getSqlInsertFields() {
    return super.getSqlInsertFields()
        + ", "
        + """
        description, identifiable_objecttype,
        identifiable_type, label, previewfileresource,
        preview_hints, split_label, tags_uuids, subjects_uuids
        """;
  }

  @Override
  protected String getSqlInsertValues() {
    return super.getSqlInsertValues()
        + ", "
        + """
        :description::JSONB, :identifiableObjectType,
        :type, :label::JSONB, :previewFileResource,
        :previewImageRenderingHints::JSONB, :split_label::TEXT[], :tags_uuids::UUID[], :subjects_uuids::UUID[]
        """;
  }

  @Override
  public String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    return super.getSqlSelectAllFields(tableAlias, mappingPrefix)
        + ", "
        + getSqlSelectReducedFields(tableAlias, mappingPrefix);
  }

  @Override
  protected String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return super.getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + ", "
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
        + ".preview_hints "
        + mappingPrefix
        + "_previewImageRenderingHints";
  }

  @Override
  public String getSqlUpdateFieldValues() {
    // do not update/left out from statement (not changed since insert):
    // identifiable_type
    return super.getSqlUpdateFieldValues()
        + ", description=:description::JSONB, label=:label::JSONB, previewfileresource=:previewFileResource, "
        + "preview_hints=:previewImageRenderingHints::JSONB, split_label=:split_label::TEXT[]"
        + ", tags_uuids=:tags_uuids::UUID[], subjects_uuids=:subjects_uuids::UUID[]";
  }

  @Autowired
  protected IdentifiableRepositoryImpl(Jdbi dbi, CudamiConfig cudamiConfig) {
    super(
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
    super(
        dbi,
        tableName,
        tableAlias,
        mappingPrefix,
        identifiableImplClass,
        offsetForAlternativePaging);

    // register row mapper for given class and mapping prefix
    // (until now everywhere BeanMapper.factory... was used. If this changes, row
    // mapper registration may be moved back into each repository impl?)
    dbi.registerRowMapper(
        BeanMapper.factory(UrlAlias.class, UrlAliasRepositoryImpl.MAPPING_PREFIX));
  }

  protected String addCrossTablePagingAndSorting(
      PageRequest pageRequest, StringBuilder innerQuery, final String crossTableAlias) {
    String orderBy = getOrderBy(pageRequest.getSorting());
    if (!StringUtils.hasText(orderBy)) {
      orderBy = "ORDER BY idx ASC";
      innerQuery.append(
          " ORDER BY " + crossTableAlias + ".sortindex"); // must be the column itself to use window
      // functions
    }
    addPagingAndSorting(pageRequest, innerQuery);
    return orderBy;
  }

  @Override
  public void addRelatedEntity(UUID identifiableUuid, UUID entityUuid) throws RepositoryException {
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
  public void addRelatedFileresource(UUID identifiableUuid, UUID fileResourceUuid)
      throws RepositoryException {
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
  protected BiConsumer<Map<UUID, I>, RowView> createAdditionalReduceRowsBiConsumer() {
    if (additionalReduceRowsBiConsumer == null) {
      return super.createAdditionalReduceRowsBiConsumer();
    }
    return additionalReduceRowsBiConsumer;
  }

  // FIXME delete
  // @Override
  // protected String addSearchTermMappings(String searchTerm, Map<String, Object>
  // argumentMappings) {
  // argumentMappings.put(SearchTermTemplates.ARRAY_CONTAINS.placeholder,
  // splitToArray(searchTerm));
  // return super.addSearchTermMappings(searchTerm, argumentMappings);
  // }

  @Override
  protected BiConsumer<Map<UUID, I>, RowView> createBasicReduceRowsBiConsumer() {
    return (map, rowView) -> {
      I identifiable =
          map.computeIfAbsent(
              rowView.getColumn(mappingPrefix + "_uuid", UUID.class),
              fn -> {
                return (I) rowView.getRow(uniqueObjectImplClass);
              });

      setPreviewImageFromRowView(rowView, identifiable);
      setIdentifiersFromRowView(rowView, identifiable);
      setLocalizedUrlAliasesFromRowView(rowView, identifiable);

      extendReducedIdentifiable(identifiable, rowView);
    };
  }

  private void setLocalizedUrlAliasesFromRowView(RowView rowView, I identifiable) {
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
  }

  private void setIdentifiersFromRowView(RowView rowView, I identifiable) {
    if (rowView.getColumn("id_uuid", UUID.class) != null) {
      Identifier dbIdentifier = rowView.getRow(Identifier.class);
      identifiable.addIdentifier(dbIdentifier);
    }
  }

  private void setPreviewImageFromRowView(RowView rowView, I identifiable) {
    if (rowView.getColumn("pi_uuid", UUID.class) != null) {
      // see definition in
      // FileResourceMetadataRepositoryimpl.SQL_PREVIEW_IMAGE_FIELDS_PI:
      // file.uuid pi_uuid, file.filename pi_filename, file.mimetype pi_mimeType,
      // file.uri pi_uri, file.http_base_url pi_httpBaseUrl

      // TODO workaround as long at is not possible to register two RowMappers for one
      // type
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
  }

  protected BiConsumer<Map<UUID, I>, RowView> createFullReduceRowsBiConcumer() {
    return (map, rowView) -> {
      I identifiable =
          map.computeIfAbsent(
              rowView.getColumn(mappingPrefix + "_uuid", UUID.class),
              fn -> {
                return (I) rowView.getRow(uniqueObjectImplClass);
              });

      setPreviewImageFromRowView(rowView, identifiable);
      setIdentifiersFromRowView(rowView, identifiable);
      setLocalizedUrlAliasesFromRowView(rowView, identifiable);

      setTagsFromRowView(rowView, identifiable);
      setSubjectsFromRowView(rowView, identifiable);

      extendReducedIdentifiable(identifiable, rowView);
    };
  }

  private void setSubjectsFromRowView(RowView rowView, I identifiable) {
    UUID subjectUuid =
        rowView.getColumn(SubjectRepositoryImpl.MAPPING_PREFIX + "_uuid", UUID.class);
    if (subjectUuid != null
        && (identifiable.getSubjects() == null
            || !identifiable.getSubjects().stream()
                .anyMatch(subj -> Objects.equals(subj.getUuid(), subjectUuid)))) {
      Subject subject = rowView.getRow(Subject.class);
      identifiable.addSubject(subject);
    }
  }

  private void setTagsFromRowView(RowView rowView, I identifiable) {
    if (rowView.getColumn(TagRepositoryImpl.MAPPING_PREFIX + "_uuid", UUID.class) != null) {
      Tag tag = rowView.getRow(Tag.class);
      if (tag != null) {
        identifiable.addTag(tag);
      }
    }
  }

  @Override
  public I create() throws RepositoryException {
    return (I) new Identifiable();
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

  @Override
  protected PageResponse<I> find(
      PageRequest pageRequest, String commonSql, Map<String, Object> argumentMappings)
      throws RepositoryException {
    PageResponse<I> pageResponse = super.find(pageRequest, commonSql, argumentMappings);

    // FIXME: try to avoid doing this after database select! Delete when jsonb
    // search without
    // split-field implemented
    filterByLocalizedTextFields(pageRequest, pageResponse, getJsonbFields());

    return pageResponse;
  }

  @Override
  @Deprecated
  /**
   * @deprecated use method with PageRequest signature instead
   */
  public PageResponse<I> findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial) throws RepositoryException {
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
  public PageResponse<Entity> findRelatedEntities(UUID identifiableUuid, PageRequest pageRequest)
      throws RepositoryException {
    StringBuilder commonSql =
        new StringBuilder(
            " FROM entities e"
                + " INNER JOIN rel_identifiable_entities rel ON e.uuid=rel.entity_uuid"
                + " WHERE rel.identifiable_uuid = :identifiableUuid");
    Map<String, Object> argumentMappings = new HashMap<>(0);
    argumentMappings.put("identifiableUuid", identifiableUuid);
    addFiltering(pageRequest, commonSql, argumentMappings);

    StringBuilder query = new StringBuilder("SELECT rel.sortindex AS idx, *" + commonSql);
    pageRequest.setSorting(new Sorting(new Order(Direction.ASC, "idx")));
    addPagingAndSorting(pageRequest, query);
    List<Entity> list =
        dbi.withHandle(h -> h.createQuery(query.toString()).mapToBean(Entity.class).list());

    StringBuilder countQuery = new StringBuilder("SELECT count(*)" + commonSql);
    long total = retrieveCount(countQuery, argumentMappings);

    PageResponse<Entity> pageResponse = new PageResponse<>(list, pageRequest, total);
    return pageResponse;
  }

  @Override
  public PageResponse<FileResource> findRelatedFileResources(
      UUID identifiableUuid, PageRequest pageRequest) throws RepositoryException {
    StringBuilder commonSql =
        new StringBuilder(
            " FROM FROM fileresources f"
                + " INNER JOIN rel_identifiable_fileresources rel ON f.uuid=rel.fileresource_uuid"
                + " WHERE rel.identifiable_uuid = :identifiableUuid");
    Map<String, Object> argumentMappings = new HashMap<>(0);
    argumentMappings.put("identifiableUuid", identifiableUuid);
    addFiltering(pageRequest, commonSql, argumentMappings);

    StringBuilder query = new StringBuilder("SELECT rel.sortindex AS idx, *" + commonSql);
    pageRequest.setSorting(new Sorting(new Order(Direction.ASC, "idx")));
    addPagingAndSorting(pageRequest, query);
    List<FileResource> list =
        dbi.withHandle(h -> h.createQuery(query.toString()).mapToBean(FileResource.class).list());

    StringBuilder countQuery = new StringBuilder("SELECT count(*)" + commonSql);
    long total = retrieveCount(countQuery, argumentMappings);

    PageResponse<FileResource> pageResponse = new PageResponse<>(list, pageRequest, total);
    return pageResponse;
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    List<String> allowedOrderByFields = super.getAllowedOrderByFields();
    allowedOrderByFields.addAll(Arrays.asList("identifiableObjectType", "label", "type"));
    return allowedOrderByFields;
  }

  @Override
  public I getByIdentifier(String namespace, String identifierId) throws RepositoryException {
    UUID identifiableUuid =
        dbi.withHandle(
            h ->
                h.createQuery(
                        """
        SELECT identifiable FROM identifiers
        WHERE namespace = :namespace
          AND identifier = :id;
        """) /* affords index only scan on "unique_namespace_identifier" (V14.04.00) */
                    .bind("namespace", namespace)
                    .bind("id", identifierId)
                    .mapTo(UUID.class)
                    .findOne()
                    .orElse(null));

    if (identifiableUuid == null) return null;

    return getByUuid(identifiableUuid);
  }

  @Override
  public String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "description":
        return tableAlias + ".description";
      case "identifiableObjectType":
        return tableAlias + ".identifiable_objecttype";
      case "label":
        return tableAlias + ".label";
      case "previewImage":
        return tableAlias + ".previewfileresource";
      case "type":
        return tableAlias + ".identifiable_type";
      default:
        return super.getColumnName(modelProperty);
    }
  }

  @Override
  protected LinkedHashMap<String, Function<I, Optional<Object>>> getJsonbFields() {
    LinkedHashMap<String, Function<I, Optional<Object>>> jsonbFields = super.getJsonbFields();
    jsonbFields.put("description", i -> Optional.ofNullable(i.getDescription()));
    jsonbFields.put("label", i -> Optional.ofNullable(i.getLabel()));
    jsonbFields.put(
        "previewImageRenderingHints", i -> Optional.ofNullable(i.getPreviewImageRenderingHints()));
    return jsonbFields;
  }

  @Override
  public List<Locale> getLanguages() throws RepositoryException {
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
  public List<I> getRandom(int count) throws RepositoryException {
    throw new UnsupportedOperationException(); // TODO: not yet implemented
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

  // FIXME: delete when proper jsonb contains search implemented
  protected boolean hasSplitColumn(String propertyName) {
    // only label for now
    return "label".equals(propertyName);
  }

  @Override
  /**
   * Override super.retrieveList because of always joining identifiers, preview image and url
   * aliases for {@Identifiable}.
   */
  protected List<I> retrieveList(
      String fieldsSql,
      String fieldsSqlAdditionalJoins,
      StringBuilder innerQuery,
      final Map<String, Object> argumentMappings,
      String orderBy)
      throws RepositoryException {
    final String sql =
        "SELECT "
            + fieldsSql
            + ","
            + identifierRepositoryImpl.getSqlSelectAllFields(
                IdentifierRepositoryImpl.TABLE_ALIAS, IdentifierRepositoryImpl.MAPPING_PREFIX)
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
              // handle.execute("SET cust.code=:customerID", "bav");
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

  @Override
  /**
   * Override super.retrieveOne because of always joining identifiers, preview image, url aliases,
   * tags and subjects for {@Identifiable}.
   */
  public I retrieveOne(
      String fieldsSql,
      Filtering filtering,
      String sqlAdditionalJoins,
      Map<String, Object> argumentMappings,
      String innerSelect)
      throws RepositoryException {
    StringBuilder sql =
        new StringBuilder(
            "SELECT"
                + fieldsSql
                + ","
                + identifierRepositoryImpl.getSqlSelectAllFields(
                    IdentifierRepositoryImpl.TABLE_ALIAS, IdentifierRepositoryImpl.MAPPING_PREFIX)
                + ","
                + ImageFileResourceRepositoryImpl.SQL_PREVIEW_IMAGE_FIELDS_PI
                + ", "
                + UrlAliasRepositoryImpl.getSelectFields(true)
                + ", "
                + TagRepositoryImpl.SQL_REDUCED_FIELDS_TAGS
                + ", "
                + SubjectRepositoryImpl.SQL_REDUCED_FIELDS_SUBJECTS
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
                + " LEFT JOIN %1$s %2$s ON %2$s.uuid = ANY(%3$s.tags_uuids)"
                    .formatted(
                        TagRepositoryImpl.TABLE_NAME, TagRepositoryImpl.TABLE_ALIAS, tableAlias)
                + " LEFT JOIN %1$s %2$s ON %2$s.uuid = ANY(%3$s.subjects_uuids)"
                    .formatted(
                        SubjectRepositoryImpl.TABLE_NAME,
                        SubjectRepositoryImpl.TABLE_ALIAS,
                        tableAlias));

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

  public void save(
      I identifiable,
      Map<String, Object> bindings,
      BiFunction<String, Map<String, Object>, String> sqlModifier)
      throws RepositoryException {
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
    bindings.put("subjects_uuids", extractUuids(identifiable.getSubjects()));

    super.save(identifiable, bindings, sqlModifier);
  }

  @Override
  public List<Entity> setRelatedEntities(UUID identifiableUuid, List<Entity> entities)
      throws RepositoryException {
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
    return findRelatedEntities(identifiableUuid, new PageRequest(0, entities.size())).getContent();
  }

  @Override
  public List<FileResource> setRelatedFileResources(
      UUID identifiableUuid, List<FileResource> fileResources) throws RepositoryException {
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
    return findRelatedFileResources(identifiableUuid, new PageRequest(0, fileResources.size()))
        .getContent();
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

  public void update(
      I identifiable,
      Map<String, Object> bindings,
      BiFunction<String, Map<String, Object>, String> sqlModifier)
      throws RepositoryException {
    if (bindings == null) {
      bindings = new HashMap<>(0);
    }
    final UUID previewImageUuid =
        identifiable.getPreviewImage() == null ? null : identifiable.getPreviewImage().getUuid();
    bindings.put("previewFileResource", previewImageUuid);
    // split label
    bindings.put("split_label", splitToArray(identifiable.getLabel()));
    bindings.put("tags_uuids", extractUuids(identifiable.getTags()));
    bindings.put("subjects_uuids", extractUuids(identifiable.getSubjects()));

    super.update(identifiable, bindings, sqlModifier);
    // do not update/left out from statement (not changed since insert):
    // uuid, created, identifiable_type, identifiable_objecttype, refid
  }
}
