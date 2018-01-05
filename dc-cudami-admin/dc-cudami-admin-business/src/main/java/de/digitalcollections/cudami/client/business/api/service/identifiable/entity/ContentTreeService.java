package de.digitalcollections.cudami.client.business.api.service.identifiable.entity;

import de.digitalcollections.cudami.model.api.identifiable.entity.ContentTree;
import de.digitalcollections.cudami.model.api.identifiable.resource.ContentNode;
import java.util.List;

/**
 * Service for ContentTree.
 *
 * @param <C> domain object
 */
public interface ContentTreeService<C extends ContentTree> extends EntityService<C> {

  List<ContentNode> getRootNodes(C ContentTree);
}
