package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable;

import static de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifierRepositoryImpl.SQL_FULL_FIELDS_ID;
import static de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource.FileResourceMetadataRepositoryImpl.SQL_PREVIEW_IMAGE_FIELDS_PI;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.JdbiRepositoryImpl;
import de.digitalcollections.model.api.filter.Filtering;
import de.digitalcollections.model.api.identifiable.Identifiable;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.paging.Order;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.SearchPageRequest;
import de.digitalcollections.model.api.paging.SearchPageResponse;
import de.digitalcollections.model.api.paging.Sorting;
import de.digitalcollections.model.api.paging.enums.Direction;
import de.digitalcollections.model.impl.identifiable.IdentifiableImpl;
import de.digitalcollections.model.impl.identifiable.IdentifierImpl;
import de.digitalcollections.model.impl.identifiable.resource.ImageFileResourceImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import de.digitalcollections.model.impl.paging.SearchPageResponseImpl;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.jdbi.v3.core.result.RowView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

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
  public BiFunction<LinkedHashMap<UUID, I>, RowView, LinkedHashMap<UUID, I>>
      additionalReduceRowsBiFunction =
          (map, rowView) -> {
            return map;
          };
  public final BiFunction<LinkedHashMap<UUID, I>, RowView, LinkedHashMap<UUID, I>>
      basicReduceRowsBiFunction;
  public final BiFunction<LinkedHashMap<UUID, I>, RowView, LinkedHashMap<UUID, I>>
      fullReduceRowsBiFunction;
  protected final Class identifiableImplClass;
  protected final IdentifierRepository identifierRepository;
  private final String sqlInsertFields;
  private final String sqlInsertValues;
  protected String sqlSelectAllFields;
  protected final String sqlSelectAllFieldsJoins;
  protected String sqlSelectReducedFields;
  private final String sqlUpdateFieldValues;

  @Autowired
  private IdentifiableRepositoryImpl(Jdbi dbi, IdentifierRepository identifierRepository) {
    this(
        dbi,
        identifierRepository,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        IdentifiableImpl.class,
        getSqlSelectAllFields(TABLE_ALIAS, MAPPING_PREFIX),
        getSqlSelectReducedFields(TABLE_ALIAS, MAPPING_PREFIX),
        getSqlInsertFields(),
        getSqlInsertValues(),
        getSqlUpdateFieldValues());
    // register row mappers for always joined classes and mapping prefix. as it is in autowired
    // constructor, this will be done only once at instantiation done by Spring
    dbi.registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "pi"));
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
      BiFunction<LinkedHashMap<UUID, I>, RowView, LinkedHashMap<UUID, I>>
          additionalReduceRowsBiFunction) {
    super(dbi, tableName, tableAlias, mappingPrefix);

    // register row mapper for given class and mapping prefix
    // (until now everywhere BeanMapper.factory... was used. If this changes, row mapper
    // registration may be moved back into each repository impl?)
    dbi.registerRowMapper(BeanMapper.factory(identifiableImplClass, mappingPrefix));
    dbi.registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"));
    dbi.registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "pi"));

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

  private BiFunction<LinkedHashMap<UUID, I>, RowView, LinkedHashMap<UUID, I>>
      createReduceRowsBiFunction(boolean withIdentifiers, boolean withPreviewImage) {
    return (map, rowView) -> {
      I identifiable =
          map.computeIfAbsent(
              rowView.getColumn(mappingPrefix + "_uuid", UUID.class),
              fn -> {
                return (I) rowView.getRow(identifiableImplClass);
              });

      if (withPreviewImage && rowView.getColumn("pi_uuid", UUID.class) != null) {
        identifiable.setPreviewImage(rowView.getRow(ImageFileResourceImpl.class));
      }
      if (withIdentifiers && rowView.getColumn("id_uuid", UUID.class) != null) {
        IdentifierImpl dbIdentifier = rowView.getRow(IdentifierImpl.class);
        identifiable.addIdentifier(dbIdentifier);
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
    List<I> result = retrieveList(sqlSelectReducedFields, innerQuery, argumentMappings);

    StringBuilder sqlCount = new StringBuilder("SELECT count(*)" + commonSql);
    addFiltering(pageRequest, sqlCount);
    long total = retrieveCount(sqlCount, argumentMappings);

    return new PageResponseImpl<>(result, pageRequest, total);
  }

  @Override
  public SearchPageResponse<I> find(SearchPageRequest searchPageRequest) {
    String commonSql =
        " FROM "
            + tableName
            + " AS "
            + tableAlias
            + " LEFT JOIN LATERAL jsonb_object_keys("
            + tableAlias
            + ".label) lbl(keys) ON "
            + tableAlias
            + ".label IS NOT NULL"
            + " LEFT JOIN LATERAL jsonb_object_keys("
            + tableAlias
            + ".description) dsc(keys) ON "
            + tableAlias
            + ".description IS NOT NULL"
            + " WHERE ("
            + tableAlias
            + ".label->>lbl.keys ILIKE '%' || :searchTerm || '%'"
            + " OR "
            + tableAlias
            + ".description->>dsc.keys ILIKE '%' || :searchTerm || '%')";
    return find(searchPageRequest, commonSql, Map.of("searchTerm", searchPageRequest.getQuery()));
  }

  protected SearchPageResponse<I> find(
      SearchPageRequest searchPageRequest, String commonSql, Map<String, Object> argumentMappings) {
    StringBuilder innerQuery = new StringBuilder("SELECT *" + commonSql);
    addFiltering(searchPageRequest, innerQuery);
    addPageRequestParams(searchPageRequest, innerQuery);
    List<I> result = retrieveList(sqlSelectReducedFields, innerQuery, argumentMappings);

    StringBuilder countQuery = new StringBuilder("SELECT count(*)" + commonSql);
    addFiltering(searchPageRequest, countQuery);
    long total = retrieveCount(countQuery, argumentMappings);

    return new SearchPageResponseImpl<>(result, searchPageRequest, total);
  }

  @Override
  public List<I> findAllFull() {
    return retrieveList(sqlSelectAllFields, null, null);
  }

  @Override
  public List<I> findAllReduced() {
    return retrieveList(sqlSelectReducedFields, null, null);
  }

  @Override
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
    Filtering.defaultBuilder()
        .filter(tableAlias + ".label ->> :language")
        .startsWith(":initial")
        .build();
    filtering.add(filtering);

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
    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT * FROM "
                + tableName
                + " AS "
                + tableAlias
                + " WHERE "
                + tableAlias
                + ".uuid = :uuid");
    addFiltering(filtering, innerQuery);

    I result =
        retrieveOne(sqlSelectAllFields, innerQuery, sqlSelectAllFieldsJoins, Map.of("uuid", uuid));

    return result;
  }

  @Override
  public I findOne(Identifier identifier) {
    if (identifier.getIdentifiable() != null) {
      return findOne(identifier.getIdentifiable());
    }

    String namespace = identifier.getNamespace();
    String identifierId = identifier.getId();

    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT * FROM "
                + tableName
                + " AS "
                + tableAlias
                + " LEFT JOIN identifiers AS id ON "
                + tableAlias
                + ".uuid = id.identifiable"
                + " WHERE id.identifier = :id AND id.namespace = :namespace");

    I result =
        retrieveOne(
            sqlSelectAllFields,
            innerQuery,
            sqlSelectAllFieldsJoins,
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
      case "label":
        return tableAlias + ".label";
      case "lastModified":
        return tableAlias + ".last_modified";
      case "type":
        return tableAlias + ".identifiable_type";
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
      String fieldsSql, StringBuilder innerQuery, final Map<String, Object> argumentMappings) {
    final String sql =
        "SELECT "
            + fieldsSql
            + ","
            + SQL_FULL_FIELDS_ID
            + ","
            + SQL_PREVIEW_IMAGE_FIELDS_PI
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
            + ".previewfileresource = file.uuid";
    List<I> result =
        dbi
            .withHandle(
                h ->
                    h.createQuery(sql)
                        .bindMap(argumentMappings)
                        .reduceRows(
                            new LinkedHashMap<UUID, I>(),
                            (map, rowView) -> {
                              basicReduceRowsBiFunction.apply(map, rowView);
                              return map;
                            }))
            .values()
            .stream()
            .collect(Collectors.toList());
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
      StringBuilder innerQuery,
      String sqlSelectAllFieldsJoins,
      final Map<String, Object> argumentMappings) {
    final String sql =
        "SELECT"
            + fieldsSql
            + ","
            + SQL_FULL_FIELDS_ID
            + ","
            + SQL_PREVIEW_IMAGE_FIELDS_PI
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
            + ".previewfileresource = file.uuid";

    I result =
        dbi
            .withHandle(
                h ->
                    h.createQuery(sql)
                        .bindMap(argumentMappings)
                        .reduceRows(
                            new LinkedHashMap<UUID, I>(),
                            (map, rowView) -> {
                              fullReduceRowsBiFunction.apply(map, rowView);
                              additionalReduceRowsBiFunction.apply(map, rowView);
                              return map;
                            }))
            .values()
            .stream()
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
