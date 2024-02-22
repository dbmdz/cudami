package io.github.dbmdz.cudami.admin.controller.identifiable.entity.agent;

import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.entity.agent.CudamiCorporateBodiesClient;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import io.github.dbmdz.cudami.admin.business.i18n.LanguageService;
import io.github.dbmdz.cudami.admin.controller.ParameterHelper;
import io.github.dbmdz.cudami.admin.controller.identifiable.entity.AbstractEntitiesController;
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

/** Controller for CorporateBody management pages. */
@Controller
public class CorporateBodiesController
    extends AbstractEntitiesController<CorporateBody, CudamiCorporateBodiesClient> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CorporateBodiesController.class);

  public CorporateBodiesController(CudamiClient client, LanguageService languageService) {
    super(client.forCorporateBodies(), client, languageService);
  }

  @GetMapping("/corporatebodies/new")
  public String create(Model model) throws TechnicalException {
    model.addAttribute("activeLanguage", languageService.getDefaultLanguage());
    return "corporatebodies/create";
  }

  @GetMapping("/corporatebodies/{uuid:" + ParameterHelper.UUID_PATTERN + "}/edit")
  public String edit(
      @PathVariable UUID uuid,
      @RequestParam(name = "activeLanguage", required = false) Locale activeLanguage,
      Model model)
      throws TechnicalException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    CorporateBody corporateBody = service.getByUuid(uuid);
    List<Locale> existingLanguages =
        languageService.sortLanguages(displayLocale, corporateBody.getLabel().getLocales());

    if (activeLanguage != null && existingLanguages.contains(activeLanguage)) {
      model.addAttribute("activeLanguage", activeLanguage);
    } else {
      model.addAttribute("activeLanguage", existingLanguages.get(0));
    }
    model.addAttribute("existingLanguages", existingLanguages);
    model.addAttribute("uuid", corporateBody.getUuid());

    return "corporatebodies/edit";
  }

  @GetMapping("/corporatebodies")
  public String list(Model model) throws TechnicalException {
    model.addAttribute("existingLanguages", getExistingLanguagesFromService());

    String dataLanguage = getDataLanguage(null, languageService);
    model.addAttribute("dataLanguage", dataLanguage);

    return "corporatebodies/list";
  }

  @ModelAttribute("menu")
  protected String module() {
    return "corporatebodies";
  }

  @GetMapping("/corporatebodies/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  public String view(
      @PathVariable UUID uuid,
      @RequestParam(name = "dataLanguage", required = false) String targetDataLanguage,
      Model model)
      throws TechnicalException, ResourceNotFoundException {
    CorporateBody corporateBody = service.getByUuid(uuid);
    if (corporateBody == null) {
      throw new ResourceNotFoundException();
    }
    model.addAttribute("corporateBody", corporateBody);

    List<Locale> existingLanguages = getExistingLanguagesFromIdentifiable(corporateBody);
    String dataLanguage = getDataLanguage(targetDataLanguage, existingLanguages, languageService);
    model
        .addAttribute("existingLanguages", existingLanguages)
        .addAttribute("dataLanguage", dataLanguage);

    return "corporatebodies/view";
  }
}
