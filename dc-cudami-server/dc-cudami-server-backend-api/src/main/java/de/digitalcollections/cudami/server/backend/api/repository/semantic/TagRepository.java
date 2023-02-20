package de.digitalcollections.cudami.server.backend.api.repository.semantic;

import de.digitalcollections.cudami.server.backend.api.repository.UniqueObjectRepository;
import de.digitalcollections.model.semantic.Tag;
import java.util.List;
import java.util.UUID;

public interface TagRepository extends UniqueObjectRepository<Tag> {

  long count();

  default boolean delete(UUID uuid) {
    return delete(List.of(uuid));
  }

  boolean delete(List<UUID> uuids);

  Tag getByValue(String value);

  Tag save(Tag tag);

  Tag update(Tag tag);
}
