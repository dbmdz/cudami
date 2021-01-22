package de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.EntityService;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.entity.work.Item;
import de.digitalcollections.model.api.identifiable.entity.work.Work;
import java.util.Set;
import java.util.UUID;

public interface ItemService extends EntityService<Item> {

  boolean addDigitalObject(UUID uuid, UUID digitalObjectUuid);

  boolean addWork(UUID uuid, UUID workUuid);

  Set<DigitalObject> getDigitalObjects(UUID itemUuid);

  Set<Work> getWorks(UUID itemUuid);
}
