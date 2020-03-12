package de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity.parts;

import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.NodeRepository;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.parts.ContentNode;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import java.util.List;
import java.util.UUID;

/**
 * Repository for ContentNode persistence handling.
 *
 * @param <E> entity type
 */
public interface ContentNodeRepository<E extends Entity>
    extends NodeRepository<ContentNode>, EntityPartRepository<ContentNode, E> {

  List<E> getEntities(ContentNode contentNode);

  List<E> getEntities(UUID contentNodeUuid);

  List<E> saveEntities(ContentNode contentNode, List<E> entities);

  List<E> saveEntities(UUID contentNodeUuid, List<E> entities);

  List<FileResource> getFileResources(ContentNode contentNode);

  List<FileResource> getFileResources(UUID contentNodeUuid);

  List<FileResource> saveFileResources(ContentNode contentNode, List<FileResource> fileResources);

  List<FileResource> saveFileResources(UUID contentNodeUuid, List<FileResource> fileResources);

  ContentNode saveWithParentContentTree(ContentNode contentNode, UUID parentContentTreeUuid);

  ContentNode saveWithParentContentNode(ContentNode contentNode, UUID parentContentNodeUuid);
}
