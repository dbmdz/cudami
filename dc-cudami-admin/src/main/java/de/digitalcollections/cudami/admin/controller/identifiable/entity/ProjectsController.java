package de.digitalcollections.cudami.admin.controller.identifiable.entity;

import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.CudamiLocalesClient;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiProjectsClient;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/** Controller for project management pages. */
@Controller
public class ProjectsController extends AbstractController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProjectsController.class);

  private final LanguageSortingHelper languageSortingHelper;
  private final CudamiLocalesClient localeService;
  private final CudamiProjectsClient service;

  public ProjectsController(LanguageSortingHelper languageSortingHelper, CudamiClient client) {
    this.languageSortingHelper = languageSortingHelper;
    this.localeService = client.forLocales();
    this.service = client.forProjects();
  }

  @PostMapping("/api/projects/{uuid}/digitalobjects")
  public ResponseEntity addDigitalObjects(
      @PathVariable UUID uuid, @RequestBody List<DigitalObject> digitalObjects)
      throws TechnicalException {
    boolean successful = service.addDigitalObjects(uuid, digitalObjects);
    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }

  @GetMapping("/projects/new")
  public String create(Model model) throws TechnicalException {
    model.addAttribute("activeLanguage", localeService.getDefaultLanguage());
    return "projects/create";
  }

  @GetMapping("/api/projects/new")
  @ResponseBody
  public Project create() {
    return service.create();
  }

  @GetMapping("/projects/{uuid}/edit")
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

  @GetMapping("/api/projects")
  @ResponseBody
  public PageResponse<Project> findAll(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "searchTerm", required = false) String searchTerm)
      throws TechnicalException {
    SearchPageRequest searchPageRequest = new SearchPageRequest(searchTerm, pageNumber, pageSize);
    return service.find(searchPageRequest);
  }

  @GetMapping("/api/projects/{uuid}")
  @ResponseBody
  public Project getByUuid(@PathVariable UUID uuid) throws TechnicalException {
    return service.getByUuid(uuid);
  }

  @GetMapping("/api/projects/{uuid}/digitalobjects")
  @ResponseBody
  public PageResponse<DigitalObject> getDigitalObjects(
      @PathVariable UUID uuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "searchTerm", required = false) String searchTerm)
      throws TechnicalException {
    SearchPageRequest searchPageRequest = new SearchPageRequest(searchTerm, pageNumber, pageSize);
    return service.getDigitalObjects(uuid, searchPageRequest);
  }

  @GetMapping("/projects")
  public String list(Model model) throws TechnicalException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    model.addAttribute(
        "existingLanguages",
        languageSortingHelper.sortLanguages(displayLocale, service.getLanguages()));

    return "projects/list";
  }

  @ModelAttribute("menu")
  protected String module() {
    return "projects";
  }

  @DeleteMapping("/api/projects/{projectUuid}/digitalobjects/{digitalobjectUuid}")
  @ResponseBody
  public ResponseEntity removeDigitalObject(
      @PathVariable UUID projectUuid, @PathVariable UUID digitalobjectUuid)
      throws TechnicalException {
    boolean successful = service.removeDigitalObject(projectUuid, digitalobjectUuid);
    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }

  @PostMapping("/api/projects")
  public ResponseEntity save(@RequestBody Project project) {
    try {
      Project projectDb = service.save(project);
      return ResponseEntity.status(HttpStatus.CREATED).body(projectDb);
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save project: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @PutMapping("/api/projects/{uuid}")
  public ResponseEntity update(@PathVariable UUID uuid, @RequestBody Project project) {
    try {
      Project projectDb = service.update(uuid, project);
      return ResponseEntity.ok(projectDb);
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save project with uuid={}", uuid, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @GetMapping("/projects/{uuid}")
  public String view(@PathVariable UUID uuid, Model model)
      throws TechnicalException, ResourceNotFoundException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    Project project = service.getByUuid(uuid);
    if (project == null) {
      throw new ResourceNotFoundException();
    }
    List<Locale> existingLanguages =
        languageSortingHelper.sortLanguages(displayLocale, project.getLabel().getLocales());
    model.addAttribute("existingLanguages", existingLanguages).addAttribute("project", project);
    return "projects/view";
  }
}
