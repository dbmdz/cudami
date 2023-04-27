package de.digitalcollections.cudami.server.business.api.service.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;

/** Service for Website. */
public interface WebsiteService extends EntityService<Website> {

  PageResponse<Webpage> findRootWebpages(Website website, PageRequest pageRequest)
      throws ServiceException;

  List<Webpage> getRootWebpages(Website website) throws ServiceException;

  boolean updateRootWebpagesOrder(Website website, List<Webpage> rootPages) throws ServiceException;
}
