package de.digitalcollections.cudami.server.controller.converter;

import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.FilterLogicalOperator;
import de.digitalcollections.model.list.filtering.FilterOperation;
import de.digitalcollections.model.list.filtering.Filtering;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
public class StringToFilterCriteriaGenericConverter implements GenericConverter {

  @Autowired private ConversionService conversionService;

  @Override
  public Set<ConvertiblePair> getConvertibleTypes() {
    Set<ConvertiblePair> convertibleTypes = new HashSet<>();
    convertibleTypes.add(new ConvertiblePair(String.class, FilterCriterion.class));
    convertibleTypes.add(new ConvertiblePair(String.class, Filtering.class));
    return convertibleTypes;
  }

  @Override
  public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
    if (source == null) {
      return null;
    }
    if (!(source instanceof String sourceString))
      throw new IllegalArgumentException("`source` parameter must be of type `String`.");
    sourceString = URLDecoder.decode(sourceString, StandardCharsets.UTF_8);
    if (!StringUtils.hasText(sourceString)) {
      return null;
    } else if (!sourceString.contains(":")) {
      throw new IllegalArgumentException("No separator ':' found");
    }

    if (targetType.getType().equals(FilterCriterion.class)) {
      return buildFilterCriterion(sourceString);

    } else if (targetType.getType().equals(Filtering.class)) {
      return buildFiltering(sourceString);

    } else {
      throw new UnsupportedOperationException();
    }
  }

  private Filtering buildFiltering(String filter) {
    if (filter.matches("[^{].+[^}]")) {
      /* No braces around, that meets both legacy style, e.g. `label:like:blubb`,
       * and Filtering style - handy version (no braces if it is only one `FilterCriteria`),
       * e.g. `lastname:eq:meier;age:gt:30`.
       * We add the braces and let the following procedure do its job.
       */
      filter = "{%s}".formatted(filter);
    }

    /* Filtering style, e.g.
     * `{$OR;label:like:blubb;description:like:blubb};{active:eq:true}`
     * or `{lastname:eq:meier;age:gt:30}`
     */
    Matcher filterCriteriaStrings = Pattern.compile("[{](.+?)[}]").matcher(filter);
    Filtering result = new Filtering();
    while (filterCriteriaStrings.find()) {
      List<String> criterionStrings =
          Arrays.stream(filterCriteriaStrings.group(1).split(";", 0))
              .collect(Collectors.toCollection(ArrayList::new));
      // obtain the logical operator (and/or)
      AtomicReference<FilterLogicalOperator> link =
          new AtomicReference<>(FilterLogicalOperator.AND);
      criterionStrings.parallelStream()
          .filter(s -> s.matches("(?iu)\\s*\\$[[:alpha:]]\\s*"))
          .findFirst()
          .ifPresent(
              s -> {
                link.set(
                    Optional.ofNullable(FilterLogicalOperator.fromValue(s.strip().substring(1)))
                        .orElse(FilterLogicalOperator.AND));
                criterionStrings.remove(s);
              });
      // build `FilterCriterion`s and add them to `Filtering`
      List<FilterCriterion> criterions =
          criterionStrings.stream().map(this::buildFilterCriterion).toList();
      result.add(link.get(), criterions);
    }
    return result;
  }

  private FilterCriterion buildFilterCriterion(String filterSource) {
    // to support filtercriterion without leading property name (backwards
    // compatibility)
    // we do have to detect if it is old (without expression/property (only one ":")
    // or (new (with expression, two ":") style:
    // TODO: remove old style, when no longer support is needed
    boolean newStyle = false;
    String[] filterParts = filterSource.split(":");
    if (filterParts.length > 2 && FilterOperation.fromValue(filterParts[1]) != null) {
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
    boolean isNativeExpression = false;
    String operationAcronym = null;
    String operationValue = "";
    if (newStyle) {
      expression = filterParts[0];
      if (expression.startsWith("[") && expression.endsWith("]")) {
        // native expression marked by surrounding brackets
        isNativeExpression = true;
        expression = expression.substring(1, expression.length() - 1);
      }
      operationAcronym = filterParts[1];
      for (int i = 2; i < filterParts.length; i++) {
        if (i > 2) {
          // add separator again (was part of value and no filter separator)
          operationValue += ":";
        }
        operationValue += filterParts[i];
      }
    } else {
      // old style without expression part
      int separatorPosition =
          filterSource.indexOf(
              ':'); // index of the first occurrence of ":" (operation value may contain
      // ":", too...
      operationAcronym = filterSource.substring(0, separatorPosition);
      operationValue = filterSource.substring(separatorPosition + 1);
    }

    // Convert the operation acronym to enum
    if (operationAcronym == null) {
      throw new IllegalArgumentException("No operation acronym found");
    }
    FilterOperation filterOperation = FilterOperation.fromValue(operationAcronym);

    return createFilterCriterion(
        String.class,
        expression,
        isNativeExpression,
        filterOperation,
        operationValue,
        conversionService);
  }

  public static <T> FilterCriterion<T> createFilterCriterion(
      Class<T> targetClass,
      String expression,
      boolean isNativeExpression,
      FilterOperation filterOperation,
      String operationValue,
      ConversionService conversionService)
      throws IllegalArgumentException {
    // no value operand (e.g. "set")
    if (filterOperation.getOperandCount() == FilterOperation.OperandCount.NO_VALUE) {
      FilterCriterion fc =
          new FilterCriterion(
              expression, isNativeExpression, filterOperation, null, null, null, null);
      return fc;
    }

    // single value operand (e.g. "eq")
    if (filterOperation.getOperandCount() == FilterOperation.OperandCount.SINGLEVALUE) {
      if (operationValue == null) {
        throw new IllegalArgumentException("No operation value found");
      }
      T value = conversionService.convert(operationValue, targetClass);
      FilterCriterion<T> fc =
          new FilterCriterion<>(expression, isNativeExpression, filterOperation, value);
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
      List<T> convertedValues = new ArrayList<>();
      convertedValues.addAll(
          Arrays.stream(operationValues)
              .map(s -> conversionService.convert(s, targetClass))
              .collect(Collectors.toList()));
      FilterCriterion<T> fc =
          new FilterCriterion<>(
              expression, isNativeExpression, filterOperation, null, null, null, convertedValues);
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
      Comparable<T> minValue = null;
      Comparable<T> maxValue = null;

      // Convert
      Comparable<T> value1 =
          (Comparable) conversionService.convert(operationValues[0], targetClass);
      Comparable<T> value2 =
          (Comparable) conversionService.convert(operationValues[1], targetClass);

      if (value1 != null && value2 != null) {
        // Set min and max values
        if (value1.compareTo((T) value2) > 0) {
          minValue = value2;
          maxValue = value1;
        } else {
          minValue = value1;
          maxValue = value2;
        }
      }
      FilterCriterion<T> fc =
          new FilterCriterion<>(
              expression, isNativeExpression, filterOperation, null, minValue, maxValue, null);
      return fc;
    }
    return null;
  }
}
