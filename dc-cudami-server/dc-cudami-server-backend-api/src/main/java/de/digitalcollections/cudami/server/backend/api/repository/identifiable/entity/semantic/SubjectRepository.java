package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.semantic;

import de.digitalcollections.model.identifiable.entity.semantic.Subject;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.UUID;

public interface SubjectRepository {

  long count();

  Subject getByUuid(UUID uuid);

  Subject save(Subject subject);

  Subject update(Subject subject);

  default boolean delete(UUID uuid) {
    return delete(List.of(uuid));
  }

  boolean delete(List<UUID> uuids);

  PageResponse<Subject> find(PageRequest pageRequest);

  Subject getByTypeAndIdentifier(String type, String namespace, String id);
}
