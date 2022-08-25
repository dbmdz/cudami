package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.work;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work.ExpressionTypeRepository;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.ExpressionTypeService;
import de.digitalcollections.model.identifiable.entity.work.ExpressionType;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("expressionTypeService")
@Transactional(rollbackFor = Exception.class)
public class ExpressionTypeServiceImpl implements ExpressionTypeService {

  private final ExpressionTypeRepository repository;

  public ExpressionTypeServiceImpl(ExpressionTypeRepository expressionTypeRepository) {
    this.repository = expressionTypeRepository;
  }

  @Override
  public long count() {
    return repository.count();
  }

  @Override
  public ExpressionType getByUuid(UUID uuid) {
    return repository.getByUuid(uuid);
  }

  @Override
  public ExpressionType save(ExpressionType expressionType) {
    return repository.save(expressionType);
  }

  @Override
  public ExpressionType update(ExpressionType expressionType) {
    return repository.update(expressionType);
  }

  @Override
  public boolean delete(List<UUID> uuids) {
    return repository.delete(uuids);
  }

  @Override
  public PageResponse<ExpressionType> find(PageRequest pageRequest) {
    return repository.find(pageRequest);
  }
}
