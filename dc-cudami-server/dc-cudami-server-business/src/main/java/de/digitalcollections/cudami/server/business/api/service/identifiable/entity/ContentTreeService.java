package de.digitalcollections.cudami.server.business.api.service.identifiable.entity;

import de.digitalcollections.cudami.model.api.identifiable.entity.ContentTree;
import de.digitalcollections.cudami.model.api.identifiable.resource.ContentNode;
import java.util.List;

/**
 * Service for ContentTree.
 *
 * @param <E> domain object
 */
public interface ContentTreeService<E extends ContentTree> extends EntityService<E> {

  List<ContentNode> getRootNodes(E contentTree);
}
