package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.parts;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.parts.EntityPartRepository;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.parts.EntityPartService;
import de.digitalcollections.cudami.server.business.impl.service.identifiable.IdentifiableServiceImpl;
import de.digitalcollections.model.api.filter.Filtering;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.parts.EntityPart;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class EntityPartServiceImpl<P extends EntityPart> extends IdentifiableServiceImpl<P>
    implements EntityPartService<P> {

  @Autowired
  public EntityPartServiceImpl(
      @Qualifier("entityPartRepositoryImpl") EntityPartRepository<P> repository) {
    super(repository);
  }

  @Override
  public void addRelatedEntity(UUID entityPartUuid, UUID entityUuid) {
    ((EntityPartRepository<P>) repository).addRelatedEntity(entityPartUuid, entityUuid);
  }

  @Override
  public void addRelatedFileresource(UUID entityPartUuid, UUID fileResourceUuid) {
    ((EntityPartRepository<P>) repository).addRelatedFileresource(entityPartUuid, fileResourceUuid);
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
  public List<Entity> getRelatedEntities(UUID entityPartUuid) {
    return ((EntityPartRepository<P>) repository).getRelatedEntities(entityPartUuid);
  }

  @Override
  public List<FileResource> getRelatedFileResources(UUID entityPartUuid) {
    return ((EntityPartRepository<P>) repository).getRelatedFileResources(entityPartUuid);
  }

  @Override
  public List<Entity> saveRelatedEntities(UUID entityPartUuid, List<Entity> entities) {
    return ((EntityPartRepository<P>) repository).saveRelatedEntities(entityPartUuid, entities);
  }

  @Override
  public List<FileResource> saveRelatedFileResources(
      UUID entityPartUuid, List<FileResource> fileResources) {
    return ((EntityPartRepository<P>) repository)
        .saveRelatedFileResources(entityPartUuid, fileResources);
  }
}
