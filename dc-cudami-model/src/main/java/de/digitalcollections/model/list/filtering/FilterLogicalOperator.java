package de.digitalcollections.model.list.filtering;

import java.util.stream.Stream;

public enum FilterLogicalOperator {
  AND("AND"),
  OR("OR");

  private String operand;

  public static FilterLogicalOperator fromValue(String value) {
    return Stream.of(FilterLogicalOperator.values())
        .filter(lo -> lo.getOperand().equalsIgnoreCase(value))
        .findFirst()
        .orElse(null);
  }

  FilterLogicalOperator(String operand) {
    this.operand = operand;
  }

  public String getOperand() {
    return operand;
  }
}
