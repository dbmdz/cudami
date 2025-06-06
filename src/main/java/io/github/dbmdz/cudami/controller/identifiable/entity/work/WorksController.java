package io.github.dbmdz.cudami.controller.identifiable.entity.work;

import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.entity.work.CudamiWorksClient;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.work.Work;
import io.github.dbmdz.cudami.business.i18n.LanguageService;
import io.github.dbmdz.cudami.controller.ParameterHelper;
import io.github.dbmdz.cudami.controller.identifiable.entity.AbstractEntitiesController;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/** Controller for work management pages. */
@Controller
public class WorksController extends AbstractEntitiesController<Work, CudamiWorksClient> {

  public WorksController(CudamiClient client, LanguageService languageService) {
    super(client.forWorks(), client, languageService);
  }

  @GetMapping("/works")
  public String list(Model model) throws TechnicalException {
    model.addAttribute("existingLanguages", getExistingLanguagesFromService());

    String dataLanguage = getDataLanguage(null, languageService);
    model.addAttribute("dataLanguage", dataLanguage);

    return "works/list";
  }

  @GetMapping("/works/{uuid:" + ParameterHelper.UUID_PATTERN + "}/edit")
  public String edit(
      @PathVariable UUID uuid,
      @RequestParam(name = "activeLanguage", required = false) Locale activeLanguage,
      Model model)
      throws TechnicalException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    Work work = service.getByUuid(uuid);
    List<String> existingLanguages =
        languageService.sortAndMapLanguages(displayLocale, work.getLabel().getLocales());

    if (activeLanguage != null && existingLanguages.contains(activeLanguage.toLanguageTag())) {
      model.addAttribute("activeLanguage", activeLanguage.toLanguageTag());
    } else {
      model.addAttribute("activeLanguage", existingLanguages.get(0));
    }
    model.addAttribute("existingLanguages", existingLanguages);
    model.addAttribute("uuid", work.getUuid());

    return "works/edit";
  }

  @ModelAttribute("menu")
  protected String module() {
    return "works";
  }

  @GetMapping("/works/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  public String view(
      @PathVariable UUID uuid,
      @RequestParam(name = "dataLanguage", required = false) String targetDataLanguage,
      Model model)
      throws TechnicalException, ResourceNotFoundException {
    Work work = service.getByUuid(uuid);
    if (work == null) {
      throw new ResourceNotFoundException();
    }
    model.addAttribute("work", work);

    List<Locale> existingLanguages = getExistingLanguagesFromIdentifiable(work);
    String dataLanguage = getDataLanguage(targetDataLanguage, existingLanguages, languageService);
    model
        .addAttribute("existingLanguages", existingLanguages)
        .addAttribute("dataLanguage", dataLanguage);

    Locale displayLocale = LocaleContextHolder.getLocale();

    List<Locale> existingManifestationsLanguages =
        languageService.sortLanguages(
            displayLocale, ((CudamiWorksClient) service).getLanguagesOfManifestations(uuid));
    String dataLanguageManifestations =
        getDataLanguage(targetDataLanguage, existingManifestationsLanguages, languageService);
    model
        .addAttribute("existingManifestationsLanguages", existingManifestationsLanguages)
        .addAttribute("dataLanguageManifestations", dataLanguageManifestations);

    return "works/view";
  }
}
