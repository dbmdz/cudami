package de.digitalcollections.cudami.server.business.api.service.semantic;

import de.digitalcollections.cudami.server.business.api.service.exceptions.CudamiServiceException;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.semantic.Tag;
import java.util.List;
import java.util.UUID;

public interface TagService {

  long count();

  Tag getByUuid(UUID uuid);

  Tag save(Tag tag);

  Tag update(Tag tag);

  boolean delete(List<UUID> uuids);

  PageResponse<Tag> find(PageRequest pageRequest);

  Tag getByTagTypeAndIdentifier(String tagType, String namespace, String id)
      throws CudamiServiceException;
}
