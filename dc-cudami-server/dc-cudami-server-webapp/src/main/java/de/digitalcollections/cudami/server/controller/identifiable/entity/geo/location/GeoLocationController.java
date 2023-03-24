package de.digitalcollections.cudami.server.controller.identifiable.entity.geo.location;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.geo.location.GeoLocationService;
import de.digitalcollections.cudami.server.controller.ParameterHelper;
import de.digitalcollections.cudami.server.controller.identifiable.AbstractIdentifiableController;
import de.digitalcollections.model.identifiable.entity.geo.location.GeoLocation;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
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
@Tag(name = "Geo location controller")
public class GeoLocationController extends AbstractIdentifiableController<GeoLocation> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GeoLocationController.class);

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
  public long count() {
    return service.count();
  }

  @Operation(summary = "Delete a geolocation")
  @DeleteMapping(
      value = {"/v6/geolocations/{uuid:" + ParameterHelper.UUID_PATTERN + "}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity delete(
      @Parameter(example = "", description = "UUID of the geolocation") @PathVariable("uuid")
          UUID uuid)
      throws ConflictException {
    boolean successful;
    try {
      successful = service.deleteByUuid(uuid);
    } catch (ServiceException e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
    return successful
        ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
        : new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Get all geo locations as (paged, sorted, filtered) list")
  @GetMapping(
      value = {"/v6/geolocations"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<GeoLocation> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "filter", required = false) List<FilterCriterion> filterCriteria) {
    PageRequest pageRequest =
        createPageRequest(GeoLocation.class, pageNumber, pageSize, sortBy, filterCriteria);
    return service.find(pageRequest);
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

    GeoLocation result;
    if (pLocale == null) {
      result = service.getByUuid(uuid);
    } else {
      result = service.getByUuidAndLocale(uuid, pLocale);
    }
    return new ResponseEntity<>(result, result != null ? HttpStatus.OK : HttpStatus.NOT_FOUND);
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
  public List<Locale> getLanguages() {
    return service.getLanguages();
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
    service.save(geoLocation);
    return geoLocation;
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
    assert Objects.equals(uuid, geoLocation.getUuid());
    service.update(geoLocation);
    return geoLocation;
  }
}
