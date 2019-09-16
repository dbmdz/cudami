package de.digitalcollections.cudami.server.business.api.service.identifiable.entity.parts;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.NodeService;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.parts.ContentNode;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.UUID;

/**
 * Service for ContentNode.
 *
 * @param <E> entity type
 */
public interface ContentNodeService<E extends Entity> extends NodeService<ContentNode>, EntityPartService<ContentNode, E> {

  @Override
  ContentNode get(UUID uuid, Locale locale) throws IdentifiableServiceException;

  LinkedHashSet<E> getEntities(ContentNode contentNode);

  LinkedHashSet<E> getEntities(UUID contentNodeUuid);

  LinkedHashSet<E> saveEntities(ContentNode contentNode, LinkedHashSet<E> entities);

  LinkedHashSet<E> saveEntities(UUID contentNodeUuid, LinkedHashSet<E> entities);

  LinkedHashSet<FileResource> getFileResources(ContentNode contentNode);

  LinkedHashSet<FileResource> getFileResources(UUID contentNodeUuid);

  LinkedHashSet<FileResource> saveFileResources(ContentNode contentNode, LinkedHashSet<FileResource> fileResources);

  LinkedHashSet<FileResource> saveFileResources(UUID contentNodeUuid, LinkedHashSet<FileResource> fileResources);

  ContentNode saveWithParentContentTree(ContentNode contentNode, UUID parentContentTreeUuid) throws IdentifiableServiceException;

  ContentNode saveWithParentContentNode(ContentNode contentNode, UUID parentContentNodeUuid) throws IdentifiableServiceException;
}
