package de.digitalcollections.cudami.admin.controller.identifiable.entity;

import de.digitalcollections.commons.springdata.domain.PageConverter;
import de.digitalcollections.commons.springdata.domain.PageWrapper;
import de.digitalcollections.commons.springdata.domain.PageableConverter;
import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.CudamiLocalesClient;
import de.digitalcollections.cudami.client.CudamiProjectsClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.api.identifiable.entity.Project;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
import org.springframework.web.bind.annotation.ResponseBody;

/** Controller for project management pages. */
@Controller
public class ProjectsController extends AbstractController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProjectsController.class);

  private final LanguageSortingHelper languageSortingHelper;
  private final CudamiLocalesClient localeService;
  private final CudamiProjectsClient service;

  @Autowired
  public ProjectsController(
      LanguageSortingHelper languageSortingHelper, CudamiClient cudamiClient) {
    this.languageSortingHelper = languageSortingHelper;
    this.localeService = cudamiClient.forLocales();
    this.service = cudamiClient.forProjects();
  }

  @ModelAttribute("menu")
  protected String module() {
    return "projects";
  }

  @GetMapping("/projects/new")
  public String create(Model model) throws HttpException {
    model.addAttribute("activeLanguage", localeService.getDefaultLanguage());
    return "projects/create";
  }

  @GetMapping("/api/projects/new")
  @ResponseBody
  public Project create() {
    return service.create();
  }

  @GetMapping("/projects/{uuid}/edit")
  public String edit(@PathVariable UUID uuid, Model model) throws HttpException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    Project project = service.findOne(uuid);
    List<Locale> existingLanguages =
        languageSortingHelper.sortLanguages(displayLocale, project.getLabel().getLocales());

    model.addAttribute("activeLanguage", existingLanguages.get(0));
    model.addAttribute("existingLanguages", existingLanguages);
    model.addAttribute("uuid", project.getUuid());

    return "projects/edit";
  }

  @GetMapping("/api/projects/{uuid}")
  @ResponseBody
  public Project get(@PathVariable UUID uuid) throws HttpException {
    return service.findOne(uuid);
  }

  @GetMapping("/projects")
  public String list(
      Model model,
      @PageableDefault(
              sort = {"label"},
              size = 25)
          Pageable pageable)
      throws HttpException {
    final PageRequest pageRequest = PageableConverter.convert(pageable);
    final PageResponse pageResponse = service.find(pageRequest);
    Page page = PageConverter.convert(pageResponse, pageRequest);
    model.addAttribute("page", new PageWrapper(page, "/projects"));
    return "projects/list";
  }

  @GetMapping("/projects/{projectUuid}/digitalobjects/{digitalobjectUuid}/remove")
  public String removeDigitalObjectFromProject(
      @PathVariable UUID projectUuid, @PathVariable UUID digitalobjectUuid) throws HttpException {
    service.removeDigitalObject(projectUuid, digitalobjectUuid);
    return "redirect:/projects/" + projectUuid;
  }

  @PostMapping("/api/projects/new")
  public ResponseEntity save(@RequestBody Project project) {
    try {
      Project projectDb = service.save(project);
      return ResponseEntity.status(HttpStatus.CREATED).body(projectDb);
    } catch (HttpException e) {
      LOGGER.error("Cannot save project: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @PutMapping("/api/projects/{uuid}")
  public ResponseEntity update(@PathVariable UUID uuid, @RequestBody Project project) {
    try {
      Project projectDb = service.update(uuid, project);
      return ResponseEntity.ok(projectDb);
    } catch (HttpException e) {
      LOGGER.error("Cannot save project with uuid={}", uuid, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @GetMapping("/projects/{uuid}")
  public String view(
      @PathVariable UUID uuid, @PageableDefault(size = 25) Pageable pageable, Model model)
      throws HttpException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    Project project = service.findOne(uuid);
    List<Locale> existingLanguages =
        languageSortingHelper.sortLanguages(displayLocale, project.getLabel().getLocales());

    model.addAttribute("existingLanguages", existingLanguages);
    model.addAttribute("project", project);

    final PageRequest pageRequest = PageableConverter.convert(pageable);
    final PageResponse pageResponse = service.getDigitalObjects(uuid, pageRequest);
    Page page = PageConverter.convert(pageResponse, pageRequest);
    model.addAttribute("page", new PageWrapper(page, "/projects/" + uuid));

    return "projects/view";
  }
}
