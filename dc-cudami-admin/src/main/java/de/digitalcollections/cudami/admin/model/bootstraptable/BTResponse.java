package de.digitalcollections.cudami.admin.model.bootstraptable;

import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;

public class BTResponse<T extends Object> {
  private List<T> rows;
  private long total;

  public BTResponse() {}

  public BTResponse(PageResponse<T> pageResponse) {
    rows = pageResponse.getContent();
    total = pageResponse.getTotalElements();
  }

  public List<T> getRows() {
    return rows;
  }

  public long getTotal() {
    return total;
  }
}
