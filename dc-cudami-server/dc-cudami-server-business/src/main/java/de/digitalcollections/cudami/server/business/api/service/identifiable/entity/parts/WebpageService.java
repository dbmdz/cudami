package de.digitalcollections.cudami.server.business.api.service.identifiable.entity.parts;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.NodeService;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.Website;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Service for Webpage.
 *
 * @param <E> entity type
 */
public interface WebpageService<E extends Entity>
    extends NodeService<Webpage>, EntityPartService<Webpage, E> {

  Webpage getActive(UUID uuid);

  Webpage getActive(UUID uuid, Locale pLocale);

  List<Webpage> getActiveChildren(UUID uuid);

  PageResponse<Webpage> getActiveChildren(UUID uuid, PageRequest pageRequest);

  Website getWebsite(UUID webpageUuid);

  Webpage saveWithParentWebsite(Webpage webpage, UUID parentWebsiteUuid)
      throws IdentifiableServiceException;

  Webpage saveWithParentWebpage(Webpage webpage, UUID parentWebpageUuid)
      throws IdentifiableServiceException;

  boolean updateChildrenOrder(Webpage webpage, List<Webpage> children);
}
