package de.digitalcollections.cudami.admin.business.api.service.identifiable.resource;

import de.digitalcollections.cudami.admin.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.IdentifiablesContainerService;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.NodeService;
import de.digitalcollections.model.api.identifiable.resource.Webpage;
import java.util.UUID;
import org.springframework.validation.Errors;

/**
 * Service for Webpage.
 *
 * @param <W> domain object
 */
public interface WebpageService<W extends Webpage> extends ResourceService<W>, NodeService<W>, IdentifiablesContainerService<W> {

  W saveWithParentWebsite(W webpage, UUID parentWebsiteUUID, Errors results) throws IdentifiableServiceException;

  W saveWithParentWebpage(W webpage, UUID parentWebpageUUID, Errors results) throws IdentifiableServiceException;
}
