package de.digitalcollections.cudami.server.business.api.service.identifiable.web;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.NodeService;
import de.digitalcollections.model.filter.Filtering;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
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

  Webpage getActive(UUID uuid);

  Webpage getActive(UUID uuid, Locale pLocale);

  List<Webpage> getActiveChildren(UUID uuid);

  PageResponse<Webpage> getActiveChildren(UUID uuid, PageRequest pageRequest);

  Website getWebsite(UUID webpageUuid);

  Webpage saveWithParentWebsite(Webpage webpage, UUID parentWebsiteUuid)
      throws IdentifiableServiceException;
}
