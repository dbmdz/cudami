package de.digitalcollections.cudami.admin.controller.identifiable.entity;

import de.digitalcollections.cudami.admin.controller.AbstractPagingAndSortingController;
import de.digitalcollections.cudami.admin.controller.ParameterHelper;
import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.CudamiLocalesClient;
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

/** Controller for digital objects management pages. */
@Controller
public class DigitalObjectsController extends AbstractPagingAndSortingController<DigitalObject> {

  private final LanguageSortingHelper languageSortingHelper;
  private final CudamiLocalesClient localeService;
  private final CudamiDigitalObjectsClient service;

  public DigitalObjectsController(
      LanguageSortingHelper languageSortingHelper, CudamiClient client) {
    this.languageSortingHelper = languageSortingHelper;
    this.localeService = client.forLocales();
    this.service = client.forDigitalObjects();
  }

  @GetMapping("/digitalobjects")
  public String list(Model model) throws TechnicalException {
    List<Locale> existingLanguages =
        getExistingLanguages(service.getLanguages(), languageSortingHelper);
    model.addAttribute("existingLanguages", existingLanguages);

    String dataLanguage = getDataLanguage(null, localeService);
    model.addAttribute("dataLanguage", dataLanguage);

    return "digitalobjects/list";
  }

  @ModelAttribute("menu")
  protected String module() {
    return "digitalobjects";
  }

  @GetMapping("/digitalobjects/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  public String view(@PathVariable UUID uuid, Model model)
      throws TechnicalException, ResourceNotFoundException {
    DigitalObject digitalObject = service.getByUuid(uuid);
    if (digitalObject == null) {
      throw new ResourceNotFoundException();
    }

    Locale displayLocale = LocaleContextHolder.getLocale();
    List<Locale> existingLanguages =
        languageSortingHelper.sortLanguages(displayLocale, digitalObject.getLabel().getLocales());
    List<Locale> existingCollectionLanguages =
        languageSortingHelper.sortLanguages(displayLocale, service.getLanguagesOfCollections(uuid));
    List<Locale> existingProjectLanguages =
        languageSortingHelper.sortLanguages(displayLocale, service.getLanguagesOfProjects(uuid));
    List<Locale> existingContainedDigitalObjectLanguages =
        languageSortingHelper.sortLanguages(
            displayLocale, service.getLanguagesOfContainedDigitalObjects(uuid));

    model
        .addAttribute("defaultLanguage", localeService.getDefaultLanguage().getLanguage())
        .addAttribute("digitalObject", digitalObject)
        .addAttribute("existingLanguages", existingLanguages)
        .addAttribute("existingCollectionLanguages", existingCollectionLanguages)
        .addAttribute(
            "existingContainedDigitalObjectLanguages", existingContainedDigitalObjectLanguages)
        .addAttribute("existingProjectLanguages", existingProjectLanguages);
    return "digitalobjects/view";
  }
}
