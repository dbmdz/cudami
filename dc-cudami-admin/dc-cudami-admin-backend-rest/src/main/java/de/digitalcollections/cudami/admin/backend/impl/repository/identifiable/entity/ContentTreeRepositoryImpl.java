package de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.entity;

import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity.ContentTreeRepository;
import de.digitalcollections.model.api.identifiable.entity.ContentTree;
import de.digitalcollections.model.api.identifiable.entity.parts.ContentNode;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.ContentTreeImpl;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ContentTreeRepositoryImpl<C extends ContentTree> extends EntityRepositoryImpl<C> implements ContentTreeRepository<C> {

  @Autowired
  private ContentTreeRepositoryEndpoint endpoint;

  @Override
  public long count() {
    return endpoint.count();
  }

  @Override
  public C create() {
    return (C) new ContentTreeImpl();
  }

  @Override
  public PageResponse<C> find(PageRequest pageRequest) {
    FindParams f = getFindParams(pageRequest);
    PageResponse<ContentTree> pageResponse = endpoint.find(f.getPageNumber(), f.getPageSize(), f.getSortField(), f.getSortDirection(), f.getNullHandling());
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
  public List<ContentNode> getRootNodes(C contentTree) {
    return (List<ContentNode>) endpoint.getRootNodes(contentTree.getUuid());
  }
}
