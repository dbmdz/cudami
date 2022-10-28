package de.digitalcollections.cudami.admin.controller.relation;

import de.digitalcollections.commons.springmvc.controller.AbstractController;
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

  private final LanguageSortingHelper languageSortingHelper;
  private final CudamiLocalesClient localeService;
  private final MessageSource messageSource;
  private final CudamiPredicatesClient service;

  public PredicatesController(
      MessageSource messageSource,
      LanguageSortingHelper languageSortingHelper,
      CudamiClient client) {
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
    return "predicates/create";
  }

  @GetMapping("/predicates/{uuid}/edit")
  public String edit(
      @PathVariable UUID uuid,
      @RequestParam(name = "activeLanguage", required = false) Locale activeLanguage,
      Model model)
      throws TechnicalException {
    Predicate predicate = service.getByUuid(uuid);

    List<Locale> existingLanguages =
        getExistingLanguages(localeService.getDefaultLanguage(), predicate);

    if (activeLanguage != null && existingLanguages.contains(activeLanguage)) {
      model.addAttribute("activeLanguage", activeLanguage);
    } else {
      model.addAttribute("activeLanguage", existingLanguages.get(0));
    }
    model.addAttribute("existingLanguages", existingLanguages);
    model.addAttribute("value", predicate.getValue());
    model.addAttribute("uuid", predicate.getUuid());

    return "predicates/edit";
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
      @ModelAttribute @Valid Predicate predicate,
      BindingResult results,
      Model model,
      SessionStatus status,
      RedirectAttributes redirectAttributes)
      throws TechnicalException {
    verifyBinding(results);
    if (results.hasErrors()) {
      Locale defaultLanguage = localeService.getDefaultLanguage();
      model.addAttribute("existingLanguages", getExistingLanguages(defaultLanguage, predicate));
      model.addAttribute("allLanguages", getAllLanguages());
      model.addAttribute("activeLanguage", defaultLanguage);
      return "predicates/create";
    }
    Predicate predicateDB = null;
    try {
      //      predicateDB = service.save(predicate, results);
      predicateDB = service.save(predicate);
      LOGGER.info("Successfully saved website");
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save website: ", e);
      String message =
          messageSource.getMessage("error.technical_error", null, LocaleContextHolder.getLocale());
      redirectAttributes.addFlashAttribute("error_message", message);
      return "redirect:/websites";
    }
    //    if (results.hasErrors()) {
    //      return "predicates/create";
    //    }
    status.setComplete();
    String message =
        messageSource.getMessage("msg.created_successfully", null, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute("success_message", message);
    return "redirect:/predicates/" + predicateDB.getUuid().toString();
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
