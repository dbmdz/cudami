package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.semantic;

import de.digitalcollections.cudami.server.backend.api.repository.UniqueObjectRepository;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.model.semantic.Subject;

public interface SubjectRepository extends UniqueObjectRepository<Subject> {

  Subject getByTypeAndIdentifier(String type, String namespace, String id)
      throws RepositoryException;
}
