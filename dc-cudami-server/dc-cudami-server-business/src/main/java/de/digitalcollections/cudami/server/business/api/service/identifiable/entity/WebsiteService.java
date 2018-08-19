package de.digitalcollections.cudami.server.business.api.service.identifiable.entity;

import de.digitalcollections.model.api.identifiable.entity.Website;
import de.digitalcollections.model.api.identifiable.resource.Webpage;
import java.util.List;
import java.util.UUID;

/**
 * Service for Website.
 *
 * @param <W> domain object
 */
public interface WebsiteService<W extends Website> extends EntityService<W> {

  List<Webpage> getRootPages(W website);

  List<Webpage> getRootPages(UUID uuid);
}
