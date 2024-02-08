package de.digitalcollections.cudami.admin.controller.legal;

import de.digitalcollections.cudami.admin.business.i18n.LanguageService;
import de.digitalcollections.cudami.admin.business.impl.validator.LabelNotBlankValidator;
import de.digitalcollections.cudami.admin.controller.AbstractUniqueObjectController;
import de.digitalcollections.cudami.admin.controller.ParameterHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.legal.CudamiLicensesClient;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.legal.License;
import de.digitalcollections.model.text.LocalizedText;
import java.util.Collections;
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
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Controller for license management pages. */
@Controller
@SessionAttributes(value = {"license"})
public class LicensesController extends AbstractUniqueObjectController<License> {

  private static final Logger LOGGER = LoggerFactory.getLogger(LicensesController.class);

  private final LabelNotBlankValidator labelNotBlankValidator;
  private final MessageSource messageSource;

  public LicensesController(
      CudamiClient client,
      LanguageService languageService,
      MessageSource messageSource,
      LabelNotBlankValidator labelNotBlankValidator) {
    super(client.forLicenses(), languageService);
    this.labelNotBlankValidator = labelNotBlankValidator;
    this.messageSource = messageSource;
  }

  @GetMapping("/licenses/new")
  public String create(Model model) throws TechnicalException {
    License license = service.create();
    Locale defaultLanguage = languageService.getDefaultLanguage();
    license.setLabel(new LocalizedText(defaultLanguage, ""));
    model.addAttribute("license", license);

    List<Locale> existingLanguages = List.of(defaultLanguage);
    List<Locale> sortedLanguages = languageService.getAllLanguages();
    model.addAttribute("existingLanguages", existingLanguages);
    model.addAttribute("allLanguages", sortedLanguages);
    model.addAttribute("activeLanguage", defaultLanguage);

    model.addAttribute("mode", "create");
    return "licenses/create-or-edit";
  }

  @GetMapping("/licenses/{uuid:" + ParameterHelper.UUID_PATTERN + "}/edit")
  public String edit(
      @PathVariable UUID uuid,
      @RequestParam(name = "activeLanguage", required = false) Locale activeLanguage,
      Model model)
      throws TechnicalException, ResourceNotFoundException {
    License license = service.getByUuid(uuid);
    if (license == null) {
      throw new ResourceNotFoundException();
    }
    model.addAttribute("license", license);

    List<Locale> existingLanguages =
        languageService.getExistingLanguages(
            languageService.getDefaultLanguage(), license.getLabel());
    model.addAttribute("existingLanguages", existingLanguages);

    if (activeLanguage != null && existingLanguages.contains(activeLanguage)) {
      model.addAttribute("activeLanguage", activeLanguage);
    } else {
      model.addAttribute("activeLanguage", existingLanguages.get(0));
    }

    model.addAttribute("allLanguages", languageService.getAllLanguages());

    model.addAttribute("mode", "edit");
    return "licenses/create-or-edit";
  }

  @GetMapping("/licenses")
  public String list(Model model) throws TechnicalException {
    List<Locale> existingLanguages =
        languageService.getExistingLanguagesForLocales(
            ((CudamiLicensesClient) service).getLanguages());
    model.addAttribute("existingLanguages", existingLanguages);

    String dataLanguage = getDataLanguage(null, languageService);
    model.addAttribute("dataLanguage", dataLanguage);

    return "licenses/list";
  }

  @ModelAttribute("menu")
  protected String module() {
    return "licenses";
  }

  @PostMapping("/licenses/new")
  public String save(
      @ModelAttribute @Valid License license,
      BindingResult results,
      Model model,
      SessionStatus status,
      RedirectAttributes redirectAttributes) {
    model.addAttribute("mode", "create");

    verifyBinding(results);
    if (results.hasErrors()) {
      return "licenses/create-or-edit";
    }
    try {
      service.save(license);
      LOGGER.info("Successfully saved license");
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save license: ", e);
      String message =
          messageSource.getMessage("error.technical_error", null, LocaleContextHolder.getLocale());
      redirectAttributes.addFlashAttribute("error_message", message);
      return "redirect:/licenses";
    }
    if (results.hasErrors()) {
      return "licenses/create-or-edit";
    }
    status.setComplete();
    String message =
        messageSource.getMessage("msg.created_successfully", null, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute("success_message", message);
    return "redirect:/licenses";
  }

  @PostMapping(value = "/licenses/{uuid:" + ParameterHelper.UUID_PATTERN + "}/edit")
  public String update(
      @PathVariable UUID uuid,
      @ModelAttribute("formData") License licenseFormData,
      @ModelAttribute License license,
      BindingResult results,
      Model model,
      SessionStatus status,
      RedirectAttributes redirectAttributes)
      throws TechnicalException {
    model.addAttribute("mode", "edit");
    verifyBinding(results);

    // just update the fields, that were editable
    // needed session attribute independent "predicateFormData" because session
    // attributes just get
    // data set,
    // but do not remove hashmap entry (language, if tab is removed)
    license.setLabel(licenseFormData.getLabel());

    labelNotBlankValidator.validate(license.getLabel(), results);

    // TODO: move validate() to service layer on server side using new
    // ValidationException of dc
    // model?
    if (results.hasErrors()) {
      Locale defaultLanguage = languageService.getDefaultLanguage();
      model.addAttribute(
          "existingLanguages",
          languageService.getExistingLanguages(defaultLanguage, license.getLabel()));
      model.addAttribute("allLanguages", languageService.getAllLanguages());
      model.addAttribute("activeLanguage", defaultLanguage);
      return "licenses/create-or-edit";
    }

    try {
      service.update(uuid, license);
    } catch (TechnicalException e) {
      String message = "Cannot update license with uuid=" + uuid + ": " + e;
      LOGGER.error(message, e);
      redirectAttributes.addFlashAttribute("error_message", message);
      return "redirect:/licenses/" + uuid + "/edit";
    }

    status.setComplete();
    String message =
        messageSource.getMessage(
            "msg.changes_saved_successfully", null, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute("success_message", message);
    return "redirect:/licenses/" + uuid;
  }

  @GetMapping("/licenses/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  public String view(
      @PathVariable UUID uuid,
      @RequestParam(name = "dataLanguage", required = false) String targetDataLanguage,
      Model model)
      throws TechnicalException, ResourceNotFoundException {
    License license = service.getByUuid(uuid);
    if (license == null) {
      throw new ResourceNotFoundException();
    }

    List<Locale> existingLanguages = Collections.emptyList();
    LocalizedText label = license.getLabel();
    if (!CollectionUtils.isEmpty(label)) {
      Locale displayLocale = LocaleContextHolder.getLocale();
      existingLanguages = languageService.sortLanguages(displayLocale, label.getLocales());
    }

    String dataLanguage = targetDataLanguage;
    if (dataLanguage == null && languageService != null) {
      dataLanguage = languageService.getDefaultLanguage().getLanguage();
    }

    model
        .addAttribute("license", license)
        .addAttribute("existingLanguages", existingLanguages)
        .addAttribute("dataLanguage", dataLanguage);
    return "licenses/view";
  }
}
