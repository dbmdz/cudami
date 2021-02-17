package de.digitalcollections.cudami.server.business.api.service.identifiable.web;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.NodeService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.parts.EntityPartService;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Service for Webpage. */
public interface WebpageService extends NodeService<Webpage>, EntityPartService<Webpage> {

  Webpage getActive(UUID uuid);

  Webpage getActive(UUID uuid, Locale pLocale);

  List<Webpage> getActiveChildren(UUID uuid);

  PageResponse<Webpage> getActiveChildren(UUID uuid, PageRequest pageRequest);

  Website getWebsite(UUID webpageUuid);

  Webpage saveWithParentWebsite(Webpage webpage, UUID parentWebsiteUuid)
      throws IdentifiableServiceException;
}
