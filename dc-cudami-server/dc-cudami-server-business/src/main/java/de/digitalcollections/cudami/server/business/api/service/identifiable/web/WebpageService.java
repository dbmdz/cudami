package de.digitalcollections.cudami.server.business.api.service.identifiable.web;

import de.digitalcollections.cudami.server.business.api.service.content.ManagedContentService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.NodeService;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.validation.ValidationException;
import java.util.List;
import java.util.Locale;

/** Service for Webpage. */
public interface WebpageService extends NodeService<Webpage>, ManagedContentService<Webpage> {

  PageResponse<Webpage> findActiveChildren(Webpage webpage, PageRequest pageRequest)
      throws ServiceException;

  PageResponse<Webpage> findRootWebpagesForWebsite(Website website, PageRequest pageRequest)
      throws ServiceException;

  Webpage getByExampleAndActive(Webpage webpage) throws ServiceException;

  Webpage getByExampleAndActiveAndLocale(Webpage webpage, Locale pLocale) throws ServiceException;

  /**
   * Returns a list of children (non recursive)
   *
   * @param webpage the parent webpage
   * @return List of children Webpages
   */
  List<Webpage> getActiveChildren(Webpage webpage) throws ServiceException;

  /**
   * Returns a list of active children, with recursivly all children have their active children set
   *
   * @param webpage the parent webpage
   * @return List of active children Webpages
   */
  List<Webpage> getActiveChildrenTree(Webpage webpage) throws ServiceException;

  /**
   * Returns a list of children, with recursively all children have their children set
   *
   * @param webpage the parent webpage
   * @return List of active children webpages
   */
  List<Webpage> getChildrenTree(Webpage webpage) throws ServiceException;

  Website getWebsite(Webpage webpage) throws ServiceException;

  Webpage saveWithParentWebsite(Webpage webpage, Website website)
      throws ServiceException, ValidationException;
}
