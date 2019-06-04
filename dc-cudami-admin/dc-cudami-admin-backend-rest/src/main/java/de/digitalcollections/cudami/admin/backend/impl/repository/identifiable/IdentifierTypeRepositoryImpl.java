package de.digitalcollections.cudami.admin.backend.impl.repository.identifiable;

import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.IdentifierTypeRepository;
import de.digitalcollections.model.api.identifiable.IdentifierType;
import de.digitalcollections.model.api.paging.Order;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.Sorting;
import de.digitalcollections.model.impl.identifiable.IdentifierTypeImpl;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class IdentifierTypeRepositoryImpl implements IdentifierTypeRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(IdentifierTypeRepositoryImpl.class);

  @Autowired
  private IdentifierTypeRepositoryEndpoint endpoint;

  @Override
  public IdentifierType create() {
    return new IdentifierTypeImpl();
  }

  @Override
  public PageResponse<IdentifierType> find(PageRequest pageRequest) {
    FindParams f = getFindParams(pageRequest);
    PageResponse<IdentifierType> pageResponse = endpoint.find(f.getPageNumber(), f.getPageSize(), f.getSortField(), f.getSortDirection(), f.getNullHandling());
    return getGenericPageResponse(pageResponse);
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

  private static class FindParams {

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

  protected PageResponse<IdentifierType> getGenericPageResponse(PageResponse pageResponse) {
    PageResponse<IdentifierType> genericPageResponse;
    if (pageResponse.hasContent()) {
      List<IdentifierType> content = pageResponse.getContent();
      List<IdentifierType> genericContent = convertToGenericList(content);
      genericPageResponse = (PageResponse<IdentifierType>) pageResponse;
      genericPageResponse.setContent(genericContent);
    } else {
      genericPageResponse = (PageResponse<IdentifierType>) pageResponse;
    }
    return genericPageResponse;
  }

  protected List<IdentifierType> convertToGenericList(List<IdentifierType> identifierTypes) {
    if (identifierTypes == null) {
      return null;
    }
    List<IdentifierType> genericContent = identifierTypes.stream().map(s -> (IdentifierType) s).collect(Collectors.toList());
    return genericContent;
  }

  @Override
  public IdentifierType findOne(UUID uuid) {
    return (IdentifierType) endpoint.findOne(uuid);
  }

  @Override
  public IdentifierType save(IdentifierType identifierType) {
    return (IdentifierType) endpoint.save(identifierType);
  }

  @Override
  public IdentifierType update(IdentifierType identifierType) {
    return (IdentifierType) endpoint.update(identifierType.getUuid(), identifierType);
  }
}
