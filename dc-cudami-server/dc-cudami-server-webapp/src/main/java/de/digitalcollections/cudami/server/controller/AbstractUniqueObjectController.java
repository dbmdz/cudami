package de.digitalcollections.cudami.server.controller;

import de.digitalcollections.cudami.server.business.api.service.UniqueObjectService;
import de.digitalcollections.cudami.server.controller.converter.StringToFilterCriteriaGenericConverter;
import de.digitalcollections.model.UniqueObject;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.FilterOperation;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import java.lang.reflect.Field;
import java.util.List;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;

public abstract class AbstractUniqueObjectController<U extends UniqueObject> {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(AbstractUniqueObjectController.class);

  @Autowired private ConversionService conversionService;

  protected abstract UniqueObjectService<U> getService();

  protected PageRequest createPageRequest(
      Class targetClass,
      int pageNumber,
      int pageSize,
      List<Order> sortBy,
      List<FilterCriterion> filterCriteria) {
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize);

    // add filtering
    if (filterCriteria != null) {
      Filtering filtering = null;
      // TODO: add datalanguage to be able to validate that multilanguage fields in
      // filtercriteria
      // have
      // already "_language" as postfix assigned...

      // convert filter criterion String value(s) to type of expression field (as
      // generics
      // type got lost over http (and list contains eventually different
      // FilterCriterion types)...)
      for (FilterCriterion fc : filterCriteria) {
        if (filtering == null) {
          filtering = new Filtering();
        }
        String expression = fc.getExpression();
        boolean isNativeExpression = fc.isNativeExpression();
        if (isNativeExpression) {
          filtering.add(fc);
          continue;
        }
        FilterOperation filterOperation = fc.getOperation();
        String operationValue = (String) fc.getValue();
        try {
          String basicExpression = expression;
          if (expression.contains("_")) {
            basicExpression = expression.split("_")[0];
          }
          Class<?> fieldClass = getFieldType(targetClass, basicExpression);

          if (Comparable.class.isAssignableFrom(fieldClass)) {
            FilterCriterion convertedFc =
                StringToFilterCriteriaGenericConverter.createFilterCriterion(
                    fieldClass,
                    expression,
                    isNativeExpression,
                    filterOperation,
                    operationValue,
                    conversionService);
            filtering.add(convertedFc);
          } else {
            filtering.add(fc);
          }
        } catch (NoSuchFieldException | SecurityException e) {
          LOGGER.warn("Field " + expression + " not found in class " + targetClass.getSimpleName());
        }
      }
      pageRequest.setFiltering(filtering);
    }

    // add sorting
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    return pageRequest;
  }

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
}
