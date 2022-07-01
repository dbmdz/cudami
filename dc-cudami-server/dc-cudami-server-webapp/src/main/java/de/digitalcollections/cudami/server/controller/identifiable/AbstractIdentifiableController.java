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
