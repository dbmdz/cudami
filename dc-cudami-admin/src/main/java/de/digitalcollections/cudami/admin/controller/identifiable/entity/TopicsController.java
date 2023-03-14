package de.digitalcollections.cudami.admin.controller.identifiable.entity;

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

import de.digitalcollections.cudami.admin.controller.ParameterHelper;
import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiTopicsClient;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.Topic;
import de.digitalcollections.model.view.BreadcrumbNavigation;
import de.digitalcollections.model.view.BreadcrumbNode;

/** Controller for topics management pages. */
@Controller
public class TopicsController extends AbstractEntitiesController<Topic, CudamiTopicsClient> {

  private static final Logger LOGGER = LoggerFactory.getLogger(TopicsController.class);

  public TopicsController(LanguageSortingHelper languageSortingHelper, CudamiClient client) {
    super(client.forTopics(), languageSortingHelper, client.forLocales());
  }

  @GetMapping("/topics/new")
  public String create(
      Model model,
      @RequestParam(name = "parentType", required = false) String parentType,
      @RequestParam(name = "parentUuid", required = false) UUID parentUuid)
      throws TechnicalException {
    model.addAttribute("activeLanguage", localeService.getDefaultLanguage());
    model.addAttribute("parentType", parentType);
    model.addAttribute("parentUuid", parentUuid);
    return "topics/create";
  }

  @GetMapping("/topics/{uuid:" + ParameterHelper.UUID_PATTERN + "}/edit")
  public String edit(
      @PathVariable UUID uuid,
      @RequestParam(name = "activeLanguage", required = false) Locale activeLanguage,
      Model model)
      throws TechnicalException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    Topic topic = service.getByUuid(uuid);
    List<Locale> existingLanguages =
        languageSortingHelper.sortLanguages(displayLocale, topic.getLabel().getLocales());

    if (activeLanguage != null && existingLanguages.contains(activeLanguage)) {
      model.addAttribute("activeLanguage", activeLanguage);
    } else {
      model.addAttribute("activeLanguage", existingLanguages.get(0));
    }
    model.addAttribute("existingLanguages", existingLanguages);
    model.addAttribute("uuid", topic.getUuid());

    return "topics/edit";
  }

  @GetMapping("/topics")
  public String list(Model model) throws TechnicalException {
    model.addAttribute(
        "existingLanguages", getExistingLanguagesForLocales(service.getLanguagesOfTopTopics()));

    String dataLanguage = getDataLanguage(null, localeService);
    model.addAttribute("dataLanguage", dataLanguage);

    return "topics/list";
  }

  @ModelAttribute("menu")
  protected String module() {
    return "topics";
  }

  @GetMapping("/topics/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  public String view(
      @PathVariable UUID uuid,
      @RequestParam(name = "dataLanguage", required = false) String targetDataLanguage,
      Model model)
      throws TechnicalException, ResourceNotFoundException {
    Topic topic = service.getByUuid(uuid);
    if (topic == null) {
      throw new ResourceNotFoundException();
    }
    model.addAttribute("topic", topic);

    List<Locale> existingLanguages = getExistingLanguagesFromIdentifiable(topic);
    String dataLanguage = getDataLanguage(targetDataLanguage, localeService);
    model
        .addAttribute("existingLanguages", existingLanguages)
        .addAttribute("dataLanguage", dataLanguage);

    List<Locale> existingSubtopicsLanguages =
        getExistingLanguagesFromIdentifiables(topic.getChildren());
    model
        .addAttribute("existingSubtopicsLanguages", existingSubtopicsLanguages)
        .addAttribute("dataLanguageSubtopics", getDataLanguage(null, localeService));

    final Locale displayLocale = LocaleContextHolder.getLocale();
    List<Locale> existingEntitiesLanguages = service.getLanguagesOfEntities(uuid);
    model
        .addAttribute(
            "existingEntitiesLanguages",
            languageSortingHelper.sortLanguages(displayLocale, existingEntitiesLanguages))
        .addAttribute("dataLanguageEntities", getDataLanguage(null, localeService));

    List<Locale> existingFileResourcesLanguages = service.getLanguagesOfFileResources(uuid);
    model
        .addAttribute(
            "existingFileResourcesLanguages",
            languageSortingHelper.sortLanguages(displayLocale, existingFileResourcesLanguages))
        .addAttribute("dataLanguageFileResources", getDataLanguage(null, localeService));

    BreadcrumbNavigation breadcrumbNavigation = service.getBreadcrumbNavigation(uuid);
    List<BreadcrumbNode> breadcrumbs = breadcrumbNavigation.getNavigationItems();
    model.addAttribute("breadcrumbs", breadcrumbs);

    return "topics/view";
  }

  @GetMapping("/topics/{refId:[0-9]+}")
  public String viewByRefId(
      @PathVariable long refId,
      @RequestParam(name = "dataLanguage", required = false) String targetDataLanguage,
      Model model)
      throws TechnicalException, ResourceNotFoundException {
    Topic topic = service.getByRefId(refId);
    if (topic == null) {
      throw new ResourceNotFoundException();
    }
    return view(topic.getUuid(), targetDataLanguage, model);
  }
}
