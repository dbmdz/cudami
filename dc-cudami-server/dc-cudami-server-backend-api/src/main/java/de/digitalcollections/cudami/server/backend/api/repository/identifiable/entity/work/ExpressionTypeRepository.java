package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work;

import de.digitalcollections.model.identifiable.entity.work.ExpressionType;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.UUID;

public interface ExpressionTypeRepository {

  long count();

  ExpressionType getByUuid(UUID uuid);

  ExpressionType save(ExpressionType expressionType);

  ExpressionType update(ExpressionType expressionType);

  default boolean delete(UUID uuid) {
    return delete(List.of(uuid));
  }

  boolean delete(List<UUID> uuids);

  PageResponse<ExpressionType> find(PageRequest pageRequest);
}
