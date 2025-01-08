package io.github.dbmdz.cudami.controller.identifiable.entity.agent;

import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.entity.agent.CudamiPersonsClient;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.agent.Person;
import io.github.dbmdz.cudami.business.i18n.LanguageService;
import io.github.dbmdz.cudami.controller.ParameterHelper;
import io.github.dbmdz.cudami.controller.identifiable.entity.AbstractEntitiesController;
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

/** Controller for Person management pages. */
@Controller
public class PersonsController extends AbstractEntitiesController<Person, CudamiPersonsClient> {

  private static final Logger LOGGER = LoggerFactory.getLogger(PersonsController.class);

  public PersonsController(CudamiClient client, LanguageService languageService) {
    super(client.forPersons(), client, languageService);
  }

  @GetMapping("/persons/new")
  public String create(Model model) throws TechnicalException {
    model.addAttribute("activeLanguage", languageService.getDefaultLanguage());
    return "persons/create";
  }

  @GetMapping("/persons/{uuid:" + ParameterHelper.UUID_PATTERN + "}/edit")
  public String edit(
      @PathVariable UUID uuid,
      @RequestParam(name = "activeLanguage", required = false) Locale activeLanguage,
      Model model)
      throws TechnicalException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    Person person = service.getByUuid(uuid);
    List<String> existingLanguages =
        languageService.sortAndMapLanguages(displayLocale, person.getLabel().getLocales());

    if (activeLanguage != null && existingLanguages.contains(activeLanguage.toLanguageTag())) {
      model.addAttribute("activeLanguage", activeLanguage.toLanguageTag());
    } else {
      model.addAttribute("activeLanguage", existingLanguages.get(0));
    }
    model.addAttribute("existingLanguages", existingLanguages);
    model.addAttribute("uuid", person.getUuid());

    return "persons/edit";
  }

  @GetMapping("/persons")
  public String list(Model model) throws TechnicalException {
    model.addAttribute("existingLanguages", getExistingLanguagesFromService());

    String dataLanguage = getDataLanguage(null, languageService);
    model.addAttribute("dataLanguage", dataLanguage);

    return "persons/list";
  }

  @ModelAttribute("menu")
  protected String module() {
    return "persons";
  }

  @GetMapping("/persons/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  public String view(
      @PathVariable UUID uuid,
      @RequestParam(name = "dataLanguage", required = false) String targetDataLanguage,
      Model model)
      throws TechnicalException, ResourceNotFoundException {
    Person person = service.getByUuid(uuid);
    if (person == null) {
      throw new ResourceNotFoundException();
    }
    model.addAttribute("person", person);

    List<Locale> existingLanguages = getExistingLanguagesFromIdentifiable(person);
    String dataLanguage = getDataLanguage(targetDataLanguage, existingLanguages, languageService);
    model
        .addAttribute("existingLanguages", existingLanguages)
        .addAttribute("dataLanguage", dataLanguage);

    return "persons/view";
  }
}
