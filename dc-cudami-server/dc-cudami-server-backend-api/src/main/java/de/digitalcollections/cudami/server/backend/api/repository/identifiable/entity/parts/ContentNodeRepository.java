package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.parts;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiablesContainerRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.NodeRepository;
import de.digitalcollections.model.api.identifiable.Identifiable;
import de.digitalcollections.model.api.identifiable.entity.parts.ContentNode;
import java.util.UUID;

/**
 * Repository for ContentNode persistence handling.
 *
 * @param <C> content node instance
 * @param <I> identifiable instance
 */
public interface ContentNodeRepository<C extends ContentNode, I extends Identifiable> extends NodeRepository<C>, IdentifiablesContainerRepository<C, I> {

  C saveWithParentContentTree(C contentNode, UUID parentContentTreeUuid);

  C saveWithParentContentNode(C contentNode, UUID parentContentNodeUuid);
}
