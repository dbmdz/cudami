package de.digitalcollections.cudami.admin.controller.identifiable.entity;

import de.digitalcollections.cudami.admin.controller.AbstractPagingAndSortingController;
import de.digitalcollections.cudami.admin.controller.ParameterHelper;
import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.CudamiLocalesClient;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiTopicsClient;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.Topic;
import de.digitalcollections.model.view.BreadcrumbNavigation;
import de.digitalcollections.model.view.BreadcrumbNode;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/** Controller for topics management pages. */
@Controller
public class TopicsController extends AbstractPagingAndSortingController<Topic> {

  private static final Logger LOGGER = LoggerFactory.getLogger(TopicsController.class);

  private final LanguageSortingHelper languageSortingHelper;
  private final CudamiLocalesClient localeService;
  private final CudamiTopicsClient service;

  public TopicsController(LanguageSortingHelper languageSortingHelper, CudamiClient client) {
    this.languageSortingHelper = languageSortingHelper;
    this.localeService = client.forLocales();
    this.service = client.forTopics();
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
    List<Locale> existingLanguages =
        getExistingLanguages(service.getLanguagesOfTopTopics(), languageSortingHelper);
    model.addAttribute("existingLanguages", existingLanguages);

    String dataLanguage = getDataLanguage(null, localeService);
    model.addAttribute("dataLanguage", dataLanguage);

    return "topics/list";
  }

  @ModelAttribute("menu")
  protected String module() {
    return "topics";
  }

  @GetMapping("/topics/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  public String view(@PathVariable UUID uuid, Model model)
      throws TechnicalException, ResourceNotFoundException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    Topic topic = service.getByUuid(uuid);
    if (topic == null) {
      throw new ResourceNotFoundException();
    }
    List<Locale> existingLanguages =
        this.languageSortingHelper.sortLanguages(displayLocale, topic.getLabel().getLocales());
    List<Locale> existingSubtopicLanguages =
        topic.getChildren().stream()
            .flatMap(child -> child.getLabel().getLocales().stream())
            .collect(Collectors.toList());
    List<Locale> existingEntityLanguages = this.service.getLanguagesOfEntities(uuid);
    List<Locale> existingFileResourceLanguages = this.service.getLanguagesOfFileResources(uuid);

    model
        .addAttribute("existingLanguages", existingLanguages)
        .addAttribute(
            "existingSubtopicLanguages",
            this.languageSortingHelper.sortLanguages(displayLocale, existingSubtopicLanguages))
        .addAttribute("topic", topic)
        .addAttribute(
            "existingEntityLanguages",
            this.languageSortingHelper.sortLanguages(displayLocale, existingEntityLanguages))
        .addAttribute(
            "existingFileResourceLanguages",
            this.languageSortingHelper.sortLanguages(displayLocale, existingFileResourceLanguages));

    BreadcrumbNavigation breadcrumbNavigation = service.getBreadcrumbNavigation(uuid);
    List<BreadcrumbNode> breadcrumbs = breadcrumbNavigation.getNavigationItems();
    model.addAttribute("breadcrumbs", breadcrumbs);

    return "topics/view";
  }

  @GetMapping("/topics/{refId:[0-9]+}")
  public String viewByRefId(@PathVariable long refId, Model model)
      throws TechnicalException, ResourceNotFoundException {
    Topic topic = service.getByRefId(refId);
    if (topic == null) {
      throw new ResourceNotFoundException();
    }
    return view(topic.getUuid(), model);
  }
}
