package de.digitalcollections.cudami.server.backend.api.repository.semantic;

import de.digitalcollections.cudami.server.backend.api.repository.UniqueObjectRepository;
import de.digitalcollections.model.semantic.Tag;

public interface TagRepository extends UniqueObjectRepository<Tag> {

  Tag getByValue(String value);
}
