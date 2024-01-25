package de.digitalcollections.model.list.sorting;

/**
 * Enumeration for null handling hints that can be used in {@link Order} expressions. See Spring
 * Data Commons, but more flat design and independent of Spring libraries.
 */
public enum NullHandling {

  /** Lets the data store decide what to do with nulls. */
  NATIVE,
  /** A hint to the used data store to order entries with null values before non null entries. */
  NULLS_FIRST,
  /** A hint to the used data store to order entries with null values after non null entries. */
  NULLS_LAST;
}
