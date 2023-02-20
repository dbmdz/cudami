package de.digitalcollections.cudami.server.business.api.service.semantic;

import de.digitalcollections.cudami.server.business.api.service.UniqueObjectService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.model.semantic.Tag;
import java.util.List;
import java.util.UUID;

public interface TagService extends UniqueObjectService<Tag> {

  long count();

  boolean delete(List<UUID> uuids);

  Tag getByUuid(UUID uuid);

  Tag getByValue(String value) throws ServiceException;

  Tag save(Tag tag);

  Tag update(Tag tag);
}
