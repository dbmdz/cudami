package de.digitalcollections.cudami.server.controller.identifiable.entity.geo.location;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.EntityService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.geo.location.CanyonService;
import de.digitalcollections.cudami.server.controller.AbstractEntityController;
import de.digitalcollections.cudami.server.controller.ParameterHelper;
import de.digitalcollections.model.identifiable.entity.geo.location.Canyon;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.validation.ValidationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Canyon controller")
public class CanyonController extends AbstractEntityController<Canyon> {

  private final CanyonService service;

  public CanyonController(CanyonService canyonService) {
    this.service = canyonService;
  }

  @Operation(summary = "Delete a canyon")
  @DeleteMapping(
      value = {"/v6/canyons/{uuid:" + ParameterHelper.UUID_PATTERN + "}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity delete(
      @Parameter(example = "", description = "UUID of the canyon") @PathVariable("uuid") UUID uuid)
      throws ConflictException, ServiceException {
    return super.delete(uuid);
  }

  @Operation(summary = "Get all canyons as (paged, sorted, filtered) list")
  @GetMapping(
      value = {"/v6/canyons"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Canyon> find(
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
      summary = "Get a canyon by namespace and id",
      description =
          "Separate namespace and id with a colon, e.g. foo:bar. It is also possible, to add a .json suffix, which will be ignored then")
  @GetMapping(
      value = {"/v6/canyons/identifier/**"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Canyon> getByIdentifier(HttpServletRequest request)
      throws ServiceException, ValidationException {
    return super.getByIdentifier(request);
  }

  @Operation(summary = "Get a canyon by uuid")
  @GetMapping(
      value = {"/v6/canyons/{uuid:" + ParameterHelper.UUID_PATTERN + "}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Canyon> getByUuid(
      @Parameter(
              example = "",
              description =
                  "UUID of the canyon, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
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
  protected EntityService<Canyon> getService() {
    return service;
  }

  @Operation(summary = "save a newly created canyon")
  @PostMapping(
      value = {"/v6/canyons"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Canyon save(@RequestBody Canyon canyon, BindingResult errors)
      throws ServiceException, ValidationException {
    return super.save(canyon, errors);
  }

  @Operation(summary = "update a canyon")
  @PutMapping(
      value = {"/v6/canyons/{uuid:" + ParameterHelper.UUID_PATTERN + "}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Canyon update(
      @PathVariable("uuid") UUID uuid, @RequestBody Canyon canyon, BindingResult errors)
      throws ServiceException, ValidationException {
    return super.update(uuid, canyon, errors);
  }
}
