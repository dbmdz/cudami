package de.digitalcollections.cudami.server.controller.identifiable;

import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierTypeService;
import de.digitalcollections.model.identifiable.IdentifierType;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.Sorting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
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
@Tag(name = "Identifier type controller")
public class IdentifierTypeController {

  private final IdentifierTypeService identifierTypeService;

  public IdentifierTypeController(IdentifierTypeService identifierTypeService) {
    this.identifierTypeService = identifierTypeService;
  }

  @Operation(summary = "Get all identifier types")
  @GetMapping(
      value = {"/v5/identifiertypes"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<IdentifierType> findAll(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy) {
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    return identifierTypeService.find(pageRequest);
  }

  @Operation(summary = "get identifier type by uuid")
  @GetMapping(
      value = {
        "/v5/identifiertypes/{uuid}",
        "/v2/identifiertypes/{uuid}",
        "/latest/identifiertypes/{uuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public IdentifierType getByUuid(@PathVariable UUID uuid) {
    return identifierTypeService.getByUuid(uuid);
  }

  @Operation(summary = "save a newly created identifier type")
  @PostMapping(
      value = {"/v5/identifiertypes", "/v2/identifiertypes", "/latest/identifiertypes"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public IdentifierType save(@RequestBody IdentifierType identifierType, BindingResult errors) {
    return identifierTypeService.save(identifierType);
  }

  @Operation(summary = "update an identifier type")
  @PutMapping(
      value = {
        "/v5/identifiertypes/{uuid}",
        "/v2/identifiertypes/{uuid}",
        "/latest/identifiertypes/{uuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public IdentifierType update(
      @PathVariable UUID uuid, @RequestBody IdentifierType identifierType, BindingResult errors) {
    assert Objects.equals(uuid, identifierType.getUuid());
    return identifierTypeService.update(identifierType);
  }
}
