package de.digitalcollections.cudami.server.controller.identifiable;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import de.digitalcollections.model.paging.Sorting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Identifiable controller")
public class IdentifiableController {

  private final IdentifiableService identifiableService;

  public IdentifiableController(
      @Qualifier("identifiableService") IdentifiableService identifiableService) {
    this.identifiableService = identifiableService;
  }

  @Operation(
      summary =
          "Find limited amount of identifiables containing searchTerm in label or description")
  @GetMapping(
      value = {
        "/v5/identifiables/search",
        "/v2/identifiables/search",
        "/latest/identifiables/search"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public SearchPageResponse<Identifiable> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "searchTerm", required = false) String searchTerm) {
    SearchPageRequest pageRequest = new SearchPageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    return identifiableService.find(pageRequest);
  }

  @Operation(summary = "Find limited amount of identifiables containing searchTerm in label")
  @GetMapping(
      value = {"/v5/identifiables", "/v2/identifiables", "/latest/identifiables"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Identifiable> find(
      @RequestParam(name = "searchTerm") String searchTerm,
      @RequestParam(name = "maxResults", required = false, defaultValue = "25") int maxResults) {
    List<Identifiable> identifiables = identifiableService.find(searchTerm, maxResults);
    return identifiables;
  }

  @Operation(summary = "Get identifiable by uuid")
  @GetMapping(
      value = {
        "/v5/identifiables/{uuid}",
        "/v2/identifiables/{uuid}",
        "/latest/identifiables/{uuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Identifiable findById(@PathVariable UUID uuid) {
    return identifiableService.get(uuid);
  }

  @Operation(summary = "Get an identifiable by namespace and id")
  @GetMapping(
      value = {
        "/v5/identifiables/identifier/{namespace}:{id}",
        "/v2/identifiables/identifier/{namespace}:{id}",
        "/latest/identifiables/identifier/{namespace}:{id}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Identifiable> getByIdentifier(
      @PathVariable String namespace, @PathVariable String id) throws IdentifiableServiceException {

    Identifiable result = identifiableService.getByIdentifier(namespace, id);
    if (result == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(result, HttpStatus.OK);
  }
}
