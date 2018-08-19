package de.digitalcollections.cudami.admin.backend.api.repository.identifiable.resource;

import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.NodeRepository;
import de.digitalcollections.model.api.identifiable.resource.ContentNode;
import java.util.UUID;

/**
 * Repository for ContentNode persistence handling.
 *
 * @param <C> content node instance
 */
public interface ContentNodeRepository<C extends ContentNode> extends ResourceRepository<C>, NodeRepository<C> {

  C saveWithParentContentTree(C contentNode, UUID parentContentTreeUUID);

  C saveWithParentContentNode(C contentNode, UUID parentContentNodeUUID);

}
