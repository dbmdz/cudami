package de.digitalcollections.cudami.admin.controller.identifiable.entity;

import de.digitalcollections.cudami.admin.business.i18n.LanguageService;
import de.digitalcollections.cudami.admin.controller.ParameterHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiHeadwordEntriesClient;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.HeadwordEntry;
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

/** Controller for headwordentries management pages. */
@Controller
public class HeadwordEntriesController
    extends AbstractEntitiesController<HeadwordEntry, CudamiHeadwordEntriesClient> {

  private static final Logger LOGGER = LoggerFactory.getLogger(HeadwordEntriesController.class);

  public HeadwordEntriesController(CudamiClient client, LanguageService languageService) {
    super(client.forHeadwordEntries(), client, languageService);
  }

  @GetMapping("/headwordentries/new")
  public String create(Model model) throws TechnicalException {
    model.addAttribute("activeLanguage", languageService.getDefaultLanguage());
    return "headwordentries/create";
  }

  @GetMapping("/headwordentries/{uuid:" + ParameterHelper.UUID_PATTERN + "}/edit")
  public String edit(
      @PathVariable UUID uuid,
      @RequestParam(name = "activeLanguage", required = false) Locale activeLanguage,
      Model model)
      throws TechnicalException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    HeadwordEntry headwordEntry = service.getByUuid(uuid);
    List<Locale> existingLanguages =
        languageService.sortLanguages(displayLocale, headwordEntry.getLabel().getLocales());

    if (activeLanguage != null && existingLanguages.contains(activeLanguage)) {
      model.addAttribute("activeLanguage", activeLanguage);
    } else {
      model.addAttribute("activeLanguage", existingLanguages.get(0));
    }
    model.addAttribute("existingLanguages", existingLanguages);
    model.addAttribute("uuid", headwordEntry.getUuid());

    return "headwordentries/edit";
  }

  @GetMapping("/headwordentries")
  public String list(Model model) throws TechnicalException {
    model.addAttribute("existingLanguages", getExistingLanguagesFromService());

    String dataLanguage = getDataLanguage(null, languageService);
    model.addAttribute("dataLanguage", dataLanguage);

    return "headwordentries/list";
  }

  @ModelAttribute("menu")
  protected String module() {
    return "headwordentries";
  }

  @GetMapping("/headwordentries/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  public String view(
      @PathVariable UUID uuid,
      @RequestParam(name = "dataLanguage", required = false) String targetDataLanguage,
      Model model)
      throws TechnicalException, ResourceNotFoundException {
    HeadwordEntry headwordEntry = service.getByUuid(uuid);
    if (headwordEntry == null) {
      throw new ResourceNotFoundException();
    }
    model.addAttribute("headwordEntry", headwordEntry);

    List<Locale> existingLanguages = getExistingLanguagesFromIdentifiable(headwordEntry);
    String dataLanguage = getDataLanguage(targetDataLanguage, existingLanguages, languageService);
    model
        .addAttribute("existingLanguages", existingLanguages)
        .addAttribute("dataLanguage", dataLanguage);

    return "headwordentries/view";
  }
}
