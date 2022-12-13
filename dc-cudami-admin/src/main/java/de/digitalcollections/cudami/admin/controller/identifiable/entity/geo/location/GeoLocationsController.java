package de.digitalcollections.cudami.admin.controller.identifiable.entity.geo.location;

import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.entity.geo.location.CudamiGeoLocationsClient;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.geo.location.GeoLocation;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

/** Controller for GeoLocations management pages. */
@Controller
public class GeoLocationsController extends AbstractController {

  private static final Logger LOGGER = LoggerFactory.getLogger(GeoLocationsController.class);

  private final LanguageSortingHelper languageSortingHelper;
  private final CudamiGeoLocationsClient service;

  public GeoLocationsController(LanguageSortingHelper languageSortingHelper, CudamiClient client) {
    this.languageSortingHelper = languageSortingHelper;
    this.service = client.forGeoLocations();
  }

  @GetMapping("/geolocations")
  public String list(Model model) throws TechnicalException {
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

  @GetMapping("/geolocations/{uuid}")
  public String view(@PathVariable UUID uuid, Model model)
      throws TechnicalException, ResourceNotFoundException {
    GeoLocation geoLocation = service.getByUuid(uuid);
    if (geoLocation == null) {
      throw new ResourceNotFoundException();
    }
    Locale displayLocale = LocaleContextHolder.getLocale();
    List<Locale> existingLanguages =
        languageSortingHelper.sortLanguages(displayLocale, geoLocation.getLabel().getLocales());
    model
        .addAttribute("existingLanguages", existingLanguages)
        .addAttribute("geoLocation", geoLocation);
    return "geolocations/view";
  }
}
