package de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.entity.parts;

import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity.parts.EntityPartRepository;
import de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.IdentifiableRepositoryImpl;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.parts.EntityPart;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import java.util.LinkedHashSet;
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

  protected LinkedHashSet<Entity> convertFromGenericLinkedHashSet(LinkedHashSet<E> entities) {
    if (entities == null) {
      return null;
    }
    LinkedHashSet<Entity> result =
        entities.stream().map(s -> (Entity) s).collect(Collectors.toCollection(LinkedHashSet::new));
    return result;
  }

  protected LinkedHashSet<E> convertToGenericLinkedHashSet(LinkedHashSet<Entity> entities) {
    if (entities == null) {
      return null;
    }
    LinkedHashSet<E> genericContent =
        entities.stream().map(s -> (E) s).collect(Collectors.toCollection(LinkedHashSet::new));
    return genericContent;
  }

  @Override
  public LinkedHashSet<E> getRelatedEntities(P entityPart) {
    return getRelatedEntities(entityPart.getUuid());
  }

  @Override
  public LinkedHashSet<E> getRelatedEntities(UUID entityPartUuid) {
    return convertToGenericLinkedHashSet(endpoint.getRelatedEntities(entityPartUuid));
  }

  @Override
  public LinkedHashSet<FileResource> getRelatedFileResources(P entityPart) {
    return getRelatedFileResources(entityPart.getUuid());
  }

  @Override
  public LinkedHashSet<FileResource> getRelatedFileResources(UUID entityPartUuid) {
    return endpoint.getRelatedFileResources(entityPartUuid);
  }

  @Override
  public LinkedHashSet<E> saveRelatedEntities(P entityPart, LinkedHashSet<E> entities) {
    return saveRelatedEntities(entityPart.getUuid(), entities);
  }

  @Override
  public LinkedHashSet<E> saveRelatedEntities(UUID entityPartUuid, LinkedHashSet<E> entities) {
    return convertToGenericLinkedHashSet(
        endpoint.saveRelatedEntities(entityPartUuid, convertFromGenericLinkedHashSet(entities)));
  }

  @Override
  public LinkedHashSet<FileResource> saveRelatedFileResources(
      P entityPart, LinkedHashSet<FileResource> fileResources) {
    return saveRelatedFileResources(entityPart.getUuid(), fileResources);
  }

  @Override
  public LinkedHashSet<FileResource> saveRelatedFileResources(
      UUID entityPartUuid, LinkedHashSet<FileResource> fileResources) {
    return endpoint.saveRelatedFileResources(entityPartUuid, fileResources);
  }
}
