package de.digitalcollections.cudami.client.backend.impl.repository.identifiable.entity;

import de.digitalcollections.core.model.api.paging.Order;
import de.digitalcollections.core.model.api.paging.PageRequest;
import de.digitalcollections.core.model.api.paging.PageResponse;
import de.digitalcollections.core.model.api.paging.Sorting;
import de.digitalcollections.cudami.client.backend.api.repository.identifiable.entity.ContentTreeRepository;
import de.digitalcollections.cudami.model.api.identifiable.resource.ContentNode;
import de.digitalcollections.cudami.model.impl.identifiable.entity.ContentTreeImpl;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ContentTreeRepositoryImpl implements ContentTreeRepository<ContentTreeImpl> {

  @Autowired
  private ContentTreeRepositoryEndpoint endpoint;

  @Override
  public long count() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public ContentTreeImpl create() {
    return new ContentTreeImpl();
  }

  @Override
  public PageResponse<ContentTreeImpl> find(PageRequest pageRequest) {
    int pageNumber = pageRequest.getPageNumber();
    int pageSize = pageRequest.getPageSize();
    // FIXME add support for multiple sort fields
    String sortField = "";
    String sortDirection = "";
    String nullHandling = "";

    Sorting sorting = pageRequest.getSorting();
    if (sorting != null) {
      Iterator<Order> iterator = sorting.iterator();

//    while (iterator.hasNext()) {
      if (iterator.hasNext()) {
        Order order = iterator.next();
        sortField = order.getProperty() == null ? "" : order.getProperty();
        sortDirection = order.getDirection() == null ? "" : order.getDirection().name();
        nullHandling = order.getNullHandling() == null ? "" : order.getNullHandling().name();
      }
    }

    return endpoint.find(pageNumber, pageSize, sortField, sortDirection, nullHandling);
  }

  @Override
  public ContentTreeImpl findOne(UUID uuid) {
    return endpoint.findOne(uuid);
  }

  @Override
  public List<ContentNode> getRootNodes(ContentTreeImpl contentTreeImpl) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public ContentTreeImpl save(ContentTreeImpl contentTree) {
    return (ContentTreeImpl) endpoint.save(contentTree);
  }

  @Override
  public ContentTreeImpl update(ContentTreeImpl contentTree) {
    return (ContentTreeImpl) endpoint.update(contentTree.getUuid(), contentTree);
  }
}
