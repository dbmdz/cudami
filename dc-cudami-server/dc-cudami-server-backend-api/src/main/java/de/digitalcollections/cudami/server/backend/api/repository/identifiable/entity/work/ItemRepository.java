package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EntityRepository;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.entity.item.Item;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Repository for Item persistence handling. */
public interface ItemRepository extends EntityRepository<Item> {

  default PageResponse<DigitalObject> findDigitalObjects(Item item, PageRequest pageRequest) {
    if (item == null) {
      return null;
    }
    return findDigitalObjects(item.getUuid(), pageRequest);
  }

  PageResponse<DigitalObject> findDigitalObjects(UUID itemUuid, PageRequest pageRequest);

  PageResponse<Item> findItemsByManifestation(UUID manifestationUuid, PageRequest pageRequest)
      throws RepositoryException;

  List<Locale> getLanguagesOfDigitalObjects(UUID uuid);

  List<Locale> getLanguagesOfItemsForManifestation(UUID manifestationUuid);
}
