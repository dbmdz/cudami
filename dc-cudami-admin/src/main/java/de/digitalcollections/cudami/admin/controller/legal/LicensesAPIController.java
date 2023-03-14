package de.digitalcollections.cudami.admin.controller.legal;

import de.digitalcollections.cudami.admin.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.admin.controller.AbstractPagingAndSortingController;
import de.digitalcollections.cudami.admin.controller.ParameterHelper;
import de.digitalcollections.cudami.admin.model.bootstraptable.BTResponse;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.CudamiLocalesClient;
import de.digitalcollections.cudami.client.legal.CudamiLicensesClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.legal.License;
import de.digitalcollections.model.list.paging.PageResponse;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/** Controller for all public "Licenses" endpoints (API). */
@RestController
public class LicensesAPIController extends AbstractPagingAndSortingController<License> {

  private static final Logger LOGGER = LoggerFactory.getLogger(LicensesAPIController.class);
  private final CudamiLocalesClient localeService;
  private final CudamiLicensesClient service;

  public LicensesAPIController(CudamiClient client) {
    this.localeService = client.forLocales();
    this.service = client.forLicenses();
  }

  @GetMapping("/api/licenses/new")
  @ResponseBody
  public License createModel() throws TechnicalException {
    return service.create();
  }

  @SuppressFBWarnings
  @GetMapping("/api/licenses")
  @ResponseBody
  public BTResponse<License> find(
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(name = "limit", required = false, defaultValue = "1") int limit,
      @RequestParam(name = "search", required = false) String searchTerm,
      @RequestParam(name = "sort", required = false, defaultValue = "url") String sort,
      @RequestParam(name = "order", required = false, defaultValue = "asc") String order,
      @RequestParam(name = "dataLanguage", required = false) String dataLanguage)
      throws TechnicalException, ServiceException {
    PageResponse<License> pageResponse =
        super.find(localeService, service, offset, limit, searchTerm, sort, order, dataLanguage);
    return new BTResponse<>(pageResponse);
  }

  @GetMapping("/api/licenses/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  @ResponseBody
  public License getByUuid(@PathVariable UUID uuid) throws TechnicalException {
    return service.getByUuid(uuid);
  }

  @PostMapping("/api/licenses")
  public ResponseEntity save(@RequestBody License license) {
    try {
      License licenseDb = service.save(license);
      return ResponseEntity.status(HttpStatus.CREATED).body(licenseDb);
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save license: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @PutMapping("/api/licenses/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  public ResponseEntity update(@PathVariable UUID uuid, @RequestBody License license) {
    try {
      License licenseDb = service.update(uuid, license);
      return ResponseEntity.ok(licenseDb);
    } catch (TechnicalException e) {
      LOGGER.error("Cannot update license with uuid={}", uuid, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }
}
