package de.digitalcollections.cudami.admin.business.api.service.identifiable.entity.parts;

import de.digitalcollections.cudami.admin.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.NodeService;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import java.util.UUID;

/**
 * Service for Webpage.
 *
 * @param <E> entity type
 */
public interface WebpageService<E extends Entity> extends NodeService<Webpage>, EntityPartService<Webpage, E> {

  Webpage saveWithParentWebsite(Webpage webpage, UUID parentWebsiteUuid) throws IdentifiableServiceException;

  Webpage saveWithParentWebpage(Webpage webpage, UUID parentWebpageUuid) throws IdentifiableServiceException;
}
