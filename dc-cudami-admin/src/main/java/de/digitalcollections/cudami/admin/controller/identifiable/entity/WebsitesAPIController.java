package de.digitalcollections.cudami.admin.controller.identifiable.entity;

import de.digitalcollections.cudami.admin.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.admin.controller.AbstractPagingAndSortingController;
import de.digitalcollections.cudami.admin.model.bootstraptable.BTResponse;
import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.CudamiLocalesClient;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiWebsitesClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
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

/** Controller for all public "Websites" endpoints (API). */
@RestController
public class WebsitesAPIController extends AbstractPagingAndSortingController<Website> {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebsitesAPIController.class);

  private final LanguageSortingHelper languageSortingHelper;
  private final CudamiLocalesClient localeService;
  private final CudamiWebsitesClient service;

  public WebsitesAPIController(LanguageSortingHelper languageSortingHelper, CudamiClient client) {
    this.languageSortingHelper = languageSortingHelper;
    this.localeService = client.forLocales();
    this.service = client.forWebsites();
  }

  @GetMapping("/api/websites/new")
  @ResponseBody
  public Website create() throws TechnicalException {
    return service.create();
  }

  @SuppressFBWarnings
  @GetMapping("/api/websites")
  @ResponseBody
  public BTResponse<Website> find(
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(name = "limit", required = false, defaultValue = "1") int limit,
      @RequestParam(name = "search", required = false) String searchTerm,
      @RequestParam(name = "sort", required = false, defaultValue = "label") String sort,
      @RequestParam(name = "order", required = false, defaultValue = "asc") String order,
      @RequestParam(name = "itemLocale", required = false) String itemLocale)
      throws TechnicalException, ServiceException {
    PageResponse<Website> pageResponse =
        super.find(localeService, service, offset, limit, searchTerm, sort, order, itemLocale);
    return new BTResponse<>(pageResponse);
  }

  @GetMapping("/api/websites/{uuid}/webpages")
  @ResponseBody
  public PageResponse<Webpage> findRootpages(
      @PathVariable UUID uuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "searchTerm", required = false) String searchTerm)
      throws TechnicalException {
    PageRequest pageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
    return service.findRootWebpages(uuid, pageRequest);
  }

  @GetMapping("/api/websites/{uuid}")
  @ResponseBody
  public Website getByUuid(@PathVariable UUID uuid) throws TechnicalException {
    return service.getByUuid(uuid);
  }

  @PostMapping("/api/websites")
  public ResponseEntity save(@RequestBody Website website) {
    try {
      Website websiteDb = service.save(website);
      return ResponseEntity.status(HttpStatus.CREATED).body(websiteDb);
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save website: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @PutMapping("/api/websites/{uuid}")
  public ResponseEntity update(@PathVariable UUID uuid, @RequestBody Website website) {
    try {
      Website websiteDb = service.update(uuid, website);
      return ResponseEntity.ok(websiteDb);
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save website with uuid={}", uuid, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @PutMapping("/api/websites/{uuid}/webpages")
  public ResponseEntity updateRootPagesOrder(
      @PathVariable UUID uuid, @RequestBody List<Webpage> rootPages) throws TechnicalException {
    boolean successful = service.updateRootWebpagesOrder(uuid, rootPages);
    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }
}
