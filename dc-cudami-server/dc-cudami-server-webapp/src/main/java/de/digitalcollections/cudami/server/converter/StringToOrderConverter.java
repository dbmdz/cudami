package de.digitalcollections.cudami.server.converter;

import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.NullHandling;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.core.convert.converter.Converter;

/**
 * Converter for converting URL params for Sorting from String to instance of Order. Used in
 * WebController. Fills model object Order.
 *
 * @see Order
 */
public class StringToOrderConverter implements Converter<String, Order> {

  private final Pattern ORDER_PATTERN =
      Pattern.compile(
          "^(?i)"
              + "(?<property>[a-z]+)"
              + "(_(?<subProperty>[a-z\\-]+))?" // subProperty allowing a-z and dash "-"
              + "(\\.(?<direction>asc|desc))?"
              + "(\\.(?<nullHandling>nullsfirst|nullslast))?"
              + "(\\.(?<ignoreCase>ignorecase))?"
              + "$");

  @Override
  public Order convert(String source) {
    if (source == null) {
      return null;
    }
    Matcher matcher = ORDER_PATTERN.matcher(source);
    if (!matcher.matches()) {
      return null;
    }
    Order.OrderBuilder<?, ?> order = Order.builder();
    String property = matcher.group("property");
    order.property(property);
    String subProperty = matcher.group("subProperty");
    if (subProperty != null) {
      order.subProperty(subProperty);
    }
    String direction = matcher.group("direction");
    if (direction != null) {
      order.direction(Direction.fromString(direction));
    } else {
      order.direction(Sorting.DEFAULT_DIRECTION);
    }
    String nullHandling = matcher.group("nullHandling");
    if (nullHandling != null) {
      if ("nullsfirst".equals(nullHandling.toLowerCase())) {
        order.nullHandling(NullHandling.NULLS_FIRST);
      } else {
        order.nullHandling(NullHandling.NULLS_LAST);
      }
    } else {
      order.nullHandling(NullHandling.NATIVE);
    }
    String ignoreCase = matcher.group("ignoreCase");
    if (ignoreCase != null) {
      order.ignoreCase(true);
    } else {
      order.ignoreCase(false);
    }
    return order.build();
  }
}
