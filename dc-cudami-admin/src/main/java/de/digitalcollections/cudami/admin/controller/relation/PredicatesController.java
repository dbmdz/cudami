package de.digitalcollections.cudami.admin.controller.relation;

import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.business.impl.validator.LabelNotBlankValidator;
import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.CudamiLocalesClient;
import de.digitalcollections.cudami.client.relation.CudamiPredicatesClient;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.relation.Predicate;
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

/** Controller for predicate management pages. */
@Controller
@SessionAttributes(value = {"predicate"})
public class PredicatesController extends AbstractController {

  private static final Logger LOGGER = LoggerFactory.getLogger(PredicatesController.class);

  private final LabelNotBlankValidator labelNotBlankValidator;
  private final LanguageSortingHelper languageSortingHelper;
  private final CudamiLocalesClient localeService;
  private final MessageSource messageSource;
  private final CudamiPredicatesClient service;

  public PredicatesController(
      MessageSource messageSource,
      LanguageSortingHelper languageSortingHelper,
      CudamiClient client,
      LabelNotBlankValidator labelNotBlankValidator) {
    this.labelNotBlankValidator = labelNotBlankValidator;
    this.languageSortingHelper = languageSortingHelper;
    this.localeService = client.forLocales();
    this.messageSource = messageSource;
    this.service = client.forPredicates();
  }

  @GetMapping("/predicates/new")
  public String create(Model model) throws TechnicalException {
    Predicate predicate = service.create();
    Locale defaultLanguage = localeService.getDefaultLanguage();
    predicate.setLabel(new LocalizedText(defaultLanguage, ""));
    model.addAttribute("predicate", predicate);
    List<Locale> existingLanguages = List.of(defaultLanguage);

    List<Locale> sortedLanguages = getAllLanguages();

    model.addAttribute("existingLanguages", existingLanguages);
    model.addAttribute("allLanguages", sortedLanguages);
    model.addAttribute("activeLanguage", defaultLanguage);

    model.addAttribute("mode", "create");
    return "predicates/create-or-edit";
  }

  @GetMapping("/predicates/{uuid}/delete")
  public String delete(@PathVariable UUID uuid, Model model, RedirectAttributes redirectAttributes)
      throws TechnicalException {
    service.deleteByUuid(uuid);
    String message =
        messageSource.getMessage("msg.deleted_successfully", null, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute("success_message", message);
    return "redirect:/predicates";
  }

  @GetMapping("/predicates/{uuid}/edit")
  public String edit(
      @PathVariable UUID uuid,
      @RequestParam(name = "activeLanguage", required = false) Locale activeLanguage,
      Model model)
      throws TechnicalException {
    Predicate predicate = service.getByUuid(uuid);
    model.addAttribute("predicate", predicate);

    List<Locale> existingLanguages =
        getExistingLanguages(localeService.getDefaultLanguage(), predicate);
    model.addAttribute("existingLanguages", existingLanguages);

    if (activeLanguage != null && existingLanguages.contains(activeLanguage)) {
      model.addAttribute("activeLanguage", activeLanguage);
    } else {
      model.addAttribute("activeLanguage", existingLanguages.get(0));
    }

    List<Locale> sortedLanguages = getAllLanguages();
    model.addAttribute("allLanguages", sortedLanguages);

    model.addAttribute("mode", "edit");
    return "predicates/create-or-edit";
  }

  private List<Locale> getAllLanguages() throws TechnicalException {
    List<Locale> allLanguagesAsLocales = localeService.getAllLanguagesAsLocales();
    final Locale displayLocale = LocaleContextHolder.getLocale();
    List<Locale> sortedLanguages =
        languageSortingHelper.sortLanguages(displayLocale, allLanguagesAsLocales);
    return sortedLanguages;
  }

  private List<Locale> getExistingLanguages(Locale defaultLanguage, Predicate predicate) {
    List<Locale> existingLanguages = List.of(defaultLanguage);
    LocalizedText label = predicate.getLabel();
    if (!CollectionUtils.isEmpty(label)) {
      Locale displayLocale = LocaleContextHolder.getLocale();
      existingLanguages =
          languageSortingHelper.sortLanguages(displayLocale, predicate.getLabel().getLocales());
    }
    return existingLanguages;
  }

  @GetMapping("/predicates")
  public String list(Model model) throws TechnicalException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    List<Locale> existingLanguages =
        languageSortingHelper.sortLanguages(displayLocale, service.getLanguages());
    model.addAttribute("existingLanguages", existingLanguages);
    model.addAttribute("defaultLanguage", localeService.getDefaultLanguage());
    return "predicates/list";
  }

