package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EntityRelationsRepository;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.EntityRelationsService;
import de.digitalcollections.model.api.identifiable.entity.EntityRelation;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EntityRelationsServiceImpl implements EntityRelationsService {

  private final EntityRelationsRepository repository;

  @Autowired
  public EntityRelationsServiceImpl(EntityRelationsRepository repository) {
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
