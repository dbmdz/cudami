package de.digitalcollections.cudami.admin.controller.semantic;

import de.digitalcollections.cudami.admin.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.admin.controller.AbstractUniqueObjectController;
import de.digitalcollections.cudami.admin.controller.ParameterHelper;
import de.digitalcollections.cudami.admin.model.bootstraptable.BTResponse;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.semantic.Headword;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/** Controller for all public "Headwords" endpoints (API). */
@RestController
public class HeadwordsAPIController extends AbstractUniqueObjectController<Headword> {

  private static final Logger LOGGER = LoggerFactory.getLogger(HeadwordsAPIController.class);

  public HeadwordsAPIController(CudamiClient client) {
    // no "languageService" needed / no multilingual fields
    super(client.forHeadwords(), null);
  }

  @GetMapping("/api/headwords/new")
  @ResponseBody
  public Headword createModel() throws TechnicalException {
    return service.create();
  }

  @SuppressFBWarnings
  @GetMapping("/api/headwords")
  @ResponseBody
  public BTResponse<Headword> find(
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(name = "limit", required = false, defaultValue = "10") int limit,
      @RequestParam(name = "search", required = false) String searchTerm,
      @RequestParam(name = "sort", required = false, defaultValue = "label") String sortProperty,
      @RequestParam(name = "order", required = false, defaultValue = "asc") String sortOrder)
      throws TechnicalException, ServiceException {
    // no "dataLanguage" / no multilingual fields
    return find(Headword.class, offset, limit, sortProperty, sortOrder, "label", searchTerm, null);
  }

  @GetMapping("/api/headwords/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  @ResponseBody
  public Headword getByUuid(@PathVariable UUID uuid) throws TechnicalException {
    return service.getByUuid(uuid);
  }
}
