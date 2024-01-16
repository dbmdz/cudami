package de.digitalcollections.cudami.admin.controller.identifiable.web;

import de.digitalcollections.cudami.admin.business.i18n.LanguageService;
import de.digitalcollections.cudami.admin.controller.ParameterHelper;
import de.digitalcollections.cudami.admin.controller.identifiable.AbstractIdentifiablesController;
import de.digitalcollections.cudami.admin.model.bootstraptable.BTRequest;
import de.digitalcollections.cudami.admin.model.bootstraptable.BTResponse;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.web.CudamiWebpagesClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.list.paging.PageResponse;
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

/** Controller for webpage management pages. */
@RestController
public class WebpagesAPIController
    extends AbstractIdentifiablesController<Webpage, CudamiWebpagesClient> {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebpagesAPIController.class);

  public WebpagesAPIController(CudamiClient client, LanguageService languageService) {
    super(client.forWebpages(), client, languageService);
  }

  @GetMapping("/api/webpages/new")
  @ResponseBody
  public Webpage create() throws TechnicalException {
    return service.create();
  }

  @GetMapping("/api/webpages/{uuid:" + ParameterHelper.UUID_PATTERN + "}/webpages")
  @ResponseBody
  public BTResponse<Webpage> findSubpages(
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
        ((CudamiWebpagesClient) service).findSubpages(uuid, btRequest);
    return new BTResponse<>(pageResponse);
  }

  @GetMapping("/api/webpages/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  @ResponseBody
  public Webpage getByUuid(@PathVariable UUID uuid) throws TechnicalException {
    return service.getByUuid(uuid);
  }

  @PostMapping("/api/webpages")
  public ResponseEntity save(
      @RequestBody Webpage webpage,
      @RequestParam("parentType") String parentType,
      @RequestParam("parentUuid") UUID parentUuid) {
    try {
      Webpage webpageDb = null;
      if (parentType.equals("website")) {
        webpageDb = ((CudamiWebpagesClient) service).saveWithParentWebsite(webpage, parentUuid);
      } else {
        webpageDb = ((CudamiWebpagesClient) service).saveWithParentWebpage(webpage, parentUuid);
      }
      return ResponseEntity.status(HttpStatus.CREATED).body(webpageDb);
    } catch (TechnicalException e) {
      if (parentType.equals("website")) {
        LOGGER.error("Cannot save top-level webpage: ", e);
      } else if (parentType.equals("webpage")) {
        LOGGER.error("Cannot save webpage: ", e);
      }
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @PutMapping("/api/webpages/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  public ResponseEntity update(@PathVariable UUID uuid, @RequestBody Webpage webpage) {
    try {
      Webpage webpageDb = service.update(uuid, webpage);
      return ResponseEntity.ok(webpageDb);
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save webpage with uuid={}", uuid, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  /** used in webpages/view.html */
  @PutMapping("/api/webpages/{uuid:" + ParameterHelper.UUID_PATTERN + "}/webpages")
  public ResponseEntity updateSubpagesOrder(
      @PathVariable UUID uuid, @RequestBody List<Webpage> subpages) throws TechnicalException {
    boolean successful = ((CudamiWebpagesClient) service).updateChildrenOrder(uuid, subpages);
    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }
}
