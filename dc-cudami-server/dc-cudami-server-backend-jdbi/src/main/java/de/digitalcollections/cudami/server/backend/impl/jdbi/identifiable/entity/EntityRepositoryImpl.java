package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EntityRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifiableRepositoryImpl;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.EntityRelation;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.EntityImpl;
import de.digitalcollections.model.impl.identifiable.resource.FileResourceImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
public class EntityRepositoryImpl<E extends Entity> extends IdentifiableRepositoryImpl<E> implements EntityRepository<E> {

  protected final Jdbi dbi;
  private final IdentifiableRepository identifiableRepository;

  @Autowired
  public EntityRepositoryImpl(Jdbi dbi, @Qualifier("identifiableRepositoryImpl") IdentifiableRepository identifiableRepository) {
    this.dbi = dbi;
    this.identifiableRepository = identifiableRepository;
  }

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM entities";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOnly());
    return count;
  }

  @Override
  public PageResponse<E> find(PageRequest pageRequest) {
    StringBuilder query = new StringBuilder("SELECT e.entity_type as entityType, e.uuid as uuid, i.label as label, i.description as description")
        .append(" FROM entities e INNER JOIN identifiables i ON e.uuid=i.uuid");

    addPageRequestParams(pageRequest, query);
    List<EntityImpl> result = dbi.withHandle(h -> h.createQuery(query.toString())
        .mapToBean(EntityImpl.class)
        .list());
    long total = count();
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public E findOne(UUID uuid) {
    String query = "SELECT e.entity_type as entityType, e.uuid as uuid, i.label as label, i.description as description"
                   + " FROM entities e INNER JOIN identifiables i ON e.uuid=i.uuid"
                   + " WHERE e.uuid = :uuid";

    E entity = (E) dbi.withHandle(h -> h.createQuery(query)
        .bind("uuid", uuid)
        .mapToBean(EntityImpl.class)
        .findOnly());
    return entity;
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[]{"created", "entity_type", "last_modified"};
  }

  @Override
  public E save(E entity) {
    identifiableRepository.save(entity);
    dbi.withHandle(h -> h.createUpdate("INSERT INTO entities(entity_type, uuid) VALUES (:entityType, :uuid)")
        .bindBean(entity)
        .execute());
    return findOne(entity.getUuid());
  }

  @Override
  public E update(E entity) {
    identifiableRepository.update(entity);
    // do not update/left out from statement: created, uuid
    dbi.withHandle(h -> h.createUpdate("UPDATE entities SET entity_type=:entityType WHERE uuid=:uuid")
        .bindBean(entity)
        .execute());
    return findOne(entity.getUuid());
  }

  @Override
  public void addRelatedFileresource(E entity, FileResource fileResource) {
    addRelatedFileresource(entity.getUuid(), fileResource.getUuid());
  }

  @Override
  public void addRelatedFileresource(UUID entityUuid, UUID fileResourceUuid) {
    Integer sortIndex = selectNextSortIndexForParentChildren(dbi, "rel_entity_fileresources", "entity_uuid", entityUuid);
    dbi.withHandle(h -> h.createUpdate("INSERT INTO rel_entity_fileresources(entity_uuid, fileresource_uuid, sortindex) VALUES (:entity_uuid, :fileresource_uuid, :sortindex)")
        .bind("entity_uuid", entityUuid)
        .bind("fileresource_uuid", fileResourceUuid)
        .bind("sortindex", sortIndex)
        .execute());
  }

  @Override
  public void addRelation(EntityRelation<E> relation) {
    addRelation(relation.getSubject().getUuid(), relation.getPredicate(), relation.getObject().getUuid());
  }

  @Override
  public void addRelation(UUID subjectEntityUuid, String predicate, UUID objectEntityUuid) {
    dbi.withHandle(h -> h.createUpdate("INSERT INTO rel_entity_entities(subject_uuid, predicate, object_uuid) VALUES (:subject_uuid, :predicate, :object_uuid)")
        .bind("subject_uuid", subjectEntityUuid)
        .bind("predicate", predicate)
        .bind("object_uuid", objectEntityUuid)
        .execute());
  }

  @Override
  public LinkedHashSet<FileResource> getRelatedFileResources(E entity) {
    return getRelatedFileResources(entity.getUuid());
  }

  @Override
  public LinkedHashSet<FileResource> getRelatedFileResources(UUID entityUuid) {
    StringBuilder query = new StringBuilder("SELECT *")
        .append(" FROM fileresources f INNER JOIN rel_entity_fileresources ref ON f.uuid=ref.fileresource_uuid")
        .append(" WHERE ref.entity_uuid = :entityUuid")
        .append(" ORDER BY ref.sortindex");

    List<FileResourceImpl> result = dbi.withHandle(h -> h.createQuery(query.toString())
        .bind("entityUuid", entityUuid)
        .mapToBean(FileResourceImpl.class)
        .list());
    return new LinkedHashSet<>(result);
  }

  @Override
  public List<EntityRelation> getRelations(E subjectEntity) {
    StringBuilder query = new StringBuilder("SELECT rel.predicate as predicate, e.uuid as uuid, e.created as created, e.description as description. e.identifiable_type as identifiable_type, e.label as label, e.last_modified as last_modified, e.entity_type as entity_type")
        .append(" FROM rel_entity_entities rel INNER JOIN entities e ON rel.object_uuid=e.uuid")
        .append(" WHERE rel.subject_uuid = :uuid");

    List<EntityRelation> result = dbi.withHandle(h -> h.createQuery(query.toString())
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
  public LinkedHashSet<FileResource> saveRelatedFileResources(E entity, LinkedHashSet<FileResource> fileResources) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public LinkedHashSet<FileResource> saveRelatedFileResources(UUID entityUuid, LinkedHashSet<UUID> fileResourcesUuids) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public List<EntityRelation> saveRelations(List<EntityRelation> relations) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

}
