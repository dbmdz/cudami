package de.digitalcollections.cudami.server.business.api.service.identifiable.entity;

import de.digitalcollections.model.api.identifiable.entity.Website;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import java.util.List;
import java.util.UUID;

/** Service for Website. */
public interface WebsiteService extends EntityService<Website> {

  List<Webpage> getRootPages(Website website);

  List<Webpage> getRootPages(UUID uuid);
}
