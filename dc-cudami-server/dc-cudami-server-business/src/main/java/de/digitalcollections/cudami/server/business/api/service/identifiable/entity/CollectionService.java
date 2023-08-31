package de.digitalcollections.cudami.server.business.api.service.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.content.ManagedContentService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.NodeService;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.Locale;

public interface CollectionService
    extends NodeService<Collection>, EntityService<Collection>, ManagedContentService<Collection> {

  boolean addDigitalObject(Collection collection, DigitalObject digitalObject)
      throws ServiceException;

  boolean addDigitalObjects(Collection collection, List<DigitalObject> digitalObjects)
      throws ServiceException;

  PageResponse<Collection> findActive(PageRequest pageRequest) throws ServiceException;

  PageResponse<Collection> findActiveChildren(Collection collection, PageRequest pageRequest)
      throws ServiceException;

  PageResponse<DigitalObject> findDigitalObjects(Collection collection, PageRequest pageRequest)
      throws ServiceException;

  Collection getByExampleAndActive(Collection collection) throws ServiceException;

  Collection getByExampleAndActiveAndLocale(Collection collection, Locale pLocale)
      throws ServiceException;

  List<Collection> getActiveChildren(Collection collection) throws ServiceException;

  boolean removeDigitalObject(Collection collection, DigitalObject digitalObject)
      throws ServiceException;

  /**
   * Removes a digitalObject from all collections, to which it was connected to.
   *
   * @param digitalObject the digital object
   * @return boolean value for success
   */
  boolean removeDigitalObjectFromAllCollections(DigitalObject digitalObject)
      throws ServiceException;

  boolean setDigitalObjects(Collection collection, List<DigitalObject> digitalObjects)
      throws ServiceException;
}
