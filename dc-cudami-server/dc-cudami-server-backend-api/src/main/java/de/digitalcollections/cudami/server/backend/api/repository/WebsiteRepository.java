package de.digitalcollections.cudami.server.backend.api.repository;

import de.digitalcollections.cudami.model.api.identifiable.Node;
import de.digitalcollections.cudami.model.api.entity.Website;
import java.util.List;

/**
 * Repository for Website persistence handling.
 *
 * @param <W> entity instance
 */
public interface WebsiteRepository<W extends Website> extends IdentifiableRepository<W> {

  List<Node> getRootNodes(W website);
}
