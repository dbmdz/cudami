package de.digitalcollections.cudami.admin.controller.relation;

import de.digitalcollections.cudami.admin.controller.AbstractPagingAndSortingController;
import de.digitalcollections.cudami.admin.model.bootstraptable.BTResponse;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.CudamiLocalesClient;
import de.digitalcollections.cudami.client.relation.CudamiPredicatesClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.relation.Predicate;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/** Controller for all public "Predicates" endpoints (API). */
@RestController
public class PredicatesAPIController extends AbstractPagingAndSortingController<Predicate> {

  private static final Logger LOGGER = LoggerFactory.getLogger(PredicatesAPIController.class);

  private final CudamiLocalesClient localeService;
  private final CudamiPredicatesClient service;

  public PredicatesAPIController(CudamiClient client) {
    this.localeService = client.forLocales();
    this.service = client.forPredicates();
  }

  @SuppressFBWarnings
  @GetMapping("/api/predicates")
  @ResponseBody
  public BTResponse<Predicate> find(
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(name = "limit", required = false, defaultValue = "1") int limit,
      @RequestParam(name = "search", required = false) String searchTerm,
      @RequestParam(name = "sort", required = false, defaultValue = "value") String sort,
      @RequestParam(name = "order", required = false, defaultValue = "asc") String order,
      @RequestParam(name = "itemLocale", required = false) String itemLocale)
      throws TechnicalException {

    PageResponse<Predicate> pageResponse =
        super.find(localeService, service, offset, limit, searchTerm, sort, order, itemLocale);
    return new BTResponse<>(pageResponse);
  }
}
