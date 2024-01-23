package de.digitalcollections.cudami.server.controller;

import de.digitalcollections.cudami.server.controller.converter.StringToFilterCriteriaGenericConverter;
import de.digitalcollections.model.list.filtering.FilterCriteria;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.FilterLogicalOperator;
import de.digitalcollections.model.list.filtering.FilterOperation;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;

public abstract class AbstractPagingAndSortingController {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(AbstractPagingAndSortingController.class);

  /**
   * Get Class of a field of a given class.
   *
   * @param clz class to search in
   * @param fieldName name of field
   * @return Class/Type of field (if found)
   * @throws NoSuchFieldException thrown if not found
   */
  public static Class getFieldType(Class clz, String fieldName) throws NoSuchFieldException {
    Field field = FieldUtils.getField(clz, fieldName, true);
    if (field == null) {
      throw new NoSuchFieldException();
    }
    return field.getType();
  }

  @Autowired private ConversionService conversionService;

  /**
   * Convert filter criterion string value(s) to type of expression field.
   *
   * <p>Because the generic type got lost over HTTP and the list may contain different
   * FilterCriterion types.
   */
  private FilterCriterion makeTypedFilterCriterion(FilterCriterion fc, Class<?> targetClass) {
    if (fc.isNativeExpression()) return fc;
    String expression = fc.getExpression();
    FilterOperation filterOperation = fc.getOperation();
    String operationValue =
        switch (filterOperation.getOperandCount()) {
          case SINGLEVALUE ->
              fc.getValue() instanceof String svalue ? svalue : fc.getValue().toString();
          case MULTIVALUE -> String.join(",", fc.getValues());
          case MIN_MAX_VALUES ->
              String.join(
                  ",",
                  fc.getMinValue() instanceof String smin ? smin : fc.getMinValue().toString(),
                  fc.getMaxValue() instanceof String smax ? smax : fc.getMaxValue().toString());
          case NO_VALUE -> null;
        };
    try {
      String basicExpression = expression;
      Class<?> fieldClass;
      if (expression.contains(".")) {
        // e.g. parent.uuid
        String[] expressions = expression.split("\\.");
        String firstExpression = expressions[0];
        String secondExpression = expressions[1];
        Class<?> firstClass = getFieldType(targetClass, firstExpression);
        try {
          fieldClass = getFieldType(firstClass, secondExpression);
        } catch (NoSuchFieldException e) {
          // happens for e.g. `label.und-Latn` or `name.de-Latn`
          LOGGER.debug(
              "Field {} in property {} (class {}) not found. Taking the latter one instead.",
              secondExpression,
              firstExpression,
              targetClass.getSimpleName());
          fieldClass = firstClass;
        }
      } else if (expression.contains("_")) {
        basicExpression = expression.split("_")[0];
        fieldClass = getFieldType(targetClass, basicExpression);
      } else {
        fieldClass = getFieldType(targetClass, basicExpression);
      }

      if (Comparable.class.isAssignableFrom(fieldClass)) {
        return StringToFilterCriteriaGenericConverter.createFilterCriterion(
            fieldClass, expression, false, filterOperation, operationValue, conversionService);
      }
      return fc;
    } catch (NoSuchFieldException | SecurityException e) {
      LOGGER.warn("Field " + expression + " not found in class " + targetClass.getSimpleName());
    }
    return null;
  }

  protected Filtering mergeFilters(
      Class<?> targetClass, Filtering filtering, List<FilterCriterion> filterCriterions) {
    Filtering resultingFiltering = null;
    // process `filtering` first
    if (filtering != null && !filtering.isEmpty()) {
      for (FilterCriteria fca : filtering.getFilterCriteriaList()) {
        // looping over a cloned list to change the original one w/o side effects
        ((ArrayList<FilterCriterion>) fca.clone())
            .forEach(
                fc -> {
                  fca.remove(fc);
                  FilterCriterion typedFc = makeTypedFilterCriterion(fc, targetClass);
                  if (typedFc != null) fca.add(typedFc);
                });
      }
      resultingFiltering = filtering;
    }

    // add `FilterCriterion`s (legacy version)
    if (filterCriterions != null && !filterCriterions.isEmpty()) {
      if (resultingFiltering == null) resultingFiltering = new Filtering();

      // TODO: add datalanguage to be able to validate that multilanguage fields in
      // filtercriteria have already "_language" (it is ".lang", right?) as suffix
      // assigned...
      List<FilterCriterion> typedCriterions =
          filterCriterions.parallelStream()
              .map(fc -> makeTypedFilterCriterion(fc, targetClass))
              .filter(fc -> fc != null)
              .toList();
      resultingFiltering.add(FilterLogicalOperator.AND, typedCriterions);
    }
    return resultingFiltering;
  }

  protected PageRequest createPageRequest(
      Class<?> targetClass,
      int pageNumber,
      int pageSize,
      List<Order> sortBy,
      List<FilterCriterion> filterCriterions,
      Filtering filtering) {
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize);
    pageRequest.setFiltering(mergeFilters(targetClass, filtering, filterCriterions));
    // add sorting
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    return pageRequest;
  }
}
