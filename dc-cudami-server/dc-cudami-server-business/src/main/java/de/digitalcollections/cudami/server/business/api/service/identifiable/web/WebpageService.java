package de.digitalcollections.cudami.server.business.api.service.identifiable.web;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.NodeService;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
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
        Filtering.builder()
            .add(
                FilterCriterion.builder()
                    .withExpression("publicationStart")
                    .lessOrEqualAndSet(now)
                    .build())
            .add(
                FilterCriterion.builder()
                    .withExpression("publicationEnd")
                    .greaterOrNotSet(now)
                    .build())
            .build();

    return filtering;
  }

  PageResponse<Webpage> findActiveChildren(UUID uuid, PageRequest pageRequest);

  PageResponse<Webpage> findRootWebpagesForWebsite(UUID websiteUuid, PageRequest pageRequest);

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

  /**
   * Returns a list of children, with recursively all children have their children set
   *
   * @param uuid UUID of the parent webpage
   * @return List of active children webpages
   */
  List<Webpage> getChildrenTree(UUID uuid);

  Website getWebsite(UUID webpageUuid);

  Webpage saveWithParentWebsite(Webpage webpage, UUID parentWebsiteUuid) throws ServiceException;
}
