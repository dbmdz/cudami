package de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.model.api.identifiable.resource.ContentNode;
import java.util.List;
import java.util.UUID;

/**
 * Repository for ContentNode persistence handling.
 *
 * @param <C> resource instance
 */
public interface ContentNodeRepository<C extends ContentNode> extends IdentifiableRepository<C> {

  List<ContentNode> getSubNodes(C contentNode);

  ContentNode saveWithParentContentTree(C contentNode, UUID parentContentTreeUuid);

  ContentNode saveWithParentContentNode(C contentNode, UUID parentContentNodeUuid);
}
