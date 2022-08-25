package de.digitalcollections.cudami.server.controller.identifiable.entity.work;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.ExpressionTypeService;
import de.digitalcollections.model.identifiable.entity.work.ExpressionType;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
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
@Tag(name = "Expression type Controller")
public class ExpressionTypeController {

  private final ExpressionTypeService expressionTypeService;

  public ExpressionTypeController(ExpressionTypeService expressionTypeService) {
    this.expressionTypeService = expressionTypeService;
  }

  @Operation(summary = "Get all expression types")
  @GetMapping(
      value = {"/v6/expressiontypes"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<ExpressionType> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "searchTerm", required = false) String searchTerm) {
    PageRequest pageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    return expressionTypeService.find(pageRequest);
  }

  @Operation(summary = "Get expression type by UUID")
  @GetMapping(
      value = {"/v6/expressiontypes/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ExpressionType getByUuid(@PathVariable UUID uuid) {
    return expressionTypeService.getByUuid(uuid);
  }

  @Operation(summary = "Save a newly created expression type")
  @PostMapping(
      value = {"/v6/expressiontypes"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ExpressionType save(@RequestBody ExpressionType expressionType, BindingResult errors) {
    return expressionTypeService.save(expressionType);
  }

  @Operation(summary = "Update an expression type")
  @PutMapping(
      value = {"/v6/expressiontypes/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ExpressionType update(
      @PathVariable UUID uuid, @RequestBody ExpressionType expressionType, BindingResult errors) {
    assert Objects.equals(uuid, expressionType.getUuid());
    return expressionTypeService.update(expressionType);
  }
}
