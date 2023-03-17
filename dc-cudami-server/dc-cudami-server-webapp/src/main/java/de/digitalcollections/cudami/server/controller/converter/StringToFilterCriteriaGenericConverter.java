package de.digitalcollections.cudami.server.controller.converter;

import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.FilterOperation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
/**
 * Converter for converting URL params for Filtering from String to FilterCriterion-instance. Used
 * in WebController. Fills model object FilterCriterion.
 *
 * <p>For available filter operations and request examples:
 *
 * @see de.digitalcollections.model.api.filter.enums.FilterOperation
 * @see de.digitalcollections.model.api.filter.FilterCriterion
 */
public class StringToFilterCriteriaGenericConverter<C extends Comparable<C>>
    implements GenericConverter {

  @Autowired private ConversionService conversionService;

  @Override
  public Set<ConvertiblePair> getConvertibleTypes() {
    Set<ConvertiblePair> convertibleTypes = new HashSet<>();
    convertibleTypes.add(new ConvertiblePair(String.class, FilterCriterion.class));
    return convertibleTypes;
  }

  @Override
  public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
    Class targetClass;
    try {
      targetClass = (Class<?>) targetType.getResolvableType().getGeneric(0).getType();
    } catch (Exception e) {
      targetClass = String.class;
    }
    return convert(source, targetClass);
  }

  protected Object convert(Object source, Class targetClass) throws IllegalArgumentException {
    if (source == null) {
      return null;
    }

    String filter = (String) source;
    if (!filter.contains(":")) {
      throw new IllegalArgumentException("No separator ':' found");
    }

    // to support filtercriterion without leading property name (backwards
    // compatibility)
    // we do have to detect if it is old (without expression/property (only one ":")
    // or (new (with expression, two ":") style:
    // TODO: remove old style, when no longer support is needed
    boolean newStyle = false;
    String[] filterParts = filter.split(":");
    if (filterParts.length > 2) {
      newStyle = true;
    } else if (filterParts.length == 2) {
      // length = 2 could also be new style of no value operations (like "set",
      // "unset"), check second part:
      FilterOperation filterOperation = FilterOperation.fromValue(filterParts[1]);
      if (filterOperation != null
          && filterOperation.getOperandCount() == FilterOperation.OperandCount.NO_VALUE) {
        newStyle = true;
      }
    }

    String expression = null;
    String operationAcronym = null;
    String operationValue = "";
    if (newStyle) {
      expression = filterParts[0];
      operationAcronym = filterParts[1];
      for (int i = 2; i < filterParts.length; i++) {
        if (i > 2) {
          // add separator again (was part of value and no filter separator)
          operationValue = operationValue + ":";
        }
        operationValue = operationValue + filterParts[i];
      }
    } else {
      // old style without expression part
      int separatorPosition =
          filter.indexOf(':'); // index of the first occurrence of ":" (operation value may contain
      // ":", too...
      operationAcronym = filter.substring(0, separatorPosition);
      operationValue = filter.substring(separatorPosition + 1);
    }

    // Convert the operation acronym to enum
    if (operationAcronym == null) {
      throw new IllegalArgumentException("No operation acronym found");
    }
    FilterOperation filterOperation = FilterOperation.fromValue(operationAcronym);

    return createFilterCriterion(
        targetClass, expression, filterOperation, operationValue, conversionService);
  }

  public static FilterCriterion createFilterCriterion(
      Class<?> targetClass,
      String expression,
      FilterOperation filterOperation,
      String operationValue,
      ConversionService conversionService)
      throws IllegalArgumentException {
    // no value operand (e.g. "set")
    if (filterOperation.getOperandCount() == FilterOperation.OperandCount.NO_VALUE) {
      FilterCriterion fc = new FilterCriterion(expression, filterOperation, null, null, null, null);
      return fc;
    }

    // single value operand (e.g. "eq")
    if (filterOperation.getOperandCount() == FilterOperation.OperandCount.SINGLEVALUE) {
      if (operationValue == null) {
        throw new IllegalArgumentException("No operation value found");
      }
      Object value = conversionService.convert(operationValue, targetClass);
      FilterCriterion fc = new FilterCriterion(expression, filterOperation, value);
      return fc;
    }

    // multi value operand (e.g. "in")
    if (filterOperation.getOperandCount() == FilterOperation.OperandCount.MULTIVALUE) {
      if (operationValue == null) {
        throw new IllegalArgumentException("No operation values found");
      }
      String[] operationValues = StringUtils.tokenizeToStringArray(operationValue, ",");
      if (operationValues == null || operationValues.length < 1) {
        throw new IllegalArgumentException("Operation values can't be empty");
      }
      Collection<String> originalValues = Arrays.asList(operationValues);
      Collection convertedValues = new ArrayList<>();
      convertedValues.addAll(
          originalValues.stream()
              .map(s -> conversionService.convert(s, targetClass))
              .collect(Collectors.toList()));
      FilterCriterion fc =
          new FilterCriterion(expression, filterOperation, null, null, null, convertedValues);
      return fc;
    }

    // min max value operand (e.g. "between")
    if (filterOperation.getOperandCount() == FilterOperation.OperandCount.MIN_MAX_VALUES) {
      if (operationValue == null) {
        throw new IllegalArgumentException("No operation values found");
      }
      String[] operationValues = StringUtils.tokenizeToStringArray(operationValue, ",");
      if (operationValues == null || operationValues.length < 1) {
        throw new IllegalArgumentException("Operation values can't be empty");
      }
      if (operationValues.length != 2) {
        throw new IllegalArgumentException("For min/max operation two values are expected");
      }
      Comparable minValue = null;
      Comparable maxValue = null;

      // Convert
      Comparable value1 = (Comparable) conversionService.convert(operationValues[0], targetClass);
      Comparable value2 = (Comparable) conversionService.convert(operationValues[1], targetClass);

      if (value1 != null && value2 != null) {
        // Set min and max values
        if (value1.compareTo(value2) > 0) {
          minValue = value2;
          maxValue = value1;
        } else {
          minValue = value1;
          maxValue = value2;
        }
      }
      FilterCriterion fc =
          new FilterCriterion(expression, filterOperation, null, minValue, maxValue, null);
      return fc;
    }
    return null;
  }
}
