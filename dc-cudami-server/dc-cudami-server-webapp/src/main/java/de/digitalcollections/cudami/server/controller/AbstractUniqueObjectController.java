package de.digitalcollections.cudami.server.controller;

import de.digitalcollections.cudami.server.business.api.service.UniqueObjectService;
import de.digitalcollections.cudami.server.controller.converter.StringToFilterCriteriaGenericConverter;
import de.digitalcollections.model.UniqueObject;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.FilterOperation;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;

public abstract class AbstractUniqueObjectController<U extends UniqueObject> {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(AbstractUniqueObjectController.class);

  @Autowired private ConversionService conversionService;

  protected abstract UniqueObjectService<U> getService();

  /**
   * The usual find implementation
   *
   * <p>For {@code filterCriteria} we use a varargs parameter instead of a {@code Map<String,
   * FilterCriterion<?>>}, because the beautiful shorthand {@code Map.of} does not support null
   * values and so it would make things unnecessary difficult inside the extending class.
   *
   * <p>Do not mess things up by passing {@code null} for {@code filterCriteria} if there are not
   * any. Since it is varargs you can just omit this parameter.
   *
   * @param pageNumber
   * @param pageSize
   * @param sortBy
   * @param searchTerm
   * @param labelTerm
   * @param labelLanguage
   * @param filterCriteria must be {@code Pair}s of a {@code String}, the expression, and the
   *     corresponding {@code FilterCriterion}
   * @return
   */
  public PageResponse<U> find(
      int pageNumber,
      int pageSize,
      List<Order> sortBy,
      String searchTerm,
      String labelTerm,
      Locale labelLanguage,
      Pair<String, FilterCriterion<?>>... filterCriteria) {
    return find(
        pageNumber,
        pageSize,
        sortBy,
        searchTerm,
        labelTerm,
        labelLanguage,
        null,
        null,
        filterCriteria);
  }

  // FIXME: remove find with all the deprecated style, use filtering, no need for getService()...
  /**
   * The usual find implementation
   *
   * <p>For {@code filterCriteria} we use a varargs parameter instead of a {@code Map<String,
   * FilterCriterion<?>>}, because the beautiful shorthand {@code Map.of} does not support null
   * values and so it would make things unnecessary difficult inside the extending class.
   *
   * <p>Do not mess things up by passing {@code null} for {@code filterCriteria} if there are not
   * any. Since it is varargs you can just omit this parameter.
   *
   * @param pageNumber
   * @param pageSize
   * @param sortBy
   * @param searchTerm
   * @param labelTerm
   * @param labelLanguage
   * @param nameTerm
   * @param nameLanguage
   * @param filterCriteria must be {@code Pair}s of a {@code String}, the expression, and the
   *     corresponding {@code FilterCriterion}
   * @return
   */
  public PageResponse<U> find(
      int pageNumber,
      int pageSize,
      List<Order> sortBy,
      String searchTerm,
      String labelTerm,
      Locale labelLanguage,
      String nameTerm,
      Locale nameLanguage,
      Pair<String, FilterCriterion<?>>... filterCriteria) {
    PageRequest pageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }

    // Since Map.of doesn't support null values we try it this varargs-way.
    for (Pair<String, FilterCriterion<?>> criterionPair : filterCriteria) {
      if (criterionPair.getRight() == null) {
        continue;
      }
      String expression = criterionPair.getLeft();
      FilterCriterion<?> criterion = criterionPair.getRight();
      criterion.setExpression(expression);
      pageRequest.add(new Filtering(List.of(criterion)));
    }

    // FIXME: move to repository!
    addLabelFilter(pageRequest, labelTerm, labelLanguage);
    addNameFilter(pageRequest, nameTerm, nameLanguage);
    return getService().find(pageRequest);
  }

  protected void addLabelFilter(PageRequest pageRequest, String labelTerm, Locale labelLanguage) {
    addFilterForSplitField("label", pageRequest, labelTerm, labelLanguage);
  }

  protected void addNameFilter(PageRequest pageRequest, String nameTerm, Locale nameLanguage) {
    addFilterForSplitField("name", pageRequest, nameTerm, nameLanguage);
  }

  private void addFilterForSplitField(
      String expression, PageRequest pageRequest, String term, Locale language) {
    // FIXME: move to repository
    if (expression == null || pageRequest == null || term == null) {
      return;
    }
    term = term.trim();
    if (language != null) {
      expression += "." + language.getLanguage();
    }
    FilterOperation operation = FilterOperation.CONTAINS;
    if (term.matches("\".+\"")) {
      operation = FilterOperation.EQUALS;
    }
    pageRequest.add(
        Filtering.builder().add(new FilterCriterion<>(expression, operation, term)).build());
  }

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
      // TODO: add datalanguage to be able to validate that multilanguage fields in filtercriteria
      // have
      // already "_language" as postfix assigned...

      // convert filter criterion String value(s) to type of expression field (as generics
      // type got lost over http (and list contains eventually different FilterCriterion types)...)
      for (FilterCriterion fc : filterCriteria) {
        String expression = fc.getExpression();
        FilterOperation filterOperation = fc.getOperation();
        String operationValue = (String) fc.getValue();
        try {
          Field field = targetClass.getDeclaredField(expression);
          Class<?> fieldClass = field.getType();
          if (filtering == null) {
            filtering = new Filtering();
          }
          FilterCriterion convertedFc =
              StringToFilterCriteriaGenericConverter.createFilterCriterion(
                  fieldClass, expression, filterOperation, operationValue, conversionService);
          filtering.add(convertedFc);
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
}
