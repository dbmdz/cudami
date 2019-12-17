package de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.entity;

import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity.EntityRepository;
import de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.IdentifiableRepositoryImpl;
import de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.IdentifiableRepositoryImpl.FindParams;
import de.digitalcollections.model.api.http.exceptions.client.ResourceNotFoundException;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.EntityRelation;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.EntityImpl;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class EntityRepositoryImpl<E extends Entity> extends IdentifiableRepositoryImpl<E>
    implements EntityRepository<E> {

  @Autowired private EntityRepositoryEndpoint endpoint;

  @Override
  public void addRelatedFileresource(E entity, FileResource fileResource) {
    addRelatedFileresource(entity.getUuid(), fileResource.getUuid());
  }

  @Override
  public void addRelatedFileresource(UUID entityUuid, UUID fileResourceUuid) {
    endpoint.addRelatedFileresource(entityUuid, fileResourceUuid);
  }

  @Override
  public void addRelation(EntityRelation<E> relation) {
    addRelation(
        relation.getSubject().getUuid(), relation.getPredicate(), relation.getObject().getUuid());
  }

  @Override
  public void addRelation(UUID subjectEntityUuid, String predicate, UUID objectEntityUuid) {
    endpoint.addRelation(subjectEntityUuid, predicate, objectEntityUuid);
  }

  @Override
  public long count() {
    return endpoint.count();
  }

  @Override
  public E create() {
    return (E) new EntityImpl();
  }

  @Override
  public PageResponse<E> find(PageRequest pageRequest) {
    FindParams f = getFindParams(pageRequest);
    PageResponse<Entity> pageResponse =
        endpoint.find(
            f.getPageNumber(),
            f.getPageSize(),
            f.getSortField(),
            f.getSortDirection(),
            f.getNullHandling());
    return getGenericPageResponse(pageResponse);
  }

  @Override
  public E findOneByIdentifier(String namespace, String id) {
    try {
      return (E) endpoint.findOneByIdentifier(namespace, id);
    } catch (ResourceNotFoundException e) {
      return null;
    }
  }

  @Override
  public E findOne(UUID uuid) {
    return (E) endpoint.findOne(uuid);
  }

  @Override
  public E findOne(UUID uuid, Locale locale) {
    return (E) endpoint.findOne(uuid, locale.toString());
  }

  @Override
  public LinkedHashSet<FileResource> getRelatedFileResources(E entity) {
    return getRelatedFileResources(entity.getUuid());
  }

  @Override
  public LinkedHashSet<FileResource> getRelatedFileResources(UUID entityUuid) {
    return endpoint.getRelatedFileResources(entityUuid);
  }

  @Override
  public List<EntityRelation> getRelations(E subjectEntity) {
    return getRelations(subjectEntity.getUuid());
  }

  @Override
  public List<EntityRelation> getRelations(UUID subjectEntityUuid) {
    return endpoint.getRelations(subjectEntityUuid);
  }

  @Override
  public E save(E identifiable) {
    return (E) endpoint.save(identifiable);
  }

  @Override
  public LinkedHashSet<FileResource> saveRelatedFileResources(
      E entity, LinkedHashSet<FileResource> fileResources) {
    return saveRelatedFileResources(entity.getUuid(), fileResources);
  }

  @Override
  public LinkedHashSet<FileResource> saveRelatedFileResources(
      UUID entityUuid, LinkedHashSet<FileResource> fileResources) {
    return endpoint.saveRelatedFileResources(entityUuid, fileResources);
  }

  @Override
  public List<EntityRelation> saveRelations(List<EntityRelation> relations) {
    return endpoint.saveRelations(relations);
  }

  @Override
  public E update(E identifiable) {
    return (E) endpoint.update(identifiable.getUuid(), identifiable);
  }
}
