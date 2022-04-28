package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.relation;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.relation.EntityRelationRepository;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.relation.EntityRelationService;
import de.digitalcollections.model.identifiable.entity.relation.EntityRelation;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import java.util.List;
import java.util.UUID;
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
  public void addRelation(UUID subjectEntityUuid, String predicate, UUID objectEntityUuid) {
    repository.addRelation(subjectEntityUuid, predicate, objectEntityUuid);
  }

  @Override
  public void deleteBySubject(UUID subjectEntityUuid) {
    repository.deleteBySubject(subjectEntityUuid);
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
  public List<EntityRelation> save(List<EntityRelation> entityRelations) {
    return repository.save(entityRelations);
  }
}
