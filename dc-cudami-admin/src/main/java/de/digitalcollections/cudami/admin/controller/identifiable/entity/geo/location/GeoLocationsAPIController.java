package de.digitalcollections.cudami.admin.controller.identifiable.entity.geo.location;

import de.digitalcollections.cudami.admin.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.admin.controller.identifiable.AbstractIdentifiablesController;
import de.digitalcollections.cudami.admin.model.bootstraptable.BTResponse;
import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.entity.geo.location.CudamiGeoLocationsClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.geo.location.GeoLocation;
import de.digitalcollections.model.list.paging.PageResponse;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/** Controller for all public "GeoLocations" endpoints (API). */
@RestController
public class GeoLocationsAPIController
    extends AbstractIdentifiablesController<GeoLocation, CudamiGeoLocationsClient> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GeoLocationsAPIController.class);

  public GeoLocationsAPIController(
      LanguageSortingHelper languageSortingHelper, CudamiClient client) {
    super(client.forGeoLocations(), languageSortingHelper, client.forLocales());
  }

  @GetMapping("/api/geolocations/new")
  @ResponseBody
  public GeoLocation create() throws TechnicalException {
    return service.create();
  }

  @SuppressFBWarnings
  @GetMapping("/api/geolocations")
  @ResponseBody
  public BTResponse<GeoLocation> find(
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(name = "limit", required = false, defaultValue = "1") int limit,
      @RequestParam(name = "search", required = false) String searchTerm,
      @RequestParam(name = "sort", required = false, defaultValue = "url") String sort,
      @RequestParam(name = "order", required = false, defaultValue = "asc") String order,
      @RequestParam(name = "dataLanguage", required = false) String dataLanguage)
      throws TechnicalException, ServiceException {
    PageResponse<GeoLocation> pageResponse =
        super.find(localeService, service, offset, limit, searchTerm, sort, order, dataLanguage);
    return new BTResponse<>(pageResponse);
  }
}
