package de.digitalcollections.cudami.admin.business.api.service.identifiable.entity;

import de.digitalcollections.model.api.identifiable.entity.ContentTree;
import de.digitalcollections.model.api.identifiable.entity.parts.ContentNode;
import java.util.List;

/**
 * Service for ContentTree.
 *
 * @param <C> domain object
 */
public interface ContentTreeService<C extends ContentTree> extends EntityService<C> {

  List<ContentNode> getRootNodes(C contentTree);
}
