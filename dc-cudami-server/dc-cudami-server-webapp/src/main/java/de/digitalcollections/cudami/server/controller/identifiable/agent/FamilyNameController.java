package de.digitalcollections.cudami.server.controller.identifiable.agent;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.agent.FamilyNameService;
import de.digitalcollections.cudami.server.controller.ParameterHelper;
import de.digitalcollections.cudami.server.controller.identifiable.AbstractIdentifiableController;
import de.digitalcollections.model.identifiable.agent.FamilyName;
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
@Tag(name = "Family name controller")
public class FamilyNameController extends AbstractIdentifiableController<FamilyName> {

  private final FamilyNameService service;

  public FamilyNameController(FamilyNameService familyNameservice) {
    this.service = familyNameservice;
  }

  @Override
  @Operation(summary = "Get all family names as (paged, sorted, filtered) list")
  @GetMapping(
      value = {"/v6/familynames"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<FamilyName> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "filter", required = false) List<FilterCriterion> filterCriteria,
      @RequestParam(name = "filtering", required = false) Filtering filtering)
      throws ServiceException {
    return super.find(pageNumber, pageSize, sortBy, filterCriteria, filtering);
  }

  @Operation(
      summary = "Get a family name by namespace and id",
      description =
          "Separate namespace and id with a colon, e.g. foo:bar. It is also possible, to add a .json suffix, which will be ignored then")
  @GetMapping(
      value = {"/v6/familynames/identifier/**", "/v5/familynames/identifier/**"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Override
  public ResponseEntity<FamilyName> getByIdentifier(HttpServletRequest request)
      throws ServiceException, ValidationException {
    return super.getByIdentifier(request);
  }

  @Operation(summary = "Get a familyname by namespace and id")
  @GetMapping(
      value = {"/v6/familynames/identifier", "/v5/familynames/identifier"},
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

  @Operation(summary = "Get a familyname by uuid")
  @GetMapping(
      value = {
        "/v6/familynames/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/familynames/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<FamilyName> getByUuid(
      @Parameter(
              example = "",
              description =
                  "UUID of the familyname, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @Parameter(
              example = "",
              name = "pLocale",
              description =
                  "Desired locale, e.g. <tt>de_DE</tt>. If unset, contents in all languages will be returned")
          @RequestParam(name = "pLocale", required = false)
          Locale pLocale)
      throws ServiceException {
    if (pLocale == null) {
      return super.getByUuid(uuid);
    } else {
      return super.getByUuidAndLocale(uuid, pLocale);
    }
  }

  @Override
  @Operation(summary = "Get languages of all family names")
  @GetMapping(
      value = {"/v6/familynames/languages"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Locale> getLanguages() throws ServiceException {
    return super.getLanguages();
  }

  @Override
  protected IdentifiableService<FamilyName> getService() {
    return service;
  }

  @Override
  @Operation(summary = "save a newly created family")
  @PostMapping(
      value = {"/v6/familynames", "/v5/familynames"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public FamilyName save(@RequestBody FamilyName familyName, BindingResult errors)
      throws ServiceException, ValidationException {
    return super.save(familyName, errors);
  }

  @Override
  @Operation(summary = "update a familyname")
  @PutMapping(
      value = {
        "/v6/familynames/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/familynames/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public FamilyName update(
      @PathVariable("uuid") UUID uuid, @RequestBody FamilyName familyName, BindingResult errors)
      throws ServiceException, ValidationException {
    return super.update(uuid, familyName, errors);
  }
}
