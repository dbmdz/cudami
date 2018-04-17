package de.digitalcollections.cudami.server.business.api.service.identifiable.resource;

import de.digitalcollections.cudami.model.api.identifiable.parts.Text;
import de.digitalcollections.cudami.model.api.identifiable.resource.Webpage;

/**
 * Service for Webpage.
 *
 * @param <W> domain object
 */
public interface WebpageService<W extends Webpage> extends ResourceService<W> {

  Text getContentBlocks(W webpage);
}
