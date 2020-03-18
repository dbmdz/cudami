package de.digitalcollections.cudami.server.business.api.service.identifiable.entity.parts;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.NodeService;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.parts.ContentNode;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Service for ContentNode.
 *
 * @param <E> entity type
 */
public interface ContentNodeService<E extends Entity>
    extends NodeService<ContentNode>, EntityPartService<ContentNode, E> {

  @Override
  ContentNode get(UUID uuid, Locale locale) throws IdentifiableServiceException;

  List<E> getEntities(ContentNode contentNode);

  List<E> getEntities(UUID contentNodeUuid);

  List<E> saveEntities(ContentNode contentNode, List<E> entities);

  List<E> saveEntities(UUID contentNodeUuid, List<E> entities);

  List<FileResource> getFileResources(ContentNode contentNode);

  List<FileResource> getFileResources(UUID contentNodeUuid);

  List<FileResource> saveFileResources(ContentNode contentNode, List<FileResource> fileResources);

  List<FileResource> saveFileResources(UUID contentNodeUuid, List<FileResource> fileResources);

  ContentNode saveWithParentContentTree(ContentNode contentNode, UUID parentContentTreeUuid)
      throws IdentifiableServiceException;

  ContentNode saveWithParentContentNode(ContentNode contentNode, UUID parentContentNodeUuid)
      throws IdentifiableServiceException;
}
