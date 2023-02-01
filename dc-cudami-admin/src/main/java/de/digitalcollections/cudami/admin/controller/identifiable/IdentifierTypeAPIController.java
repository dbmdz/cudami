package de.digitalcollections.cudami.admin.controller.identifiable;

import de.digitalcollections.cudami.admin.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.admin.controller.AbstractPagingAndSortingController;
import de.digitalcollections.cudami.admin.controller.ParameterHelper;
import de.digitalcollections.cudami.admin.model.bootstraptable.BTResponse;
import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.CudamiIdentifierTypesClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.IdentifierType;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
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

/** Controller for all public "IdentifierTypes" endpoints (API). */
@RestController
public class IdentifierTypeAPIController
    extends AbstractPagingAndSortingController<IdentifierType> {

  private static final Logger LOGGER = LoggerFactory.getLogger(IdentifierTypeAPIController.class);

  private final CudamiIdentifierTypesClient service;

  public IdentifierTypeAPIController(
      CudamiClient client, LanguageSortingHelper languageSortingHelper) {
    super(languageSortingHelper);
    this.service = client.forIdentifierTypes();
  }

  @GetMapping("/api/identifiertypes/new")
  @ResponseBody
  public IdentifierType createModel() throws TechnicalException {
    return service.create();
  }

  @SuppressFBWarnings
  @GetMapping("/api/identifiertypes")
  @ResponseBody
  public BTResponse<IdentifierType> find(
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(name = "limit", required = false, defaultValue = "1") int limit,
      @RequestParam(name = "search", required = false) String searchTerm,
      @RequestParam(name = "sort", required = false, defaultValue = "lastname") String sort,
      @RequestParam(name = "order", required = false, defaultValue = "asc") String order)
      throws TechnicalException, ServiceException {
    PageRequest pageRequest = createPageRequest(sort, order, null, null, offset, limit, searchTerm);
    PageResponse<IdentifierType> pageResponse = service.find(pageRequest);
    return new BTResponse<>(pageResponse);
  }

  @GetMapping("/api/identifiertypes/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  @ResponseBody
  public IdentifierType getByUuid(@PathVariable UUID uuid) throws TechnicalException {
    return service.getByUuid(uuid);
  }

  @PostMapping("/api/identifiertypes")
  public ResponseEntity save(@RequestBody IdentifierType identifierType) {
    try {
      IdentifierType identifierTypeDb = service.save(identifierType);
      return ResponseEntity.status(HttpStatus.CREATED).body(identifierTypeDb);
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save identifier type: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @PutMapping("/api/identifiertypes/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  public ResponseEntity update(
      @PathVariable UUID uuid, @RequestBody IdentifierType identifierType) {
    try {
      IdentifierType identifierTypeDb = service.update(uuid, identifierType);
      return ResponseEntity.ok(identifierTypeDb);
    } catch (TechnicalException e) {
      LOGGER.error("Cannot update identifier type with uuid={}", uuid, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }
}
