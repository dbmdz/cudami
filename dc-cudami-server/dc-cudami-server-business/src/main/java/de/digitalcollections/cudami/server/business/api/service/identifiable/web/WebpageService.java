package de.digitalcollections.cudami.server.business.api.service.identifiable.web;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.NodeService;
import de.digitalcollections.model.filter.Filtering;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Service for Webpage. */
public interface WebpageService extends NodeService<Webpage> {

  default Filtering filteringForActive() {
    // business logic that defines, what "active" means
    LocalDate now = LocalDate.now();
    Filtering filtering =
        Filtering.defaultBuilder()
            .filter("publicationStart")
            .lessOrEqualAndSet(now)
            .filter("publicationEnd")
            .greaterOrNotSet(now)
            .build();
    return filtering;
  }

  SearchPageResponse<Webpage> findActiveChildren(UUID uuid, SearchPageRequest searchPageRequest);

  SearchPageResponse<Webpage> findRootPagesForWebsite(
      UUID websiteUuid, SearchPageRequest searchPageRequest);

  Webpage getActive(UUID uuid);

  Webpage getActive(UUID uuid, Locale pLocale);

  /**
   * Returns a list of children (non recursive)
   *
   * @param uuid UUID of the parent webpage
   * @return List of children Webpages
   */
  List<Webpage> getActiveChildren(UUID uuid);

  /**
   * Returns a list of active children, with recursivly all children have their active children set
   *
   * @param uuid UUID of the parent webpage
   * @return List of acrive children Webpages
   */
  List<Webpage> getActiveChildrenTree(UUID uuid);

  PageResponse<Webpage> getActiveChildren(UUID uuid, PageRequest pageRequest);

  Website getWebsite(UUID webpageUuid);

  Webpage saveWithParentWebsite(Webpage webpage, UUID parentWebsiteUuid)
      throws IdentifiableServiceException;

  /**
   * Returns a list of children, with recursively all children have their children set
   *
   * @param uuid UUID of the parent webpage
   * @return List of active children webpages
   */
  List<Webpage> getChildrenTree(UUID uuid);
}
