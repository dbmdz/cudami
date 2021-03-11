package de.digitalcollections.cudami.admin.controller.identifiable.entity.geo.location;

import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.CudamiLocalesClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.cudami.client.identifiable.entity.geo.location.CudamiGeoLocationsClient;
import de.digitalcollections.model.identifiable.entity.geo.location.GeoLocation;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/** Controller for GeoLocations management pages. */
@Controller
public class GeoLocationsController extends AbstractController {

  private static final Logger LOGGER = LoggerFactory.getLogger(GeoLocationsController.class);

  private final LanguageSortingHelper languageSortingHelper;
  private final CudamiLocalesClient localeService;
  private final CudamiGeoLocationsClient service;

  @Autowired
  public GeoLocationsController(
      LanguageSortingHelper languageSortingHelper, CudamiClient cudamiClient) {
    this.languageSortingHelper = languageSortingHelper;
    this.localeService = cudamiClient.forLocales();
    this.service = cudamiClient.forGeoLocations();
  }

  @GetMapping("/api/geolocations/new")
  @ResponseBody
  public GeoLocation create() {
    return service.create();
  }

  @GetMapping("/api/geolocations")
  @ResponseBody
  public PageResponse<GeoLocation> findAll(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize)
      throws HttpException {
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize);
    return service.find(pageRequest);
  }

  @GetMapping("/geolocations")
  public String list(Model model) throws HttpException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    model.addAttribute(
        "existingLanguages",
        languageSortingHelper.sortLanguages(displayLocale, service.getLanguages()));
    return "geolocations/list";
  }

  @ModelAttribute("menu")
  protected String module() {
    return "geolocations";
  }
}
