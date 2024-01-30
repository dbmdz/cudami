package de.digitalcollections.cudami.server.controller.identifiable.entity.geo.location;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.EntityService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.geo.location.HumanSettlementService;
import de.digitalcollections.cudami.server.controller.AbstractEntityController;
import de.digitalcollections.cudami.server.controller.ParameterHelper;
import de.digitalcollections.model.identifiable.entity.geo.location.HumanSettlement;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.validation.ValidationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Human settlement controller")
public class HumanSettlementController extends AbstractEntityController<HumanSettlement> {

  private final HumanSettlementService service;

  public HumanSettlementController(HumanSettlementService humanSettlementService) {
    this.service = humanSettlementService;
  }

  @Operation(summary = "Delete a human settlement")
  @DeleteMapping(
      value = {"/v6/humansettlements/{uuid:" + ParameterHelper.UUID_PATTERN + "}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity delete(
      @Parameter(example = "", description = "UUID of the human settlement") @PathVariable("uuid")
          UUID uuid)
      throws ConflictException, ServiceException {
    return super.delete(uuid);
  }

  @Operation(summary = "Get all human settlements as (paged, sorted, filtered) list")
  @GetMapping(
      value = {"/v6/humansettlements"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<HumanSettlement> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "filter", required = false) List<FilterCriterion> filterCriteria,
      @RequestParam(name = "filtering", required = false) Filtering filtering)
      throws ServiceException {
    return super.find(pageNumber, pageSize, sortBy, filterCriteria, filtering);
  }

  @Override
  @Operation(
      summary = "Get a geolocation by namespace and id",
      description =
          "Separate namespace and id with a colon, e.g. foo:bar. It is also possible, to add a .json suffix, which will be ignored then")
  @GetMapping(
      value = {
        "/v6/humansettlements/identifier/**",
        "/v5/humansettlements/identifier/**",
        "/v2/humansettlements/identifier/**",
        "/latest/humansettlements/identifier/**"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<HumanSettlement> getByIdentifier(HttpServletRequest request)
      throws ServiceException, ValidationException {
    return super.getByIdentifier(request);
  }

  @Operation(summary = "Get a human settlement by namespace and id")
  @GetMapping(
      value = {
        "/v6/humansettlements/identifier",
        "/v5/humansettlements/identifier",
        "/v2/humansettlements/identifier",
        "/latest/humansettlements/identifier"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> getByIdentifier(
      @RequestParam(name = "namespace", required = true) String namespace,
      @RequestParam(name = "id", required = true) String id,
      HttpServletRequest request)
      throws ServiceException {
    URI newLocation =
        URI.create(request.getRequestURI().concat(String.format("/%s:%s", namespace, id)));
    return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY).location(newLocation).build();
  }

  @Operation(summary = "Get a human settlement by uuid")
  @GetMapping(
      value = {
        "/v6/humansettlements/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/humansettlements/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v2/humansettlements/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/latest/humansettlements/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<HumanSettlement> getByUuid(
      @Parameter(
              example = "",
              description =
                  "UUID of the human settlement, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @Parameter(
              name = "pLocale",
              description =
                  "Desired locale, e.g. <tt>de</tt>. If unset, contents in all languages will be returned")
          @RequestParam(name = "pLocale", required = false)
          Locale pLocale)
      throws ServiceException {
    if (pLocale == null) {
      return getByUuid(uuid);
    } else {
      return getByUuidAndLocale(uuid, pLocale);
    }
  }

  @Override
  protected EntityService<HumanSettlement> getService() {
    return service;
  }

  @Operation(summary = "save a newly created human settlement")
  @PostMapping(
      value = {
        "/v6/humansettlements",
        "/v5/humansettlements",
        "/v2/humansettlements",
        "/latest/humansettlements"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public HumanSettlement save(@RequestBody HumanSettlement humanSettlement, BindingResult errors)
      throws ServiceException, ValidationException {
    return super.save(humanSettlement, errors);
  }

  @Operation(summary = "update a human settlement")
  @PutMapping(
      value = {
        "/v6/humansettlements/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/humansettlements/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v2/humansettlements/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/latest/humansettlements/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public HumanSettlement update(
      @PathVariable("uuid") UUID uuid,
      @RequestBody HumanSettlement humanSettlement,
      BindingResult errors)
      throws ServiceException, ValidationException {
    return super.update(uuid, humanSettlement, errors);
  }
}
