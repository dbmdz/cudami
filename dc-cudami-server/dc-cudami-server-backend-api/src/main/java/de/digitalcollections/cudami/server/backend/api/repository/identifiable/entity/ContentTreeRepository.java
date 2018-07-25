package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.model.api.identifiable.entity.ContentTree;
import de.digitalcollections.model.api.identifiable.resource.ContentNode;
import java.util.List;

/**
 * Repository for ContentTree persistence handling.
 *
 * @param <C> entity instance
 */
public interface ContentTreeRepository<C extends ContentTree> extends IdentifiableRepository<C> {

  List<ContentNode> getRootNodes(C contentTree);
}
