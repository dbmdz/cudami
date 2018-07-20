package de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity;

import de.digitalcollections.cudami.model.api.identifiable.entity.ContentTree;
import de.digitalcollections.cudami.model.api.identifiable.resource.ContentNode;
import java.util.List;

/**
 * Repository for ContentTree persistence handling.
 *
 * @param <C> entity instance
 */
public interface ContentTreeRepository<C extends ContentTree> extends EntityRepository<C> {

  List<ContentNode> getRootNodes(C contentTree);
}
