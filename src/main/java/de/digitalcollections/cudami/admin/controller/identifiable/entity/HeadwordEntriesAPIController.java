package de.digitalcollections.cudami.admin.controller.identifiable.entity;

import de.digitalcollections.cudami.admin.business.i18n.LanguageService;
import de.digitalcollections.cudami.admin.controller.ParameterHelper;
import de.digitalcollections.cudami.admin.model.bootstraptable.BTResponse;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiHeadwordEntriesClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.HeadwordEntry;
import de.digitalcollections.model.relation.Predicate;
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

/** Controller for all public "HeadwordEntry" endpoints (API). */
@RestController
public class HeadwordEntriesAPIController
    extends AbstractEntitiesController<HeadwordEntry, CudamiHeadwordEntriesClient> {

  private static final Logger LOGGER = LoggerFactory.getLogger(HeadwordEntriesAPIController.class);

  public HeadwordEntriesAPIController(CudamiClient client, LanguageService languageService) {
    super(client.forHeadwordEntries(), client, languageService);
  }

  @GetMapping("/api/headwordentries/new")
  @ResponseBody
  public HeadwordEntry create() throws TechnicalException {
    return service.create();
  }

  @SuppressFBWarnings
  @GetMapping("/api/headwordentries")
  @ResponseBody
  public BTResponse<HeadwordEntry> find(
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(name = "limit", required = false, defaultValue = "10") int limit,
      @RequestParam(name = "search", required = false) String searchTerm,
      @RequestParam(name = "sort", required = false, defaultValue = "label") String sortProperty,
      @RequestParam(name = "order", required = false, defaultValue = "asc") String sortOrder,
      @RequestParam(name = "dataLanguage", required = false) String dataLanguage)
      throws TechnicalException {
    return find(
        Predicate.class, offset, limit, sortProperty, sortOrder, "label", searchTerm, dataLanguage);
  }

  @GetMapping("/api/headwordentries/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  @ResponseBody
  public HeadwordEntry getByUuid(@PathVariable UUID uuid) throws TechnicalException {
    return service.getByUuid(uuid);
  }

  @PostMapping("/api/headwordentries")
  public ResponseEntity save(@RequestBody HeadwordEntry headwordEntry) {
    try {
      HeadwordEntry headwordEntryDb = service.save(headwordEntry);
      return ResponseEntity.status(HttpStatus.CREATED).body(headwordEntryDb);
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save headwordEntry: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @PutMapping("/api/headwordentries/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  public ResponseEntity update(@PathVariable UUID uuid, @RequestBody HeadwordEntry headwordEntry) {
    try {
      HeadwordEntry headwordEntryDb = service.update(uuid, headwordEntry);
      return ResponseEntity.ok(headwordEntryDb);
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save headwordEntry with uuid={}", uuid, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }
}
