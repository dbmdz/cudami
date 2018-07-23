package de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.resource;

import de.digitalcollections.core.model.api.paging.Order;
import de.digitalcollections.core.model.api.paging.PageRequest;
import de.digitalcollections.core.model.api.paging.PageResponse;
import de.digitalcollections.core.model.api.paging.Sorting;
import de.digitalcollections.cudami.admin.backend.api.repository.LocaleRepository;
import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.resource.ContentNodeRepository;
import de.digitalcollections.cudami.model.impl.identifiable.parts.MultilanguageDocumentImpl;
import de.digitalcollections.cudami.model.impl.identifiable.parts.TextImpl;
import de.digitalcollections.cudami.model.impl.identifiable.resource.ContentNodeImpl;
import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ContentNodeRepositoryImpl implements ContentNodeRepository<ContentNodeImpl> {

  @Autowired
  private ContentNodeRepositoryEndpoint endpoint;

  @Autowired
  LocaleRepository localeRepository;

  @Override
  public long count() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public ContentNodeImpl create() {
    Locale defaultLocale = localeRepository.getDefault();
    ContentNodeImpl contentNode = new ContentNodeImpl();
    contentNode.setLabel(new TextImpl(defaultLocale, ""));
    contentNode.setDescription(new MultilanguageDocumentImpl(defaultLocale));
    return contentNode;
  }

  @Override
  public PageResponse<ContentNodeImpl> find(PageRequest pageRequest) {
    int pageNumber = pageRequest.getPageNumber();
    int pageSize = pageRequest.getPageSize();

    Sorting sorting = pageRequest.getSorting();
    Iterator<Order> iterator = sorting.iterator();

    // FIXME add support for multiple sort fields
    String sortField = "";
    String sortDirection = "";
    String nullHandling = "";
//    while (iterator.hasNext()) {
    if (iterator.hasNext()) {
      Order order = iterator.next();
      sortField = order.getProperty() == null ? "" : order.getProperty();
      sortDirection = order.getDirection() == null ? "" : order.getDirection().name();
      nullHandling = order.getNullHandling() == null ? "" : order.getNullHandling().name();
    }

    return endpoint.find(pageNumber, pageSize, sortField, sortDirection, nullHandling);
  }

  @Override
  public ContentNodeImpl findOne(UUID uuid) {
    return endpoint.findOne(uuid);
  }

  @Override
  public ContentNodeImpl save(ContentNodeImpl webpage) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public ContentNodeImpl saveWithParentContentTree(ContentNodeImpl webpage, UUID parentWebsiteUUID) {
    return (ContentNodeImpl) endpoint.saveWithParentContentTree(webpage, parentWebsiteUUID);
  }

  @Override
  public ContentNodeImpl saveWithParentContentNode(ContentNodeImpl webpage, UUID parentWebpageUUID) {
    return (ContentNodeImpl) endpoint.saveWithParentContentNode(webpage, parentWebpageUUID);
  }

  @Override
  public ContentNodeImpl update(ContentNodeImpl webpage) {
    return (ContentNodeImpl) endpoint.update(webpage.getUuid(), webpage);
  }
}
