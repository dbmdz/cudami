package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EntityRepository;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.entity.work.Item;
import de.digitalcollections.model.api.identifiable.entity.work.Work;
import java.util.Set;
import java.util.UUID;

/**
 * Repository for Item persistence handling.
 *
 * @param <I> instance of item implementation
 */
public interface ItemRepository<I extends Item> extends EntityRepository<I> {

  Set<DigitalObject> getDigitalObjects(UUID itemUuid);

  Set<Work> getWorks(UUID itemUuid);

  boolean addWork(UUID itemUuid, UUID workUuid);

  boolean addDigitalObject(UUID itemUuid, UUID digitalObjectUuid);
}
