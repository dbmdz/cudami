package de.digitalcollections.cudami.admin.model.bootstraptable;

import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.Order;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;

@SuppressFBWarnings
public class BTRequest extends PageRequest {

  public BTRequest(int offset, int limit) {
    this(offset, limit, null);
  }

  public BTRequest(int offset, int limit, String sortProperty, String sortOrder) {
    this(offset, limit, sortProperty, sortOrder, null);
  }

  public BTRequest(int offset, int limit, List<Order> sortOrders) {
    super((int) Math.ceil(offset / limit), limit, sortOrders);
  }

  public BTRequest(
      int offset, int limit, String sortProperty, String sortOrder, String sortLanguage) {
    this(offset, limit, createSorting(sortProperty, sortOrder, sortLanguage));
  }

  public static List<Order> createSorting(
      String sortProperty, String sortOrder, String sortLanguage) {
    return List.of(
        Order.builder()
            .property(sortProperty)
            .subProperty(sortLanguage)
            .direction(Direction.fromString(sortOrder))
            .build());
  }
}
