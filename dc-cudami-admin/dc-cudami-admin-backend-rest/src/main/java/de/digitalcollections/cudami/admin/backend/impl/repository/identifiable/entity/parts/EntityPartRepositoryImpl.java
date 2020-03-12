package de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.entity.parts;

import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity.parts.EntityPartRepository;
import de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.IdentifiableRepositoryImpl;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.parts.EntityPart;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class EntityPartRepositoryImpl<P extends EntityPart, E extends Entity>
    extends IdentifiableRepositoryImpl<P> implements EntityPartRepository<P, E> {

  @Autowired private EntityPartRepositoryEndpoint endpoint;

  @Override
  public void addRelatedEntity(EntityPart entityPart, Entity entity) {
    addRelatedEntity(entityPart.getUuid(), entity.getUuid());
  }

  @Override
  public void addRelatedEntity(UUID entityPartUuid, UUID entityUuid) {
    endpoint.addRelatedEntity(entityPartUuid, entityUuid);
  }

  @Override
  public void addRelatedFileresource(EntityPart entityPart, FileResource fileResource) {
    addRelatedFileresource(entityPart.getUuid(), fileResource.getUuid());
  }

  @Override
  public void addRelatedFileresource(UUID entityPartUuid, UUID fileResourceUuid) {
    endpoint.addRelatedFileresource(entityPartUuid, fileResourceUuid);
  }

  @Override
  public List<E> getRelatedEntities(P entityPart) {
    return getRelatedEntities(entityPart.getUuid());
  }

  @Override
  public List<E> getRelatedEntities(UUID entityPartUuid) {
    List<Entity> relatedEntities = endpoint.getRelatedEntities(entityPartUuid);
    return relatedEntities.stream().map(e -> (E) e).collect(Collectors.toList());
  }

  @Override
  public List<FileResource> getRelatedFileResources(P entityPart) {
    return getRelatedFileResources(entityPart.getUuid());
  }

  @Override
  public List<FileResource> getRelatedFileResources(UUID entityPartUuid) {
    return endpoint.getRelatedFileResources(entityPartUuid);
  }

  @Override
  public List<E> saveRelatedEntities(P entityPart, List<E> entities) {
    return saveRelatedEntities(entityPart.getUuid(), entities);
  }

  @Override
  public List<E> saveRelatedEntities(UUID entityPartUuid, List<E> entities) {
    List<Entity> relatedEntities =
        endpoint.saveRelatedEntities(
            entityPartUuid, entities.stream().map(Entity.class::cast).collect(Collectors.toList()));
    return relatedEntities.stream().map(e -> (E) e).collect(Collectors.toList());
  }

  @Override
  public List<FileResource> saveRelatedFileResources(
      P entityPart, List<FileResource> fileResources) {
    return saveRelatedFileResources(entityPart.getUuid(), fileResources);
  }

  @Override
  public List<FileResource> saveRelatedFileResources(
      UUID entityPartUuid, List<FileResource> fileResources) {
    return endpoint.saveRelatedFileResources(entityPartUuid, fileResources);
  }
}
