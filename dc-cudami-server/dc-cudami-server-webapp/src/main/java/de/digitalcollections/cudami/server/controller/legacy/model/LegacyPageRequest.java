package de.digitalcollections.cudami.server.controller.legacy.model;

import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.FilterLogicalOperator;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.sorting.Sorting;

/** {@link PageRequest} that sets filters for label and description if {@code searchTerm} is set. */
public final class LegacyPageRequest extends PageRequest {

  public LegacyPageRequest(String searchTerm, int pageNumber, int pageSize) {
    super(searchTerm, pageNumber, pageSize);
  }

  public LegacyPageRequest(String searchTerm, int pageNumber, int pageSize, Sorting sorting) {
    super(searchTerm, pageNumber, pageSize, sorting);
  }

  public LegacyPageRequest(
      int pageNumber, int pageSize, Sorting sorting, Filtering filtering, String searchTerm) {
    super(pageNumber, pageSize, sorting, filtering, searchTerm);
  }

  @Override
  protected void init() {
    super.init();
    if (searchTerm != null) {
      // add filtering on label and description
      add(
          Filtering.builder()
              .filterCriterion(
                  FilterLogicalOperator.OR,
                  FilterCriterion.builder().withExpression("label").isEquals(searchTerm).build())
              .filterCriterion(
                  FilterLogicalOperator.OR,
                  FilterCriterion.builder()
                      .withExpression("description")
                      .isEquals(searchTerm)
                      .build())
              .build());
    }
  }
}
