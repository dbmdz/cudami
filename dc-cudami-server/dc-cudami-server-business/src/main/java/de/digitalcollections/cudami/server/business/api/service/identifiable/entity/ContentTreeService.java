package de.digitalcollections.cudami.server.business.api.service.identifiable.entity;

import de.digitalcollections.model.api.identifiable.entity.ContentTree;
import de.digitalcollections.model.api.identifiable.entity.parts.ContentNode;
import java.util.List;
import java.util.UUID;

/**
 * Service for ContentTree.
 *
 * @param <C> domain object
 */
public interface ContentTreeService<C extends ContentTree> extends EntityService<C> {

  List<ContentNode> getRootNodes(C contentTree);

  List<ContentNode> getRootNodes(UUID uuid);
}
