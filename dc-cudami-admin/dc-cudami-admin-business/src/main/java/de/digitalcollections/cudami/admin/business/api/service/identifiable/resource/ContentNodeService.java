package de.digitalcollections.cudami.admin.business.api.service.identifiable.resource;

import de.digitalcollections.cudami.admin.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.NodeService;
import de.digitalcollections.model.api.identifiable.resource.ContentNode;
import java.util.UUID;
import org.springframework.validation.Errors;

/**
 * Service for ContentNode.
 *
 * @param <C> domain object
 */
public interface ContentNodeService<C extends ContentNode> extends ResourceService<C>, NodeService<C> {

  ContentNode saveWithParentContentTree(C contentNode, UUID parentContentTreeUUID, Errors results) throws IdentifiableServiceException;

  ContentNode saveWithParentContentNode(C contentNode, UUID parentContentTreeUUID, Errors results) throws IdentifiableServiceException;

}
