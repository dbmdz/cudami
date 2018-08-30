package de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.entity.parts;

import de.digitalcollections.cudami.admin.backend.api.repository.LocaleRepository;
import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity.parts.ContentNodeRepository;
import de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.IdentifiableRepositoryImpl;
import de.digitalcollections.model.api.identifiable.entity.parts.ContentNode;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.parts.LocalizedTextImpl;
import de.digitalcollections.model.impl.identifiable.parts.structuredcontent.LocalizedStructuredContentImpl;
import de.digitalcollections.model.impl.identifiable.entity.parts.ContentNodeImpl;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ContentNodeRepositoryImpl<C extends ContentNode> extends IdentifiableRepositoryImpl<C> implements ContentNodeRepository<C> {

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
    Locale defaultLocale = localeRepository.getDefault();
    C contentNode = (C) new ContentNodeImpl();
    contentNode.setLabel(new LocalizedTextImpl(defaultLocale, ""));
    contentNode.setDescription(new LocalizedStructuredContentImpl(defaultLocale));
    return contentNode;
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

  @Override
  public List<Identifiable> getIdentifiables(C contentNode) {
    return getIdentifiables(contentNode.getUuid());
  }

  private List<Identifiable> getIdentifiables(UUID uuid) {
    return endpoint.getIdentifiables(uuid);
  }
}
