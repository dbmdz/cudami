package de.digitalcollections.cudami.admin.business.api.service.identifiable.resource;

import de.digitalcollections.cudami.admin.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.model.api.identifiable.resource.Webpage;
import java.util.UUID;
import org.springframework.validation.Errors;

/**
 * Service for Webpage.
 *
 * @param <W> domain object
 */
public interface WebpageService<W extends Webpage> extends ResourceService<W> {

  Webpage save(W webpage, UUID websiteUUID, Errors results) throws IdentifiableServiceException;

}
