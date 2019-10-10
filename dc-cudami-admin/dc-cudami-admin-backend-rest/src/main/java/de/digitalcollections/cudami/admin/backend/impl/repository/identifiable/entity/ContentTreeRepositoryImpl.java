package de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.entity;

import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity.ContentTreeRepository;
import de.digitalcollections.model.api.identifiable.entity.ContentTree;
import de.digitalcollections.model.api.identifiable.entity.parts.ContentNode;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.ContentTreeImpl;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ContentTreeRepositoryImpl extends EntityRepositoryImpl<ContentTree>
    implements ContentTreeRepository {

  @Autowired private ContentTreeRepositoryEndpoint endpoint;

  @Override
  public long count() {
    return endpoint.count();
  }

  @Override
  public ContentTree create() {
    return new ContentTreeImpl();
  }

  @Override
  public PageResponse<ContentTree> find(PageRequest pageRequest) {
    FindParams f = getFindParams(pageRequest);
    PageResponse<ContentTree> pageResponse =
        endpoint.find(
            f.getPageNumber(),
            f.getPageSize(),
            f.getSortField(),
            f.getSortDirection(),
            f.getNullHandling());
    return getGenericPageResponse(pageResponse);
  }

  @Override
  public ContentTree findOne(UUID uuid) {
    return endpoint.findOne(uuid);
  }

  @Override
  public ContentTree findOne(UUID uuid, Locale locale) {
    return endpoint.findOne(uuid, locale.toString());
  }

  @Override
  public List<ContentNode> getRootNodes(ContentTree contentTree) {
    return getRootNodes(contentTree.getUuid());
  }

  @Override
  public List<ContentNode> getRootNodes(UUID uuid) {
    return endpoint.getRootNodes(uuid);
  }

  @Override
  public ContentTree save(ContentTree contentTree) {
    return endpoint.save(contentTree);
  }

  @Override
  public ContentTree update(ContentTree contentTree) {
    return endpoint.update(contentTree.getUuid(), contentTree);
  }
}
