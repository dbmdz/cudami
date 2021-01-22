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

  public static final String SQL_REDUCED_FIELDS_IDF =
      " i.uuid idf_uuid, i.label idf_label,"
          + " i.identifiable_type idf_type,"
          + " i.created idf_created, i.last_modified idf_lastModified,"
          + " i.preview_hints idf_previewImageRenderingHints";

  public static final String SQL_FULL_FIELDS_IDF =
      SQL_REDUCED_FIELDS_IDF + ", i.description idf_description";
  public static final String TABLE_ALIAS = "i";
  public static final String TABLE_NAME = "identifiables";

  /* BiFunction for reducing rows (related objects) of joins not already part of identifiable (Identifier, preview image ImageFileResource). */
  public BiFunction<LinkedHashMap<UUID, I>, RowView, LinkedHashMap<UUID, I>>
      additionalReduceRowsBiFunction =
          (map, rowView) -> {
            return map;
          };
  public final BiFunction<LinkedHashMap<UUID, I>, RowView, LinkedHashMap<UUID, I>>
      basicReduceRowsBiFunction;
  protected final String fullFieldsJoinsSql;
  protected final String fullFieldsSql;
  public final BiFunction<LinkedHashMap<UUID, I>, RowView, LinkedHashMap<UUID, I>>
      fullReduceRowsBiFunction;
  protected final Class identifiableImplClass;
  protected final IdentifierRepository identifierRepository;
  protected final String reducedFieldsSql;

  @Autowired
  private IdentifiableRepositoryImpl(Jdbi dbi, IdentifierRepository identifierRepository) {
    this(
        dbi,
        identifierRepository,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        IdentifiableImpl.class,
        SQL_REDUCED_FIELDS_IDF,
        SQL_FULL_FIELDS_IDF);
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
      String reducedFieldsSql,
      String fullFieldsSql) {
    this(
        dbi,
        identifierRepository,
        tableName,
        tableAlias,
        mappingPrefix,
        identifiableImplClass,
        reducedFieldsSql,
        fullFieldsSql,
        null,
        null);
  }

  protected IdentifiableRepositoryImpl(
      Jdbi dbi,
      IdentifierRepository identifierRepository,
      String tableName,
      String tableAlias,
      String mappingPrefix,
      Class identifiableImplClass,
      String reducedFieldsSql,
      String fullFieldsSql,
      String fullFieldsJoinsSql) {
    this(
        dbi,
        identifierRepository,
        tableName,
        tableAlias,
        mappingPrefix,
        identifiableImplClass,
        reducedFieldsSql,
        fullFieldsSql,
        fullFieldsJoinsSql,
        null);
  }

  protected IdentifiableRepositoryImpl(
      Jdbi dbi,
      IdentifierRepository identifierRepository,
      String tableName,
      String tableAlias,
      String mappingPrefix,
      Class identifiableImplClass,
      String reducedFieldsSql,
      String fullFieldsSql,
      String fullFieldsJoinsSql,
      BiFunction<LinkedHashMap<UUID, I>, RowView, LinkedHashMap<UUID, I>>
          additionalReduceRowsBiFunction) {
    super(dbi, tableName, tableAlias, mappingPrefix);

    // register row mapper for given class and mapping prefix
    // (until now everywhere BeanMapper.factory... was used. If this changes, row mapper
    // registration may be moved back into each repository impl?)
    dbi.registerRowMapper(BeanMapper.factory(identifiableImplClass, mappingPrefix));

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

    this.fullFieldsJoinsSql = fullFieldsJoinsSql;
    this.fullFieldsSql = fullFieldsSql;
    this.identifiableImplClass = identifiableImplClass;
    this.identifierRepository = identifierRepository;
    this.reducedFieldsSql = reducedFieldsSql;
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
    List<I> result = retrieveList(reducedFieldsSql, innerQuery, argumentMappings);

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
            + ".label) l(keys) ON "
            + tableAlias
            + ".label IS NOT NULL"
            + " LEFT JOIN LATERAL jsonb_object_keys("
            + tableAlias
            + ".description) d(keys) ON "
            + tableAlias
            + ".description IS NOT NULL"
            + " WHERE ("
            + tableAlias
            + ".label->>l.keys ILIKE '%' || :searchTerm || '%'"
            + " OR "
            + tableAlias
            + ".description->>d.keys ILIKE '%' || :searchTerm || '%')";
    return find(searchPageRequest, commonSql, Map.of("searchTerm", searchPageRequest.getQuery()));
  }

  protected SearchPageResponse<I> find(
      SearchPageRequest searchPageRequest, String commonSql, Map<String, Object> argumentMappings) {
    StringBuilder innerQuery = new StringBuilder("SELECT *" + commonSql);
    addFiltering(searchPageRequest, innerQuery);
    addPageRequestParams(searchPageRequest, innerQuery);
    List<I> result = retrieveList(reducedFieldsSql, innerQuery, argumentMappings);

    StringBuilder countQuery = new StringBuilder("SELECT count(*)" + commonSql);
    addFiltering(searchPageRequest, countQuery);
    long total = retrieveCount(countQuery, argumentMappings);

    return new SearchPageResponseImpl<>(result, searchPageRequest, total);
  }

  @Override
  public List<I> findAllFull() {
    return retrieveList(fullFieldsSql, null, null);
  }

  @Override
  public List<I> findAllReduced() {
    return retrieveList(reducedFieldsSql, null, null);
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

    I result = retrieveOne(fullFieldsSql, innerQuery, fullFieldsJoinsSql, Map.of("uuid", uuid));

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
            fullFieldsSql,
            innerQuery,
            fullFieldsJoinsSql,
            Map.of("id", identifierId, "namespace", namespace));

    return result;
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[] {"created", "lastModified", "type"};
  }

  @Override
  protected String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "created":
        return tableAlias + ".created";
      case "lastModified":
        return tableAlias + ".last_modified";
      case "type":
        return tableAlias + ".identifiable_type";
      default:
        return null;
    }
  }

  public String getFullFieldsSql() {
    return fullFieldsSql;
  }

  public Class<I> getIdentifiableImplClass() {
    return identifiableImplClass;
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

  public String getReducedFieldsSql() {
    return reducedFieldsSql;
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
      String fullFieldsJoinsSql,
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
            + (fullFieldsJoinsSql != null ? fullFieldsJoinsSql : "")
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
  public I save(I identifiable) {
    throw new UnsupportedOperationException("Use save of specific identifiable repository!");
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
  public I update(I identifiable) {
    throw new UnsupportedOperationException("Use update of specific identifiable repository!");
  }
}
