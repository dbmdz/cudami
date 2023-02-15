package de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.EntityService;
import de.digitalcollections.model.identifiable.entity.item.Item;
import de.digitalcollections.model.identifiable.entity.manifestation.Manifestation;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public interface ManifestationService extends EntityService<Manifestation> {

  PageResponse<Manifestation> findChildren(UUID uuid, PageRequest pageRequest);

  PageResponse<Item> findItems(UUID uuid, PageRequest pageRequest) throws ServiceException;

  List<Locale> getLanguagesOfItems(UUID uuid);
}
