package de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.entity.parts;

import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity.parts.ContentNodeRepository;
import de.digitalcollections.model.api.http.exceptions.client.ResourceNotFoundException;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.parts.ContentNode;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.parts.ContentNodeImpl;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ContentNodeRepositoryImpl<E extends Entity>
    extends EntityPartRepositoryImpl<ContentNode, E> implements ContentNodeRepository<E> {

  @Autowired private ContentNodeRepositoryEndpoint endpoint;

  @Override
  public long count() {
    return endpoint.count();
  }

  @Override
  public ContentNode create() {
    return new ContentNodeImpl();
  }

  @Override
  public PageResponse<ContentNode> find(PageRequest pageRequest) {
    FindParams f = getFindParams(pageRequest);
    PageResponse<ContentNode> pageResponse =
        endpoint.find(
            f.getPageNumber(),
            f.getPageSize(),
            f.getSortField(),
            f.getSortDirection(),
            f.getNullHandling());
    return getGenericPageResponse(pageResponse);
  }

  @Override
  public ContentNode findOneByIdentifier(String namespace, String id) {
    try {
      return endpoint.findOneByIdentifier(namespace, id);
    } catch (ResourceNotFoundException e) {
      return null;
    }
  }

  @Override
  public ContentNode findOne(UUID uuid) {
    return endpoint.findOne(uuid);
  }

  @Override
  public ContentNode findOne(UUID uuid, Locale locale) {
    return endpoint.findOne(uuid, locale.toString());
  }

  @Override
  public List<E> getEntities(ContentNode contentNode) {
    return getEntities(contentNode.getUuid());
  }

  @Override
  public List<E> getEntities(UUID contentNodeUuid) {
    List<Entity> entities = endpoint.getEntities(contentNodeUuid);
    return entities.stream().map(e -> (E) e).collect(Collectors.toList());
  }

  @Override
  public List<FileResource> getFileResources(ContentNode contentNode) {
    return getFileResources(contentNode.getUuid());
  }

  @Override
  public List<FileResource> getFileResources(UUID contentNodeUuid) {
    return endpoint.getFileResources(contentNodeUuid);
  }

  @Override
  public ContentNode getParent(UUID nodeUuid) {
    return endpoint.getParent(nodeUuid);
  }

  @Override
  public ContentNode save(ContentNode contentNode) {
    return endpoint.save(contentNode);
  }

  @Override
  public List<E> saveEntities(ContentNode contentNode, List<E> entities) {
    return saveEntities(contentNode.getUuid(), entities);
  }

  @Override
  public List<E> saveEntities(UUID contentNodeUuid, List<E> entities) {
    List<Entity> savedEntities =
        endpoint.saveEntities(
            contentNodeUuid,
            entities.stream().map(Entity.class::cast).collect(Collectors.toList()));
    return savedEntities.stream().map(e -> (E) e).collect(Collectors.toList());
  }

  @Override
  public List<FileResource> saveFileResources(
      ContentNode contentNode, List<FileResource> fileResources) {
    return saveFileResources(contentNode.getUuid(), fileResources);
  }

  @Override
  public List<FileResource> saveFileResources(
      UUID contentNodeUuid, List<FileResource> fileResources) {
    return endpoint.saveFileResources(contentNodeUuid, fileResources);
  }

  @Override
  public ContentNode update(ContentNode contentNode) {
    return endpoint.update(contentNode.getUuid(), contentNode);
  }

  @Override
  public List<ContentNode> getChildren(UUID uuid) {
    return endpoint.getChildren(uuid);
  }

  @Override
  public List<ContentNode> getChildren(ContentNode contentNode) {
    return getChildren(contentNode.getUuid());
  }

  @Override
  public ContentNode saveWithParentContentTree(
      ContentNode contentNode, UUID parentContentTreeUUID) {
    return endpoint.saveWithParentContentTree(contentNode, parentContentTreeUUID);
  }

  @Override
  public ContentNode saveWithParentContentNode(
      ContentNode contentNode, UUID parentContentNodeUUID) {
    return endpoint.saveWithParentContentNode(contentNode, parentContentNodeUUID);
  }
}
