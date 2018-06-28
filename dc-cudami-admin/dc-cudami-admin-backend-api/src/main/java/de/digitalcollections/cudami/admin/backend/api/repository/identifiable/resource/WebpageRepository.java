package de.digitalcollections.cudami.admin.backend.api.repository.identifiable.resource;

import de.digitalcollections.cudami.model.api.identifiable.resource.Webpage;
import java.util.UUID;

/**
 * Repository for Webpage persistence handling.
 *
 * @param <W> webpage instance
 */
public interface WebpageRepository<W extends Webpage> extends ResourceRepository<W> {

  W saveWithParentWebsite(W webpage, UUID parentWebsiteUUID);

  W saveWithParentWebpage(W webpage, UUID parentWebpageUUID);

}
