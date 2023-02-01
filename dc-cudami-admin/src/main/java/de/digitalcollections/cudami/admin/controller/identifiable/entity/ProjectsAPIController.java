package de.digitalcollections.cudami.admin.controller.identifiable.entity;

import de.digitalcollections.cudami.admin.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.admin.controller.ParameterHelper;
import de.digitalcollections.cudami.admin.controller.identifiable.AbstractIdentifiablesController;
import de.digitalcollections.cudami.admin.model.bootstraptable.BTResponse;
import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiProjectsClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/** Controller for all public "Projects" endpoints (API). */
@RestController
public class ProjectsAPIController
    extends AbstractIdentifiablesController<Project, CudamiProjectsClient> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProjectsAPIController.class);

  public ProjectsAPIController(LanguageSortingHelper languageSortingHelper, CudamiClient client) {
    super(client.forProjects(), languageSortingHelper, client.forLocales());
  }

  @PostMapping("/api/projects/{uuid:" + ParameterHelper.UUID_PATTERN + "}/digitalobjects")
  public ResponseEntity addDigitalObjects(
      @PathVariable UUID uuid, @RequestBody List<DigitalObject> digitalObjects)
      throws TechnicalException {
    boolean successful = service.addDigitalObjects(uuid, digitalObjects);
    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }

  @GetMapping("/api/projects/new")
  @ResponseBody
  public Project create() throws TechnicalException {
    return service.create();
  }

  /*
  Used in templates/projects/list.html
  */
  @SuppressFBWarnings
  @GetMapping("/api/projects")
  @ResponseBody
  public BTResponse<Project> find(
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(name = "limit", required = false, defaultValue = "1") int limit,
      @RequestParam(name = "search", required = false) String searchTerm,
      @RequestParam(name = "sort", required = false, defaultValue = "label") String sort,
      @RequestParam(name = "order", required = false, defaultValue = "asc") String order,
      @RequestParam(name = "dataLanguage", required = false) String dataLanguage)
      throws TechnicalException, ServiceException {
    PageResponse<Project> pageResponse =
        super.find(localeService, service, offset, limit, searchTerm, sort, order, dataLanguage);
    return new BTResponse<>(pageResponse);
  }

  @GetMapping("/api/projects/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  @ResponseBody
  public Project getByUuid(@PathVariable UUID uuid) throws TechnicalException {
    return service.getByUuid(uuid);
  }

  /*
  Used in templates/projects/view.html
  */
  @GetMapping("/api/projects/{uuid:" + ParameterHelper.UUID_PATTERN + "}/digitalobjects")
  @ResponseBody
  public BTResponse<DigitalObject> findDigitalObjects(
      @PathVariable UUID uuid,
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(name = "limit", required = false, defaultValue = "1") int limit,
      @RequestParam(name = "search", required = false) String searchTerm,
      @RequestParam(name = "sort", required = false, defaultValue = "label") String sort,
      @RequestParam(name = "order", required = false, defaultValue = "asc") String order,
      @RequestParam(name = "dataLanguage", required = false) String dataLanguage)
      throws TechnicalException {
    PageRequest pageRequest =
        createPageRequest(sort, order, dataLanguage, localeService, offset, limit, searchTerm);

    if ("label".equals(sort)) {
      if (dataLanguage == null) {
        dataLanguage = localeService.getDefaultLanguage().getLanguage();
      }
      Sorting sorting =
          Sorting.builder()
              .order(Order.builder().property("label").subProperty(dataLanguage).build())
              .build();
      pageRequest.setSorting(sorting);
    }
    PageResponse<DigitalObject> pageResponse = service.findDigitalObjects(uuid, pageRequest);
    return new BTResponse<>(pageResponse);
  }

  /*
  Used in templates/projects/view.html
  */
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

  @PutMapping("/api/projects/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  public ResponseEntity update(@PathVariable UUID uuid, @RequestBody Project project) {
    try {
      Project projectDb = service.update(uuid, project);
      return ResponseEntity.ok(projectDb);
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save project with uuid={}", uuid, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }
}