  @ModelAttribute("menu")
  protected String module() {
    return "predicates";
  }

  @PostMapping("/predicates/new")
  public String save(
      @ModelAttribute("formData") Predicate predicateFormData,
      @ModelAttribute @Valid Predicate predicate,
      BindingResult results,
      Model model,
      SessionStatus status,
      RedirectAttributes redirectAttributes)
      throws TechnicalException {
    model.addAttribute("mode", "create");
    verifyBinding(results);

    // set hashmap fields from form data:
    // needed session attribute independent "predicateFormData" because session attributes just get
    // data set,
    // but do not remove hashmap entry (language, if tab is removed)
    predicate.setLabel(predicateFormData.getLabel());
    predicate.setDescription(predicateFormData.getDescription());

    validate(predicate, results);
    // TODO: move validate() to service layer on server side using new ValidationException of dc
    // model?
    if (results.hasErrors()) {
      Locale defaultLanguage = localeService.getDefaultLanguage();
      model.addAttribute("existingLanguages", getExistingLanguages(defaultLanguage, predicate));
      model.addAttribute("allLanguages", getAllLanguages());
      model.addAttribute("activeLanguage", defaultLanguage);
      return "predicates/create-or-edit";
    }
    Predicate predicateDB = null;
    try {
      //      predicateDB = service.save(predicate, results);
      predicateDB = service.save(predicate);
      LOGGER.info("Successfully saved predicate");
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save predicate: ", e);
      String message =
          messageSource.getMessage("error.technical_error", null, LocaleContextHolder.getLocale());
      redirectAttributes.addFlashAttribute("error_message", message);
      return "redirect:/predicates";
    }
    status.setComplete();
    String message =
        messageSource.getMessage("msg.created_successfully", null, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute("success_message", message);
    return "redirect:/predicates/" + predicateDB.getUuid().toString();
  }

  @PostMapping(value = "/predicates/{pathUuid}/edit")
  public String update(
      @PathVariable UUID pathUuid,
      @ModelAttribute("formData") Predicate predicateFormData,
      @ModelAttribute Predicate predicate,
      BindingResult results,
      Model model,
      SessionStatus status,
      RedirectAttributes redirectAttributes)
      throws TechnicalException {
    model.addAttribute("mode", "edit");
    verifyBinding(results);

    // just update the fields, that were editable
    // needed session attribute independent "predicateFormData" because session attributes just get
    // data set,
    // but do not remove hashmap entry (language, if tab is removed)
    predicate.setLabel(predicateFormData.getLabel());
    predicate.setDescription(predicateFormData.getDescription());

    validate(predicate, results);
    // TODO: move validate() to service layer on server side using new ValidationException of dc
    // model?
    if (results.hasErrors()) {
      Locale defaultLanguage = localeService.getDefaultLanguage();
      model.addAttribute("existingLanguages", getExistingLanguages(defaultLanguage, predicate));
      model.addAttribute("allLanguages", getAllLanguages());
      model.addAttribute("activeLanguage", defaultLanguage);
      return "predicates/create-or-edit";
    }

    try {
      service.update(pathUuid, predicate);
    } catch (TechnicalException e) {
      String message = "Cannot update predicate with uuid=" + pathUuid + ": " + e;
      LOGGER.error(message, e);
      redirectAttributes.addFlashAttribute("error_message", message);
      return "redirect:/predicates/" + pathUuid + "/edit";
    }

    status.setComplete();
    String message =
        messageSource.getMessage(
            "msg.changes_saved_successfully", null, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute("success_message", message);
    return "redirect:/predicates/" + pathUuid;
  }

  private void validate(Predicate predicate, BindingResult results) {
    labelNotBlankValidator.validate(predicate.getLabel(), results);
  }

  @GetMapping("/predicates/{uuid}")
  public String view(@PathVariable UUID uuid, Model model)
      throws TechnicalException, ResourceNotFoundException {
    Predicate predicate = service.getByUuid(uuid);
    if (predicate == null) {
      throw new ResourceNotFoundException();
    }

    List<Locale> existingLanguages = Collections.emptyList();
    LocalizedText label = predicate.getLabel();
    if (!CollectionUtils.isEmpty(label)) {
      Locale displayLocale = LocaleContextHolder.getLocale();
      existingLanguages =
          languageSortingHelper.sortLanguages(displayLocale, predicate.getLabel().getLocales());
    }

    model
        .addAttribute("predicate", predicate)
        .addAttribute("existingLanguages", existingLanguages)
        .addAttribute("value", predicate.getValue());
    return "predicates/view";
  }
}
