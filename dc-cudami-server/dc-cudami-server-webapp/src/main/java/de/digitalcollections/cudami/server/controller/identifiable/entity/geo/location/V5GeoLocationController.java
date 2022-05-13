package de.digitalcollections.cudami.server.controller.identifiable.entity.geo.location;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.geo.location.GeoLocationService;
import de.digitalcollections.cudami.server.controller.CudamiControllerException;
import de.digitalcollections.cudami.server.controller.legacy.V5MigrationHelper;
import de.digitalcollections.model.identifiable.entity.geo.location.GeoLocation;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "V5 Geo location controller")
public class V5GeoLocationController {

  private static final Logger LOGGER = LoggerFactory.getLogger(V5GeoLocationController.class);

  private final GeoLocationService geoLocationService;

  private final ObjectMapper objectMapper;

  public V5GeoLocationController(GeoLocationService geoLocationservice, ObjectMapper objectMapper) {
    this.geoLocationService = geoLocationservice;
    this.objectMapper = objectMapper;
  }

  @Operation(summary = "get all geo locations")
  @GetMapping(
      value = {"/v5/geolocations", "/v2/geolocations", "/latest/geolocations"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "language", required = false) String language,
      @RequestParam(name = "initial", required = false) String initial,
      @RequestParam(name = "searchTerm", required = false) String searchTerm)
      throws CudamiControllerException {
    PageRequest searchPageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(V5MigrationHelper.migrate(sortBy));
      searchPageRequest.setSorting(sorting);
    }
    PageResponse<GeoLocation> pageResponse;
    if (language == null && initial == null) {
      pageResponse = geoLocationService.find(searchPageRequest);
    } else {
      pageResponse =
          geoLocationService.findByLanguageAndInitial(searchPageRequest, language, initial);
    }

    try {
      String result = V5MigrationHelper.migrate(pageResponse, objectMapper);
      return new ResponseEntity<>(result, HttpStatus.OK);
    } catch (JsonProcessingException e) {
      throw new CudamiControllerException(e);
    }
  }
}
