package de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work;

import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.entity.work.Item;
import de.digitalcollections.model.api.identifiable.entity.work.Work;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.Set;
import java.util.UUID;

public interface ItemService extends IdentifiableService<Item> {

  PageResponse<Item> findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial);

  Set<DigitalObject> getDigitalObjects(UUID itemUuid);

  Set<Work> getWorks(UUID itemUuid);

  boolean addWork(UUID uuid, UUID workUuid);

  boolean addDigitalObject(UUID uuid, UUID digitalObjectUuid);
}
