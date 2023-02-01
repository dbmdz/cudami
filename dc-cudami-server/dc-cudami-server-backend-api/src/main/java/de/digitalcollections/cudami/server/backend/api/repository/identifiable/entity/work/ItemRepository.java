package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EntityRepository;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.entity.item.Item;
import de.digitalcollections.model.identifiable.entity.work.Work;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/** Repository for Item persistence handling. */
public interface ItemRepository extends EntityRepository<Item> {

  boolean addWork(UUID itemUuid, UUID workUuid);

  PageResponse<DigitalObject> findDigitalObjects(UUID itemUuid, PageRequest pageRequest);

  List<Locale> getLanguagesOfDigitalObjects(UUID uuid);

  Set<Work> getWorks(UUID itemUuid);

  List<Item> getItemsForWork(UUID workUuid);
}
