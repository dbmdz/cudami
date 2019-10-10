package de.digitalcollections.cudami.admin.business.api.service.identifiable.entity;

import de.digitalcollections.model.api.identifiable.entity.Website;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import java.util.List;

/** Service for Website. */
public interface WebsiteService extends EntityService<Website> {

  List<Webpage> getRootPages(Website website);
}
