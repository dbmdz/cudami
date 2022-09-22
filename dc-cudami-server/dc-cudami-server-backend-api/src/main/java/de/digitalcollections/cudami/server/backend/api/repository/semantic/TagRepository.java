package de.digitalcollections.cudami.server.backend.api.repository.semantic;

import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.semantic.Tag;
import java.util.List;
import java.util.UUID;

public interface TagRepository {

  long count();

  Tag getByUuid(UUID uuid);

  Tag save(Tag tag);

  Tag update(Tag tag);

  default boolean delete(UUID uuid) {
    return delete(List.of(uuid));
  }

  boolean delete(List<UUID> uuids);

  PageResponse<Tag> find(PageRequest pageRequest);

  Tag getByTagTypeAndIdentifier(String tagType, String namespace, String id);
}
