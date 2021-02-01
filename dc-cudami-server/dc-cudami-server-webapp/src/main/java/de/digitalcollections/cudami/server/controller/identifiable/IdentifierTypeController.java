package de.digitalcollections.cudami.server.controller.identifiable;

import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierTypeService;
import de.digitalcollections.model.api.identifiable.IdentifierType;
import de.digitalcollections.model.api.paging.Order;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.Sorting;
import de.digitalcollections.model.impl.paging.PageRequestImpl;
import de.digitalcollections.model.impl.paging.SortingImpl;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiResponseObject;
import org.springframework.beans.factory.annotation.Autowired;
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

  @Autowired private IdentifierTypeService service;

  @ApiMethod(description = "Get all identifier types")
  @GetMapping(
      value = {"/latest/identifiertypes", "/v2/identifiertypes"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public PageResponse<IdentifierType> findAll(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy) {
    PageRequest pageRequest = new PageRequestImpl(pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting =
          new SortingImpl(sortBy.stream().filter(Objects::nonNull).collect(Collectors.toList()));
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
    return (IdentifierType) service.get(uuid);
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
    return (IdentifierType) service.update(identifierType);
  }
}
