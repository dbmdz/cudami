package io.github.dbmdz.cudami.admin.controller.identifiable.entity.agent;

import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.agent.CudamiFamilyNamesClient;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.agent.FamilyName;
import de.digitalcollections.model.text.LocalizedText;
import io.github.dbmdz.cudami.admin.business.i18n.LanguageService;
import io.github.dbmdz.cudami.admin.business.impl.validator.LabelNotBlankValidator;
import io.github.dbmdz.cudami.admin.controller.ParameterHelper;
import io.github.dbmdz.cudami.admin.controller.identifiable.AbstractIdentifiablesController;
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
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Controller for family names management pages. */
@Controller
@SessionAttributes(value = {"familyName"})
public class FamilynamesController
    extends AbstractIdentifiablesController<FamilyName, CudamiFamilyNamesClient> {

  private static final Logger LOGGER = LoggerFactory.getLogger(FamilynamesController.class);

  private final LabelNotBlankValidator labelNotBlankValidator;
  private final MessageSource messageSource;

  public FamilynamesController(
      CudamiClient client,
      LanguageService languageService,
      MessageSource messageSource,
      LabelNotBlankValidator labelNotBlankValidator) {
    super(client.forFamilyNames(), client, languageService);
    this.labelNotBlankValidator = labelNotBlankValidator;
    this.messageSource = messageSource;
  }

  @GetMapping("/familynames/new")
  public String create(Model model) throws TechnicalException {
    FamilyName familyName = service.create();
    Locale defaultLanguage = languageService.getDefaultLanguage();
    familyName.setLabel(new LocalizedText(defaultLanguage, ""));
    model.addAttribute("familyName", familyName);
    List<Locale> existingLanguages = List.of(defaultLanguage);

    List<Locale> sortedLanguages = languageService.getAllLanguages();

    model.addAttribute("existingLanguages", existingLanguages);
    model.addAttribute("allLanguages", sortedLanguages);
    model.addAttribute("activeLanguage", defaultLanguage);

    model.addAttribute("mode", "create");
    return "familynames/create-or-edit";
  }

  @GetMapping("/familynames/{uuid:" + ParameterHelper.UUID_PATTERN + "}/edit")
  public String edit(
      @PathVariable UUID uuid,
      @RequestParam(name = "activeLanguage", required = false) Locale activeLanguage,
      Model model)
      throws TechnicalException {
    FamilyName familyName = service.getByUuid(uuid);
    model.addAttribute("familyName", familyName);

    List<Locale> existingLanguages =
        languageService.getExistingLanguages(
            languageService.getDefaultLanguage(), familyName.getLabel());
    model.addAttribute("existingLanguages", existingLanguages);

    if (activeLanguage != null && existingLanguages.contains(activeLanguage)) {
      model.addAttribute("activeLanguage", activeLanguage);
    } else {
      model.addAttribute("activeLanguage", existingLanguages.get(0));
    }

    List<Locale> sortedLanguages = languageService.getAllLanguages();
    model.addAttribute("allLanguages", sortedLanguages);

    model.addAttribute("mode", "edit");
    return "familynames/create-or-edit";
  }

  @GetMapping("/familynames")
  public String list(Model model) throws TechnicalException {
    model.addAttribute("existingLanguages", getExistingLanguagesFromService());

    String dataLanguage = getDataLanguage(null, languageService);
    model.addAttribute("dataLanguage", dataLanguage);

    return "familynames/list";
  }

  @ModelAttribute("menu")
  protected String module() {
    return "familynames";
  }

  @PostMapping("/familynames/new")
  public String save(
      @ModelAttribute("formData") FamilyName familyNameFormData,
      @ModelAttribute @Valid FamilyName familyName,
      BindingResult results,
      Model model,
      SessionStatus status,
      RedirectAttributes redirectAttributes)
      throws TechnicalException {
    model.addAttribute("mode", "create");
    verifyBinding(results);

    // set hashmap fields from form data:
    // needed session attribute independent "familyNameFormData" because session
    // attributes just get
    // data set,
    // but do not remove hashmap entry (language, if tab is removed)
    familyName.setLabel(familyNameFormData.getLabel());
    familyName.setDescription(familyNameFormData.getDescription());

    validate(familyName, results);
    // TODO: move validate() to service layer on server side using new
    // ValidationException of dc
    // model?
    if (results.hasErrors()) {
      Locale defaultLanguage = languageService.getDefaultLanguage();
      model.addAttribute(
          "existingLanguages",
          languageService.getExistingLanguages(defaultLanguage, familyName.getLabel()));
      model.addAttribute("allLanguages", languageService.getAllLanguages());
      model.addAttribute("activeLanguage", defaultLanguage);
      return "familynames/create-or-edit";
    }
    FamilyName familyNameDB = null;
    try {
      familyNameDB = service.save(familyName);
      LOGGER.info("Successfully saved familyName");
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save familyName: ", e);
      String message =
          messageSource.getMessage("error.technical_error", null, LocaleContextHolder.getLocale());
      redirectAttributes.addFlashAttribute("error_message", message);
      return "redirect:/familynames";
    }
    status.setComplete();
    String message =
        messageSource.getMessage("msg.created_successfully", null, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute("success_message", message);
    return "redirect:/familynames/" + familyNameDB.getUuid().toString();
  }

  private void validate(Identifiable identifiable, BindingResult results) {
    labelNotBlankValidator.validate(identifiable.getLabel(), results);
  }

  @PostMapping(value = "/familynames/{pathUuid}/edit")
  public String update(
      @PathVariable UUID pathUuid,
      @ModelAttribute("formData") FamilyName familyNameFormData,
      @ModelAttribute FamilyName familyName,
      BindingResult results,
      Model model,
      SessionStatus status,
      RedirectAttributes redirectAttributes)
      throws TechnicalException {
    model.addAttribute("mode", "edit");
    verifyBinding(results);

    // just update the fields, that were editable
    // needed session attribute independent "familyNameFormData" because session
    // attributes just get
    // data set,
    // but do not remove hashmap entry (language, if tab is removed)
    familyName.setLabel(familyNameFormData.getLabel());
    familyName.setDescription(familyNameFormData.getDescription());

    validate(familyName, results);
    // TODO: move validate() to service layer on server side using new
    // ValidationException of dc
    // model?
    if (results.hasErrors()) {
      Locale defaultLanguage = languageService.getDefaultLanguage();
      model.addAttribute(
          "existingLanguages",
          languageService.getExistingLanguages(defaultLanguage, familyName.getLabel()));
      model.addAttribute("allLanguages", languageService.getAllLanguages());
      model.addAttribute("activeLanguage", defaultLanguage);
      return "familynames/create-or-edit";
    }

    try {
      service.update(pathUuid, familyName);
    } catch (TechnicalException e) {
      String message = "Cannot update familyName with uuid=" + pathUuid + ": " + e;
      LOGGER.error(message, e);
      redirectAttributes.addFlashAttribute("error_message", message);
      return "redirect:/familynames/" + pathUuid + "/edit";
    }

    status.setComplete();
    String message =
        messageSource.getMessage(
            "msg.changes_saved_successfully", null, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute("success_message", message);
    return "redirect:/familynames/" + pathUuid;
  }

  @GetMapping("/familynames/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  public String view(
      @PathVariable UUID uuid,
      @RequestParam(name = "dataLanguage", required = false) String targetDataLanguage,
      Model model)
      throws TechnicalException, ResourceNotFoundException {
    FamilyName familyName = service.getByUuid(uuid);
    if (familyName == null) {
      throw new ResourceNotFoundException();
    }
    model.addAttribute("familyName", familyName);

    List<Locale> existingLanguages = getExistingLanguagesFromIdentifiable(familyName);
    String dataLanguage = getDataLanguage(targetDataLanguage, existingLanguages, languageService);
    model
        .addAttribute("existingLanguages", existingLanguages)
        .addAttribute("dataLanguage", dataLanguage);

    return "familynames/view";
  }
}
