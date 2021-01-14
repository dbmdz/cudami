package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import static de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifierRepositoryImpl.SQL_FULL_IDENTIFIER_FIELDS_ID;
import static de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource.FileResourceMetadataRepositoryImpl.SQL_PREVIEW_IMAGE_FIELDS_PI;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EntityRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.AbstractPagingAndSortingRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifiableRepositoryImpl;
import de.digitalcollections.model.api.filter.Filtering;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.SearchPageRequest;
import de.digitalcollections.model.api.paging.SearchPageResponse;
import de.digitalcollections.model.impl.identifiable.IdentifierImpl;
import de.digitalcollections.model.impl.identifiable.entity.EntityImpl;
import de.digitalcollections.model.impl.identifiable.resource.FileResourceImpl;
import de.digitalcollections.model.impl.identifiable.resource.ImageFileResourceImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import de.digitalcollections.model.impl.paging.SearchPageResponseImpl;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
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

@Repository
public class EntityRepositoryImpl extends AbstractPagingAndSortingRepositoryImpl
    implements EntityRepository<Entity> {

  private static final Logger LOGGER = LoggerFactory.getLogger(EntityRepositoryImpl.class);

  public static final String SQL_REDUCED_ENTITY_FIELDS_E =
      " e.uuid e_uuid, e.refid e_refId, e.label e_label, e.description e_description,"
          + " e.identifiable_type e_type, e.entity_type e_entityType,"
          + " e.created e_created, e.last_modified e_lastModified,"
          + " e.preview_hints e_previewImageRenderingHints";

  public static final String SQL_FULL_ENTITY_FIELDS_E = SQL_REDUCED_ENTITY_FIELDS_E;

  public static BiFunction<LinkedHashMap<UUID, Entity>, RowView, LinkedHashMap<UUID, Entity>>
      mapRowToEntity(boolean withIdentifiers, boolean withPreviewImage) {
    return (map, rowView) -> {
      Entity entity =
          map.computeIfAbsent(
              rowView.getColumn("e_uuid", UUID.class),
              fn -> {
                return rowView.getRow(EntityImpl.class);
              });

      if (withPreviewImage && rowView.getColumn("pi_uuid", UUID.class) != null) {
        entity.setPreviewImage(rowView.getRow(ImageFileResourceImpl.class));
      }
      if (withIdentifiers && rowView.getColumn("id_uuid", UUID.class) != null) {
        IdentifierImpl dbIdentifier = rowView.getRow(IdentifierImpl.class);
        entity.addIdentifier(dbIdentifier);
      }
      return map;
    };
  }

  private final Jdbi dbi;
  private final IdentifiableRepositoryImpl identifiableRepositoryImpl;

  @Autowired
  public EntityRepositoryImpl(
      Jdbi dbi,
      IdentifierRepository identifierRepository,
      IdentifiableRepositoryImpl identifiableRepositoryImpl) {
    this.dbi = dbi;
    this.identifiableRepositoryImpl = identifiableRepositoryImpl;
  }

  @Override
  public void addRelatedFileresource(UUID entityUuid, UUID fileResourceUuid) {
    Integer nextSortIndex =
        dbi.withHandle(
            (Handle h) ->
                h.createQuery(
                        "SELECT MAX(sortIndex) + 1 FROM rel_entity_fileresources"
                            + " WHERE entity_uuid = :entityUuid")
                    .bind("entityUuid", entityUuid)
                    .mapTo(Integer.class)
                    .findOne()
                    .orElse(0));

    dbi.withHandle(
        h ->
            h.createUpdate(
                    "INSERT INTO rel_entity_fileresources(entity_uuid, fileresource_uuid, sortindex) VALUES (:entity_uuid, :fileresource_uuid, :sortindex)")
                .bind("entity_uuid", entityUuid)
                .bind("fileresource_uuid", fileResourceUuid)
                .bind("sortindex", nextSortIndex)
                .execute());
  }

  @Override
  public long count() {
    final String sql = "SELECT count(*) FROM entities";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOne().get());
    return count;
  }

  @Override
  public void delete(UUID uuid) {
    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM entities WHERE uuid = :uuid").bind("uuid", uuid).execute());
  }

  @Override
  public PageResponse<Entity> find(PageRequest pageRequest) {
    StringBuilder innerQuery = new StringBuilder("SELECT * FROM entities AS e");
    addFiltering(pageRequest, innerQuery);
    addPageRequestParams(pageRequest, innerQuery);

    final String sql =
        "SELECT"
            + SQL_REDUCED_ENTITY_FIELDS_E
            + ","
            + SQL_PREVIEW_IMAGE_FIELDS_PI
            + " FROM ("
            + innerQuery
            + ") AS e"
            + " LEFT JOIN fileresources_image AS file ON e.previewfileresource = file.uuid";

    List<Entity> result =
        dbi
            .withHandle(
                h ->
                    h.createQuery(sql)
                        .registerRowMapper(BeanMapper.factory(EntityImpl.class, "e"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "pi"))
                        .reduceRows(new LinkedHashMap<UUID, Entity>(), mapRowToEntity(false, true)))
            .values()
            .stream()
            .collect(Collectors.toList());

    StringBuilder sqlCount = new StringBuilder("SELECT count(*) FROM entities AS e");
    addFiltering(pageRequest, sqlCount);
    long total =
        dbi.withHandle(h -> h.createQuery(sqlCount.toString()).mapTo(Long.class).findOne().get());

    return new PageResponseImpl<>(result, pageRequest, total);
  }

  @Override
  public SearchPageResponse<Entity> find(SearchPageRequest searchPageRequest) {
    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT * FROM entities AS e"
                + " LEFT JOIN LATERAL jsonb_object_keys(e.label) l(keys) ON e.label IS NOT null"
                + " LEFT JOIN LATERAL jsonb_object_keys(e.description) d(keys) on e.description is not null"
                + " WHERE (e.label->>l.keys ILIKE '%' || :searchTerm || '%'"
                + " OR e.description->>d.keys ilike '%' || :searchTerm || '%')");
    addFiltering(searchPageRequest, innerQuery);
    addPageRequestParams(searchPageRequest, innerQuery);

    final String sql =
        "SELECT"
            + SQL_REDUCED_ENTITY_FIELDS_E
            + ","
            + SQL_PREVIEW_IMAGE_FIELDS_PI
            + " FROM ("
            + innerQuery
            + ") AS e"
            + " LEFT JOIN fileresources_image AS file ON e.previewfileresource = file.uuid";

    List<Entity> result =
        dbi.withHandle(
            h ->
                h
                    .createQuery(sql)
                    .bind("searchTerm", searchPageRequest.getQuery())
                    .registerRowMapper(BeanMapper.factory(EntityImpl.class, "col"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "pi"))
                    .reduceRows(new LinkedHashMap<UUID, Entity>(), mapRowToEntity(false, true))
                    .values()
                    .stream()
                    .collect(Collectors.toList()));

    StringBuilder countQuery =
        new StringBuilder(
            "SELECT count(*) FROM entities as e"
                + " LEFT JOIN LATERAL jsonb_object_keys(e.label) l(keys) on e.label is not null"
                + " LEFT JOIN LATERAL jsonb_object_keys(e.description) d(keys) on e.description is not null"
                + " WHERE (e.label->>l.keys ilike '%' || :searchTerm || '%'"
                + " OR e.description->>d.keys ilike '%' || :searchTerm || '%')");
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
  public List<Entity> findAllReduced() {
    final String sql =
        "SELECT"
            + SQL_REDUCED_ENTITY_FIELDS_E
            + ","
            + SQL_FULL_IDENTIFIER_FIELDS_ID
            + " FROM entities AS e"
            + " LEFT JOIN identifiers AS id ON e.uuid = id.identifiable";

    List<Entity> result =
        dbi
            .withHandle(
                h ->
                    h.createQuery(sql)
                        .registerRowMapper(BeanMapper.factory(EntityImpl.class, "e"))
                        .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                        .reduceRows(new LinkedHashMap<UUID, Entity>(), mapRowToEntity(true, false)))
            .values()
            .stream()
            .collect(Collectors.toList());
    return result;
  }

  @Override
  public Entity findOne(UUID uuid) {
    return findOne(uuid, null);
  }

  @Override
  public Entity findOne(UUID uuid, Filtering filtering) {
    StringBuilder innerQuery =
        new StringBuilder("SELECT * FROM collections AS e" + " WHERE e.uuid = :uuid");
    addFiltering(filtering, innerQuery);

    final String sql =
        "SELECT"
            + SQL_FULL_ENTITY_FIELDS_E
            + ","
            + SQL_FULL_IDENTIFIER_FIELDS_ID
            + ","
            + SQL_PREVIEW_IMAGE_FIELDS_PI
            + " FROM ("
            + innerQuery
            + ") AS e"
            + " LEFT JOIN identifiers AS id ON e.uuid = id.identifiable"
            + " LEFT JOIN fileresources_image AS file ON e.previewfileresource = file.uuid";

    Entity result =
        dbi.withHandle(
                h ->
                    h.createQuery(sql)
                        .bind("uuid", uuid)
                        .registerRowMapper(BeanMapper.factory(EntityImpl.class, "e"))
                        .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "pi"))
                        .reduceRows(new LinkedHashMap<UUID, Entity>(), mapRowToEntity(true, true)))
            .get(uuid);

    return result;
  }

  @Override
  public Entity findOne(Identifier identifier) {
    if (identifier.getIdentifiable() != null) {
      return findOne(identifier.getIdentifiable());
    }

    String namespace = identifier.getNamespace();
    String identifierId = identifier.getId();

    String innerQuery =
        "SELECT * FROM entities AS e"
            + " LEFT JOIN identifiers AS id ON e.uuid = id.identifiable"
            + " WHERE id.identifier = :id AND id.namespace = :namespace";

    final String sql =
        "SELECT"
            + SQL_FULL_ENTITY_FIELDS_E
            + ","
            + SQL_FULL_IDENTIFIER_FIELDS_ID
            + ","
            + SQL_PREVIEW_IMAGE_FIELDS_PI
            + " FROM ("
            + innerQuery
            + ") AS e"
            + " LEFT JOIN identifiers AS id ON e.uuid = id.identifiable"
            + " LEFT JOIN fileresources_image AS file ON e.previewfileresource = file.uuid";

    Optional<Entity> result =
        dbi
            .withHandle(
                h ->
                    h.createQuery(sql)
                        .bind("id", identifierId)
                        .bind("namespace", namespace)
                        .registerRowMapper(BeanMapper.factory(EntityImpl.class, "e"))
                        .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "pi"))
                        .reduceRows(new LinkedHashMap<UUID, Entity>(), mapRowToEntity(true, true)))
            .values()
            .stream()
            .findFirst();

    return result.orElse(null);
  }

  @Override
  public Entity findOneByRefId(long refId) {
    String innerQuery = "SELECT * FROM entities AS e" + " WHERE e.refid = :refId";

    final String sql =
        "SELECT"
            + SQL_FULL_ENTITY_FIELDS_E
            + ","
            + SQL_FULL_IDENTIFIER_FIELDS_ID
            + ","
            + SQL_PREVIEW_IMAGE_FIELDS_PI
            + " FROM ("
            + innerQuery
            + ") AS e"
            + " LEFT JOIN identifiers AS id ON e.uuid = id.identifiable"
            + " LEFT JOIN fileresources_image AS file ON e.previewfileresource = file.uuid";

    Entity result =
        dbi
            .withHandle(
                h ->
                    h.createQuery(sql)
                        .bind("refId", refId)
                        .registerRowMapper(BeanMapper.factory(EntityImpl.class, "e"))
                        .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "pi"))
                        .reduceRows(new LinkedHashMap<UUID, Entity>(), mapRowToEntity(true, true)))
            .values()
            .stream()
            .findFirst()
            .orElse(null);

    return result;
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[] {"created", "entityType", "lastModified", "refId", "type"};
  }

  @Override
  protected String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "created":
        return "e.created";
      case "entityType":
        return "e.entity_type";
      case "lastModified":
        return "e.last_modified";
      case "refId":
        return "e.refid";
      case "type":
        return "e.identifiable_type";
      default:
        return null;
    }
  }

  @Override
  public List<FileResource> getRelatedFileResources(UUID entityUuid) {
    String query =
        "SELECT * FROM fileresources f"
            + " INNER JOIN rel_entity_fileresources ref ON f.uuid=ref.fileresource_uuid"
            + " WHERE ref.entity_uuid = :entityUuid"
            + " ORDER BY ref.sortindex";

    List<FileResource> result =
        dbi.withHandle(
            h ->
                h
                    .createQuery(query)
                    .bind("entityUuid", entityUuid)
                    .mapToBean(FileResourceImpl.class)
                    .list()
                    .stream()
                    .map(FileResource.class::cast)
                    .collect(Collectors.toList()));
    return result;
  }

  @Override
  public Entity save(Entity identifiable) {
    throw new UnsupportedOperationException("Use save method of specific entity repository!");
  }

  @Override
  public List<FileResource> saveRelatedFileResources(
      Entity entity, List<FileResource> fileResources) {
    return saveRelatedFileResources(entity.getUuid(), fileResources);
  }

  @Override
  public List<FileResource> saveRelatedFileResources(
      UUID entityUuid, List<FileResource> fileResources) {
    if (fileResources == null) {
      return null;
    }
    // as we store the whole list new: delete old entries
    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM rel_entity_fileresources WHERE entity_uuid = :uuid")
                .bind("uuid", entityUuid)
                .execute());

    dbi.useHandle(
        handle -> {
          PreparedBatch preparedBatch =
              handle.prepareBatch(
                  "INSERT INTO rel_entity_fileresources(entity_uuid, fileresource_uuid, sortIndex) VALUES(:uuid, :fileResourceUuid, :sortIndex)");
          for (FileResource fileResource : fileResources) {
            preparedBatch
                .bind("uuid", entityUuid)
                .bind("fileResourceUuid", fileResource.getUuid())
                .bind("sortIndex", identifiableRepositoryImpl.getIndex(fileResources, fileResource))
                .add();
          }
          preparedBatch.execute();
        });
    return getRelatedFileResources(entityUuid);
  }

  @Override
  public Entity update(Entity identifiable) {
    throw new UnsupportedOperationException("Use update method of specific entity repo!");
  }
}
