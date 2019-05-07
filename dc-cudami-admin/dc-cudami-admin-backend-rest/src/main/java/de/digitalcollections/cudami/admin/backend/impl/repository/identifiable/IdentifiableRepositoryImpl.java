package de.digitalcollections.cudami.admin.backend.impl.repository.identifiable;

import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.model.api.identifiable.Identifiable;
import de.digitalcollections.model.api.paging.Order;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.Sorting;
import de.digitalcollections.model.impl.identifiable.IdentifiableImpl;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class IdentifiableRepositoryImpl<I extends Identifiable> implements IdentifiableRepository<I> {

  private static final Logger LOGGER = LoggerFactory.getLogger(IdentifiableRepositoryImpl.class);

  @Autowired
  private IdentifiableRepositoryEndpoint endpoint;

  @Override
  public long count() {
    return endpoint.count();
  }

  @Override
  public I create() {
    return (I) new IdentifiableImpl();
  }

  @Override
  public PageResponse<I> find(PageRequest pageRequest) {
    FindParams f = getFindParams(pageRequest);
    PageResponse<Identifiable> pageResponse = endpoint.find(f.getPageNumber(), f.getPageSize(), f.getSortField(), f.getSortDirection(), f.getNullHandling());
    return getGenericPageResponse(pageResponse);
  }

  @Override
  public List<I> find(String searchTerm, int maxResults) {
    return convertToGenericList(endpoint.find(searchTerm, maxResults));
  }

  /**
   * Wrapper for find params
   *
   * @param pageRequest source for find params
   * @return wrapped find params
   */
  protected FindParams getFindParams(PageRequest pageRequest) {
    int pageNumber = pageRequest.getPageNumber();
    int pageSize = pageRequest.getPageSize();

    Sorting sorting = pageRequest.getSorting();
    Iterator<Order> iterator = sorting.iterator();

    String sortField = "";
    String sortDirection = "";
    String nullHandling = "";

    if (iterator.hasNext()) {
      Order order = iterator.next();
      sortField = order.getProperty() == null ? "" : order.getProperty();
      sortDirection = order.getDirection() == null ? "" : order.getDirection().name();
      nullHandling = order.getNullHandling() == null ? "" : order.getNullHandling().name();
    }

    return new FindParams(pageNumber, pageSize, sortField, sortDirection, nullHandling);
  }

  public class FindParams {

    public FindParams(int pageNumber, int pageSize, String sortField, String sortDirection, String nullHandling) {
      this.pageNumber = pageNumber;
      this.pageSize = pageSize;
      this.sortField = sortField;
      this.sortDirection = sortDirection;
      this.nullHandling = nullHandling;
    }

    final int pageNumber;
    final int pageSize;
    final String sortField;
    final String sortDirection;
    final String nullHandling;

    public int getPageNumber() {
      return pageNumber;
    }

    public int getPageSize() {
      return pageSize;
    }

    public String getSortField() {
      return sortField;
    }

    public String getSortDirection() {
      return sortDirection;
    }

    public String getNullHandling() {
      return nullHandling;
    }
  }

  protected PageResponse<I> getGenericPageResponse(PageResponse pageResponse) {
    PageResponse<I> genericPageResponse;
    if (pageResponse.hasContent()) {
      List<Identifiable> content = pageResponse.getContent();
      List<I> genericContent = convertToGenericList(content);
      genericPageResponse = (PageResponse<I>) pageResponse;
      genericPageResponse.setContent(genericContent);
    } else {
      genericPageResponse = (PageResponse<I>) pageResponse;
    }
    return genericPageResponse;
  }

  protected List<I> convertToGenericList(List<Identifiable> identifiables) {
    if (identifiables == null) {
      return null;
    }
    List<I> genericContent = identifiables.stream().map(s -> (I) s).collect(Collectors.toList());
    return genericContent;
  }

  @Override
  public I findOne(UUID uuid) {
    return (I) endpoint.findOne(uuid);
  }

  @Override
  public I findOne(UUID uuid, Locale locale) {
    return (I) endpoint.findOne(uuid, locale.toString());
  }

  @Override
  public I save(I identifiable) {
    return (I) endpoint.save(identifiable);
  }

  @Override
  public I update(I identifiable) {
    return (I) endpoint.update(identifiable.getUuid(), identifiable);
  }

}
