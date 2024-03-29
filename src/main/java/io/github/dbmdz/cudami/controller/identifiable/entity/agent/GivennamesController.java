package io.github.dbmdz.cudami.controller.identifiable.entity.agent;

import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.agent.CudamiGivenNamesClient;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.agent.GivenName;
import de.digitalcollections.model.text.LocalizedText;
import io.github.dbmdz.cudami.business.i18n.LanguageService;
import io.github.dbmdz.cudami.business.impl.validator.LabelNotBlankValidator;
import io.github.dbmdz.cudami.controller.ParameterHelper;
import io.github.dbmdz.cudami.controller.identifiable.AbstractIdentifiablesController;
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

/** Controller for given names management pages. */
@Controller
@SessionAttributes(value = {"givenName"})
public class GivennamesController
    extends AbstractIdentifiablesController<GivenName, CudamiGivenNamesClient> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GivennamesController.class);

  private final LabelNotBlankValidator labelNotBlankValidator;
  private final MessageSource messageSource;

  public GivennamesController(
      CudamiClient client,
      LanguageService languageService,
      MessageSource messageSource,
      LabelNotBlankValidator labelNotBlankValidator) {
    super(client.forGivenNames(), client, languageService);
    this.labelNotBlankValidator = labelNotBlankValidator;
    this.messageSource = messageSource;
  }

  @GetMapping("/givennames/new")
  public String create(Model model) throws TechnicalException {
    GivenName givenName = service.create();
    Locale defaultLanguage = languageService.getDefaultLanguage();
    givenName.setLabel(new LocalizedText(defaultLanguage, ""));
    model.addAttribute("givenName", givenName);
    List<Locale> existingLanguages = List.of(defaultLanguage);

    List<Locale> sortedLanguages = languageService.getAllLanguages();

    model.addAttribute("existingLanguages", existingLanguages);
    model.addAttribute("allLanguages", sortedLanguages);
    model.addAttribute("activeLanguage", defaultLanguage);

    model.addAttribute("mode", "create");
    return "givennames/create-or-edit";
  }

  @GetMapping("/givennames/{uuid:" + ParameterHelper.UUID_PATTERN + "}/edit")
  public String edit(
      @PathVariable UUID uuid,
      @RequestParam(name = "activeLanguage", required = false) Locale activeLanguage,
      Model model)
      throws TechnicalException {
    GivenName givenName = service.getByUuid(uuid);
    model.addAttribute("givenName", givenName);

    List<Locale> existingLanguages =
        languageService.getExistingLanguages(
            languageService.getDefaultLanguage(), givenName.getLabel());
    model.addAttribute("existingLanguages", existingLanguages);

    if (activeLanguage != null && existingLanguages.contains(activeLanguage)) {
      model.addAttribute("activeLanguage", activeLanguage);
    } else {
      model.addAttribute("activeLanguage", existingLanguages.get(0));
    }

    List<Locale> sortedLanguages = languageService.getAllLanguages();
    model.addAttribute("allLanguages", sortedLanguages);

    model.addAttribute("mode", "edit");
    return "givennames/create-or-edit";
  }

  @GetMapping("/givennames")
  public String list(Model model) throws TechnicalException {
    model.addAttribute("existingLanguages", getExistingLanguagesFromService());

    String dataLanguage = getDataLanguage(null, languageService);
    model.addAttribute("dataLanguage", dataLanguage);

    return "givennames/list";
  }

  @ModelAttribute("menu")
  protected String module() {
    return "givennames";
  }

  @PostMapping("/givennames/new")
  public String save(
      @ModelAttribute("formData") GivenName givenNameFormData,
      @ModelAttribute @Valid GivenName givenName,
      BindingResult results,
      Model model,
      SessionStatus status,
      RedirectAttributes redirectAttributes)
      throws TechnicalException {
    model.addAttribute("mode", "create");
    verifyBinding(results);

    // set hashmap fields from form data:
    // needed session attribute independent "givenNameFormData" because session
    // attributes just get
    // data set,
    // but do not remove hashmap entry (language, if tab is removed)
    givenName.setLabel(givenNameFormData.getLabel());
    givenName.setDescription(givenNameFormData.getDescription());

    validate(givenName, results);
    // TODO: move validate() to service layer on server side using new
    // ValidationException of dc
    // model?
    if (results.hasErrors()) {
      Locale defaultLanguage = languageService.getDefaultLanguage();
      model.addAttribute(
          "existingLanguages",
          languageService.getExistingLanguages(defaultLanguage, givenName.getLabel()));
      model.addAttribute("allLanguages", languageService.getAllLanguages());
      model.addAttribute("activeLanguage", defaultLanguage);
      return "givennames/create-or-edit";
    }
    GivenName givenNameDB = null;
    try {
      givenNameDB = service.save(givenName);
      LOGGER.info("Successfully saved givenName");
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save givenName: ", e);
      String message =
          messageSource.getMessage("error.technical_error", null, LocaleContextHolder.getLocale());
      redirectAttributes.addFlashAttribute("error_message", message);
      return "redirect:/givennames";
    }
    status.setComplete();
    String message =
        messageSource.getMessage("msg.created_successfully", null, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute("success_message", message);
    return "redirect:/givennames/" + givenNameDB.getUuid().toString();
  }

  private void validate(Identifiable identifiable, BindingResult results) {
    labelNotBlankValidator.validate(identifiable.getLabel(), results);
  }

  @PostMapping(value = "/givennames/{pathUuid}/edit")
  public String update(
      @PathVariable UUID pathUuid,
      @ModelAttribute("formData") GivenName givenNameFormData,
      @ModelAttribute GivenName givenName,
      BindingResult results,
      Model model,
      SessionStatus status,
      RedirectAttributes redirectAttributes)
      throws TechnicalException {
    model.addAttribute("mode", "edit");
    verifyBinding(results);

    // just update the fields, that were editable
    // needed session attribute independent "givenNameFormData" because session
    // attributes just get
    // data set,
    // but do not remove hashmap entry (language, if tab is removed)
    givenName.setLabel(givenNameFormData.getLabel());
    givenName.setDescription(givenNameFormData.getDescription());

    validate(givenName, results);
    // TODO: move validate() to service layer on server side using new
    // ValidationException of dc
    // model?
    if (results.hasErrors()) {
      Locale defaultLanguage = languageService.getDefaultLanguage();
      model.addAttribute(
          "existingLanguages",
          languageService.getExistingLanguages(defaultLanguage, givenName.getLabel()));
      model.addAttribute("allLanguages", languageService.getAllLanguages());
      model.addAttribute("activeLanguage", defaultLanguage);
      return "givennames/create-or-edit";
    }

    try {
      service.update(pathUuid, givenName);
    } catch (TechnicalException e) {
      String message = "Cannot update givenName with uuid=" + pathUuid + ": " + e;
      LOGGER.error(message, e);
      redirectAttributes.addFlashAttribute("error_message", message);
      return "redirect:/givennames/" + pathUuid + "/edit";
    }

    status.setComplete();
    String message =
        messageSource.getMessage(
            "msg.changes_saved_successfully", null, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute("success_message", message);
    return "redirect:/givennames/" + pathUuid;
  }

  @GetMapping("/givennames/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  public String view(
      @PathVariable UUID uuid,
      @RequestParam(name = "dataLanguage", required = false) String targetDataLanguage,
      Model model)
      throws TechnicalException, ResourceNotFoundException {
    GivenName givenName = service.getByUuid(uuid);
    if (givenName == null) {
      throw new ResourceNotFoundException();
    }
    model.addAttribute("givenName", givenName);

    List<Locale> existingLanguages = getExistingLanguagesFromIdentifiable(givenName);
    String dataLanguage = getDataLanguage(targetDataLanguage, existingLanguages, languageService);
    model
        .addAttribute("existingLanguages", existingLanguages)
        .addAttribute("dataLanguage", dataLanguage);

    return "givennames/view";
  }
}
