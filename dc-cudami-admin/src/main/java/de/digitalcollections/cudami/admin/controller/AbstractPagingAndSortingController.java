package de.digitalcollections.cudami.admin.controller;

import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.business.i18n.LanguageService;
import de.digitalcollections.cudami.client.CudamiRestClient;
import de.digitalcollections.model.UniqueObject;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;

public abstract class AbstractPagingAndSortingController<T extends UniqueObject>
    extends AbstractController {

  @SuppressFBWarnings
  protected PageRequest createPageRequest(int offset, int limit, String sort, String order) {
    Sorting sorting = null;
    if (sort != null && order != null) {
      Order sortingOrder =
          Order.builder().property(sort).direction(Direction.fromString(order)).build();
      sorting = Sorting.builder().order(sortingOrder).build();
    }
    PageRequest pageRequest =
        PageRequest.builder()
            .pageNumber((int) Math.ceil(offset / limit))
            .pageSize(limit)
            .sorting(sorting)
            .build();
    return pageRequest;
  }

  @SuppressFBWarnings
  protected PageRequest createPageRequest(
      int pageNumber, int pageSize, String searchField, String searchTerm, List<Order> sortBy) {
    PageRequest pageRequest;
    if (searchField == null) {
      pageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
    } else {
      pageRequest = new PageRequest(pageNumber, pageSize);
    }

    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    return pageRequest;
  }

  @SuppressFBWarnings
  protected PageRequest createPageRequest(
      String sort,
      String order,
      String dataLanguage,
      LanguageService languageService,
      int offset,
      int limit,
      String searchTerm)
      throws TechnicalException {
    Sorting sorting = null;
    if (sort != null && order != null) {
      Order sortingOrder;
      if ("label".equals(sort) && dataLanguage != null) {
        String language = getDataLanguage(dataLanguage, languageService);
        sortingOrder =
            Order.builder()
                .property("label")
                .subProperty(language)
                .direction(Direction.fromString(order))
                .build();
      } else {
        sortingOrder =
            Order.builder().property(sort).direction(Direction.fromString(order)).build();
      }
      sorting = Sorting.builder().order(sortingOrder).build();
    }
    PageRequest pageRequest =
        PageRequest.builder()
            .pageNumber((int) Math.ceil(offset / limit))
            .pageSize(limit)
            .searchTerm(searchTerm)
            .sorting(sorting)
            .build();
    return pageRequest;
  }

  public PageResponse<T> find(
      LanguageService languageService,
      CudamiRestClient<T> service,
      int offset,
      int limit,
      String searchTerm,
      String sort,
      String order,
      String dataLanguage)
      throws TechnicalException {

    PageRequest pageRequest =
        createPageRequest(sort, order, dataLanguage, languageService, offset, limit, searchTerm);
    PageResponse<T> pageResponse = service.find(pageRequest);
    return pageResponse;
  }

  protected String getDataLanguage(String targetDataLanguage, LanguageService languageService)
      throws TechnicalException {
    String dataLanguage = targetDataLanguage;
    if (dataLanguage == null && languageService != null) {
      dataLanguage = languageService.getDefaultLanguage().getLanguage();
    }
    return dataLanguage;
  }
}
