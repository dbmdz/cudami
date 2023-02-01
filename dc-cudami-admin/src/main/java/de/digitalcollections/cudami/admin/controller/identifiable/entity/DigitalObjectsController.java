package de.digitalcollections.cudami.admin.controller.identifiable.entity;

import de.digitalcollections.cudami.admin.controller.ParameterHelper;
import de.digitalcollections.cudami.admin.controller.identifiable.AbstractIdentifiablesController;
import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiDigitalObjectsClient;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
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

/** Controller for digital objects management pages. */
@Controller
public class DigitalObjectsController
    extends AbstractIdentifiablesController<DigitalObject, CudamiDigitalObjectsClient> {

  public DigitalObjectsController(
      LanguageSortingHelper languageSortingHelper, CudamiClient client) {
    super(client.forDigitalObjects(), languageSortingHelper, client.forLocales());
  }

  @GetMapping("/digitalobjects")
  public String list(Model model) throws TechnicalException {
    model.addAttribute("existingLanguages", getExistingLanguagesFromService());

    String dataLanguage = getDataLanguage(null, localeService);
    model.addAttribute("dataLanguage", dataLanguage);

    return "digitalobjects/list";
  }

  @ModelAttribute("menu")
  protected String module() {
    return "digitalobjects";
  }

  @GetMapping("/digitalobjects/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  public String view(
      @PathVariable UUID uuid,
      @RequestParam(name = "dataLanguage", required = false) String targetDataLanguage,
      Model model)
      throws TechnicalException, ResourceNotFoundException {
    DigitalObject digitalObject = service.getByUuid(uuid);
    if (digitalObject == null) {
      throw new ResourceNotFoundException();
    }
    model.addAttribute("digitalObject", digitalObject);

    List<Locale> existingLanguages = getExistingLanguagesFromIdentifiable(digitalObject);
    String dataLanguage = getDataLanguage(targetDataLanguage, localeService);
    model
        .addAttribute("existingLanguages", existingLanguages)
        .addAttribute("dataLanguage", dataLanguage);

    Locale displayLocale = LocaleContextHolder.getLocale();

    List<Locale> existingCollectionsLanguages = service.getLanguagesOfCollections(uuid);
    model
        .addAttribute(
            "existingCollectionsLanguages",
            languageSortingHelper.sortLanguages(displayLocale, existingCollectionsLanguages))
        .addAttribute("dataLanguageCollections", getDataLanguage(null, localeService));

    List<Locale> existingProjectsLanguages = service.getLanguagesOfProjects(uuid);
    model
        .addAttribute(
            "existingProjectsLanguages",
            languageSortingHelper.sortLanguages(displayLocale, existingProjectsLanguages))
        .addAttribute("dataLanguageProjects", getDataLanguage(null, localeService));

    List<Locale> existingContainedDigitalObjectsLanguages =
        service.getLanguagesOfContainedDigitalObjects(uuid);
    model
        .addAttribute(
            "existingDigitalObjectsLanguages",
            languageSortingHelper.sortLanguages(
                displayLocale, existingContainedDigitalObjectsLanguages))
        .addAttribute("dataLanguageDigitalObjects", getDataLanguage(null, localeService));

    return "digitalobjects/view";
  }
}
