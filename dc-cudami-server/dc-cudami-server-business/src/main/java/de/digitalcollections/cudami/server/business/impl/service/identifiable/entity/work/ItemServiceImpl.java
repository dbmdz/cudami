package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.work;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work.ItemRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.ItemService;
import de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.EntityServiceImpl;
import de.digitalcollections.cudami.server.config.HookProperties;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.entity.item.Item;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// @Transactional should not be set in derived class to prevent overriding, check base class instead
@Service
public class ItemServiceImpl extends EntityServiceImpl<Item> implements ItemService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ItemServiceImpl.class);

  @Autowired
  public ItemServiceImpl(
      ItemRepository repository,
      IdentifierService identifierService,
      UrlAliasService urlAliasService,
      HookProperties hookProperties,
      LocaleService localeService,
      CudamiConfig cudamiConfig) {
    super(
        repository,
        identifierService,
        urlAliasService,
        hookProperties,
        localeService,
        cudamiConfig);
  }

  @Override
  public PageResponse<DigitalObject> findDigitalObjects(UUID itemUuid, PageRequest pageRequest) {
    return ((ItemRepository) repository).findDigitalObjects(itemUuid, pageRequest);
  }

  @Override
  public PageResponse<Item> findItemsByManifestation(
      UUID manifestationUuid, PageRequest pageRequest) throws ServiceException {
    try {
      return ((ItemRepository) repository).findItemsByManifestation(manifestationUuid, pageRequest);
    } catch (RepositoryException e) {
      throw new ServiceException(
          "Cannot retrieve items for manifestation with uuid=" + manifestationUuid + ": " + e, e);
    }
  }

  @Override
  public List<Locale> getLanguagesOfDigitalObjects(UUID uuid) {
    return ((ItemRepository) repository).getLanguagesOfDigitalObjects(uuid);
  }

  @Override
  public List<Locale> getLanguagesOfItemsForManifestation(UUID manifestationUuid) {
    return ((ItemRepository) repository).getLanguagesOfItemsForManifestation(manifestationUuid);
  }

  @Override
  public List<Item> getItemsForWork(UUID workUuid) {
    return ((ItemRepository) repository).getItemsForWork(workUuid);
  }
}
