package de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.resource;

import de.digitalcollections.cudami.admin.backend.api.repository.LocaleRepository;
import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.resource.ContentNodeRepository;
import de.digitalcollections.model.api.identifiable.resource.ContentNode;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.resource.ContentNodeImpl;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ContentNodeRepositoryImpl<C extends ContentNode> extends ResourceRepositoryImpl<C> implements ContentNodeRepository<C> {

  @Autowired
  LocaleRepository localeRepository;

  @Autowired
  private ContentNodeRepositoryEndpoint endpoint;

  @Override
  public long count() {
    return endpoint.count();
  }

  @Override
  public C create() {
    return (C) new ContentNodeImpl();
  }

  @Override
  public PageResponse<C> find(PageRequest pageRequest) {
    FindParams f = getFindParams(pageRequest);
    PageResponse<ContentNode> pageResponse = endpoint.find(f.getPageNumber(), f.getPageSize(), f.getSortField(), f.getSortDirection(), f.getNullHandling());
    return getGenericPageResponse(pageResponse);
  }

  @Override
  public C findOne(UUID uuid) {
    return (C) endpoint.findOne(uuid);
  }

  @Override
  public C save(C identifiable) {
    return (C) endpoint.save(identifiable);
  }

  @Override
  public C update(C identifiable) {
    return (C) endpoint.update(identifiable.getUuid(), identifiable);
  }

  @Override
  public List<C> getChildren(UUID uuid) {
    return (List<C>) endpoint.getChildren(uuid);
  }

  @Override
  public List<C> getChildren(C contentNode) {
    return getChildren(contentNode.getUuid());
  }

  @Override
  public C saveWithParentContentTree(C contentNode, UUID parentContentTreeUUID) {
    return (C) endpoint.saveWithParentContentTree(contentNode, parentContentTreeUUID);
  }

  @Override
  public C saveWithParentContentNode(C contentNode, UUID parentContentNodeUUID) {
    return (C) endpoint.saveWithParentContentNode(contentNode, parentContentNodeUUID);
  }

}
