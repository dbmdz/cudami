package de.digitalcollections.cudami.server.controller.identifiable.entity.geo.location;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.geo.location.GeoLocationService;
import de.digitalcollections.model.identifiable.entity.geo.location.GeoLocation;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.Sorting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties.Pageable;
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
@Tag(name = "Geo location controller")
public class GeoLocationController {

  private static final Logger LOGGER = LoggerFactory.getLogger(GeoLocationController.class);

  private final GeoLocationService geoLocationService;

  public GeoLocationController(GeoLocationService geoLocationservice) {
    this.geoLocationService = geoLocationservice;
  }

  @Operation(summary = "count all geolocations")
  @GetMapping(
      value = {"/latest/geolocations/count", "/v2/geolocations/count"},
      produces = "application/json")
  public long count() {
    return geoLocationService.count();
  }

  @Operation(summary = "get all geo locations")
  @GetMapping(
      value = {"/latest/geolocations", "/v2/geolocations"},
      produces = "application/json")
  public PageResponse<GeoLocation> findAll(
      Pageable pageable,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "language", required = false) String language,
      @RequestParam(name = "initial", required = false) String initial,
      @RequestParam(name = "searchTerm", required = false) String searchTerm) {
    SearchPageRequest searchPageRequest = new SearchPageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      searchPageRequest.setSorting(sorting);
    }
    if (language == null && initial == null) {
      return geoLocationService.find(searchPageRequest);
    }
    return geoLocationService.findByLanguageAndInitial(searchPageRequest, language, initial);
  }

  @Operation(
      summary =
          "get a geolocation as JSON or XML, depending on extension or <tt>format</tt> request parameter or accept header")
  @GetMapping(
      value = {"/latest/geolocations/{uuid}", "/v2/geolocations/{uuid}"},
      produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  public ResponseEntity<GeoLocation> get(
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
      throws IdentifiableServiceException {

    GeoLocation result;
    if (pLocale == null) {
      result = geoLocationService.get(uuid);
    } else {
      result = geoLocationService.get(uuid, pLocale);
    }
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @Operation(
      summary =
          "get a geolocation as JSON or XML, depending on extension or <tt>format</tt> request parameter or accept header")
  @GetMapping(
      value = {"/latest/geolocations/identifier", "/v2/geolocations/identifier"},
      produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  public ResponseEntity<GeoLocation> getByIdentifier(
      @RequestParam(name = "namespace", required = true) String namespace,
      @RequestParam(name = "id", required = true) String id)
      throws IdentifiableServiceException {
    GeoLocation result = geoLocationService.getByIdentifier(namespace, id);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @Operation(summary = "Get languages of all geolocations")
  @GetMapping(
      value = {"/latest/geolocations/languages", "/v3/geolocations/languages"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Locale> getLanguages() {
    return geoLocationService.getLanguages();
  }

  @Operation(summary = "save a newly created geolocation")
  @PostMapping(
      value = {"/latest/geolocations", "/v2/geolocations"},
      produces = "application/json")
  public GeoLocation save(@RequestBody GeoLocation geoLocation, BindingResult errors)
      throws IdentifiableServiceException {
    return geoLocationService.save(geoLocation);
  }

  @Operation(summary = "update a geolocation")
  @PutMapping(
      value = {"/latest/geolocations/{uuid}", "/v2/geolocations/{uuid}"},
      produces = "application/json")
  public GeoLocation update(
      @PathVariable("uuid") UUID uuid, @RequestBody GeoLocation geoLocation, BindingResult errors)
      throws IdentifiableServiceException {
    assert Objects.equals(uuid, geoLocation.getUuid());
    return geoLocationService.update(geoLocation);
  }
}
