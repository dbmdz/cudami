package de.digitalcollections.cudami.server.business.api.service.identifiable.resource;

import de.digitalcollections.cudami.model.api.identifiable.resource.Webpage;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import java.util.Locale;
import java.util.UUID;

/**
 * Service for Webpage.
 *
 * @param <W> domain object
 */
public interface WebpageService<W extends Webpage> extends ResourceService<W> {

  W get(UUID uuid, Locale locale) throws IdentifiableServiceException;

  W save(W webpage, UUID uuid) throws IdentifiableServiceException;
}
