package de.digitalcollections.cudami.server.controller.identifiable.agent;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.agent.GivenNameService;
import de.digitalcollections.cudami.server.controller.ParameterHelper;
import de.digitalcollections.cudami.server.controller.identifiable.AbstractIdentifiableController;
import de.digitalcollections.model.identifiable.agent.GivenName;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Given name controller")
public class GivenNameController extends AbstractIdentifiableController<GivenName> {

  private final GivenNameService service;

  public GivenNameController(GivenNameService givenNameService) {
    this.service = givenNameService;
  }

  @Override
  @Operation(summary = "Get all given names as (paged, sorted, filtered) list")
  @GetMapping(
      value = {"/v6/givennames"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<GivenName> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "filter", required = false) List<FilterCriterion> filterCriteria,
      @RequestParam(name = "filtering", required = false) Filtering filtering)
      throws ServiceException {
    return super.find(pageNumber, pageSize, sortBy, filterCriteria, filtering);
  }

  @Operation(
      summary = "Get a given name by namespace and id",
      description =
          "Separate namespace and id with a colon, e.g. foo:bar. It is also possible, to add a .json suffix, which will be ignored then")
  @GetMapping(
      value = {"/v6/givennames/identifier/**", "/v5/givennames/identifier/**"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Override
  public ResponseEntity<GivenName> getByIdentifier(HttpServletRequest request)
      throws ServiceException, ValidationException {
    return super.getByIdentifier(request);
  }

  @Operation(summary = "Get a givenname by namespace and id")
  @GetMapping(
      value = {"/v6/givennames/identifier", "/v5/givennames/identifier"},
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

  @Operation(summary = "Get a givenname by uuid")
  @GetMapping(
      value = {
        "/v6/givennames/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/givennames/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<GivenName> getByUuid(
      @Parameter(
              example = "",
              description =
                  "UUID of the givenname, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @Parameter(
              name = "pLocale",
              description =
                  "Desired locale, e.g. <tt>de_DE</tt>. If unset, contents in all languages will be returned")
          @RequestParam(name = "pLocale", required = false)
          Locale pLocale)
      throws ServiceException {

    GivenName result;
    if (pLocale == null) {
      return super.getByUuid(uuid);
    } else {
      return super.getByUuidAndLocale(uuid, pLocale);
    }
  }

  @Override
  @Operation(summary = "Get languages of all given names")
  @GetMapping(
      value = {"/v6/givennames/languages"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Locale> getLanguages() throws ServiceException {
    return super.getLanguages();
  }

  @Override
  protected IdentifiableService<GivenName> getService() {
    return service;
  }

  @Override
  @Operation(summary = "save a newly created givenname")
  @PostMapping(
      value = {"/v6/givennames", "/v5/givennames"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public GivenName save(@RequestBody GivenName givenName, BindingResult errors)
      throws ServiceException, ValidationException {
    return super.save(givenName, errors);
  }

  @Override
  @Operation(summary = "update a givenname")
  @PutMapping(
      value = {
        "/v6/givennames/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/givennames/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public GivenName update(
      @PathVariable("uuid") UUID uuid, @RequestBody GivenName givenName, BindingResult errors)
      throws ServiceException, ValidationException {
    return super.update(uuid, givenName, errors);
  }
}
