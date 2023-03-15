package de.digitalcollections.cudami.admin.controller.identifiable.entity.agent;

import de.digitalcollections.cudami.admin.business.i18n.LanguageService;
import de.digitalcollections.cudami.admin.controller.ParameterHelper;
import de.digitalcollections.cudami.admin.controller.identifiable.AbstractIdentifiablesController;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.agent.CudamiFamilyNamesClient;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.agent.FamilyName;
import de.digitalcollections.model.text.LocalizedText;
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

/** Controller for family names management pages. */
@Controller
public class FamilynamesController
    extends AbstractIdentifiablesController<FamilyName, CudamiFamilyNamesClient> {

  private static final Logger LOGGER = LoggerFactory.getLogger(FamilynamesController.class);

  public FamilynamesController(LanguageService languageService, CudamiClient client) {
    super(client.forFamilyNames(), languageService);
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
    final Locale displayLocale = LocaleContextHolder.getLocale();
    FamilyName familyName = service.getByUuid(uuid);
    List<Locale> existingLanguages =
        languageService.sortLanguages(displayLocale, familyName.getLabel().getLocales());

    if (activeLanguage != null && existingLanguages.contains(activeLanguage)) {
      model.addAttribute("activeLanguage", activeLanguage);
    } else {
      model.addAttribute("activeLanguage", existingLanguages.get(0));
    }
    model.addAttribute("existingLanguages", existingLanguages);
    model.addAttribute("uuid", familyName.getUuid());

    return "familynames/edit";
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
    String dataLanguage = getDataLanguage(targetDataLanguage, languageService);
    model
        .addAttribute("existingLanguages", existingLanguages)
        .addAttribute("dataLanguage", dataLanguage);

    return "familynames/view";
  }
}
