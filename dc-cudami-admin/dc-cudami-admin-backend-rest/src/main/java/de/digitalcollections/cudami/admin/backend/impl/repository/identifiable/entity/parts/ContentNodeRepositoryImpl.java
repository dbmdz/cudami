package de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.entity.parts;

import de.digitalcollections.cudami.admin.backend.api.repository.LocaleRepository;
import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity.parts.ContentNodeRepository;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.parts.ContentNode;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.parts.ContentNodeImpl;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ContentNodeRepositoryImpl<E extends Entity> extends EntityPartRepositoryImpl<ContentNode, E> implements ContentNodeRepository<E> {

  @Autowired
  LocaleRepository localeRepository;

  @Autowired
  private ContentNodeRepositoryEndpoint endpoint;

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
    PageResponse<ContentNode> pageResponse = endpoint.find(f.getPageNumber(), f.getPageSize(), f.getSortField(), f.getSortDirection(), f.getNullHandling());
    return getGenericPageResponse(pageResponse);
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
  public LinkedHashSet<E> getEntities(ContentNode contentNode) {
    return getEntities(contentNode.getUuid());
  }

  @Override
  public LinkedHashSet<E> getEntities(UUID contentNodeUuid) {
    return convertToGenericLinkedHashSet(endpoint.getEntities(contentNodeUuid));
  }

  @Override
  public LinkedHashSet<FileResource> getFileResources(ContentNode contentNode) {
    return getFileResources(contentNode.getUuid());
  }

  @Override
  public LinkedHashSet<FileResource> getFileResources(UUID contentNodeUuid) {
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
  public LinkedHashSet<E> saveEntities(ContentNode contentNode, LinkedHashSet<E> entities) {
    return saveEntities(contentNode.getUuid(), entities);
  }

  @Override
  public LinkedHashSet<E> saveEntities(UUID contentNodeUuid, LinkedHashSet<E> entities) {
    return convertToGenericLinkedHashSet(endpoint.saveEntities(contentNodeUuid, convertFromGenericLinkedHashSet(entities)));
  }

  @Override
  public LinkedHashSet<FileResource> saveFileResources(ContentNode contentNode, LinkedHashSet<FileResource> fileResources) {
    return saveFileResources(contentNode.getUuid(), fileResources);
  }

  @Override
  public LinkedHashSet<FileResource> saveFileResources(UUID contentNodeUuid, LinkedHashSet<FileResource> fileResources) {
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
  public ContentNode saveWithParentContentTree(ContentNode contentNode, UUID parentContentTreeUUID) {
    return endpoint.saveWithParentContentTree(contentNode, parentContentTreeUUID);
  }

  @Override
  public ContentNode saveWithParentContentNode(ContentNode contentNode, UUID parentContentNodeUUID) {
    return endpoint.saveWithParentContentNode(contentNode, parentContentNodeUUID);
  }
}
