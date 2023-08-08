package de.digitalcollections.cudami.admin.controller.identifiable.entity.geo.location;

import de.digitalcollections.cudami.admin.business.i18n.LanguageService;
import de.digitalcollections.cudami.admin.controller.ParameterHelper;
import de.digitalcollections.cudami.admin.controller.identifiable.entity.AbstractEntitiesController;
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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/** Controller for GeoLocations management pages. */
@Controller
public class GeoLocationsController
    extends AbstractEntitiesController<GeoLocation, CudamiGeoLocationsClient> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GeoLocationsController.class);

  public GeoLocationsController(CudamiClient client, LanguageService languageService) {
    super(client.forGeoLocations(), client, languageService);
  }

  @GetMapping("/geolocations")
  public String list(Model model) throws TechnicalException {
    model.addAttribute("existingLanguages", getExistingLanguagesFromService());

    String dataLanguage = getDataLanguage(null, languageService);
    model.addAttribute("dataLanguage", dataLanguage);

    return "geolocations/list";
  }

  @ModelAttribute("menu")
  protected String module() {
    return "geolocations";
  }

  @GetMapping("/geolocations/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  public String view(
      @PathVariable UUID uuid,
      @RequestParam(name = "dataLanguage", required = false) String targetDataLanguage,
      Model model)
      throws TechnicalException, ResourceNotFoundException {
    GeoLocation geoLocation = service.getByUuid(uuid);
    if (geoLocation == null) {
      throw new ResourceNotFoundException();
    }
    model.addAttribute("geoLocation", geoLocation);

    List<Locale> existingLanguages = getExistingLanguagesFromIdentifiable(geoLocation);
    String dataLanguage = getDataLanguage(targetDataLanguage, existingLanguages, languageService);
    model
        .addAttribute("existingLanguages", existingLanguages)
        .addAttribute("dataLanguage", dataLanguage);

    return "geolocations/view";
  }
}
