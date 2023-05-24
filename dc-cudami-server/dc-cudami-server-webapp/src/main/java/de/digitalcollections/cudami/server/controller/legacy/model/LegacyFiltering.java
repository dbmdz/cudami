package de.digitalcollections.cudami.server.controller.legacy.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonInclude(value = Include.NON_NULL)
public class LegacyFiltering {

  private List<FilterCriterion> filterCriteria;

  public LegacyFiltering() {
    init();
  }

  public LegacyFiltering(List<FilterCriterion> filterCriteria) {
    this();
    this.filterCriteria = filterCriteria;
  }

  public LegacyFiltering(Filtering filtering) {
    this();
    init(filtering);
  }

  /**
   * Add all filter criteria of given filtering to existing filtering. Initialise if no existing
   * filtering.
   *
   * @param filtering new filtering to add
   * @return complete filtering
   */
  public List<FilterCriterion> add(LegacyFiltering filtering) {
    if (filtering == null || filtering.getFilterCriteria() == null) {
      return getFilterCriteria();
    }
    if (getFilterCriteria() == null) {
      setFilterCriteria(filtering.getFilterCriteria());
    } else {
      filterCriteria.addAll(filtering.getFilterCriteria());
    }
    return getFilterCriteria();
  }

  public void add(FilterCriterion filterCriterion) {
    if (filterCriteria == null) {
      filterCriteria = new ArrayList<>();
    }
    filterCriteria.add(filterCriterion);
  }

  public void add(String expression, FilterCriterion filterCriterion) {
    if (filterCriterion == null || expression == null) {
      return;
    }

    FilterCriterion filterCriterionWithExpression = new FilterCriterion(filterCriterion);
    filterCriterionWithExpression.setExpression(expression);

    filterCriteria.add(filterCriterionWithExpression);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof LegacyFiltering)) {
      return false;
    }
    LegacyFiltering filtering = (LegacyFiltering) o;
    return Objects.equals(filterCriteria, filtering.filterCriteria);
  }

  /**
   * @return returns all filter criterias
   */
  public List<FilterCriterion> getFilterCriteria() {
    return filterCriteria;
  }

  /**
   * Returns the filter criteria registered for the given property.
   *
   * @param property given property
   * @return the filter criteria registered for the given property
   */
  public FilterCriterion getFilterCriterionFor(String property) {
    return filterCriteria.stream()
        .filter(f -> f.getExpression().equals(property))
        .findFirst()
        .orElse(null);
  }

  @Override
  public int hashCode() {
    return Objects.hash(filterCriteria) + Objects.hash("Filtering");
  }

  protected void init() {
    if (filterCriteria == null) this.filterCriteria = new ArrayList<>(0);
  }

  private void init(Filtering filtering) {
    if (filtering == null || filtering.isEmpty()) return;
    filtering.stream().filter(fc -> fc != null).forEach(filterCriteria::add);
  }

  /**
   * @param filterCriteria set list of filter criteria
   */
  public void setFilterCriteria(List<FilterCriterion> filterCriteria) {
    this.filterCriteria = filterCriteria;
  }

  @Override
  public String toString() {
    return "LegacyFiltering{" + "filterCriteria=" + filterCriteria + '}';
  }
}
