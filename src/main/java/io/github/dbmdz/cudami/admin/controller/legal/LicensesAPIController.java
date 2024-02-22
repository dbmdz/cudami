package io.github.dbmdz.cudami.admin.controller.legal;

import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.legal.License;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.dbmdz.cudami.admin.business.api.service.exceptions.ServiceException;
import io.github.dbmdz.cudami.admin.business.i18n.LanguageService;
import io.github.dbmdz.cudami.admin.controller.AbstractUniqueObjectController;
import io.github.dbmdz.cudami.admin.model.bootstraptable.BTResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/** Controller for all public "Licenses" endpoints (API). */
@RestController
public class LicensesAPIController extends AbstractUniqueObjectController<License> {

  private static final Logger LOGGER = LoggerFactory.getLogger(LicensesAPIController.class);

  public LicensesAPIController(CudamiClient client, LanguageService languageService) {
    super(client.forLicenses(), languageService);
  }

  @SuppressFBWarnings
  @GetMapping("/api/licenses")
  @ResponseBody
  public BTResponse<License> find(
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(name = "limit", required = false, defaultValue = "10") int limit,
      @RequestParam(name = "search", required = false) String searchTerm,
      @RequestParam(name = "sort", required = false, defaultValue = "url") String sortProperty,
      @RequestParam(name = "order", required = false, defaultValue = "asc") String sortOrder,
      @RequestParam(name = "dataLanguage", required = false) String dataLanguage)
      throws TechnicalException, ServiceException {
    return find(
        License.class, offset, limit, sortProperty, sortOrder, "url", searchTerm, dataLanguage);
  }
}
