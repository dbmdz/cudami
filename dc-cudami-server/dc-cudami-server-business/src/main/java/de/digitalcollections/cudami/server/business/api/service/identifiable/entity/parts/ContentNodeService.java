package de.digitalcollections.cudami.server.business.api.service.identifiable.entity.parts;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiablesContainerService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.NodeService;
import de.digitalcollections.model.api.identifiable.Identifiable;
import de.digitalcollections.model.api.identifiable.entity.parts.ContentNode;
import java.util.Locale;
import java.util.UUID;

/**
 * Service for ContentNode.
 *
 * @param <C> contentnode isntance
 * @param <I> identifiable instance
 */
public interface ContentNodeService<C extends ContentNode, I extends Identifiable> extends NodeService<C>, IdentifiablesContainerService<C, I> {

  C get(UUID uuid, Locale locale) throws IdentifiableServiceException;

  C saveWithParentContentTree(C contentNode, UUID parentContentTreeUuid) throws IdentifiableServiceException;

  C saveWithParentContentNode(C contentNode, UUID parentContentNodeUuid) throws IdentifiableServiceException;
}
