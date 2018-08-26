package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity;

import de.digitalcollections.model.api.identifiable.entity.ContentTree;
import de.digitalcollections.model.api.identifiable.entity.parts.ContentNode;
import java.util.List;
import java.util.UUID;

/**
 * Repository for ContentTree persistence handling.
 *
 * @param <C> content tree instance
 */
public interface ContentTreeRepository<C extends ContentTree> extends EntityRepository<C> {

  List<ContentNode> getRootNodes(C contentTree);

  List<ContentNode> getRootNodes(UUID uuid);
}
