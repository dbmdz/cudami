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
import de.digitalcollections.model.identifiable.entity.item.Item;
import de.digitalcollections.model.identifiable.entity.manifestation.Manifestation;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.validation.ValidationException;
import java.util.List;
import java.util.Locale;
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
  public boolean clearPartOfItem(Item item, Item parentItem) throws ServiceException {
    item = getByExample(item);
    if (item == null) {
      return false;
    }
    if (item.getPartOfItem() == null
        || !item.getPartOfItem().getUuid().equals(parentItem.getUuid())) {
      return false;
    }

    item.setPartOfItem(null);
    try {
      update(item);
    } catch (ValidationException e) {
      throw new ServiceException("Cannot not clear part of item: " + e, e);
    }
    return true;
  }

  @Override
  public PageResponse<Item> findItemsByManifestation(
      Manifestation manifestation, PageRequest pageRequest) throws ServiceException {
    try {
      return ((ItemRepository) repository).findItemsByManifestation(manifestation, pageRequest);
    } catch (RepositoryException e) {
      throw new ServiceException(
          "Cannot retrieve items for manifestation with uuid=" + manifestation + ": " + e, e);
    }
  }

  @Override
  public List<Locale> getLanguagesOfDigitalObjects(Item item) throws ServiceException {
    try {
      return ((ItemRepository) repository).getLanguagesOfDigitalObjects(item);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public List<Locale> getLanguagesOfItemsForManifestation(Manifestation manifestation)
      throws ServiceException {
    try {
      return ((ItemRepository) repository).getLanguagesOfItemsForManifestation(manifestation);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public boolean removeParentItemChildren(Item parentItem) throws ServiceException {
    PageRequest pageRequest =
        PageRequest.builder()
            .filtering(
                Filtering.builder()
                    .add(
                        FilterCriterion.builder()
                            .withExpression("partOfItem.uuid")
                            .isEquals(parentItem.getUuid())
                            .build())
                    .build())
            .pageSize(99999)
            .pageNumber(0)
            .build();
    try {
      PageResponse<Item> pageResponse = repository.find(pageRequest);
      if (pageResponse == null
          || pageResponse.getContent() == null
          || pageResponse.getContent().isEmpty()) {
        return false;
      }
      for (Item childItem : pageResponse.getContent()) {
        if (!clearPartOfItem(childItem, parentItem)) {
          return false;
        }
      }
      return true;
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }
}
