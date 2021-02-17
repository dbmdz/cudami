package de.digitalcollections.cudami.admin.paging;

import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.Sorting;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PageableConverter {

  public static PageRequest convert(Pageable pageable) {
    if (pageable == null) {
      return null;
    }
    int pageNumber = pageable.getPageNumber();
    int pageSize = pageable.getPageSize();

    final Sort sort = pageable.getSort();
    Sorting sorting = SortConverter.convert(sort);

    PageRequest pageRequest = new PageRequest(pageNumber, pageSize, sorting);
    return pageRequest;
  }

  public static PageRequest convert(
      Pageable pageable, int defaultPageSize, String defaultSortField) {
    PageRequest pageRequest = convert(pageable);
    if (pageRequest.getPageSize() == -1 && defaultPageSize != -1) {
      pageRequest.setPageSize(defaultPageSize);
    }
    if (pageRequest.getSorting() == null && defaultSortField != null) {
      Sorting sorting = new Sorting(defaultSortField);
      pageRequest.setSorting(sorting);
    }
    return pageRequest;
  }

  public static Pageable convert(PageRequest pageRequest) {
    if (pageRequest == null) {
      return null;
    }
    int pageNumber = pageRequest.getPageNumber();
    int pageSize = pageRequest.getPageSize();

    final Sorting sorting = pageRequest.getSorting();
    Sort sort = SortConverter.convert(sorting);

    if (sort == null) {
      return org.springframework.data.domain.PageRequest.of(pageNumber, pageSize);
    }
    return org.springframework.data.domain.PageRequest.of(pageNumber, pageSize, sort);
  }
}
