package de.digitalcollections.cudami.admin.controller.identifiable.entity;

import de.digitalcollections.cudami.admin.business.i18n.LanguageService;
import de.digitalcollections.cudami.admin.business.impl.validator.LabelNotBlankValidator;
import de.digitalcollections.cudami.admin.controller.ParameterHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiWebsitesClient;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.text.LocalizedText;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Controller for website management pages. */
@Controller
public class WebsitesController extends AbstractEntitiesController<Website, CudamiWebsitesClient> {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebsitesController.class);

  private final LabelNotBlankValidator labelNotBlankValidator;
  private final MessageSource messageSource;

  public WebsitesController(
      CudamiClient client,
      LanguageService languageService,
      MessageSource messageSource,
      LabelNotBlankValidator labelNotBlankValidator) {
    super(client.forWebsites(), languageService);
    this.labelNotBlankValidator = labelNotBlankValidator;
    this.messageSource = messageSource;
  }

  @GetMapping("/websites/new")
  public String create(Model model) throws TechnicalException {
    Website website = service.create();
    Locale defaultLanguage = languageService.getDefaultLanguage();
    website.setLabel(new LocalizedText(defaultLanguage, ""));
    model.addAttribute("website", website);

    List<Locale> existingLanguages = List.of(defaultLanguage);
    List<Locale> sortedLanguages = languageService.getAllLanguages();
    model.addAttribute("existingLanguages", existingLanguages);
    model.addAttribute("allLanguages", sortedLanguages);
    model.addAttribute("activeLanguage", defaultLanguage);

    model.addAttribute("mode", "create");
    return "websites/create-or-edit";
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

  @PostMapping("/websites/new")
  public String save(
      @ModelAttribute @Valid Website website,
      BindingResult results,
      Model model,
      SessionStatus status,
      RedirectAttributes redirectAttributes) {
    model.addAttribute("mode", "create");

    verifyBinding(results);

    labelNotBlankValidator.validate(website.getLabel(), results);

    if (results.hasErrors()) {
      return "websites/create-or-edit";
    }
    try {
      service.save(website);
      LOGGER.info("Successfully saved website");
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save website: ", e);
      String message =
          messageSource.getMessage("error.technical_error", null, LocaleContextHolder.getLocale());
      redirectAttributes.addFlashAttribute("error_message", message);
      return "redirect:/websites";
    }
    if (results.hasErrors()) {
      return "websites/create-or-edit";
    }
    status.setComplete();
    String message =
        messageSource.getMessage("msg.created_successfully", null, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute("success_message", message);
    return "redirect:/websites";
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
