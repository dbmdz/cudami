package de.digitalcollections.cudami.client.business.api.service.entity;

import de.digitalcollections.cudami.model.api.entity.Website;
import de.digitalcollections.cudami.model.api.identifiable.Webpage;
import java.util.List;

/**
 * Service for Website.
 *
 * @param <W> domain object
 */
public interface WebsiteService<W extends Website> extends EntityService<W> {

  List<Webpage> getRootNodes(W website);
}
