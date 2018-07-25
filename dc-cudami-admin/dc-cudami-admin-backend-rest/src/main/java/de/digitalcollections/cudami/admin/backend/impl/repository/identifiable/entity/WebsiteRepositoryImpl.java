package de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.entity;

import de.digitalcollections.cudami.admin.backend.api.repository.LocaleRepository;
import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity.WebsiteRepository;
import de.digitalcollections.model.api.identifiable.resource.Webpage;
import de.digitalcollections.model.api.paging.Order;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.Sorting;
import de.digitalcollections.model.impl.identifiable.entity.WebsiteImpl;
import de.digitalcollections.model.impl.identifiable.parts.LocalizedTextImpl;
import de.digitalcollections.model.impl.identifiable.parts.structuredcontent.LocalizedStructuredContentImpl;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class WebsiteRepositoryImpl implements WebsiteRepository<WebsiteImpl> {

  @Autowired
  private WebsiteRepositoryEndpoint endpoint;

  @Autowired
  private LocaleRepository localeRepository;

  @Override
  public long count() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public WebsiteImpl create() {
    Locale defaultLocale = localeRepository.getDefault();
    WebsiteImpl website = new WebsiteImpl();
    website.setLabel(new LocalizedTextImpl(defaultLocale, ""));
    website.setDescription(new LocalizedStructuredContentImpl(defaultLocale));
    return website;
  }

  @Override
  public PageResponse<WebsiteImpl> find(PageRequest pageRequest) {
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
  public WebsiteImpl findOne(UUID uuid) {
    return endpoint.findOne(uuid);
  }

  @Override
  public List<Webpage> getRootNodes(WebsiteImpl website) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public WebsiteImpl save(WebsiteImpl website) {
    return (WebsiteImpl) endpoint.save(website);
  }

  @Override
  public WebsiteImpl update(WebsiteImpl website) {
    return (WebsiteImpl) endpoint.update(website.getUuid(), website);
  }
}
