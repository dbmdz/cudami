package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.parts;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.parts.EntityPartRepository;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.parts.EntityPartService;
import de.digitalcollections.cudami.server.business.impl.service.identifiable.IdentifiableServiceImpl;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.parts.EntityPart;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class EntityPartServiceImpl<P extends EntityPart, E extends Entity>
    extends IdentifiableServiceImpl<P> implements EntityPartService<P, E> {

  @Autowired
  public EntityPartServiceImpl(
      @Qualifier("entityPartRepositoryImpl") EntityPartRepository<P, E> repository) {
    super(repository);
  }

  @Override
  public void addRelatedEntity(P entityPart, E entity) {
    ((EntityPartRepository) repository).addRelatedEntity(entityPart, entity);
  }

  @Override
  public void addRelatedEntity(UUID entityPartUuid, UUID entityUuid) {
    ((EntityPartRepository) repository).addRelatedEntity(entityPartUuid, entityUuid);
  }

  @Override
  public void addRelatedFileresource(P entityPart, FileResource fileResource) {
    ((EntityPartRepository) repository).addRelatedFileresource(entityPart, fileResource);
  }

  @Override
  public void addRelatedFileresource(UUID entityPartUuid, UUID fileResourceUuid) {
    ((EntityPartRepository) repository).addRelatedFileresource(entityPartUuid, fileResourceUuid);
  }

  @Override
  public List<E> getRelatedEntities(P entityPart) {
    return ((EntityPartRepository) repository).getRelatedEntities(entityPart);
  }

  @Override
  public List<E> getRelatedEntities(UUID entityPartUuid) {
    return ((EntityPartRepository) repository).getRelatedEntities(entityPartUuid);
  }

  @Override
  public List<FileResource> getRelatedFileResources(P entityPart) {
    return ((EntityPartRepository) repository).getRelatedFileResources(entityPart);
  }

  @Override
  public List<FileResource> getRelatedFileResources(UUID entityPartUuid) {
    return ((EntityPartRepository) repository).getRelatedFileResources(entityPartUuid);
  }

  @Override
  public List<E> saveRelatedEntities(P entityPart, List<E> entities) {
    return ((EntityPartRepository) repository).saveRelatedEntities(entityPart, entities);
  }

  @Override
  public List<E> saveRelatedEntities(UUID entityPartUuid, List<E> entities) {
    return ((EntityPartRepository) repository).saveRelatedEntities(entityPartUuid, entities);
  }

  @Override
  public List<FileResource> saveRelatedFileResources(
      P entityPart, List<FileResource> fileResources) {
    return ((EntityPartRepository) repository).saveRelatedFileResources(entityPart, fileResources);
  }

  @Override
  public List<FileResource> saveRelatedFileResources(
      UUID entityPartUuid, List<FileResource> fileResources) {
    return ((EntityPartRepository) repository)
        .saveRelatedFileResources(entityPartUuid, fileResources);
  }
}
