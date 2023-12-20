package de.digitalcollections.model.list.filtering;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * List of {@link FilterCriterion} extended by a {@code criterionLink} describing how the criteria
 * are logically linked.
 */
public class FilterCriteria extends ArrayList<FilterCriterion> {

  private FilterLogicalOperator criterionLink = FilterLogicalOperator.AND;

  public FilterCriteria() {
    super(1);
  }

  public FilterCriteria(FilterLogicalOperator criterionLink) {
    this();
    this.criterionLink = criterionLink;
  }

  /**
   * Creates an AND-linked list of criteria.
   *
   * @param criterions
   */
  public FilterCriteria(FilterCriterion... criterions) {
    this();
    Stream.of(criterions).forEachOrdered(this::add);
  }

  /**
   * Creates an AND-linked list of criteria.
   *
   * @param criterions
   */
  public FilterCriteria(List<FilterCriterion> criterions) {
    this();
    addAll(criterions);
  }

  public FilterCriteria(FilterLogicalOperator criterionLink, FilterCriterion... criterions) {
    this(criterions);
    this.criterionLink = criterionLink;
  }

  public FilterCriteria(FilterLogicalOperator criterionLink, List<FilterCriterion> criterions) {
    this(criterions);
    this.criterionLink = criterionLink;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || !(o instanceof FilterCriteria)) return false;
    FilterCriteria other = (FilterCriteria) o;
    return super.equals(o) && this.criterionLink == other.criterionLink;
  }

  @Override
  public int hashCode() {
    return super.hashCode() + this.criterionLink.hashCode() + "FilterCriteria".hashCode();
  }

  @Override
  public String toString() {
    return "FilterCriteria{criterionLink=" + criterionLink + ", content=" + super.toString() + "}";
  }

  public FilterLogicalOperator getCriterionLink() {
    return criterionLink;
  }

  public void setCriterionLink(FilterLogicalOperator criterionLink) {
    this.criterionLink = criterionLink;
  }

  public boolean hasFilterCriterionFor(String property) {
    return stream().anyMatch(fc -> Objects.equals(fc.getExpression(), property));
  }

  @SuppressWarnings("unchecked")
  public <T> FilterCriterion<T> getFilterCriterionFor(String property) {
    return stream()
        .filter(fc -> Objects.equals(fc.getExpression(), property))
        .findFirst()
        .orElse(null);
  }
}
