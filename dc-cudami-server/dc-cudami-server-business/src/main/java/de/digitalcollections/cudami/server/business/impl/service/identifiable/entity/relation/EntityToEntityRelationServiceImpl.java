package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.relation;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.relation.EntityToEntityRelationRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.relation.EntityToEntityRelationService;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.relation.EntityRelation;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = {Exception.class})
public class EntityToEntityRelationServiceImpl implements EntityToEntityRelationService {

  private final EntityToEntityRelationRepository repository;

  @Autowired
  public EntityToEntityRelationServiceImpl(EntityToEntityRelationRepository repository) {
    this.repository = repository;
  }

  @Override
  public void addRelation(EntityRelation relation) throws ServiceException {
    try {
      repository.addRelation(relation);
    } catch (RepositoryException e) {
      throw new ServiceException(
          "Cannot add the relation: %s %s %s"
              .formatted(relation.getSubject(), relation.getPredicate(), relation.getObject()),
          e);
    }
  }

  @Override
  public void deleteByObject(Entity objectEntity) throws ServiceException {
    try {
      repository.deleteByObject(objectEntity);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public void deleteBySubject(Entity subjectEntity) throws ServiceException {
    try {
      repository.deleteBySubject(subjectEntity);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  private Entity extractEntityWithUuidOnly(Entity entity) {
    Entity entityWithUuidOnly;

    try {
      entityWithUuidOnly = entity.getClass().getConstructor().newInstance();
      entityWithUuidOnly.setUuid(entity.getUuid());
    } catch (NoSuchMethodException
        | InstantiationException
        | IllegalAccessException
        | InvocationTargetException e) {
      // For whatever reason, we cannot construct the entity, so
      // as a fallback, we construct an Entity object manually
      // and set the UUID
      entityWithUuidOnly = Entity.builder().uuid(entity.getUuid()).build();
    }

    return entityWithUuidOnly;
  }

  @Override
  public PageResponse<EntityRelation> find(PageRequest pageRequest) throws ServiceException {
    try {
      return repository.find(pageRequest);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public PageResponse<EntityRelation> findBySubject(Entity subjectEntity, PageRequest pageRequest)
      throws ServiceException {
    try {
      return repository.findBySubject(subjectEntity, pageRequest);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public void save(List<EntityRelation> entityRelations) throws ServiceException {
    // We assume, that all referenced predicates, the "normal" and the additional
    // ones
    // are already available in the service. If not, the repository would throw
    // a ForeignKey exception
    try {
      repository.save(entityRelations);
    } catch (Exception e) {
      throw new ServiceException("Cannot persist EntityRelations " + entityRelations + ": " + e, e);
    }
  }

  @Override
  public void setEntityRelations(
      Entity entity, List<EntityRelation> relations, boolean deleteExisting)
      throws ServiceException {
    if (deleteExisting) {
      // Check, if there are already persisted EntityRelations for the entity
      // If yes, delete them
      deleteByObject(entity);
    }

    // save all entity relations and set the UUID of the object
    relations.stream().forEach(r -> r.setObject(extractEntityWithUuidOnly(entity)));
    save(relations);
  }
}
