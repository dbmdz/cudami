package io.github.dbmdz.cudami.controller.relation;

import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.relation.Predicate;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.dbmdz.cudami.business.i18n.LanguageService;
import io.github.dbmdz.cudami.controller.AbstractUniqueObjectController;
import io.github.dbmdz.cudami.model.bootstraptable.BTResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/** Controller for all public "Predicates" endpoints (API). */
@RestController
public class PredicatesAPIController extends AbstractUniqueObjectController<Predicate> {

  private static final Logger LOGGER = LoggerFactory.getLogger(PredicatesAPIController.class);

  public PredicatesAPIController(CudamiClient client, LanguageService languageService) {
    super(client.forPredicates(), languageService);
  }

  @SuppressFBWarnings
  @GetMapping("/api/predicates")
  @ResponseBody
  public BTResponse<Predicate> find(
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(name = "limit", required = false, defaultValue = "10") int limit,
      @RequestParam(name = "search", required = false) String searchTerm,
      @RequestParam(name = "sort", required = false, defaultValue = "value") String sortProperty,
      @RequestParam(name = "order", required = false, defaultValue = "asc") String sortOrder,
      @RequestParam(name = "dataLanguage", required = false) String dataLanguage)
      throws TechnicalException {
    return find(
        Predicate.class, offset, limit, sortProperty, sortOrder, "value", searchTerm, dataLanguage);
  }
}
