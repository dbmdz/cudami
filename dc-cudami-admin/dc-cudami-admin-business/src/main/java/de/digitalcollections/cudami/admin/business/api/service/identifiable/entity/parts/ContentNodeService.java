package de.digitalcollections.cudami.admin.business.api.service.identifiable.entity.parts;

import de.digitalcollections.cudami.admin.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.IdentifiablesContainerService;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.NodeService;
import de.digitalcollections.model.api.identifiable.Identifiable;
import de.digitalcollections.model.api.identifiable.entity.parts.ContentNode;
import java.util.UUID;
import org.springframework.validation.Errors;

/**
 * Service for ContentNode.
 *
 * @param <C> contentnode isntance
 * @param <I> identifiable instance
 */
public interface ContentNodeService<C extends ContentNode, I extends Identifiable> extends NodeService<C>, IdentifiablesContainerService<C, I> {

  ContentNode saveWithParentContentTree(C contentNode, UUID parentContentTreeUUID, Errors results) throws IdentifiableServiceException;

  ContentNode saveWithParentContentNode(C contentNode, UUID parentContentTreeUUID, Errors results) throws IdentifiableServiceException;

}
