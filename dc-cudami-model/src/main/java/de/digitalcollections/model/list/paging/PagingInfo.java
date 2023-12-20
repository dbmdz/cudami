package de.digitalcollections.model.list.paging;

import java.util.ArrayList;
import java.util.List;

/**
 * @deprecated use {@link PageResponse#getNavItems} instead.
 */
@Deprecated(forRemoval = true, since = "11.1.0")
public class PagingInfo {

  private final int currentNumber;
  private final List<PageItem> items;
  private final int maxPageItemsToDisplay;
  private final int pageSize;
  private final long totalItems;
  private final int totalPages;
  private final String url;

  /**
   * PagingInfo
   *
   * @param totalItems total number of items to be paged
   * @param currentPage number of the current page, starting with 0
   * @param totalPages maximum page number
   * @param maxPageItemsToDisplay maximum number of page navigation items to be displayed
   * @param pageSize page size
   * @param url base url
   */
  public PagingInfo(
      long totalItems,
      int currentPage,
      int totalPages,
      int maxPageItemsToDisplay,
      int pageSize,
      String url) {
    this.totalItems = totalItems;
    this.totalPages = totalPages;
    this.maxPageItemsToDisplay = maxPageItemsToDisplay;
    this.pageSize = pageSize;
    this.url = url;
    items = new ArrayList<>(0);

    currentNumber = currentPage + 1; // start from 1 to match page.page

    int start;
    int size;
    if (totalPages <= maxPageItemsToDisplay) {
      start = 1;
      size = totalPages;
    } else if (currentNumber <= maxPageItemsToDisplay - maxPageItemsToDisplay / 2) {
      start = 1;
      size = maxPageItemsToDisplay;
    } else if (currentNumber >= totalPages - maxPageItemsToDisplay / 2) {
      start = totalPages - maxPageItemsToDisplay + 1;
      size = maxPageItemsToDisplay;
    } else {
      start = currentNumber - maxPageItemsToDisplay / 2;
      size = maxPageItemsToDisplay;
    }

    for (int i = 0; i < size; i++) {
      items.add(new PageItem(start + i, (start + i) == currentNumber));
    }
  }

  public List<PageItem> getItems() {
    return items;
  }

  public int getNumber() {
    return currentNumber;
  }

  public int getSize() {
    return pageSize;
  }

  public long getTotalItems() {
    return totalItems;
  }

  public int getTotalPages() {
    return totalPages;
  }

  public String getUrl() {
    return url;
  }

  public boolean hasNextPage() {
    return currentNumber < totalPages;
  }

  public boolean hasPreviousPage() {
    return currentNumber > 1;
  }

  public boolean isFirstPage() {
    return currentNumber == 1;
  }

  public boolean isLastPage() {
    return currentNumber == totalPages;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName()
        + "{maxPageItemsToDisplay="
        + maxPageItemsToDisplay
        + ", items="
        + items
        + ", currentNumber="
        + currentNumber
        + ", totalItems="
        + totalItems
        + ", totalPages="
        + totalPages
        + ", pageSize="
        + pageSize
        + ", url='"
        + url
        + "'}";
  }
}
