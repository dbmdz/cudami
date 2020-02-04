package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EntityRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifiableRepositoryImpl;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.EntityRelation;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.IdentifierImpl;
import de.digitalcollections.model.impl.identifiable.entity.EntityImpl;
import de.digitalcollections.model.impl.identifiable.resource.FileResourceImpl;
import de.digitalcollections.model.impl.identifiable.resource.ImageFileResourceImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class EntityRepositoryImpl<E extends Entity> extends IdentifiableRepositoryImpl<E>
    implements EntityRepository<E> {

  private static final Logger LOGGER = LoggerFactory.getLogger(EntityRepositoryImpl.class);

  // select all details shown/needed in single object details page
  private static final String FIND_ONE_BASE_SQL =
      "SELECT e.uuid e_uuid, e.refid e_refId, e.label e_label, e.description e_description,"
          + " e.identifiable_type e_type, e.entity_type e_entityType,"
          + " e.created e_created, e.last_modified e_last_modified,"
          + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
          + " file.filename f_filename, file.mimetype f_mimetype, file.size_in_bytes f_size_in_bytes, file.uri f_uri"
          + " FROM entities as e"
          + " LEFT JOIN identifiers as id on e.uuid = id.identifiable"
          + " LEFT JOIN fileresources_image as file on e.previewfileresource = file.uuid";

  // select only what is shown/needed in paged list (to avoid unnecessary payload/traffic):
  private static final String REDUCED_FIND_ONE_BASE_SQL =
      "SELECT e.uuid e_uuid, e.refid e_refId, e.label e_label, e.description e_description,"
          + " e.identifiable_type e_type, , e.entity_type e_entityType,"
          + " e.created e_created, e.last_modified e_lastModified,"
          + " file.uri f_uri, file.filename f_filename"
          + " FROM entities as e"
          + " LEFT JOIN fileresources_image as file on e.previewfileresource = file.uuid";

  @Autowired
  public EntityRepositoryImpl(Jdbi dbi, IdentifierRepository identifierRepository) {
    super(dbi, identifierRepository);
  }

  @Override
  public void addRelatedFileresource(E entity, FileResource fileResource) {
    addRelatedFileresource(entity.getUuid(), fileResource.getUuid());
  }

  @Override
  public void addRelatedFileresource(UUID entityUuid, UUID fileResourceUuid) {
    Integer sortIndex =
        selectNextSortIndexForParentChildren(
            dbi, "rel_entity_fileresources", "entity_uuid", entityUuid);
    dbi.withHandle(
        h ->
            h.createUpdate(
                    "INSERT INTO rel_entity_fileresources(entity_uuid, fileresource_uuid, sortindex) VALUES (:entity_uuid, :fileresource_uuid, :sortindex)")
                .bind("entity_uuid", entityUuid)
                .bind("fileresource_uuid", fileResourceUuid)
                .bind("sortindex", sortIndex)
                .execute());
  }

  @Override
  public void addRelation(EntityRelation<E> relation) {
    addRelation(
        relation.getSubject().getUuid(), relation.getPredicate(), relation.getObject().getUuid());
  }

  @Override
  public void addRelation(UUID subjectEntityUuid, String predicate, UUID objectEntityUuid) {
    dbi.withHandle(
        h ->
            h.createUpdate(
                    "INSERT INTO rel_entity_entities(subject_uuid, predicate, object_uuid) VALUES (:subject_uuid, :predicate, :object_uuid)")
                .bind("subject_uuid", subjectEntityUuid)
                .bind("predicate", predicate)
                .bind("object_uuid", objectEntityUuid)
                .execute());
  }

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM entities";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOne().get());
    return count;
  }

  @Override
  public PageResponse<E> find(PageRequest pageRequest) {
    StringBuilder query = new StringBuilder(REDUCED_FIND_ONE_BASE_SQL);
    addPageRequestParams(pageRequest, query);

    List<EntityImpl> result =
        dbi.withHandle(
            h ->
                h.createQuery(query.toString())
                    .registerRowMapper(BeanMapper.factory(EntityImpl.class, "i"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                    .reduceRows(
                        new LinkedHashMap<UUID, EntityImpl>(),
                        (map, rowView) -> addPreviewImage(map, rowView, EntityImpl.class, "i_uuid"))
                    .values().stream()
                    .collect(Collectors.toList()));
    long total = count();
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public E findOne(UUID uuid) {
    String query = FIND_ONE_BASE_SQL + " WHERE e.uuid = :uuid";

    Optional<EntityImpl> resultOpt =
        dbi.withHandle(
            h ->
                h.createQuery(query).bind("uuid", uuid)
                    .registerRowMapper(BeanMapper.factory(EntityImpl.class, "i"))
                    .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                    .reduceRows(
                        new LinkedHashMap<UUID, EntityImpl>(),
                        (map, rowView) ->
                            addPreviewImageAndIdentifiers(map, rowView, EntityImpl.class, "e_uuid"))
                    .values().stream()
                    .findFirst());
    if (!resultOpt.isPresent()) {
      return null;
    }
    return (E) resultOpt.get();
  }

  @Override
  public E findOne(Identifier identifier) {
    if (identifier.getIdentifiable() != null) {
      return findOne(identifier.getIdentifiable());
    }

    String namespace = identifier.getNamespace();
    String identifierId = identifier.getId();

    String query = FIND_ONE_BASE_SQL + " WHERE id.identifier = :id AND id.namespace = :namespace";

    Optional<EntityImpl> resultOpt =
        dbi.withHandle(
            h ->
                h.createQuery(query).bind("id", identifierId).bind("namespace", namespace)
                    .registerRowMapper(BeanMapper.factory(EntityImpl.class, "i"))
                    .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                    .reduceRows(
                        new LinkedHashMap<UUID, EntityImpl>(),
                        (map, rowView) ->
                            addPreviewImageAndIdentifiers(map, rowView, EntityImpl.class, "e_uuid"))
                    .values().stream()
                    .findFirst());
    if (!resultOpt.isPresent()) {
      return null;
    }
    return (E) resultOpt.get();
  }

  @Override
  public E findOneByRefId(long refId) {
    String query = FIND_ONE_BASE_SQL + " WHERE e.refid = :refId";

    Optional<EntityImpl> resultOpt =
        dbi.withHandle(
            h ->
                h.createQuery(query).bind("refId", refId)
                    .registerRowMapper(BeanMapper.factory(EntityImpl.class, "i"))
                    .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                    .reduceRows(
                        new LinkedHashMap<UUID, EntityImpl>(),
                        (map, rowView) ->
                            addPreviewImageAndIdentifiers(map, rowView, EntityImpl.class, "e_uuid"))
                    .values().stream()
                    .findFirst());
    if (!resultOpt.isPresent()) {
      return null;
    }
    return (E) resultOpt.get();
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[] {"e.created", "e.last_modified", "e.identifiable_type", "e.entity_type"};
  }

  @Override
  public LinkedHashSet<FileResource> getRelatedFileResources(E entity) {
    return getRelatedFileResources(entity.getUuid());
  }

  @Override
  public LinkedHashSet<FileResource> getRelatedFileResources(UUID entityUuid) {
    String query =
        "SELECT * FROM fileresources f"
            + " INNER JOIN rel_entity_fileresources ref ON f.uuid=ref.fileresource_uuid"
            + " WHERE ref.entity_uuid = :entityUuid"
            + " ORDER BY ref.sortindex";

    List<FileResourceImpl> result =
        dbi.withHandle(
            h ->
                h.createQuery(query)
                    .bind("entityUuid", entityUuid)
                    .mapToBean(FileResourceImpl.class)
                    .list());
    return new LinkedHashSet<>(result);
  }

  @Override
  public List<EntityRelation> getRelations(E subjectEntity) {
    // query predicate and object entity (subject entity is given)
    String query =
        "SELECT rel.predicate as predicate, e.uuid as uuid, e.created as created, e.description as description, e.identifiable_type as identifiable_type, e.label as label, e.last_modified as last_modified, e.entity_type as entity_type"
            + " FROM rel_entity_entities rel"
            + " INNER JOIN entities e ON rel.object_uuid=e.uuid"
            + " WHERE rel.subject_uuid = :uuid";

    List<EntityRelation> result =
        dbi.withHandle(
            h ->
                h.createQuery(query)
                    .bind("uuid", subjectEntity.getUuid())
                    .map(new EntityRelationMapper(subjectEntity))
                    .list());
    return result;
  }

  @Override
  public List<EntityRelation> getRelations(UUID subjectEntityUuid) {
    E subjectEntity = findOne(subjectEntityUuid);
    return getRelations(subjectEntity);
  }

  @Override
  public E save(E entity) {
    throw new UnsupportedOperationException("use save of specific/inherited entity repository");
  }

  @Override
  public LinkedHashSet<FileResource> saveRelatedFileResources(
      E entity, LinkedHashSet<FileResource> fileResources) {
    return saveRelatedFileResources(entity.getUuid(), fileResources);
  }

  @Override
  public LinkedHashSet<FileResource> saveRelatedFileResources(
      UUID entityUuid, LinkedHashSet<FileResource> fileResources) {
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
                .bind("sortIndex", getIndex(fileResources, fileResource))
                .add();
          }
          preparedBatch.execute();
        });
    return getRelatedFileResources(entityUuid);
  }

  @Override
  public List<EntityRelation> saveRelations(List<EntityRelation> relations) {
    if (relations == null) {
      return null;
    }
    // get subject uuid:
    UUID subjectUuid = relations.get(0).getSubject().getUuid();
    // as we store the whole list new: delete old entries
    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM rel_entity_entities WHERE entity_uuid = :uuid")
                .bind("uuid", subjectUuid)
                .execute());

    dbi.useHandle(
        handle -> {
          PreparedBatch preparedBatch =
              handle.prepareBatch(
                  "INSERT INTO rel_entity_entities(subject_uuid, predicate, object_uuid) VALUES(:subjectUuid, :predicate, :objectUuid)");
          for (EntityRelation relation : relations) {
            preparedBatch
                .bind("subjectUuid", subjectUuid)
                .bind("predicate", relation.getPredicate())
                .bind("objectUuid", relation.getObject().getUuid())
                .add();
          }
          preparedBatch.execute();
        });
    return getRelations(subjectUuid);
  }

  @Override
  public E update(E entity) {
    throw new UnsupportedOperationException("use update of specific/inherited entity repository");
  }
}
