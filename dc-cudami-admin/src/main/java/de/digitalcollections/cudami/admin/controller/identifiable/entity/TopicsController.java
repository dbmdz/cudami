package de.digitalcollections.cudami.admin.controller.identifiable.entity;

import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.CudamiLocalesClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiTopicsClient;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.Topic;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
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

  @ModelAttribute("menu")
  protected String module() {
    return "topics";
  }

  @GetMapping({"/subtopics/new", "/topics/new"})
  public String create(
      Model model,
      @RequestParam(name = "parentType", required = false) String parentType,
      @RequestParam(name = "parentUuid", required = false) UUID parentUuid)
      throws Exception {
    model.addAttribute("activeLanguage", localeService.getDefaultLanguage());
    model.addAttribute("parentType", parentType);
    model.addAttribute("parentUuid", parentUuid);
    return "topics/create";
  }

  @GetMapping("/api/topics/new")
  @ResponseBody
  public Topic create() {
    return service.create();
  }

  @GetMapping({"/subtopics/{uuid}/edit", "/topics/{uuid}/edit"})
  public String edit(
      @PathVariable UUID uuid,
      @RequestParam(name = "activeLanguage", required = false) Locale activeLanguage,
      Model model)
      throws HttpException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    Topic topic = service.findOne(uuid);
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
  public PageResponse<Topic> findAllTop(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "searchTerm", required = false) String searchTerm)
      throws HttpException {
    SearchPageRequest pageRequest = new SearchPageRequest(searchTerm, pageNumber, pageSize);
    return this.service.findTopCollections(pageRequest);
  }

  @GetMapping("/api/topics/{uuid}/subtopics")
  @ResponseBody
  public PageResponse<Topic> findSubtopic(
      @PathVariable UUID uuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "searchTerm", required = false) String searchTerm)
      throws HttpException {
    SearchPageRequest searchPageRequest = new SearchPageRequest(searchTerm, pageNumber, pageSize);
    return service.findSubtopics(uuid, searchPageRequest);
  }

  @GetMapping("/api/topics/{uuid}")
  @ResponseBody
  public Topic get(@PathVariable UUID uuid) throws HttpException {
    return service.findOne(uuid);
  }

  @GetMapping("/api/topics/{uuid}/entities")
  @ResponseBody
  public PageResponse<Entity> getAttachedEntites(
      @PathVariable UUID uuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize)
      throws HttpException {
    return this.service.getEntities(uuid, new PageRequest(pageNumber, pageSize));
  }

  @GetMapping("/api/topics/{uuid}/fileresources")
  @ResponseBody
  public PageResponse<FileResource> getRelatedFileResources(
      @PathVariable UUID uuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize)
      throws HttpException {
    return this.service.getFileResources(uuid, new PageRequest(pageNumber, pageSize));
  }

  @GetMapping("/topics")
  public String list(Model model) throws HttpException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    model.addAttribute(
        "existingLanguages",
        languageSortingHelper.sortLanguages(displayLocale, service.getTopTopicsLanguages()));
    return "topics/list";
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
    } catch (HttpException e) {
      LOGGER.error("Cannot save topic: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @PutMapping("/api/topics/{uuid}")
  public ResponseEntity update(@PathVariable UUID uuid, @RequestBody Topic topic) {
    try {
      Topic topicDb = service.update(uuid, topic);
      return ResponseEntity.ok(topicDb);
    } catch (HttpException e) {
      LOGGER.error("Cannot save topic with uuid={}", uuid, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @GetMapping({"/subtopics/{refId:[0-9]+}", "/topics/{refId:[0-9]+}"})
  public String viewByRefId(@PathVariable long refId, Model model)
      throws HttpException, ResourceNotFoundException {
    Topic topic = service.findOneByRefId(refId);
    if (topic == null) {
      throw new ResourceNotFoundException();
    }
    return view(topic.getUuid(), model);
  }

  @GetMapping({
    "/subtopics/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}",
    "/topics/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}"
  })
  public String view(@PathVariable UUID uuid, Model model)
      throws HttpException, ResourceNotFoundException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    Topic topic = service.findOne(uuid);
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
}
