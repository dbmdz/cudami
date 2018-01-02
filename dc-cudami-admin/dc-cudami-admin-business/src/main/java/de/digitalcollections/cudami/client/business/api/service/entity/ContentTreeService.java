package de.digitalcollections.cudami.client.business.api.service.entity;

import de.digitalcollections.cudami.model.api.entity.ContentTree;
import de.digitalcollections.cudami.model.api.identifiable.ContentNode;
import java.util.List;

/**
 * Service for ContentTree.
 *
 * @param <C> domain object
 */
public interface ContentTreeService<C extends ContentTree> extends EntityService<C> {

  List<ContentNode> getRootNodes(C ContentTree);
}
