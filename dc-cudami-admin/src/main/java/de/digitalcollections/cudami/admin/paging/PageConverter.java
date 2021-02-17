package de.digitalcollections.cudami.admin.paging;

import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public class PageConverter {

  public static Page convert(PageResponse pageResponse) {
    if (pageResponse == null) {
      return null;
    }
    return convert(pageResponse, pageResponse.getPageRequest());
  }

  public static Page convert(PageResponse pageResponse, PageRequest pageRequest) {
    if (pageResponse == null) {
      return null;
    }
    Pageable pageable = PageableConverter.convert(pageRequest);
    @SuppressWarnings("unchecked")
    Page page = new PageImpl(pageResponse.getContent(), pageable, pageResponse.getTotalElements());
    return page;
  }
}
