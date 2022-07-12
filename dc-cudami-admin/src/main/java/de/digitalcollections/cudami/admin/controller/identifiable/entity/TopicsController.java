package de.digitalcollections.cudami.admin.controller.identifiable.entity;

import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.CudamiLocalesClient;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiTopicsClient;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.Topic;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import de.digitalcollections.model.view.BreadcrumbNavigation;
import de.digitalcollections.model.view.BreadcrumbNode;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/** Controller for topics management pages. */
@Controller
public class TopicsController extends AbstractController {

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

  @GetMapping("/api/topics/new")
  @ResponseBody
  public Topic create() throws TechnicalException {
    return service.create();
  }

  @GetMapping("/topics/{uuid}/edit")
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

  @GetMapping("/api/topics")
  @ResponseBody
  public PageResponse<Topic> findTop(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "searchTerm", required = false) String searchTerm,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy)
      throws TechnicalException {
    PageRequest pageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    return this.service.findTopTopics(pageRequest);
  }

  @GetMapping("/api/topics/{uuid}/topics")
  @ResponseBody
  public PageResponse<Topic> findSubtopic(
      @PathVariable UUID uuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "searchTerm", required = false) String searchTerm)
      throws TechnicalException {
    PageRequest pageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
    return service.findSubtopics(uuid, pageRequest);
  }

  @GetMapping("/api/topics/{uuid}/entities")
  @ResponseBody
  public PageResponse<Entity> getAttachedEntites(
      @PathVariable UUID uuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize)
      throws TechnicalException {
    return this.service.findEntities(uuid, new PageRequest(pageNumber, pageSize));
  }

  @GetMapping("/api/topics/{uuid}")
  @ResponseBody
  public Topic getByUuid(@PathVariable UUID uuid) throws TechnicalException {
    return service.getByUuid(uuid);
  }

  @GetMapping("/api/topics/{uuid}/fileresources")
  @ResponseBody
  public PageResponse<FileResource> getRelatedFileResources(
      @PathVariable UUID uuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize)
      throws TechnicalException {
    return this.service.findFileResources(uuid, new PageRequest(pageNumber, pageSize));
  }

  @GetMapping("/topics")
  public String list(Model model) throws TechnicalException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    model.addAttribute(
        "existingLanguages",
        languageSortingHelper.sortLanguages(displayLocale, service.getLanguagesOfTopTopics()));
    return "topics/list";
  }

  @ModelAttribute("menu")
  protected String module() {
    return "topics";
  }

  @PostMapping("/api/topics")
  public ResponseEntity save(
      @RequestBody Topic topic,
      @RequestParam(name = "parentUuid", required = false) UUID parentUuid) {
    try {
      Topic topicDb = null;
      if (parentUuid == null) {
        topicDb = service.save(topic);
      } else {
        topicDb = service.saveWithParentTopic(topic, parentUuid);
      }
      return ResponseEntity.status(HttpStatus.CREATED).body(topicDb);
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save topic: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @PutMapping("/api/topics/{uuid}")
  public ResponseEntity update(@PathVariable UUID uuid, @RequestBody Topic topic) {
    try {
      Topic topicDb = service.update(uuid, topic);
      return ResponseEntity.ok(topicDb);
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save topic with uuid={}", uuid, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @GetMapping(
      "/topics/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}")
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
