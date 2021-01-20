package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.EntityRelationsService;
import de.digitalcollections.model.api.identifiable.entity.EntityRelation;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.relation.EntityRelationRepository;

@Service
public class EntityRelationsServiceImpl implements EntityRelationsService {

  private final EntityRelationRepository repository;

  @Autowired
  public EntityRelationsServiceImpl(EntityRelationRepository repository) {
    this.repository = repository;
  }

  @Override
  public PageResponse<EntityRelation> find(PageRequest pageRequest) {
    return repository.find(pageRequest);
  }

  @Override
  public List<EntityRelation> saveEntityRelations(List<EntityRelation> entityRelations) {
    return repository.save(entityRelations);
  }
}
