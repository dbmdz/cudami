package io.github.dbmdz.cudami.controller.identifiable;

import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.IdentifierType;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.dbmdz.cudami.business.api.service.exceptions.ServiceException;
import io.github.dbmdz.cudami.controller.AbstractUniqueObjectController;
import io.github.dbmdz.cudami.model.bootstraptable.BTResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/** Controller for all public "IdentifierTypes" endpoints (API). */
@RestController
public class IdentifierTypeAPIController extends AbstractUniqueObjectController<IdentifierType> {

  private static final Logger LOGGER = LoggerFactory.getLogger(IdentifierTypeAPIController.class);

  public IdentifierTypeAPIController(CudamiClient client) {
    super(client.forIdentifierTypes(), null);
  }

  @SuppressFBWarnings
  @GetMapping("/api/identifiertypes")
  @ResponseBody
  public BTResponse<IdentifierType> find(
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(name = "limit", required = false, defaultValue = "10") int limit,
      @RequestParam(name = "search", required = false) String searchTerm,
      @RequestParam(name = "sort", required = false, defaultValue = "namespace")
          String sortProperty,
      @RequestParam(name = "order", required = false, defaultValue = "asc") String sortOrder)
      throws TechnicalException, ServiceException {
    // no "dataLanguage" / no multilingual fields
    return find(
        IdentifierType.class,
        offset,
        limit,
        sortProperty,
        sortOrder,
        "namespace",
        searchTerm,
        null);
  }
}
