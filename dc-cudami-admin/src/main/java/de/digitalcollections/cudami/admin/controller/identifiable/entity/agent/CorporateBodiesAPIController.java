package de.digitalcollections.cudami.admin.controller.identifiable.entity.agent;

import de.digitalcollections.cudami.admin.controller.AbstractPagingAndSortingController;
import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.CudamiLocalesClient;
import de.digitalcollections.cudami.client.identifiable.entity.agent.CudamiCorporateBodiesClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
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

/** Controller for all public "CorporateBodies" endpoints (API). */
@RestController
public class CorporateBodiesAPIController
    extends AbstractPagingAndSortingController<CorporateBody> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CorporateBodiesAPIController.class);

  private final LanguageSortingHelper languageSortingHelper;
  private final CudamiLocalesClient localeService;
  private final CudamiCorporateBodiesClient service;

  public CorporateBodiesAPIController(
      LanguageSortingHelper languageSortingHelper, CudamiClient client) {
    this.languageSortingHelper = languageSortingHelper;
    this.localeService = client.forLocales();
    this.service = client.forCorporateBodies();
  }

  @GetMapping("/api/corporatebodies/new")
  @ResponseBody
  public CorporateBody create() throws TechnicalException {
    return service.create();
  }

  @GetMapping("/api/corporatebodies")
  @ResponseBody
  public PageResponse<CorporateBody> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "searchTerm", required = false) String searchTerm,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy)
      throws TechnicalException {
    PageRequest pageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    return service.find(pageRequest);
  }

  @GetMapping("/api/corporatebodies/{uuid}")
  @ResponseBody
  public CorporateBody getByUuid(@PathVariable UUID uuid) throws TechnicalException {
    return service.getByUuid(uuid);
  }

  @PostMapping("/api/corporatebodies")
  public ResponseEntity save(@RequestBody CorporateBody corporateBody) {
    try {
      CorporateBody corporateBodyDb = service.save(corporateBody);
      return ResponseEntity.status(HttpStatus.CREATED).body(corporateBodyDb);
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save corporate body: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @PutMapping("/api/corporatebodies/{uuid}")
  public ResponseEntity update(@PathVariable UUID uuid, @RequestBody CorporateBody corporateBody) {
    try {
      CorporateBody corporateBodyDb = service.update(uuid, corporateBody);
      return ResponseEntity.ok(corporateBodyDb);
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save corporate body with uuid={}", uuid, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }
}
