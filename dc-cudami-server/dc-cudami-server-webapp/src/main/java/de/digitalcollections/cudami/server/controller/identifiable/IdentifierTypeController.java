package de.digitalcollections.cudami.server.controller.identifiable;

import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierTypeService;
import de.digitalcollections.model.identifiable.IdentifierType;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.Sorting;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiResponseObject;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(description = "The identifier types controller", name = "Identifier types controller")
public class IdentifierTypeController {

  private final IdentifierTypeService service;

  public IdentifierTypeController(IdentifierTypeService service) {
    this.service = service;
  }

  @ApiMethod(description = "Get all identifier types")
  @GetMapping(
      value = {"/latest/identifiertypes", "/v2/identifiertypes"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public PageResponse<IdentifierType> findAll(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy) {
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    return service.find(pageRequest);
  }

  @ApiMethod(description = "get identifier type by uuid")
  @GetMapping(
      value = {"/latest/identifiertypes/{uuid}", "/v2/identifiertypes/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public IdentifierType findById(@PathVariable UUID uuid) {
    return service.get(uuid);
  }

  @ApiMethod(description = "save a newly created identifier type")
  @PostMapping(
      value = {"/latest/identifiertypes", "/v2/identifiertypes"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public IdentifierType save(@RequestBody IdentifierType identifierType, BindingResult errors) {
    return service.save(identifierType);
  }

  @ApiMethod(description = "update an identifier type")
  @PutMapping(
      value = {"/latest/identifiertypes/{uuid}", "/v2/identifiertypes/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public IdentifierType update(
      @PathVariable UUID uuid, @RequestBody IdentifierType identifierType, BindingResult errors) {
    assert Objects.equals(uuid, identifierType.getUuid());
    return service.update(identifierType);
  }
}
