package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work;

import de.digitalcollections.model.identifiable.entity.work.Involvement;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.UUID;

public interface InvolvementRepository {

  long count();

  Involvement getByUuid(UUID uuid);

  Involvement save(Involvement involvement);

  Involvement update(Involvement involvement);

  default boolean delete(UUID uuid) {
    return delete(List.of(uuid));
  }

  boolean delete(List<UUID> uuids);

  PageResponse<Involvement> find(PageRequest pageRequest);
}
