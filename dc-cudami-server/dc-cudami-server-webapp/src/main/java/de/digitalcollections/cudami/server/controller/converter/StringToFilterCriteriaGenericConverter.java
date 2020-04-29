package de.digitalcollections.cudami.server.controller.converter;

import de.digitalcollections.model.api.filter.FilterCriterion;
import de.digitalcollections.model.api.filter.enums.FilterOperation;
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
 * <p>The following table describes available filter operation params and how to define fieldName,
 * operation and operation values in URL:
 *
 * <table border="1">
 * <caption>Filter Criterion Structure</caption>
 * <tr>
 * <td>Symbol</td>
 * <td>Operation</td>
 * <td>Example filter query param</td>
 * <tr>
 * <td>eq</td>
 * <td>Equals</td>
 * <td>city=eq:Sydney</td>
 * <tr>
 * <td>neq</td>
 * <td>Not Equals</td>
 * <td>country=neq:uk</td>
 * <tr>
 * <td>gt</td>
 * <td>Greater Than</td>
 * <td>amount=gt:10000</td>
 * <tr>
 * <td>gte</td>
 * <td>Greater Than or equals to</td>
 * <td>amount=gte:10000</td>
 * <tr>
 * <td>lt</td>
 * <td>Less Than</td>
 * <td>amount=lt:10000</td>
 * <tr>
 * <td>lte</td>
 * <td>Less Than or equals to</td>
 * <td>amount=lte:10000</td>
 * <tr>
 * <td>in</td>
 * <td>IN</td>
 * <td>country=in:uk, usa, au</td>
 * <tr>
 * <td>nin</td>
 * <td>Not IN</td>
 * <td>country=nin:fr, de, nz</td>
 * <tr>
 * <td>btn</td>
 * <td>Between</td>
 * <td>joiningDate=btn:2018-01-01, 2016-01-01</td>
 * <tr>
 * <td>like</td>
 * <td>Like</td>
 * <td>firstName=like:John</td>
 * </tr>
 * </table>
 *
 * @see FilterCriterion
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
    String operationAndValues = (String) source;

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

    Object convertedSingleValue = null;
    C minValue = null;
    C maxValue = null;
    Collection convertedValues = new ArrayList<>();

    Class targetClass = (Class<?>) targetType.getResolvableType().getGeneric(0).getType();
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
    FilterCriterion fc =
        new FilterCriterion(
            null, filterOperation, convertedSingleValue, minValue, maxValue, convertedValues);
    return fc;
  }
}
