package de.digitalcollections.cudami.admin.controller.identifiable.semantic;

import de.digitalcollections.cudami.admin.business.i18n.LanguageService;
import de.digitalcollections.cudami.admin.business.impl.validator.LabelNotBlankValidator;
import de.digitalcollections.cudami.admin.controller.ParameterHelper;
import de.digitalcollections.cudami.admin.controller.identifiable.AbstractIdentifiablesController;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.config.CudamiConfigClient;
import de.digitalcollections.cudami.client.identifiable.entity.semantic.CudamiSubjectsClient;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.semantic.Subject;
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
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Controller for subject management pages. */
@Controller
@SessionAttributes(value = {"subject"})
public class SubjectsController
    extends AbstractIdentifiablesController<Subject, CudamiSubjectsClient> {

  private static final Logger LOGGER = LoggerFactory.getLogger(SubjectsController.class);

  private final LabelNotBlankValidator labelNotBlankValidator;
  private final MessageSource messageSource;

  private CudamiConfigClient cudamiConfigClient;

  public SubjectsController(
      CudamiClient client,
      LanguageService languageService,
      MessageSource messageSource,
      LabelNotBlankValidator labelNotBlankValidator) {
    super(client.forSubjects(), client, languageService);
    this.cudamiConfigClient = client.forConfig();
    this.labelNotBlankValidator = labelNotBlankValidator;
    this.messageSource = messageSource;
  }

  @GetMapping("/subjects/new")
  public String create(Model model) throws TechnicalException {
    Subject subject = service.create();
    Locale defaultLanguage = languageService.getDefaultLanguage();
    subject.setLabel(new LocalizedText(defaultLanguage, ""));
    model.addAttribute("subject", subject);

    List<Locale> existingLanguages = List.of(defaultLanguage);
    model.addAttribute("existingLanguages", existingLanguages);

    List<Locale> sortedLanguages = languageService.getAllLanguages();
    model.addAttribute("allLanguages", sortedLanguages);
    model.addAttribute("activeLanguage", defaultLanguage);

    List<String> subjectTypes =
        cudamiConfigClient.getConfig().getTypeDeclarations().getSubjectTypes();
    model.addAttribute("subjectTypes", subjectTypes);

    model.addAttribute("mode", "create");
    return "subjects/create-or-edit";
  }

  @GetMapping("/subjects/{uuid:" + ParameterHelper.UUID_PATTERN + "}/edit")
  public String edit(
      @PathVariable UUID uuid,
      @RequestParam(name = "activeLanguage", required = false) Locale activeLanguage,
      Model model)
      throws TechnicalException {
    Subject subject = service.getByUuid(uuid);
    model.addAttribute("subject", subject);

    List<Locale> existingLanguages =
        languageService.getExistingLanguages(
            languageService.getDefaultLanguage(), subject.getLabel());
    model.addAttribute("existingLanguages", existingLanguages);

    if (activeLanguage != null && existingLanguages.contains(activeLanguage)) {
      model.addAttribute("activeLanguage", activeLanguage);
    } else {
      model.addAttribute("activeLanguage", existingLanguages.get(0));
    }

    List<Locale> sortedLanguages = languageService.getAllLanguages();
    model.addAttribute("allLanguages", sortedLanguages);

    List<String> subjectTypes =
        cudamiConfigClient.getConfig().getTypeDeclarations().getSubjectTypes();
    model.addAttribute("subjectTypes", subjectTypes);

    model.addAttribute("mode", "edit");
    return "subjects/create-or-edit";
  }

  @GetMapping("/subjects")
  public String list(Model model) throws TechnicalException {
    List<Locale> existingLanguages =
        languageService.getExistingLanguagesForLocales(
            ((CudamiSubjectsClient) service).getLanguages());
    model.addAttribute("existingLanguages", existingLanguages);

    String dataLanguage = getDataLanguage(null, languageService);
    model.addAttribute("dataLanguage", dataLanguage);

    return "subjects/list";
  }

  @ModelAttribute("menu")
  protected String module() {
    return "subjects";
  }

  @PostMapping("/subjects/new")
  public String save(
      @ModelAttribute("formData") Subject subjectFormData,
      @ModelAttribute @Valid Subject subject,
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
    subject.setLabel(subjectFormData.getLabel());
    subject.setSubjectType(subjectFormData.getSubjectType());

    validate(subject, results);
    // TODO: move validate() to service layer on server side using new
    // ValidationException of dc model?
    if (results.hasErrors()) {
      Locale defaultLanguage = languageService.getDefaultLanguage();
      model.addAttribute(
          "existingLanguages",
          languageService.getExistingLanguages(defaultLanguage, subject.getLabel()));
      model.addAttribute("allLanguages", languageService.getAllLanguages());
      model.addAttribute("activeLanguage", defaultLanguage);
      List<String> subjectTypes =
          cudamiConfigClient.getConfig().getTypeDeclarations().getSubjectTypes();
      model.addAttribute("subjectTypes", subjectTypes);
      return "subjects/create-or-edit";
    }
    Subject subjectDB = null;
    try {
      subjectDB = service.save(subject);
      LOGGER.info("Successfully saved subject");
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save subject: ", e);
      String message =
          messageSource.getMessage("error.technical_error", null, LocaleContextHolder.getLocale());
      redirectAttributes.addFlashAttribute("error_message", message);
      return "redirect:/subjects";
    }
    status.setComplete();
    String message =
        messageSource.getMessage("msg.created_successfully", null, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute("success_message", message);
    return "redirect:/subjects/" + subjectDB.getUuid().toString();
  }

  private void validate(Subject subject, BindingResult results) {
    labelNotBlankValidator.validate(subject.getLabel(), results);
  }

  @PostMapping(value = "/subjects/{pathUuid}/edit")
  public String update(
      @PathVariable UUID pathUuid,
      @ModelAttribute("formData") Subject subjectFormData,
      @ModelAttribute Subject subject,
      BindingResult results,
      Model model,
      SessionStatus status,
      RedirectAttributes redirectAttributes)
      throws TechnicalException {
    model.addAttribute("mode", "edit");
    verifyBinding(results);

    // just update the fields, that were editable
    // needed session attribute independent "familyNameFormData" because session
    // attributes just get data set,
    // but do not remove hashmap entry (language, if tab is removed)
    subject.setLabel(subjectFormData.getLabel());
    subject.setSubjectType(subjectFormData.getSubjectType());

    validate(subject, results);
    // TODO: move validate() to service layer on server side using new
    // ValidationException of dc model?
    if (results.hasErrors()) {
      Locale defaultLanguage = languageService.getDefaultLanguage();
      model.addAttribute(
          "existingLanguages",
          languageService.getExistingLanguages(defaultLanguage, subject.getLabel()));
      model.addAttribute("allLanguages", languageService.getAllLanguages());
      model.addAttribute("activeLanguage", defaultLanguage);
      List<String> subjectTypes =
          cudamiConfigClient.getConfig().getTypeDeclarations().getSubjectTypes();
      model.addAttribute("subjectTypes", subjectTypes);
      return "subjects/create-or-edit";
    }

    try {
      service.update(pathUuid, subject);
    } catch (TechnicalException e) {
      String message = "Cannot update subject with uuid=" + pathUuid + ": " + e;
      LOGGER.error(message, e);
      redirectAttributes.addFlashAttribute("error_message", message);
      return "redirect:/subjects/" + pathUuid + "/edit";
    }

    status.setComplete();
    String message =
        messageSource.getMessage(
            "msg.changes_saved_successfully", null, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute("success_message", message);
    return "redirect:/subjects/" + pathUuid;
  }

  @GetMapping("/subjects/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  public String view(
      @PathVariable UUID uuid,
      @RequestParam(name = "dataLanguage", required = false) String targetDataLanguage,
      Model model)
      throws TechnicalException, ResourceNotFoundException {
    Subject subject = service.getByUuid(uuid);
    if (subject == null) {
      throw new ResourceNotFoundException();
    }
    model.addAttribute("subject", subject);

    List<Locale> existingLanguages = getExistingLanguagesFromIdentifiable(subject);
    String dataLanguage = getDataLanguage(targetDataLanguage, existingLanguages, languageService);

    model
        .addAttribute("existingLanguages", existingLanguages)
        .addAttribute("dataLanguage", dataLanguage);
    return "subjects/view";
  }
}
