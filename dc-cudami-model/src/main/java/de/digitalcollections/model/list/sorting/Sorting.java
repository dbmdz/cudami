package de.digitalcollections.model.list.sorting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * SortingImpl option for queries. You have to provide at least a list of properties to sort for
 * that must not include {@literal null} or empty strings. The direction defaults to {@link
 * Sorting#DEFAULT_DIRECTION}. See Spring Data Commons, but more flat design and independent of
 * Spring libraries.
 */
public class Sorting implements Iterable<Order> {

  public static final Direction DEFAULT_DIRECTION = Direction.ASC;

  public static Builder builder() {
    return new Builder();
  }

  private List<Order> orders;

  public Sorting() {}

  /**
   * Creates a new {@link Sorting} instance using the given {@link Order}s.
   *
   * @param orders must not be {@literal null}.
   */
  public Sorting(Order... orders) {
    this(Arrays.asList(orders));
  }

  /**
   * Creates a new {@link Sorting} instance.
   *
   * @param orders must not be {@literal null} or contain {@literal null}.
   */
  public Sorting(List<Order> orders) {
    if (orders == null) {
      orders = new ArrayList<>();
    }
    orders = orders.stream().filter(Objects::nonNull).collect(Collectors.toList());
    if (orders.isEmpty()) {
      throw new IllegalArgumentException(
          "You have to provide at least one sort property to sort by!");
    }
    this.orders = orders;
  }

  /**
   * Creates a new {@link Sorting} instance. Order defaults to Direction#ASC.
   *
   * @param properties must not be {@literal null} or contain {@literal null} or empty strings
   */
  public Sorting(String... properties) {
    this(DEFAULT_DIRECTION, properties);
  }

  /**
   * Creates a new {@link Sorting} instance.
   *
   * @param direction defaults to {@link Sorting#DEFAULT_DIRECTION} (for {@literal null} cases, too)
   * @param properties must not be {@literal null}, empty or contain {@literal null} or empty
   *     strings.
   */
  public Sorting(Direction direction, String... properties) {
    this(direction, properties == null ? new ArrayList<String>() : Arrays.asList(properties));
  }

  /**
   * Creates a new {@link Sorting} instance.
   *
   * @param direction defaults to {@link Sorting#DEFAULT_DIRECTION} (for {@literal null} cases, too)
   * @param properties must not be {@literal null} or contain {@literal null} or empty strings.
   */
  public Sorting(Direction direction, List<String> properties) {

    if (properties == null || properties.isEmpty()) {
      throw new IllegalArgumentException("You have to provide at least one property to sort by!");
    }

    this.orders = new ArrayList<>(properties.size());

    for (String property : properties) {
      this.orders.add(new Order(direction, property));
    }
  }

  /**
   * Returns a new {@link Sorting} consisting of the {@link Order}s of the current {@link Sorting}
   * combined with the given ones.
   *
   * @param sort can be {@literal null}.
   * @return a new combined sort
   */
  public Sorting and(Sorting sort) {

    if (sort == null) {
      return this;
    }

    ArrayList<Order> these = new ArrayList<>(this.orders);

    for (Order order : sort) {
      these.add(order);
    }

    return new Sorting(these);
  }

  private String collectionToCommaDelimitedString(List<Order> coll) {
    if (coll == null || coll.isEmpty()) {
      return "";
    }
    StringBuilder sb = new StringBuilder(0);
    Iterator<?> it = coll.iterator();
    while (it.hasNext()) {
      sb.append("").append(it.next()).append("");
      if (it.hasNext()) {
        sb.append(",");
      }
    }
    return sb.toString();
  }

  @Override
  public boolean equals(Object obj) {

    if (this == obj) {
      return true;
    }

    if (!(obj instanceof Sorting)) {
      return false;
    }

    Sorting that = (Sorting) obj;

    return this.orders.equals(that.orders);
  }

  /**
   * Returns the order registered for the given property.
   *
   * @param property given property
   * @return the order registered for the given property
   */
  public Order getOrderFor(String property) {

    for (Order order : this) {
      if (order.getProperty().equals(property)) {
        return order;
      }
    }

    return null;
  }

  public List<Order> getOrders() {
    return orders;
  }

  @Override
  public int hashCode() {

    int result = 17;
    result = 31 * result + orders.hashCode();
    return result;
  }

  @Override
  public Iterator<Order> iterator() {
    return this.orders.iterator();
  }

  public void setOrders(List<Order> orders) {
    if (orders == null) {
      orders = new ArrayList<>(0);
    }
    orders = orders.stream().filter(Objects::nonNull).collect(Collectors.toList());
    if (orders.isEmpty()) {
      throw new IllegalArgumentException(
          "You have to provide at least one sort property to sort by!");
    }
    this.orders = orders;
  }

  @Override
  public String toString() {
    return collectionToCommaDelimitedString(orders);
  }

  public static class Builder {

    private List<Order> orders;

    public Sorting build() {
      return new Sorting(orders);
    }

    public Builder order(Order order) {
      if (orders == null) {
        orders = new ArrayList<>(0);
      }
      orders.add(order);
      return this;
    }
  }
}
