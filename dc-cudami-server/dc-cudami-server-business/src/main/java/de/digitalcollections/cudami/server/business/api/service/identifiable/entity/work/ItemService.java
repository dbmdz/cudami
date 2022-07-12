package de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.EntityService;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.work.Item;
import de.digitalcollections.model.identifiable.entity.work.Work;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.Set;
import java.util.UUID;

public interface ItemService extends EntityService<Item> {

  boolean addDigitalObject(UUID uuid, UUID digitalObjectUuid)
      throws ConflictException, ValidationException, IdentifiableServiceException;

  boolean addWork(UUID uuid, UUID workUuid);

  PageResponse<DigitalObject> findDigitalObjects(UUID itemUuid, PageRequest pageRequest);

  Set<Work> getWorks(UUID itemUuid);
}
