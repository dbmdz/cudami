package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.parts;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.parts.EntityPartRepository;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.parts.EntityPartService;
import de.digitalcollections.cudami.server.business.impl.service.identifiable.IdentifiableServiceImpl;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.parts.EntityPart;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import java.util.LinkedHashSet;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class EntityPartServiceImpl<EP extends EntityPart, E extends Entity> extends IdentifiableServiceImpl<EP> implements EntityPartService<EP, E> {

  @Autowired
  public EntityPartServiceImpl(@Qualifier("entityPartRepositoryImpl") EntityPartRepository<EP, E> repository) {
    super(repository);
  }

  @Override
  public void addRelatedEntity(EP entityPart, E entity) {
    ((EntityPartRepository) repository).addRelatedEntity(entityPart, entity);
  }

  @Override
  public void addRelatedEntity(UUID entityPartUuid, UUID entityUuid) {
    ((EntityPartRepository) repository).addRelatedEntity(entityPartUuid, entityUuid);
  }

  @Override
  public void addRelatedFileresource(EP entityPart, FileResource fileResource) {
    ((EntityPartRepository) repository).addRelatedFileresource(entityPart, fileResource);
  }

  @Override
  public void addRelatedFileresource(UUID entityPartUuid, UUID fileResourceUuid) {
    ((EntityPartRepository) repository).addRelatedFileresource(entityPartUuid, fileResourceUuid);
  }

  @Override
  public LinkedHashSet<E> getRelatedEntities(EP entityPart) {
    return ((EntityPartRepository) repository).getRelatedEntities(entityPart);
  }

  @Override
  public LinkedHashSet<E> getRelatedEntities(UUID entityPartUuid) {
    return ((EntityPartRepository) repository).getRelatedEntities(entityPartUuid);
  }

  @Override
  public LinkedHashSet<FileResource> getRelatedFileResources(EP entityPart) {
    return ((EntityPartRepository) repository).getRelatedFileResources(entityPart);
  }

  @Override
  public LinkedHashSet<FileResource> getRelatedFileResources(UUID entityPartUuid) {
    return ((EntityPartRepository) repository).getRelatedFileResources(entityPartUuid);
  }

  @Override
  public LinkedHashSet<E> saveRelatedEntities(EP entityPart, LinkedHashSet<E> entities) {
    return ((EntityPartRepository) repository).saveRelatedEntities(entityPart, entities);
  }

  @Override
  public LinkedHashSet<E> saveRelatedEntities(UUID entityPartUuid, LinkedHashSet<E> entities) {
    return ((EntityPartRepository) repository).saveRelatedEntities(entityPartUuid, entities);
  }

  @Override
  public LinkedHashSet<FileResource> saveRelatedFileResources(EP entityPart, LinkedHashSet<FileResource> fileResources) {
    return ((EntityPartRepository) repository).saveRelatedFileResources(entityPart, fileResources);
  }

  @Override
  public LinkedHashSet<FileResource> saveRelatedFileResources(UUID entityPartUuid, LinkedHashSet<FileResource> fileResources) {
    return ((EntityPartRepository) repository).saveRelatedFileResources(entityPartUuid, fileResources);
  }

}
