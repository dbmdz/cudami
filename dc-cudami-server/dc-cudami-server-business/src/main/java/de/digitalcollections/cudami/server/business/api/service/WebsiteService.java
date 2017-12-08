package de.digitalcollections.cudami.server.business.api.service;

import de.digitalcollections.cudami.model.api.identifiable.Node;
import de.digitalcollections.cudami.model.api.identifiable.Website;
import java.util.List;

/**
 * Service for Website.
 *
 * @param <W> domain object
 */
public interface WebsiteService<W extends Website> extends IdentifiableService<W> {

  List<Node> getRootNodes(W website);
}
