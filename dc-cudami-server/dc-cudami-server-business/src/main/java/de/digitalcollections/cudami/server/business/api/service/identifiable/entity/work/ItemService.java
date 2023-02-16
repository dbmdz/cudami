package de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.EntityService;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.entity.item.Item;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public interface ItemService extends EntityService<Item> {
  PageResponse<DigitalObject> findDigitalObjects(UUID itemUuid, PageRequest pageRequest);

  List<Locale> getLanguagesOfDigitalObjects(UUID uuid);

  List<Locale> getLanguagesOfItemsForManifestation(UUID manifestationUuid);

  List<Item> getItemsForWork(UUID workUuid);

  PageResponse<Item> findItemsByManifestation(UUID uuid, PageRequest pageRequest)
      throws ServiceException;
}
