package de.digitalcollections.cudami.admin.controller.identifiable.entity.parts;

import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.backend.api.repository.LocaleRepository;
import de.digitalcollections.cudami.admin.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.CudamiSubtopicsClient;
import de.digitalcollections.model.api.identifiable.Node;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.Topic;
import de.digitalcollections.model.api.identifiable.entity.parts.Subtopic;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.view.BreadcrumbNavigation;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

/** Controller for subtopics management pages. */
@Controller
public class SubtopicsController extends AbstractController {

  private static final Logger LOGGER = LoggerFactory.getLogger(SubtopicsController.class);

  private final LanguageSortingHelper languageSortingHelper;
  private final LocaleRepository localeRepository;
  private final CudamiSubtopicsClient service;

  @Autowired
  public SubtopicsController(
      LanguageSortingHelper languageSortingHelper,
      LocaleRepository localeRepository,
      CudamiClient cudamiClient) {
    this.languageSortingHelper = languageSortingHelper;
    this.localeRepository = localeRepository;
    this.service = cudamiClient.forSubtopics();
  }

  @ModelAttribute("menu")
  protected String module() {
    return "subtopics";
  }

  @GetMapping("/subtopics/new")
  public String create(
      Model model,
      @RequestParam("parentType") String parentType,
      @RequestParam("parentUuid") String parentUuid) {
    model.addAttribute("activeLanguage", localeRepository.getDefaultLanguage());
    model.addAttribute("parentType", parentType);
    model.addAttribute("parentUuid", parentUuid);
    return "subtopics/create";
  }

  @GetMapping("/api/subtopics/new")
  @ResponseBody
  public Subtopic create() {
    return (Subtopic) service.create();
  }

  @GetMapping("/subtopics/{uuid}/edit")
  public String edit(@PathVariable UUID uuid, Model model) throws Exception {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    Subtopic subtopic = (Subtopic) service.findOne(uuid);
    List<Locale> existingLanguages =
        languageSortingHelper.sortLanguages(displayLocale, subtopic.getLabel().getLocales());

    model.addAttribute("activeLanguage", existingLanguages.get(0));
    model.addAttribute("existingLanguages", existingLanguages);
    model.addAttribute("uuid", subtopic.getUuid());

    return "subtopics/edit";
  }

  @GetMapping("/api/subtopics/{uuid}")
  @ResponseBody
  public Subtopic get(@PathVariable UUID uuid) throws Exception {
    return (Subtopic) service.findOne(uuid);
  }

  @PostMapping("/api/subtopics/new")
  public ResponseEntity save(
      @RequestBody Subtopic subtopic,
      @RequestParam("parentType") String parentType,
      @RequestParam("parentUuid") UUID parentUuid)
      throws IdentifiableServiceException {
    try {
      Subtopic subtopicDb = null;
      if (parentType.equals("topic")) {
        subtopicDb = service.saveWithParentTopic(subtopic, parentUuid);
      } else {
        subtopicDb = service.saveWithParentSubtopic(subtopic, parentUuid);
      }
      return ResponseEntity.status(HttpStatus.CREATED).body(subtopicDb);
    } catch (Exception e) {
      if (parentType.equals("topic")) {
        LOGGER.error("Cannot save top-level subtopic: ", e);
      } else if (parentType.equals("subtopic")) {
        LOGGER.error("Cannot save subtopic: ", e);
      }
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @PutMapping("/api/subtopics/{uuid}")
  public ResponseEntity update(@PathVariable UUID uuid, @RequestBody Subtopic subtopic)
      throws IdentifiableServiceException {
    try {
      Subtopic subtopicDb = (Subtopic) service.update(uuid, subtopic);
      return ResponseEntity.ok(subtopicDb);
    } catch (Exception e) {
      LOGGER.error("Cannot save subtopic with uuid={}", uuid, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @GetMapping("/subtopics/{uuid}")
  public String view(@PathVariable UUID uuid, Model model) throws Exception {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    Subtopic subtopic = (Subtopic) service.findOne(uuid);
    List<Locale> existingLanguages =
        languageSortingHelper.sortLanguages(displayLocale, subtopic.getLabel().getLocales());

    model.addAttribute("existingLanguages", existingLanguages);
    model.addAttribute("subtopic", subtopic);

    List<FileResource> relatedFileResources = service.getFileResources(uuid);
    model.addAttribute("relatedFileResources", relatedFileResources);

    List<Entity> relatedEntities = service.getEntities(uuid);
    model.addAttribute("relatedEntities", relatedEntities);

    BreadcrumbNavigation breadcrumbNavigation = service.getBreadcrumbNavigation(uuid);
    List<Node> breadcrumbs = breadcrumbNavigation.getNavigationItems();
    // Cut out first breadcrumb node (the one with empty uuid), which identifies the topic, since
    // it is handled individually
    breadcrumbs.removeIf(n -> n.getUuid() == null);
    model.addAttribute("breadcrumbs", breadcrumbs);

    Topic topic = service.getTopic(uuid);
    model.addAttribute("topic", topic);

    return "subtopics/view";
  }
}
