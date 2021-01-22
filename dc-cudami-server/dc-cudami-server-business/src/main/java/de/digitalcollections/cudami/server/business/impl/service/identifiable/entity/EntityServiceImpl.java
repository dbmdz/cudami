package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EntityRepository;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.EntityService;
import de.digitalcollections.cudami.server.business.impl.service.identifiable.IdentifiableServiceImpl;
import de.digitalcollections.model.api.filter.Filtering;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class EntityServiceImpl<E extends Entity> extends IdentifiableServiceImpl<E>
    implements EntityService<E> {

  @Autowired
  public EntityServiceImpl(@Qualifier("entityRepositoryImpl") EntityRepository<E> repository) {
    super(repository);
  }

  @Override
  public void addRelatedFileresource(E entity, FileResource fileResource) {
    ((EntityRepository<E>) repository).addRelatedFileresource(entity, fileResource);
  }

  @Override
  public void addRelatedFileresource(UUID entityUuid, UUID fileResourceUuid) {
    ((EntityRepository<E>) repository).addRelatedFileresource(entityUuid, fileResourceUuid);
  }

  protected Filtering filteringForActive() {
    // business logic that defines, what "active" means
    LocalDate now = LocalDate.now();
    Filtering filtering =
        Filtering.defaultBuilder()
            .filter("publicationStart")
            .lessOrEqualAndSet(now)
            .filter("publicationEnd")
            .greaterOrNotSet(now)
            .build();
    return filtering;
  }

  @Override
  public E getByRefId(long refId) {
    return ((EntityRepository<E>) repository).findOneByRefId(refId);
  }

  @Override
  public List<FileResource> getRelatedFileResources(E entity) {
    return ((EntityRepository<E>) repository).getRelatedFileResources(entity);
  }

  @Override
  public List<FileResource> getRelatedFileResources(UUID entityUuid) {
    return ((EntityRepository<E>) repository).getRelatedFileResources(entityUuid);
  }

  @Override
  public List<FileResource> saveRelatedFileResources(E entity, List<FileResource> fileResources) {
    return ((EntityRepository<E>) repository).saveRelatedFileResources(entity, fileResources);
  }

  @Override
  public List<FileResource> saveRelatedFileResources(
      UUID entityUuid, List<FileResource> fileResources) {
    return ((EntityRepository<E>) repository).saveRelatedFileResources(entityUuid, fileResources);
  }
}
