package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.work;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work.ItemRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.DigitalObjectService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.ItemService;
import de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.EntityServiceImpl;
import de.digitalcollections.cudami.server.config.HookProperties;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.work.Item;
import de.digitalcollections.model.identifiable.entity.work.Work;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// @Transactional should not be set in derived class to prevent overriding, check base class instead
@Service
public class ItemServiceImpl extends EntityServiceImpl<Item> implements ItemService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ItemServiceImpl.class);

  private final DigitalObjectService digitalObjectService;

  @Autowired
  public ItemServiceImpl(
      ItemRepository repository,
      DigitalObjectService digitalObjectService,
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
    this.digitalObjectService = digitalObjectService;
  }

  @Override
  public boolean addDigitalObject(UUID itemUuid, UUID digitalObjectUuid)
      throws ConflictException, ValidationException, IdentifiableServiceException {
    // Retrieve the Item. If it does not exist, return false
    Item item = repository.getByUuidAndFiltering(itemUuid, null);
    if (item == null) {
      return false;
    }

    // Retrieve the DigitalObject
    DigitalObject digitalObject = digitalObjectService.getByUuid(digitalObjectUuid);
    if (digitalObject == null) {
      return false;
    }

    // Ensure, that the DigitalObject is either not connected with any item or already belongs to
    // the item
    Item digitalObjectItem = digitalObject.getItem();
    if (digitalObjectItem != null && digitalObjectItem.getUuid().equals(itemUuid)) {
      return true; // nothing to do
    }
    if (digitalObjectItem != null && !digitalObjectItem.getUuid().equals(itemUuid)) {
      LOGGER.warn(
          "Trying to connect DigitalObject "
              + digitalObjectUuid
              + " to item "
              + itemUuid
              + ", but it already belongs to item "
              + digitalObjectItem.getUuid());
      throw new ConflictException(
          "DigitalObject "
              + digitalObject.getUuid()
              + " already belongs to item "
              + digitalObject.getItem().getUuid());
    }

    digitalObject.setItem(item);
    digitalObjectService.update(digitalObject);
    return true;
  }

  @Override
  public boolean addWork(UUID itemUuid, UUID workUuid) {
    return ((ItemRepository) repository).addWork(itemUuid, workUuid);
  }

  @Override
  public Set<DigitalObject> getDigitalObjects(UUID itemUuid) {
    return ((ItemRepository) repository).getDigitalObjects(itemUuid);
  }

  @Override
  public Set<Work> getWorks(UUID itemUuid) {
    return ((ItemRepository) repository).getWorks(itemUuid);
  }
}
