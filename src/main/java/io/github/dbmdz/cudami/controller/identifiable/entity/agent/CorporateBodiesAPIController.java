package io.github.dbmdz.cudami.controller.identifiable.entity.agent;

import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.entity.agent.CudamiCorporateBodiesClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import io.github.dbmdz.cudami.business.api.service.exceptions.ServiceException;
import io.github.dbmdz.cudami.business.i18n.LanguageService;
import io.github.dbmdz.cudami.controller.ParameterHelper;
import io.github.dbmdz.cudami.controller.identifiable.entity.AbstractEntitiesController;
import io.github.dbmdz.cudami.model.bootstraptable.BTResponse;
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
    extends AbstractEntitiesController<CorporateBody, CudamiCorporateBodiesClient> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CorporateBodiesAPIController.class);

  public CorporateBodiesAPIController(CudamiClient client, LanguageService languageService) {
    super(client.forCorporateBodies(), client, languageService);
  }

  @GetMapping("/api/corporatebodies/new")
  @ResponseBody
  public CorporateBody create() throws TechnicalException {
    return service.create();
  }

  @GetMapping("/api/corporatebodies")
  @ResponseBody
  public BTResponse<CorporateBody> find(
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(name = "limit", required = false, defaultValue = "10") int limit,
      @RequestParam(name = "search", required = false) String searchTerm,
      @RequestParam(name = "sort", required = false, defaultValue = "label") String sortProperty,
      @RequestParam(name = "order", required = false, defaultValue = "asc") String sortOrder,
      @RequestParam(name = "dataLanguage", required = false) String dataLanguage)
      throws TechnicalException, ServiceException {
    return find(
        CorporateBody.class,
        offset,
        limit,
        sortProperty,
        sortOrder,
        "label",
        searchTerm,
        dataLanguage);
  }

  @GetMapping("/api/corporatebodies/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
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

  @PutMapping("/api/corporatebodies/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
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
