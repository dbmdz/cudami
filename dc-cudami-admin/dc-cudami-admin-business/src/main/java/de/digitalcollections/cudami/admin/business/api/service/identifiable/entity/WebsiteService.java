package de.digitalcollections.cudami.admin.business.api.service.identifiable.entity;

import de.digitalcollections.model.api.identifiable.entity.Website;
import de.digitalcollections.model.api.identifiable.resource.Webpage;
import java.util.List;

/**
 * Service for Website.
 *
 * @param <W> domain object
 */
public interface WebsiteService<W extends Website> extends EntityService<W> {

  List<Webpage> getRootNodes(W website);
}
