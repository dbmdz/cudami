package de.digitalcollections.cudami.server.controller.identifiable;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.cudami.server.controller.ParameterHelper;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.FilterOperation;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import java.util.List;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public abstract class AbstractIdentifiableController<T extends Identifiable> {

  public ResponseEntity<T> getByIdentifier(HttpServletRequest request)
      throws IdentifiableServiceException, ValidationException {
    Pair<String, String> namespaceAndId =
        ParameterHelper.extractPairOfStringsFromUri(request.getRequestURI(), "^.*?/identifier/");
    if (namespaceAndId.getLeft().isBlank()
        || (namespaceAndId.getRight() == null || namespaceAndId.getRight().isBlank())) {
      throw new ValidationException(
          "No namespace and/or id were provided in a colon separated manner");
    }

    T identifiable =
        getService().getByIdentifier(namespaceAndId.getLeft(), namespaceAndId.getRight());
    return new ResponseEntity<>(
        identifiable, identifiable != null ? HttpStatus.OK : HttpStatus.NOT_FOUND);
  }

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
  protected PageResponse<T> find(
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
  protected PageResponse<T> find(
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

    addLabelFilter(pageRequest, labelTerm, labelLanguage);
    addNameFilter(pageRequest, nameTerm, nameLanguage);
    return getService().find(pageRequest);
  }

  protected abstract IdentifiableService<T> getService();

  protected void addLabelFilter(PageRequest pageRequest, String labelTerm, Locale labelLanguage) {
    addFilterForSplitField("label", pageRequest, labelTerm, labelLanguage);
  }

  protected void addNameFilter(PageRequest pageRequest, String nameTerm, Locale nameLanguage) {
    addFilterForSplitField("name", pageRequest, nameTerm, nameLanguage);
  }

  private void addFilterForSplitField(
      String expression, PageRequest pageRequest, String term, Locale language) {
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
}
