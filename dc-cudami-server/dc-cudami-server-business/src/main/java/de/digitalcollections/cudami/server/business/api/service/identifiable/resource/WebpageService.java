package de.digitalcollections.cudami.server.business.api.service.identifiable.resource;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.NodeService;
import de.digitalcollections.model.api.identifiable.resource.Webpage;
import java.util.Locale;
import java.util.UUID;

/**
 * Service for Webpage.
 *
 * @param <W> domain object
 */
public interface WebpageService<W extends Webpage> extends ResourceService<W>, NodeService<W> {

  W get(UUID uuid, Locale locale) throws IdentifiableServiceException;

  W saveWithParentWebsite(W webpage, UUID parentWebsiteUuid) throws IdentifiableServiceException;

  W saveWithParentWebpage(W webpage, UUID parentWebpageUuid) throws IdentifiableServiceException;
}
