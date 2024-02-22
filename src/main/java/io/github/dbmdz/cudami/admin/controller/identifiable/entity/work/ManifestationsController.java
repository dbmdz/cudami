package io.github.dbmdz.cudami.admin.controller.identifiable.entity.work;

import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.entity.work.CudamiManifestationsClient;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.manifestation.Manifestation;
import io.github.dbmdz.cudami.admin.business.i18n.LanguageService;
import io.github.dbmdz.cudami.admin.controller.ParameterHelper;
import io.github.dbmdz.cudami.admin.controller.identifiable.entity.AbstractEntitiesController;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/** Controller for manifestation management pages. */
@Controller
public class ManifestationsController
    extends AbstractEntitiesController<Manifestation, CudamiManifestationsClient> {

  public ManifestationsController(CudamiClient client, LanguageService languageService) {
    super(client.forManifestations(), client, languageService);
  }

  @GetMapping("/manifestations")
  public String list(Model model) throws TechnicalException {
    model.addAttribute("existingLanguages", getExistingLanguagesFromService());

    String dataLanguage = getDataLanguage(null, languageService);
    model.addAttribute("dataLanguage", dataLanguage);

    return "manifestations/list";
  }

  @ModelAttribute("menu")
  protected String module() {
    return "manifestations";
  }

  @GetMapping("/manifestations/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  public String view(
      @PathVariable UUID uuid,
      @RequestParam(name = "dataLanguage", required = false) String targetDataLanguage,
      Model model)
      throws TechnicalException, ResourceNotFoundException {
    Manifestation manifestation = service.getByUuid(uuid);
    if (manifestation == null) {
      throw new ResourceNotFoundException();
    }
    model.addAttribute("manifestation", manifestation);

    List<Locale> existingLanguages = getExistingLanguagesFromIdentifiable(manifestation);
    String dataLanguage = getDataLanguage(targetDataLanguage, existingLanguages, languageService);
    model
        .addAttribute("existingLanguages", existingLanguages)
        .addAttribute("dataLanguage", dataLanguage);

    Locale displayLocale = LocaleContextHolder.getLocale();
    List<Locale> existingItemsLanguages =
        languageService.sortLanguages(
            displayLocale, ((CudamiManifestationsClient) service).getLanguagesOfItems(uuid));
    String dataLanguageItems =
        getDataLanguage(targetDataLanguage, existingItemsLanguages, languageService);
    model
        .addAttribute("existingItemsLanguages", existingItemsLanguages)
        .addAttribute("dataLanguageItems", dataLanguageItems);

    return "manifestations/view";
  }
}
