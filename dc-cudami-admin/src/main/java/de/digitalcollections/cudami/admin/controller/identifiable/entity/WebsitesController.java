package de.digitalcollections.cudami.admin.controller.identifiable.entity;

import de.digitalcollections.cudami.admin.business.i18n.LanguageService;
import de.digitalcollections.cudami.admin.controller.ParameterHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiWebsitesClient;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.Website;
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
import org.springframework.web.bind.annotation.RequestParam;

/** Controller for website management pages. */
@Controller
public class WebsitesController extends AbstractEntitiesController<Website, CudamiWebsitesClient> {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebsitesController.class);

  public WebsitesController(LanguageService languageService, CudamiClient client) {
    super(client.forWebsites(), languageService);
  }

  @GetMapping("/websites/new")
  public String create(Model model) throws TechnicalException {
    model.addAttribute("activeLanguage", languageService.getDefaultLanguage());
    return "websites/create";
  }

  @GetMapping("/websites/{uuid:" + ParameterHelper.UUID_PATTERN + "}/edit")
  public String edit(
      @PathVariable UUID uuid,
      @RequestParam(name = "activeLanguage", required = false) Locale activeLanguage,
      Model model)
      throws TechnicalException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    Website website = service.getByUuid(uuid);
    List<Locale> existingLanguages =
        languageService.sortLanguages(displayLocale, website.getLabel().getLocales());

    if (activeLanguage != null && existingLanguages.contains(activeLanguage)) {
      model.addAttribute("activeLanguage", activeLanguage);
    } else {
      model.addAttribute("activeLanguage", existingLanguages.get(0));
    }
    model.addAttribute("existingLanguages", existingLanguages);
    model.addAttribute("url", website.getUrl());
    model.addAttribute("uuid", website.getUuid());

    return "websites/edit";
  }

  @GetMapping("/websites")
  public String list(Model model) throws TechnicalException {
    model.addAttribute("existingLanguages", getExistingLanguagesFromService());

    String dataLanguage = getDataLanguage(null, languageService);
    model.addAttribute("dataLanguage", dataLanguage);

    return "websites/list";
  }

  @ModelAttribute("menu")
  protected String module() {
    return "websites";
  }

  @GetMapping("/websites/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  public String view(
      @PathVariable UUID uuid,
      @RequestParam(name = "dataLanguage", required = false) String targetDataLanguage,
      Model model)
      throws TechnicalException, ResourceNotFoundException {
    Website website = service.getByUuid(uuid);
    if (website == null) {
      throw new ResourceNotFoundException();
    }

    List<Locale> existingLanguages = getExistingLanguagesFromIdentifiable(website);
    String dataLanguage = getDataLanguage(targetDataLanguage, languageService);
    model
        .addAttribute("existingLanguages", existingLanguages)
        .addAttribute("dataLanguage", dataLanguage);

    List<Locale> existingWebpageLanguages =
        getExistingLanguagesFromIdentifiables(website.getRootPages());
    model
        .addAttribute("existingWebpageLanguages", existingWebpageLanguages)
        .addAttribute("dataLanguageWebpages", getDataLanguage(null, languageService));

    model.addAttribute("website", website);
    return "websites/view";
  }
}
