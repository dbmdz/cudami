package de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity.parts;

import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.NodeRepository;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.parts.ContentNode;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import java.util.LinkedHashSet;
import java.util.UUID;

/**
 * Repository for ContentNode persistence handling.
 *
 * @param <E> entity type
 */
public interface ContentNodeRepository<E extends Entity> extends NodeRepository<ContentNode>, EntityPartRepository<ContentNode, E> {

  LinkedHashSet<E> getEntities(ContentNode contentNode);

  LinkedHashSet<E> getEntities(UUID contentNodeUuid);

  LinkedHashSet<E> saveEntities(ContentNode contentNode, LinkedHashSet<E> entities);

  LinkedHashSet<E> saveEntities(UUID contentNodeUuid, LinkedHashSet<E> entities);

  LinkedHashSet<FileResource> getFileResources(ContentNode contentNode);

  LinkedHashSet<FileResource> getFileResources(UUID contentNodeUuid);

  LinkedHashSet<FileResource> saveFileResources(ContentNode contentNode, LinkedHashSet<FileResource> fileResources);

  LinkedHashSet<FileResource> saveFileResources(UUID contentNodeUuid, LinkedHashSet<FileResource> fileResources);

  ContentNode saveWithParentContentTree(ContentNode contentNode, UUID parentContentTreeUuid);

  ContentNode saveWithParentContentNode(ContentNode contentNode, UUID parentContentNodeUuid);
}
