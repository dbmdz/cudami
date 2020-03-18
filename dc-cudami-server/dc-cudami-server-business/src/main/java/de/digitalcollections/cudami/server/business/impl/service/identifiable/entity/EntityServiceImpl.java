package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EntityRepository;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.EntityService;
import de.digitalcollections.cudami.server.business.impl.service.identifiable.IdentifiableServiceImpl;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.EntityRelation;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
// @Transactional(readOnly = true)
public class EntityServiceImpl<E extends Entity> extends IdentifiableServiceImpl<E>
    implements EntityService<E> {

  @Autowired
  public EntityServiceImpl(@Qualifier("entityRepositoryImpl") EntityRepository<E> repository) {
    super(repository);
  }

  @Override
  public void addRelatedFileresource(E entity, FileResource fileResource) {
    ((EntityRepository) repository).addRelatedFileresource(entity, fileResource);
  }

  @Override
  public void addRelatedFileresource(UUID entityUuid, UUID fileResourceUuid) {
    ((EntityRepository) repository).addRelatedFileresource(entityUuid, fileResourceUuid);
  }

  @Override
  public void addRelation(EntityRelation<E> relation) {
    ((EntityRepository) repository).addRelation(relation);
  }

  @Override
  public void addRelation(UUID subjectEntityUuid, String predicate, UUID objectEntityUuid) {
    ((EntityRepository) repository).addRelation(subjectEntityUuid, predicate, objectEntityUuid);
  }

  @Override
  public E getByRefId(long refId) {
    return (E) ((EntityRepository) repository).findOneByRefId(refId);
  }

  @Override
  public List<FileResource> getRelatedFileResources(E entity) {
    return ((EntityRepository) repository).getRelatedFileResources(entity);
  }

  @Override
  public List<FileResource> getRelatedFileResources(UUID entityUuid) {
    return ((EntityRepository) repository).getRelatedFileResources(entityUuid);
  }

  @Override
  public List<EntityRelation> getRelations(E subjectEntity) {
    return ((EntityRepository) repository).getRelations(subjectEntity);
  }

  @Override
  public List<EntityRelation> getRelations(UUID subjectEntityUuid) {
    return ((EntityRepository) repository).getRelations(subjectEntityUuid);
  }

  @Override
  public List<FileResource> saveRelatedFileResources(E entity, List<FileResource> fileResources) {
    return ((EntityRepository) repository).saveRelatedFileResources(entity, fileResources);
  }

  @Override
  public List<FileResource> saveRelatedFileResources(
      UUID entityUuid, List<FileResource> fileResources) {
    return ((EntityRepository) repository).saveRelatedFileResources(entityUuid, fileResources);
  }

  @Override
  public List<EntityRelation> saveRelations(List<EntityRelation> relations) {
    return ((EntityRepository) repository).saveRelations(relations);
  }
}
