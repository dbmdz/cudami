package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.relation;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.relation.EntityRelationRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.relation.EntityRelationService;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.relation.EntityRelation;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = {Exception.class})
public class EntityRelationServiceImpl implements EntityRelationService {

  private final EntityRelationRepository repository;

  @Autowired
  public EntityRelationServiceImpl(EntityRelationRepository repository) {
    this.repository = repository;
  }

  @Override
  public void addRelation(UUID subjectEntityUuid, String predicate, UUID objectEntityUuid)
      throws ServiceException {
    try {
      repository.addRelation(subjectEntityUuid, predicate, objectEntityUuid);
    } catch (RepositoryException e) {
      throw new ServiceException(
          "Cannot add the relation: %s %s %s"
              .formatted(subjectEntityUuid, predicate, objectEntityUuid),
          e);
    }
  }

  @Override
  public void deleteBySubject(UUID subjectEntityUuid) {
    repository.deleteBySubject(subjectEntityUuid);
  }

  @Override
  public void deleteByObject(UUID objectEntityUuid) {
    repository.deleteByObject(objectEntityUuid);
  }

  @Override
  public PageResponse<EntityRelation> find(PageRequest pageRequest) {
    return repository.find(pageRequest);
  }

  @Override
  public List<EntityRelation> getBySubject(UUID subjectEntityUuid) {
    return repository.getBySubject(subjectEntityUuid);
  }

  @Override
  public void save(List<EntityRelation> entityRelations) throws ServiceException {
    // We assume, that all referenced predicates, the "normal" and the additional ones
    // are already available in the service. If not, the repository would throw
    // a ForeignKey exception
    try {
      repository.save(entityRelations);
    } catch (Exception e) {
      throw new ServiceException("Cannot persist EntityRelations " + entityRelations + ": " + e, e);
    }
  }

  @Override
  public void persistEntityRelations(
      Entity entity,
      List<EntityRelation> relations,
      boolean deleteExisting,
      Entity entityWithUuidOnly)
      throws ServiceException {
    if (deleteExisting) {
      // Check, if there are already persisted EntityRelations for the entity
      // If yes, delete them
      deleteByObject(entity);
    }

    // save all entity relations and set the UUID of the object
    List<EntityRelation> relationsToSave =
        relations.stream()
            .map(
                r -> {
                  r.setObject(entityWithUuidOnly);
                  return r;
                })
            .collect(Collectors.toList());
    save(relationsToSave);
  }
}
