package de.digitalcollections.cudami.client.backend.api.repository.identifiable.entity;

import de.digitalcollections.cudami.model.api.identifiable.entity.ContentTree;
import de.digitalcollections.cudami.model.api.identifiable.resource.ContentNode;
import java.util.List;

/**
 * Repository for ContentTree persistence handling.
 *
 * @param <E> entity instance
 */
public interface ContentTreeRepository<E extends ContentTree> extends EntityRepository<E> {

  List<ContentNode> getRootNodes(E contentTree);
}
