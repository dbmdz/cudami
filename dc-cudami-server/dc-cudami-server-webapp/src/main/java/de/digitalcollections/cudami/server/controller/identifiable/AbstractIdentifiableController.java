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
    Pair<String, String> namespaceAndId = extractNamespaceAndId(request);

    T identifiable =
        getService().getByIdentifier(namespaceAndId.getLeft(), namespaceAndId.getRight());
    return new ResponseEntity<>(
        identifiable, identifiable != null ? HttpStatus.OK : HttpStatus.NOT_FOUND);
  }

  /**
   * Extract the namespace and identifier from the HttpServletRequest
   *
   * @param request the HttpServletRequest
   * @return Pair of namespace and identifier
   * @throws ValidationException in case of a missing namespace or malformed parameter
   */
  public static Pair<String, String> extractNamespaceAndId(HttpServletRequest request)
      throws ValidationException {
    Pair<String, String> namespaceAndId =
        ParameterHelper.extractPairOfStringsFromUri(request.getRequestURI(), "^.*?/identifier/");
    if (namespaceAndId.getLeft().isBlank()
        || (namespaceAndId.getRight() == null || namespaceAndId.getRight().isBlank())) {
      throw new ValidationException(
          "No namespace and/or id were provided in a colon separated manner");
    }
    return namespaceAndId;
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
   * @param filterCriteria must be pairs of a {@code String}, the expression, and the corresponding
   *     {@code FilterCriterion}
   * @return
   */
  protected PageResponse<T> find(
      int pageNumber,
      int pageSize,
      List<Order> sortBy,
      String searchTerm,
      String labelTerm,
      Locale labelLanguage,
      Object... filterCriteria) {
    PageRequest pageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }

    if (filterCriteria.length > 0 && filterCriteria.length % 2 == 0) {
      // Since Map.of doesn't support null values we try it this varargs-way.
      // There must be two entries per criterion so the array's length must be even.
      for (int i = 0; i < filterCriteria.length; i += 2) {
        if (filterCriteria[i + 1] == null) {
          continue;
        }
        if (!(filterCriteria[i] instanceof String)
            || !(filterCriteria[i + 1] instanceof FilterCriterion)) {
          throw new IllegalArgumentException(
              "`filterCriteria` must be pairs of a `String`, the expression, and the corresponding `FilterCriterion`");
        }
        String expression = (String) filterCriteria[i];
        FilterCriterion<?> criterion = (FilterCriterion<?>) filterCriteria[i + 1];
        criterion.setExpression(expression);
        pageRequest.add(new Filtering(List.of(criterion)));
      }
    }

    addLabelFilter(pageRequest, labelTerm, labelLanguage);
    return getService().find(pageRequest);
  }

  protected abstract IdentifiableService<T> getService();

  protected void addLabelFilter(PageRequest pageRequest, String labelTerm, Locale labelLanguage) {
    if (pageRequest == null || labelTerm == null) {
      return;
    }
    labelTerm = labelTerm.trim();
    String expression = "label";
    if (labelLanguage != null) {
      expression += "." + labelLanguage.getLanguage();
    }
    FilterOperation operation = FilterOperation.CONTAINS;
    if (labelTerm.matches("\".+\"")) {
      operation = FilterOperation.EQUALS;
    }
    pageRequest.add(
        Filtering.builder().add(new FilterCriterion<>(expression, operation, labelTerm)).build());
  }
}
