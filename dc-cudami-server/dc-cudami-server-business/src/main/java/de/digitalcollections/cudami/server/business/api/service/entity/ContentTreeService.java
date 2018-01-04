package de.digitalcollections.cudami.server.business.api.service.entity;

import de.digitalcollections.cudami.model.api.entity.ContentTree;
import de.digitalcollections.cudami.model.api.identifiable.ContentNode;
import java.util.List;

/**
 * Service for ContentTree.
 *
 * @param <E> domain object
 */
public interface ContentTreeService<E extends ContentTree> extends EntityService<E> {

  List<ContentNode> getRootNodes(E contentTree);
}
