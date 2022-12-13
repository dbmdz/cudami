package de.digitalcollections.cudami.admin.controller;

import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.client.CudamiLocalesClient;
import de.digitalcollections.cudami.client.CudamiRestClient;
import de.digitalcollections.model.UniqueObject;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public abstract class AbstractPagingAndSortingController<T extends UniqueObject>
    extends AbstractController {

  @SuppressFBWarnings
  protected PageRequest createPageRequest(
      String sort,
      String order,
      String itemLocale,
      CudamiLocalesClient localeService,
      int offset,
      int limit,
      String searchTerm)
      throws TechnicalException {
    Sorting sorting = null;
    if (sort != null && order != null) {
      Order sortingOrder;
      if ("label".equals(sort)) {
        String language = itemLocale;
        if (language == null && localeService != null) {
          language = localeService.getDefaultLanguage().getLanguage();
        }
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
      CudamiLocalesClient localeService,
      CudamiRestClient<T> service,
      int offset,
      int limit,
      String searchTerm,
      String sort,
      String order,
      String itemLocale)
      throws TechnicalException {

    PageRequest pageRequest =
        createPageRequest(sort, order, itemLocale, localeService, offset, limit, searchTerm);
    PageResponse<T> pageResponse = service.find(pageRequest);
    return pageResponse;
  }
}
