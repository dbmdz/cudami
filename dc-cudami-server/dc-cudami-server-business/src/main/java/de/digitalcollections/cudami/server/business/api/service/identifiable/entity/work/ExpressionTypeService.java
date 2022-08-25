package de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work;

import de.digitalcollections.model.identifiable.entity.work.ExpressionType;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.UUID;

public interface ExpressionTypeService {

  long count();

  ExpressionType getByUuid(UUID uuid);

  ExpressionType save(ExpressionType expressionType);

  ExpressionType update(ExpressionType expressionType);

  boolean delete(List<UUID> uuids);

  PageResponse<ExpressionType> find(PageRequest pageRequest);
}
