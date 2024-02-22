package io.github.dbmdz.cudami.controller.identifiable.entity;

import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiWebsitesClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.list.paging.PageResponse;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.dbmdz.cudami.business.api.service.exceptions.ServiceException;
import io.github.dbmdz.cudami.business.i18n.LanguageService;
import io.github.dbmdz.cudami.controller.ParameterHelper;
import io.github.dbmdz.cudami.model.bootstraptable.BTRequest;
import io.github.dbmdz.cudami.model.bootstraptable.BTResponse;
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
public class WebsitesAPIController
    extends AbstractEntitiesController<Website, CudamiWebsitesClient> {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebsitesAPIController.class);

  public WebsitesAPIController(CudamiClient client, LanguageService languageService) {
    super(client.forWebsites(), client, languageService);
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
      @RequestParam(name = "limit", required = false, defaultValue = "10") int limit,
      @RequestParam(name = "search", required = false) String searchTerm,
      @RequestParam(name = "sort", required = false, defaultValue = "label") String sortProperty,
      @RequestParam(name = "order", required = false, defaultValue = "asc") String sortOrder,
      @RequestParam(name = "dataLanguage", required = false) String dataLanguage)
      throws TechnicalException, ServiceException {
    return find(
        Website.class, offset, limit, sortProperty, sortOrder, "label", searchTerm, dataLanguage);
  }

  @GetMapping("/api/websites/{uuid:" + ParameterHelper.UUID_PATTERN + "}/webpages")
  @ResponseBody
  public BTResponse<Webpage> findRootpages(
      @PathVariable UUID uuid,
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(name = "limit", required = false, defaultValue = "10") int limit,
      @RequestParam(name = "search", required = false) String searchTerm,
      @RequestParam(name = "sort", required = false) String sortProperty,
      @RequestParam(name = "order", required = false) String sortOrder,
      @RequestParam(name = "dataLanguage", required = false) String dataLanguage)
      throws TechnicalException {
    BTRequest btRequest =
        createBTRequest(
            Webpage.class,
            offset,
            limit,
            sortProperty,
            sortOrder,
            "label",
            searchTerm,
            dataLanguage);
    PageResponse<Webpage> pageResponse =
        ((CudamiWebsitesClient) service).findRootWebpages(uuid, btRequest);
    return new BTResponse<>(pageResponse);
  }

  @GetMapping("/api/websites/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
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

  @PutMapping("/api/websites/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  public ResponseEntity update(@PathVariable UUID uuid, @RequestBody Website website) {
    try {
      Website websiteDb = service.update(uuid, website);
      return ResponseEntity.ok(websiteDb);
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save website with uuid={}", uuid, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @PutMapping("/api/websites/{uuid:" + ParameterHelper.UUID_PATTERN + "}/webpages")
  public ResponseEntity updateRootPagesOrder(
      @PathVariable UUID uuid, @RequestBody List<Webpage> rootPages) throws TechnicalException {
    boolean successful = ((CudamiWebsitesClient) service).updateRootWebpagesOrder(uuid, rootPages);
    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }
}
