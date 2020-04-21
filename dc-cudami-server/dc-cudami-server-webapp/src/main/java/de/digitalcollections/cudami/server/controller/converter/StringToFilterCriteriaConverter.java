package de.digitalcollections.cudami.server.controller.converter;

import de.digitalcollections.model.api.filter.FilterCriteria;
import de.digitalcollections.model.api.filter.enums.FilterOperation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@Component
public class StringToFilterCriteriaConverter<T extends Comparable<T>>
    implements Converter<String, FilterCriteria<T>> {

  @Autowired private ConversionService conversionService;

  @Override
  public FilterCriteria<T> convert(String operationAndValues) {
    Assert.isTrue(!StringUtils.isEmpty(operationAndValues), "Filter criteria can't be empty");

    String[] filterSplit = StringUtils.split(operationAndValues, ":");
    if (filterSplit == null || filterSplit.length != 2) {
      throw new IllegalArgumentException("More than one or no separator ':' found");
    }
    String operationAcronym = filterSplit[0];
    String operationValue = filterSplit[1];
    // Convert the operation name to enum
    FilterOperation filterOperation = FilterOperation.fromValue(operationAcronym);

    String[] operationValues;
    if (!operationValue.contains(",")) {
      operationValues = new String[] {operationValue};
    } else {
      // Split the filter value as comma separated.
      operationValues = StringUtils.split(operationValue, ",");
    }
    if (operationValues == null || operationValues.length < 1) {
      throw new IllegalArgumentException("Operation value can't be empty");
    }
    Collection<String> originalValues = Arrays.asList(operationValues);

    T convertedSingleValue = null;
    T minValue = null;
    T maxValue = null;
    Collection<T> convertedValues = new ArrayList<>();
    
    // NOT WORKING 1
    // FIXME: feedback for alternative solution is welcome!:
    //    Type[] actualTypeArguments =
    //        ((ParameterizedType) convertedValues.getClass().getGenericSuperclass())
    //            .getActualTypeArguments();
    //    Type clazz = actualTypeArguments[0];
    //    Class<T> targetClass = (Class<T>) clazz.getClass();

    // NOT WORKING 2
    FilterCriteria<T> fct = new FilterCriteria<>();
    Type type = fct.getType();
    Class<T> targetClass = (Class<T>) type.getClass();
    
    if (null == filterOperation) {
      // All other operation
      convertedSingleValue = conversionService.convert(operationValues[0], targetClass);
    } else {
      switch (filterOperation) {
        case BETWEEN:
          // For operation 'btn'
          if (operationValues.length != 2) {
            throw new IllegalArgumentException("For 'btn' operation two values are expected");
          } else {

            // Convert
            T value1 = conversionService.convert(operationValues[0], targetClass);
            T value2 = conversionService.convert(operationValues[1], targetClass);

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
          }
          break;
        case IN:
        case NOT_IN:
          // For 'in' or 'nin' operation
          convertedValues.addAll(
              originalValues.stream()
                  .map(s -> conversionService.convert(s, targetClass))
                  .collect(Collectors.toList()));
          break;
        default:
          // All other operation
          convertedSingleValue = conversionService.convert(operationValues[0], targetClass);
          break;
      }
    }
    FilterCriteria<T> fc =
        new FilterCriteria<>(
            null, filterOperation, convertedSingleValue, minValue, maxValue, convertedValues);
    return fc;
  }
}
