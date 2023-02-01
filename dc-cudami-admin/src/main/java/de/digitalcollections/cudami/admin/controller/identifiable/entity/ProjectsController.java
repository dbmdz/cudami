package de.digitalcollections.cudami.admin.controller.identifiable.entity;

import de.digitalcollections.cudami.admin.controller.ParameterHelper;
import de.digitalcollections.cudami.admin.controller.identifiable.AbstractIdentifiablesController;
import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiProjectsClient;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.Project;
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

/** Controller for project management pages. */
@Controller
public class ProjectsController
    extends AbstractIdentifiablesController<Project, CudamiProjectsClient> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProjectsController.class);

  public ProjectsController(LanguageSortingHelper languageSortingHelper, CudamiClient client) {
    super(client.forProjects(), languageSortingHelper, client.forLocales());
  }

  @GetMapping("/projects/new")
  public String create(Model model) throws TechnicalException {
    model.addAttribute("activeLanguage", localeService.getDefaultLanguage());
    return "projects/create";
  }

  @GetMapping("/projects/{uuid:" + ParameterHelper.UUID_PATTERN + "}/edit")
  public String edit(
      @PathVariable UUID uuid,
      @RequestParam(name = "activeLanguage", required = false) Locale activeLanguage,
      Model model)
      throws TechnicalException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    Project project = service.getByUuid(uuid);
    List<Locale> existingLanguages =
        languageSortingHelper.sortLanguages(displayLocale, project.getLabel().getLocales());

    if (activeLanguage != null && existingLanguages.contains(activeLanguage)) {
      model.addAttribute("activeLanguage", activeLanguage);
    } else {
      model.addAttribute("activeLanguage", existingLanguages.get(0));
    }
    model.addAttribute("existingLanguages", existingLanguages);
    model.addAttribute("uuid", project.getUuid());

    return "projects/edit";
  }

  @GetMapping("/projects")
  public String list(Model model) throws TechnicalException {
    model.addAttribute("existingLanguages", getExistingLanguagesFromService());

    String dataLanguage = getDataLanguage(null, localeService);
    model.addAttribute("dataLanguage", dataLanguage);

    return "projects/list";
  }

  @ModelAttribute("menu")
  protected String module() {
    return "projects";
  }

  @GetMapping("/projects/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  public String view(
      @PathVariable UUID uuid,
      @RequestParam(name = "dataLanguage", required = false) String targetDataLanguage,
      Model model)
      throws TechnicalException, ResourceNotFoundException {
    Project project = service.getByUuid(uuid);
    if (project == null) {
      throw new ResourceNotFoundException();
    }
    model.addAttribute("project", project);

    List<Locale> existingLanguages = getExistingLanguagesFromIdentifiable(project);
    String dataLanguage = getDataLanguage(targetDataLanguage, localeService);
    model
        .addAttribute("existingLanguages", existingLanguages)
        .addAttribute("dataLanguage", dataLanguage);

    // FIXME: missing endpoint for languages of digital objects
    //    Locale displayLocale = LocaleContextHolder.getLocale();
    //    List<Locale> existingDigitalObjectLanguages =
    //        languageSortingHelper.sortLanguages(
    //            displayLocale, service.getLanguagesOfDigitalObjects(uuid));
    //    model
    //        .addAttribute("existingDigitalObjectLanguages", existingDigitalObjectLanguages)
    //        .addAttribute("dataLanguageDigitalObjects", getDataLanguage(null, localeService));

    return "projects/view";
  }
}
