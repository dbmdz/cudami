package de.digitalcollections.cudami.admin.model.bootstraptable;

import de.digitalcollections.model.list.paging.PageResponse;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;

public class BTResponse<T extends Object> {
  private List<T> rows;
  private long total;

  public BTResponse() {}

  public BTResponse(PageResponse<T> pageResponse) {
    rows = pageResponse.getContent();
    total = pageResponse.getTotalElements();
  }

  @SuppressFBWarnings
  public List<T> getRows() {
    return rows;
  }

  public long getTotal() {
    return total;
  }
}
