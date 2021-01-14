package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable;

import static de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifierRepositoryImpl.SQL_FULL_IDENTIFIER_FIELDS_ID;
import static de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource.FileResourceMetadataRepositoryImpl.SQL_PREVIEW_IMAGE_FIELDS_PI;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.AbstractPagingAndSortingRepositoryImpl;
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
import de.digitalcollections.model.impl.paging.SearchPageRequestImpl;
import de.digitalcollections.model.impl.paging.SearchPageResponseImpl;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.jdbi.v3.core.result.RowView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class IdentifiableRepositoryImpl extends AbstractPagingAndSortingRepositoryImpl
    implements IdentifiableRepository<Identifiable> {

  private static final Logger LOGGER = LoggerFactory.getLogger(IdentifiableRepositoryImpl.class);

  public static final String SQL_REDUCED_IDENTIFIABLE_FIELDS_IDF =
      " i.uuid idf_uuid, i.label idf_label,"
          + " i.identifiable_type idf_type,"
          + " i.created idf_created, i.last_modified idf_lastModified,"
          + " i.preview_hints idf_previewImageRenderingHints";

  public static final String SQL_FULL_IDENTIFIABLE_FIELDS_IDF =
      SQL_REDUCED_IDENTIFIABLE_FIELDS_IDF + ", i.description idf_description";

  public static BiFunction<
          LinkedHashMap<UUID, Identifiable>, RowView, LinkedHashMap<UUID, Identifiable>>
      mapRowToIdentifiable(boolean withIdentifiers, boolean withPreviewImage) {
    return (map, rowView) -> {
      Identifiable identifiable =
          map.computeIfAbsent(
              rowView.getColumn("idf_uuid", UUID.class),
              fn -> {
                return rowView.getRow(IdentifiableImpl.class);
              });

      if (withPreviewImage && rowView.getColumn("pi_uuid", UUID.class) != null) {
        identifiable.setPreviewImage(rowView.getRow(ImageFileResourceImpl.class));
      }
      if (withIdentifiers && rowView.getColumn("id_uuid", UUID.class) != null) {
        Identifier dbIdentifier = rowView.getRow(IdentifierImpl.class);
        identifiable.addIdentifier(dbIdentifier);
      }
      return map;
    };
  }

  private final Jdbi dbi;
  private final IdentifierRepository identifierRepository;

  @Autowired
  public IdentifiableRepositoryImpl(Jdbi dbi, IdentifierRepository identifierRepository) {
    this.dbi = dbi;
    this.identifierRepository = identifierRepository;
  }

  @Override
  public long count() {
    final String sql = "SELECT count(*) FROM identifiables";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOne().get());
    return count;
  }

  @Override
  public void delete(UUID uuid) {
    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM identifiables WHERE uuid = :uuid")
                .bind("uuid", uuid)
                .execute());
  }

  @Override
  public PageResponse<Identifiable> find(PageRequest pageRequest) {
    StringBuilder innerQuery = new StringBuilder("SELECT * FROM identifiables AS i");
    addFiltering(pageRequest, innerQuery);
    addPageRequestParams(pageRequest, innerQuery);

    final String sql =
        "SELECT"
            + SQL_REDUCED_IDENTIFIABLE_FIELDS_IDF
            + ","
            + SQL_PREVIEW_IMAGE_FIELDS_PI
            + " FROM ("
            + innerQuery
            + ") AS i"
            + " LEFT JOIN fileresources_image AS file ON i.previewfileresource = file.uuid";

    List<Identifiable> result =
        dbi
            .withHandle(
                h ->
                    h.createQuery(sql)
                        .registerRowMapper(BeanMapper.factory(IdentifiableImpl.class, "idf"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "pi"))
                        .reduceRows(
                            new LinkedHashMap<UUID, Identifiable>(),
                            mapRowToIdentifiable(false, true)))
            .values()
            .stream()
            .collect(Collectors.toList());

    StringBuilder sqlCount = new StringBuilder("SELECT count(*) FROM identifiables AS i");
    addFiltering(pageRequest, sqlCount);
    long total =
        dbi.withHandle(h -> h.createQuery(sqlCount.toString()).mapTo(Long.class).findOne().get());
    return new PageResponseImpl<>(result, pageRequest, total);
  }

  @Override
  public SearchPageResponse<Identifiable> find(SearchPageRequest searchPageRequest) {
    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT * FROM identifiables AS i"
                + " LEFT JOIN LATERAL jsonb_object_keys(i.label) l(keys) ON i.label IS NOT null"
                + " LEFT JOIN LATERAL jsonb_object_keys(i.description) d(keys) on i.description is not null"
                + " WHERE (i.label->>l.keys ILIKE '%' || :searchTerm || '%'"
                + " OR i.description->>d.keys ilike '%' || :searchTerm || '%')");
    addFiltering(searchPageRequest, innerQuery);
    addPageRequestParams(searchPageRequest, innerQuery);

    final String sql =
        "SELECT"
            + SQL_REDUCED_IDENTIFIABLE_FIELDS_IDF
            + ","
            + SQL_PREVIEW_IMAGE_FIELDS_PI
            + " FROM ("
            + innerQuery
            + ") AS i"
            + " LEFT JOIN fileresources_image AS file ON i.previewfileresource = file.uuid";

    List<Identifiable> result =
        dbi.withHandle(
            h ->
                h
                    .createQuery(sql)
                    .bind("searchTerm", searchPageRequest.getQuery())
                    .registerRowMapper(BeanMapper.factory(IdentifiableImpl.class, "col"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "pi"))
                    .reduceRows(
                        new LinkedHashMap<UUID, Identifiable>(), mapRowToIdentifiable(false, true))
                    .values()
                    .stream()
                    .collect(Collectors.toList()));

    StringBuilder countQuery =
        new StringBuilder(
            "SELECT count(*) FROM identifiables AS i"
                + " LEFT JOIN LATERAL jsonb_object_keys(i.label) l(keys) ON i.label IS NOT null"
                + " LEFT JOIN LATERAL jsonb_object_keys(i.description) d(keys) on i.description is not null"
                + " WHERE (i.label->>l.keys ILIKE '%' || :searchTerm || '%'"
                + " OR i.description->>d.keys ilike '%' || :searchTerm || '%')");
    addFiltering(searchPageRequest, countQuery);
    long total =
        dbi.withHandle(
            h ->
                h.createQuery(countQuery.toString())
                    .bind("searchTerm", searchPageRequest.getQuery())
                    .mapTo(Long.class)
                    .findOne()
                    .get());

    return new SearchPageResponseImpl<>(result, searchPageRequest, total);
  }

  @Override
  public Identifiable findOne(UUID uuid) {
    return findOne(uuid, null);
  }

  @Override
  public Identifiable findOne(UUID uuid, Filtering filtering) {
    StringBuilder innerQuery =
        new StringBuilder("SELECT * FROM identifiables AS i" + " WHERE i.uuid = :uuid");
    addFiltering(filtering, innerQuery);

    final String sql =
        "SELECT"
            + SQL_FULL_IDENTIFIABLE_FIELDS_IDF
            + ","
            + SQL_FULL_IDENTIFIER_FIELDS_ID
            + ","
            + SQL_PREVIEW_IMAGE_FIELDS_PI
            + " FROM ("
            + innerQuery
            + ") AS i"
            + " LEFT JOIN identifiers AS id ON i.uuid = id.identifiable"
            + " LEFT JOIN fileresources_image AS file ON i.previewfileresource = file.uuid";

    Identifiable result =
        dbi.withHandle(
                h ->
                    h.createQuery(sql)
                        .bind("uuid", uuid)
                        .registerRowMapper(BeanMapper.factory(IdentifiableImpl.class, "idf"))
                        .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "pi"))
                        .reduceRows(
                            new LinkedHashMap<UUID, Identifiable>(),
                            mapRowToIdentifiable(true, true)))
            .get(uuid);

    return result;
  }

  @Override
  public Identifiable findOne(Identifier identifier) {
    if (identifier.getIdentifiable() != null) {
      return findOne(identifier.getIdentifiable());
    }

    String namespace = identifier.getNamespace();
    String identifierId = identifier.getId();

    String innerQuery =
        "SELECT * FROM identifiables AS i"
            + " LEFT JOIN identifiers AS id ON i.uuid = id.identifiable"
            + " WHERE id.identifier = :id AND id.namespace = :namespace";

    final String sql =
        "SELECT"
            + SQL_FULL_IDENTIFIABLE_FIELDS_IDF
            + ","
            + SQL_FULL_IDENTIFIER_FIELDS_ID
            + ","
            + SQL_PREVIEW_IMAGE_FIELDS_PI
            + " FROM ("
            + innerQuery
            + ") AS i"
            + " LEFT JOIN identifiers AS id ON i.uuid = id.identifiable"
            + " LEFT JOIN fileresources_image AS file ON i.previewfileresource = file.uuid";

    Optional<Identifiable> result =
        dbi
            .withHandle(
                h ->
                    h.createQuery(sql)
                        .bind("id", identifierId)
                        .bind("namespace", namespace)
                        .registerRowMapper(BeanMapper.factory(IdentifiableImpl.class, "idf"))
                        .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "pi"))
                        .reduceRows(
                            new LinkedHashMap<UUID, Identifiable>(),
                            mapRowToIdentifiable(true, true)))
            .values()
            .stream()
            .findFirst();

    return result.orElse(null);
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
        return "i.created";
      case "lastModified":
        return "i.last_modified";
      case "type":
        return "i.identifiable_type";
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

  @Override
  public Identifiable save(Identifiable identifiable) {
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
  public Identifiable update(Identifiable identifiable) {
    throw new UnsupportedOperationException("Use update of specific identifiable repository!");
  }
}
