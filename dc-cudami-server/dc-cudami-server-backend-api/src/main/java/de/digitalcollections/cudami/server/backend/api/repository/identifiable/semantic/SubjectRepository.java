package de.digitalcollections.cudami.server.backend.api.repository.identifiable.semantic;

import de.digitalcollections.cudami.server.backend.api.repository.UniqueObjectRepository;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.semantic.Subject;
import java.util.List;
import java.util.Locale;

public interface SubjectRepository extends UniqueObjectRepository<Subject> {

  default Subject getByTypeAndIdentifier(String type, Identifier identifier)
      throws RepositoryException {
    if (type == null || identifier == null) {
      throw new IllegalArgumentException("get failed: given objects must not be null");
    }
    return getByTypeAndIdentifier(type, identifier.getNamespace(), identifier.getId());
  }

  Subject getByTypeAndIdentifier(String type, String namespace, String id)
      throws RepositoryException;

  List<Locale> getLanguages() throws RepositoryException;
}
