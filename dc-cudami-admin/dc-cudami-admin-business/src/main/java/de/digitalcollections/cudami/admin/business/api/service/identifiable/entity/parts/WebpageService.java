package de.digitalcollections.cudami.admin.business.api.service.identifiable.entity.parts;

import de.digitalcollections.cudami.admin.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.IdentifiablesContainerService;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.NodeService;
import de.digitalcollections.model.api.identifiable.Identifiable;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import java.util.UUID;
import org.springframework.validation.Errors;

/**
 * Service for Webpage.
 *
 * @param <W> webpage instance
 * @param <I> identifiable instance
 */
public interface WebpageService<W extends Webpage, I extends Identifiable> extends NodeService<W>, IdentifiablesContainerService<W, I> {

  W saveWithParentWebsite(W webpage, UUID parentWebsiteUUID, Errors results) throws IdentifiableServiceException;

  W saveWithParentWebpage(W webpage, UUID parentWebpageUUID, Errors results) throws IdentifiableServiceException;
}
