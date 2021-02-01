package de.digitalcollections.cudami.server.controller.identifiable;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.model.api.identifiable.Identifiable;
import de.digitalcollections.model.api.paging.Order;
import de.digitalcollections.model.api.paging.SearchPageRequest;
import de.digitalcollections.model.api.paging.SearchPageResponse;
import de.digitalcollections.model.api.paging.Sorting;
import de.digitalcollections.model.impl.paging.SearchPageRequestImpl;
import de.digitalcollections.model.impl.paging.SortingImpl;
import java.util.List;
import java.util.UUID;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiResponseObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(description = "The identifiable controller", name = "Identifiable controller")
public class IdentifiableController {

  @Autowired
  @Qualifier("identifiableServiceImpl")
  private IdentifiableService service;

  @ApiMethod(
      description =
          "Find limited amount of identifiables containing searchTerm in label or description")
  @GetMapping(
      value = {"/latest/identifiables/search", "/v2/identifiables/search"},
      produces = "application/json")
  @ApiResponseObject
  public SearchPageResponse<Identifiable> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "searchTerm", required = false) String searchTerm) {
    SearchPageRequest pageRequest = new SearchPageRequestImpl(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new SortingImpl(sortBy);
      pageRequest.setSorting(sorting);
    }
    return service.find(pageRequest);
  }

  @ApiMethod(description = "Find limited amount of identifiables containing searchTerm in label")
  @GetMapping(
      value = {"/latest/identifiables", "/v2/identifiables"},
      produces = "application/json")
  @ApiResponseObject
  public List<Identifiable> find(
      @RequestParam(name = "searchTerm") String searchTerm,
      @RequestParam(name = "maxResults", required = false, defaultValue = "25") int maxResults) {
    List<Identifiable> identifiables = service.find(searchTerm, maxResults);
    return identifiables;
  }

  @ApiMethod(description = "Get identifiable by uuid")
  @GetMapping(
      value = {"/latest/identifiables/{uuid}", "/v2/identifiables/{uuid}"},
      produces = "application/json")
  @ApiResponseObject
  public Identifiable findById(@PathVariable UUID uuid) {
    return service.get(uuid);
  }

  @ApiMethod(
      description =
          "get an identifiable as JSON or XML, depending on extension or <tt>format</tt> request parameter or accept header")
  @GetMapping(
      value = {
        "/latest/identifiables/identifier/{namespace}:{id}",
        "/v2/identifiables/identifier/{namespace}:{id}"
      },
      produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ApiResponseObject
  public ResponseEntity<Identifiable> getByIdentifier(
      @PathVariable String namespace, @PathVariable String id) throws IdentifiableServiceException {

    Identifiable result = service.getByIdentifier(namespace, id);
    if (result == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(result, HttpStatus.OK);
  }
}
