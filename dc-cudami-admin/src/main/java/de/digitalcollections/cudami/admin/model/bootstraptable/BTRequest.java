package de.digitalcollections.cudami.admin.model.bootstraptable;

import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.Order;
import java.util.List;

public class BTRequest extends PageRequest {

  public BTRequest(int offset, int limit, String sortProperty, String sortOrder) {
    this(offset, limit, sortProperty, sortOrder, null);
  }

  public BTRequest(
      int offset, int limit, String sortProperty, String sortOrder, String sortLanguage) {
    super(
        (int) Math.ceil(offset / limit),
        limit,
        List.of(
            Order.builder()
                .property(sortProperty)
                .subProperty(sortLanguage)
                .direction(Direction.fromString(sortOrder))
                .build()));
  }
}
