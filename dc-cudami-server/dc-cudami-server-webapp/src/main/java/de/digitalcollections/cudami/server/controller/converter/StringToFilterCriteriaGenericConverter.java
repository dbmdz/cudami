package de.digitalcollections.cudami.server.controller.converter;

import de.digitalcollections.model.filter.FilterCriterion;
import de.digitalcollections.model.filter.FilterOperation;
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
    Class targetClass = (Class<?>) targetType.getResolvableType().getGeneric(0).getType();
    return convert(source, targetClass);
  }

  protected Object convert(Object source, Class targetClass) throws IllegalArgumentException {
    if (source == null) {
      return null;
    }
    String operationAndValues = (String) source;

    if (!operationAndValues.contains(":")) {
      throw new IllegalArgumentException("No separator ':' found");
    }

    int separatorPosition =
        operationAndValues.indexOf(
            ':'); // index of the first occurrence of ":" (operation value may contain ":", too...
    String operationAcronym = operationAndValues.substring(0, separatorPosition);
    if (operationAcronym == null) {
      throw new IllegalArgumentException("No operation acronym found");
    }

    String operationValue = operationAndValues.substring(separatorPosition + 1);

    // Convert the operation acronym to enum
    FilterOperation filterOperation = FilterOperation.fromValue(operationAcronym);

    // no value operand (e.g. "set")
    if (filterOperation.getOperandCount() == FilterOperation.OperandCount.NO_VALUE) {
      FilterCriterion fc = new FilterCriterion(null, filterOperation, null, null, null, null);
      return fc;
    }

    // single value operand (e.g. "eq")
    if (filterOperation.getOperandCount() == FilterOperation.OperandCount.SINGLEVALUE) {
      if (operationValue == null) {
        throw new IllegalArgumentException("No operation value found");
      }
      Object value = conversionService.convert(operationValue, targetClass);
      FilterCriterion fc = new FilterCriterion(null, filterOperation, value);
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
          new FilterCriterion(null, filterOperation, null, null, null, convertedValues);
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
      C minValue = null;
      C maxValue = null;

      // Convert
      C value1 = (C) conversionService.convert(operationValues[0], targetClass);
      C value2 = (C) conversionService.convert(operationValues[1], targetClass);

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
          new FilterCriterion(null, filterOperation, null, minValue, maxValue, null);
      return fc;
    }

    throw new IllegalArgumentException("Unknown operation '" + operationAcronym + "'");
  }
}
