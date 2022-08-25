package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work;

import de.digitalcollections.model.identifiable.entity.work.Publication;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.UUID;

public interface PublicationRepository {

  long count();

  Publication getByUuid(UUID uuid);

  Publication save(Publication publication);

  Publication update(Publication publication);

  default boolean delete(UUID uuid) {
    return delete(List.of(uuid));
  }

  boolean delete(List<UUID> uuids);

  PageResponse<Publication> find(PageRequest pageRequest);
}
