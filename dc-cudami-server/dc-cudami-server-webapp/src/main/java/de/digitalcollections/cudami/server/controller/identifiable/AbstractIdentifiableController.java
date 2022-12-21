package de.digitalcollections.cudami.server.controller.identifiable;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.cudami.server.controller.AbstractUniqueObjectController;
import de.digitalcollections.cudami.server.controller.ParameterHelper;
import de.digitalcollections.model.identifiable.Identifiable;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public abstract class AbstractIdentifiableController<T extends Identifiable>
    extends AbstractUniqueObjectController<T> {

  public ResponseEntity<T> getByIdentifier(HttpServletRequest request)
      throws ServiceException, ValidationException {
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

  @Override
  protected abstract IdentifiableService<T> getService();
}
