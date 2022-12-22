package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.semantic;

import de.digitalcollections.cudami.server.backend.api.repository.UniqueObjectRepository;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.semantic.Subject;
import java.util.List;
import java.util.UUID;

public interface SubjectRepository extends UniqueObjectRepository<Subject> {

  long count();

  Subject getByUuid(UUID uuid);

  void save(Subject subject) throws RepositoryException;

  void update(Subject subject) throws RepositoryException;

  default boolean delete(UUID uuid) {
    return delete(List.of(uuid));
  }

  boolean delete(List<UUID> uuids);

  PageResponse<Subject> find(PageRequest pageRequest);

  Subject getByTypeAndIdentifier(String type, String namespace, String id);
}
