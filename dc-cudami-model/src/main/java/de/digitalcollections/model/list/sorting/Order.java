package de.digitalcollections.model.list.sorting;

import java.util.Optional;
import lombok.experimental.SuperBuilder;

/**
 * PropertyPath implements the pairing of an {@link Direction} and a property. It is used to provide
 * input for {@link Sorting}. See Spring Data Commons, but more flat design and independent of
 * Spring libraries.
 */
@SuperBuilder(buildMethodName = "prebuild")
public class Order {

  private static final Direction DEFAULT_DIRECTION = Sorting.DEFAULT_DIRECTION;
  private static final boolean DEFAULT_IGNORE_CASE = true;
  private static final NullHandling DEFAULT_NULL_HANDLING = NullHandling.NATIVE;

  private Direction direction;
  private Boolean ignoreCase;
  private NullHandling nullHandling;
  private String property;
  private String subProperty;

  public Order() {
    init();
  }

  public Order(
      Direction direction, boolean ignoreCase, NullHandling nullHandling, String property) {
    this(direction, property, ignoreCase, nullHandling);
  }

  /**
   * Creates a new {@link Order} instance. if order is {@literal null} then order defaults to {@link
   * Sorting#DEFAULT_DIRECTION}
   *
   * @param direction can be {@literal null}, will default to {@link Sorting#DEFAULT_DIRECTION}
   * @param property must not be {@literal null} or empty.
   */
  public Order(Direction direction, String property) {
    this(direction, property, DEFAULT_IGNORE_CASE, null);
  }

  /**
   * Creates a new {@link Order} instance. if order is {@literal null} then order defaults to {@link
   * Sorting#DEFAULT_DIRECTION}
   *
   * @param direction can be {@literal null}, will default to {@link Sorting#DEFAULT_DIRECTION}
   * @param property must not be {@literal null} or empty.
   * @param nullHandlingHint can be {@literal null}, will default to {@link NullHandling#NATIVE}.
   */
  public Order(Direction direction, String property, NullHandling nullHandlingHint) {
    this(direction, property, DEFAULT_IGNORE_CASE, nullHandlingHint);
  }

  /**
   * Creates a new {@link Order} instance. Takes a single property. Direction defaults to {@link
   * Sorting#DEFAULT_DIRECTION}.
   *
   * @param property must not be {@literal null} or empty.
   */
  public Order(String property) {
    this(null, property);
  }

  /**
   * Creates a new {@link Order} instance. if order is {@literal null} then order defaults to {@link
   * Sorting#DEFAULT_DIRECTION}
   *
   * @param direction can be {@literal null}, will default to {@link Sorting#DEFAULT_DIRECTION}
   * @param property must not be {@literal null} or empty.
   * @param ignoreCase true if sorting should be case insensitive. false if sorting should be case
   *     sensitive.
   * @param nullHandling can be {@literal null}, will default to {@link NullHandling#NATIVE}.
   */
  private Order(
      Direction direction, String property, boolean ignoreCase, NullHandling nullHandling) {
    init();
    if (direction != null) {
      this.direction = direction;
    }
    this.ignoreCase = ignoreCase;
    if (nullHandling != null) {
      this.nullHandling = nullHandling;
    }
    this.property = property;

    if (property == null || property.isEmpty() || property.trim().isEmpty()) {
      throw new IllegalArgumentException("Property must not null or empty!");
    }
    this.property = property;
  }

  @Override
  public boolean equals(Object obj) {

    if (this == obj) {
      return true;
    }

    if (!(obj instanceof Order)) {
      return false;
    }

    Order that = (Order) obj;

    return this.direction.equals(that.getDirection())
        && this.property.equals(that.getProperty())
        && this.ignoreCase == that.isIgnoreCase()
        && this.nullHandling.equals(that.getNullHandling());
  }

  /**
   * Returns the order the property shall be sorted for.
   *
   * @return the order the property shall be sorted for
   */
  public Direction getDirection() {
    return direction;
  }

  /**
   * Returns the used {@link NullHandling} hint, which can but may not be respected by the used
   * datastore.
   *
   * @return the used NullHandling hint, which can but may not be respected by the used datastore.
   */
  public NullHandling getNullHandling() {
    return nullHandling;
  }

  /**
   * Returns the property to order for.
   *
   * @return the property to order for
   */
  public String getProperty() {
    return property;
  }

  /**
   * Returns the optional sub property to order for.
   *
   * <p>A sub property is always related to the main property.
   *
   * @return the optional sub property to order for
   */
  public Optional<String> getSubProperty() {
    return Optional.ofNullable(subProperty);
  }

  @Override
  public int hashCode() {

    int result = 17;

    result = 31 * result + direction.hashCode();
    result = 31 * result + property.hashCode();
    result = 31 * result + (ignoreCase ? 1 : 0);
    result = 31 * result + nullHandling.hashCode();

    return result;
  }

  protected void init() {
    if (direction == null) {
      this.direction = DEFAULT_DIRECTION;
    }
    if (ignoreCase == null) {
      this.ignoreCase = DEFAULT_IGNORE_CASE;
    }
    if (nullHandling == null) {
      this.nullHandling = DEFAULT_NULL_HANDLING;
    }
  }

  /**
   * Returns whether sorting for this property shall be ascending.
   *
   * @return whether sorting for this property shall be ascending
   */
  public boolean isAscending() {
    return this.direction.isAscending();
  }

  /**
   * Returns whether sorting for this property shall be descending.
   *
   * @return whether sorting for this property shall be descending
   */
  public boolean isDescending() {
    return this.direction.isDescending();
  }

  /**
   * Returns whether or not the sort will be case sensitive.
   *
   * @return whether or not the sort will be case sensitive
   */
  public boolean isIgnoreCase() {
    return ignoreCase;
  }

  public void setDirection(Direction direction) {
    this.direction = direction;
  }

  public void setIgnoreCase(boolean ignoreCase) {
    this.ignoreCase = ignoreCase;
  }

  public void setNullHandling(NullHandling nullHandling) {
    this.nullHandling = nullHandling;
  }

  public void setProperty(String property) {
    this.property = property;
  }

  public void setSubProperty(String property) {
    this.subProperty = property;
  }

  @Override
  public String toString() {

    String result = String.format("%s: %s", property, direction);

    if (!NullHandling.NATIVE.equals(nullHandling)) {
      result += ", " + nullHandling;
    }

    if (ignoreCase) {
      result += ", ignoring case";
    }

    return result;
  }

  public abstract static class OrderBuilder<C extends Order, B extends OrderBuilder<C, B>> {

    public C build() {
      C c = prebuild();
      c.init();
      return c;
    }
  }
}
