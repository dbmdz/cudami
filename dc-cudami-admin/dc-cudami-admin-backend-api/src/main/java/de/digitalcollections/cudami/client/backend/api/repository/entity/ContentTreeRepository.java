package de.digitalcollections.cudami.client.backend.api.repository.entity;

import de.digitalcollections.cudami.model.api.entity.ContentTree;
import de.digitalcollections.cudami.model.api.identifiable.ContentNode;
import java.util.List;

/**
 * Repository for ContentTree persistence handling.
 *
 * @param <E> entity instance
 */
public interface ContentTreeRepository<E extends ContentTree> extends EntityRepository<E> {

  List<ContentNode> getRootNodes(E contentTree);
}
