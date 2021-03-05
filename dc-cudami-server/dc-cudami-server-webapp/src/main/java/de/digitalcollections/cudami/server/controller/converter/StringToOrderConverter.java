package de.digitalcollections.cudami.server.controller.converter;

import de.digitalcollections.model.paging.Direction;
import de.digitalcollections.model.paging.NullHandling;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.OrderBuilder;
import de.digitalcollections.model.paging.Sorting;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.stereotype.Component;

@Component
/**
 * Converter for converting URL params for Sorting from String to Order-instance. Used in
 * WebController. Fills model object Order.
 *
 * @see de.digitalcollections.model.api.paging.Order
 */
public class StringToOrderConverter implements GenericConverter {
  private final Pattern ORDER_PATTERN =
      Pattern.compile(
          "^(?<property>[A-Za-z]+)(_(?<subProperty>[A-Za-z]+))?(\\.(?<direction>asc|desc))?(\\.(?<nullHandling>nullsfirst|nullslast))?$");

  @Override
  public Order convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
    if (source == null) {
      return null;
    }
    String propertyString = (String) source;
    Matcher matcher = ORDER_PATTERN.matcher(propertyString);
    if (!matcher.matches()) {
      return null;
    }
    OrderBuilder order = Order.defaultBuilder();
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
      if ("nullsfirst".equals(nullHandling)) {
        order.nullHandling(NullHandling.NULLS_FIRST);
      } else {
        order.nullHandling(NullHandling.NULLS_LAST);
      }
    } else {
      order.nullHandling(NullHandling.NATIVE);
    }
    return order.build();
  }

  @Override
  public Set<ConvertiblePair> getConvertibleTypes() {
    Set<ConvertiblePair> convertibleTypes = new HashSet<>();
    convertibleTypes.add(new ConvertiblePair(String.class, Order.class));
    return convertibleTypes;
  }
}
