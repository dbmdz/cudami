package de.digitalcollections.cudami.server.controller.identifiable.entity.geo.location;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.geo.location.GeoLocationService;
import de.digitalcollections.cudami.server.controller.ParameterHelper;
import de.digitalcollections.cudami.server.controller.identifiable.AbstractIdentifiableController;
import de.digitalcollections.model.identifiable.entity.geo.location.GeoLocation;
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
@Tag(name = "Geo location controller")
public class GeoLocationController extends AbstractIdentifiableController<GeoLocation> {

  private final GeoLocationService<GeoLocation> service;

  public GeoLocationController(GeoLocationService<GeoLocation> geoLocationservice) {
    this.service = geoLocationservice;
  }

  @Operation(summary = "count all geolocations")
  @GetMapping(
      value = {
        "/v6/geolocations/count",
        "/v5/geolocations/count",
        "/v2/geolocations/count",
        "/latest/geolocations/count"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public long count() throws ServiceException {
    return super.count();
  }

  @Operation(summary = "Delete a geolocation")
  @DeleteMapping(
      value = {"/v6/geolocations/{uuid:" + ParameterHelper.UUID_PATTERN + "}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity delete(
      @Parameter(example = "", description = "UUID of the geolocation") @PathVariable("uuid")
          UUID uuid)
      throws ConflictException, ServiceException {
    return super.delete(uuid);
  }

  @Operation(summary = "Get all geo locations as (paged, sorted, filtered) list")
  @GetMapping(
      value = {"/v6/geolocations"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<GeoLocation> find(
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
        "/v6/geolocations/identifier/**",
        "/v5/geolocations/identifier/**",
        "/v2/geolocations/identifier/**",
        "/latest/geolocations/identifier/**"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<GeoLocation> getByIdentifier(HttpServletRequest request)
      throws ServiceException, ValidationException {
    return super.getByIdentifier(request);
  }

  @Operation(summary = "Get a geolocation by namespace and id")
  @GetMapping(
      value = {
        "/v6/geolocations/identifier",
        "/v5/geolocations/identifier",
        "/v2/geolocations/identifier",
        "/latest/geolocations/identifier"
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

  @Operation(summary = "Get a geolocation by uuid")
  @GetMapping(
      value = {
        "/v6/geolocations/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/geolocations/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v2/geolocations/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/latest/geolocations/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<GeoLocation> getByUuid(
      @Parameter(
              example = "",
              description =
                  "UUID of the geolocation, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @Parameter(
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

  @Operation(summary = "Get languages of all geolocations")
  @GetMapping(
      value = {
        "/v6/geolocations/languages",
        "/v5/geolocations/languages",
        "/v3/geolocations/languages",
        "/latest/geolocations/languages"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Locale> getLanguages() throws ServiceException {
    return super.getLanguages();
  }

  @Override
  protected IdentifiableService<GeoLocation> getService() {
    return service;
  }

  @Operation(summary = "save a newly created geolocation")
  @PostMapping(
      value = {"/v6/geolocations", "/v5/geolocations", "/v2/geolocations", "/latest/geolocations"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public GeoLocation save(@RequestBody GeoLocation geoLocation, BindingResult errors)
      throws ServiceException, ValidationException {
    return super.save(geoLocation, errors);
  }

  @Operation(summary = "update a geolocation")
  @PutMapping(
      value = {
        "/v6/geolocations/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/geolocations/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v2/geolocations/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/latest/geolocations/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public GeoLocation update(
      @PathVariable("uuid") UUID uuid, @RequestBody GeoLocation geoLocation, BindingResult errors)
      throws ServiceException, ValidationException {
    return super.update(uuid, geoLocation, errors);
  }
}
