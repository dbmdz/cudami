package de.digitalcollections.cudami.admin.controller.identifiable.entity.agent;

import de.digitalcollections.cudami.admin.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.admin.business.i18n.LanguageService;
import de.digitalcollections.cudami.admin.controller.ParameterHelper;
import de.digitalcollections.cudami.admin.controller.identifiable.AbstractIdentifiablesController;
import de.digitalcollections.cudami.admin.model.bootstraptable.BTResponse;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.agent.CudamiFamilyNamesClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.agent.FamilyName;
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

/** Controller for all public "FamilyName" endpoints (API). */
@RestController
public class FamilynamesAPIController
    extends AbstractIdentifiablesController<FamilyName, CudamiFamilyNamesClient> {

  private static final Logger LOGGER = LoggerFactory.getLogger(FamilynamesAPIController.class);

  public FamilynamesAPIController(CudamiClient client, LanguageService languageService) {
    super(client.forFamilyNames(), client, languageService);
  }

  @GetMapping("/api/familynames/new")
  @ResponseBody
  public FamilyName create() throws TechnicalException {
    return service.create();
  }

  @SuppressFBWarnings
  @GetMapping("/api/familynames")
  @ResponseBody
  public BTResponse<FamilyName> find(
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(name = "limit", required = false, defaultValue = "10") int limit,
      @RequestParam(name = "search", required = false) String searchTerm,
      @RequestParam(name = "sort", required = false, defaultValue = "label") String sortProperty,
      @RequestParam(name = "order", required = false, defaultValue = "asc") String sortOrder,
      @RequestParam(name = "dataLanguage", required = false) String dataLanguage)
      throws TechnicalException, ServiceException {
    return find(
        FamilyName.class,
        offset,
        limit,
        sortProperty,
        sortOrder,
        "label",
        searchTerm,
        dataLanguage);
  }

  @GetMapping("/api/familynames/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  @ResponseBody
  public FamilyName getByUuid(@PathVariable UUID uuid) throws TechnicalException {
    return service.getByUuid(uuid);
  }

  @PostMapping("/api/familynames")
  public ResponseEntity save(@RequestBody FamilyName familyName) {
    try {
      FamilyName familyNameDb = service.save(familyName);
      return ResponseEntity.status(HttpStatus.CREATED).body(familyNameDb);
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save family name: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @PutMapping("/api/familynames/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  public ResponseEntity update(@PathVariable UUID uuid, @RequestBody FamilyName familyName) {
    try {
      FamilyName familyNameDb = service.update(uuid, familyName);
      return ResponseEntity.ok(familyNameDb);
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save family name with uuid={}", uuid, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }
}
