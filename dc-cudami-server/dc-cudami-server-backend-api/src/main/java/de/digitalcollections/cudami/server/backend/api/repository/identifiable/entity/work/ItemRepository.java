package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EntityRepository;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.work.Item;
import de.digitalcollections.model.identifiable.entity.work.Work;
import java.util.Set;
import java.util.UUID;

/** Repository for Item persistence handling. */
public interface ItemRepository extends EntityRepository<Item> {

  boolean addWork(UUID itemUuid, UUID workUuid);

  Set<DigitalObject> getDigitalObjects(UUID itemUuid);

  Set<Work> getWorks(UUID itemUuid);
}
