package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable;

import static de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifierRepositoryImpl.SQL_FULL_FIELDS_ID;
import static de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource.FileResourceMetadataRepositoryImpl.SQL_PREVIEW_IMAGE_FIELDS_PI;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.JdbiRepositoryImpl;
import de.digitalcollections.model.api.filter.Filtering;
import de.digitalcollections.model.api.identifiable.Identifiable;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.SearchPageRequest;
import de.digitalcollections.model.api.paging.SearchPageResponse;
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
public class IdentifiableRepositoryImpl<I extends IdentifiableImpl> extends JdbiRepositoryImpl
    implements IdentifiableRepository<I> {

  private static final Logger LOGGER = LoggerFactory.getLogger(IdentifiableRepositoryImpl.class);

  public static final String SQL_REDUCED_FIELDS_IDF =
      " i.uuid idf_uuid, i.label idf_label,"
          + " i.identifiable_type idf_type,"
          + " i.created idf_created, i.last_modified idf_lastModified,"
          + " i.preview_hints idf_previewImageRenderingHints";

  public static final String SQL_FULL_FIELDS_IDF =
      SQL_REDUCED_FIELDS_IDF + ", i.description idf_description";

  protected final String fullFieldsSql;
  protected final Class<I> identifiableImplClass;
  protected final IdentifierRepository identifierRepository;
  protected final String reducedFieldsSql;

  @Autowired
  private IdentifiableRepositoryImpl(Jdbi dbi, IdentifierRepository identifierRepository) {
    this(
        dbi,
        identifierRepository,
        "identifiables",
        "idf",
        "i",
        (Class<I>) IdentifiableImpl.class,
        SQL_REDUCED_FIELDS_IDF,
        SQL_FULL_FIELDS_IDF);
  }

  protected IdentifiableRepositoryImpl(
      Jdbi dbi,
      IdentifierRepository identifierRepository,
      String tableName,
      String tableAlias,
      String mappingPrefix,
      Class<I> identifiableImplClass,
      String reducedFieldsSql,
      String fullFieldsSql) {
    super(dbi, tableName, tableAlias, mappingPrefix);
    this.fullFieldsSql = fullFieldsSql;
    this.identifiableImplClass = identifiableImplClass;
    this.identifierRepository = identifierRepository;
    this.reducedFieldsSql = reducedFieldsSql;
  }

  @Override
  public void delete(List<UUID> uuids) {
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
    String commonSql = " FROM " + tableName + " AS " + tableAlias;
    return find(pageRequest, commonSql, null);
  }

  protected PageResponse<I> find(
      PageRequest pageRequest, String commonSql, Map<String, Object> argumentMappings) {
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

    I result = retrieveOne(fullFieldsSql, innerQuery, Map.of("uuid", uuid));

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
        retrieveOne(fullFieldsSql, innerQuery, Map.of("id", identifierId, "namespace", namespace));

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

  public BiFunction<LinkedHashMap<UUID, I>, RowView, LinkedHashMap<UUID, I>> mapRowToIdentifiable(
      boolean withIdentifiers, boolean withPreviewImage) {
    return (map, rowView) -> {
      I identifiable =
          map.computeIfAbsent(
              rowView.getColumn(mappingPrefix + "_uuid", UUID.class),
              fn -> {
                return rowView.getRow(identifiableImplClass);
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
                        .registerRowMapper(BeanMapper.factory(identifiableImplClass, mappingPrefix))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "pi"))
                        .reduceRows(new LinkedHashMap<UUID, I>(), mapRowToIdentifiable(true, true)))
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

    I result =
        dbi
            .withHandle(
                h ->
                    h.createQuery(sql)
                        .bindMap(argumentMappings)
                        .registerRowMapper(BeanMapper.factory(identifiableImplClass, mappingPrefix))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "pi"))
                        .reduceRows(new LinkedHashMap<UUID, I>(), mapRowToIdentifiable(true, true)))
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
