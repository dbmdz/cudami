package de.digitalcollections.cudami.server.business.api.service.identifiable.resource;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.model.api.identifiable.resource.ContentNode;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Service for ContentNode.
 *
 * @param <C> domain object
 */
public interface ContentNodeService<C extends ContentNode> extends ResourceService<C> {

  C get(UUID uuid, Locale locale) throws IdentifiableServiceException;

  List<ContentNode> getSubNodes(C contentNode);

  C saveWithParentContentTree(C contentNode, UUID parentContentTreeUuid) throws IdentifiableServiceException;

  C saveWithParentContentNode(C contentNode, UUID parentContentNodeUuid) throws IdentifiableServiceException;
}
