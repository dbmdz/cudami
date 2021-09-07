package de.digitalcollections.cudami.server.controller.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.ProjectService;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import de.digitalcollections.model.paging.Sorting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Project controller")
public class ProjectController {

  private final ProjectService projectService;

  public ProjectController(ProjectService projectService) {
    this.projectService = projectService;
  }

  @Operation(summary = "Add an existing digital object to an existing project")
  @PostMapping(
      value = {
        "/v5/projects/{uuid}/digitalobjects/{digitalObjectUuid}",
        "/v3/projects/{uuid}/digitalobjects/{digitalObjectUuid}",
        "/latest/projects/{uuid}/digitalobjects/{digitalObjectUuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity addDigitalObject(
      @Parameter(example = "", description = "UUID of the project") @PathVariable("uuid")
          UUID projectUuid,
      @Parameter(example = "", description = "UUID of the digital object")
          @PathVariable("digitalObjectUuid")
          UUID digitalObjectUuid) {
    Project project = new Project();
    project.setUuid(projectUuid);

    DigitalObject digitalObject = new DigitalObject();
    digitalObject.setUuid(digitalObjectUuid);

    boolean successful = projectService.addDigitalObject(project, digitalObject);

    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Add existing digital objects to an existing project")
  @PostMapping(
      value = {
        "/v5/projects/{uuid}/digitalobjects",
        "/v3/projects/{uuid}/digitalobjects",
        "/latest/projects/{uuid}/digitalobjects"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity addDigitalObjects(
      @Parameter(example = "", description = "UUID of the project") @PathVariable("uuid")
          UUID projectUuid,
      @Parameter(example = "", description = "List of the digital objects") @RequestBody
          List<DigitalObject> digitalObjects) {
    Project project = new Project();
    project.setUuid(projectUuid);

    boolean successful = projectService.addDigitalObjects(project, digitalObjects);

    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }

  @Operation(
      summary = "Delete an existing project and the identifiers, which belong to this project")
  @DeleteMapping(
      value = {"/v5/projects/{uuid}", "/v3/projects/{uuid}", "/latest/projects/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity deleteProject(
      @Parameter(example = "", description = "UUID of the project") @PathVariable("uuid")
          UUID uuid) {

    try {
      projectService.delete(uuid);
    } catch (IdentifiableServiceException e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Operation(summary = "Get all projects in reduced form (no identifiers)")
  @GetMapping(
      value = {"/v5/projects"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Project> findAll(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "searchTerm", required = false) String searchTerm) {
    SearchPageRequest searchPageRequest = new SearchPageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      searchPageRequest.setSorting(sorting);
    }
    return projectService.find(searchPageRequest);
  }

  @Operation(summary = "Get all projects as list")
  @GetMapping(
      value = {"/v5/projectlist", "/v2/projectlist", "/latest/projectlist"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Project> findAllReducedAsList() {
    // TODO test if reduced is sufficient for use cases...
    return projectService.findAllReduced();
  }

  @Operation(summary = "Get project by namespace and id")
  @GetMapping(
      value = {
        "/v5/projects/identifier/{namespace}:{id}",
        "/v3/projects/identifier/{namespace}:{id}",
        "/latest/projects/identifier/{namespace}:{id}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Project findByIdentifier(@PathVariable String namespace, @PathVariable String id)
      throws IdentifiableServiceException {
    return projectService.getByIdentifier(namespace, id);
  }

  @Operation(summary = "Get a project by uuid")
  @GetMapping(
      value = {"/v5/projects/{uuid}", "/v2/projects/{uuid}", "/latest/projects/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Project> findByUuid(
      @Parameter(
              example = "",
              description =
                  "UUID of the project, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @Parameter(
              name = "pLocale",
              description =
                  "Desired locale, e.g. <tt>de_DE</tt>. If unset, contents in all languages will be returned")
          @RequestParam(name = "pLocale", required = false)
          Locale pLocale)
      throws IdentifiableServiceException {

    Project project;
    if (pLocale == null) {
      project = projectService.get(uuid);
    } else {
      project = projectService.get(uuid, pLocale);
    }
    return new ResponseEntity<>(project, HttpStatus.OK);
  }

  @Operation(summary = "Get paged digital objects of a project")
  @GetMapping(
      value = {"/v5/projects/{uuid}/digitalobjects"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public SearchPageResponse<DigitalObject> getDigitalObjects(
      @Parameter(example = "", description = "UUID of the project") @PathVariable("uuid")
          UUID projectUuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "searchTerm", required = false) String searchTerm) {
    SearchPageRequest searchPageRequest = new SearchPageRequest(searchTerm, pageNumber, pageSize);

    Project project = new Project();
    project.setUuid(projectUuid);
    return projectService.getDigitalObjects(project, searchPageRequest);
  }

  @Operation(summary = "Remove an existing digital object from an existing project")
  @DeleteMapping(
      value = {
        "/v5/projects/{uuid}/digitalobjects/{digitalObjectUuid}",
        "/v3/projects/{uuid}/digitalobjects/{digitalObjectUuid}",
        "/latest/projects/{uuid}/digitalobjects/{digitalObjectUuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity removeDigitalObject(
      @Parameter(example = "", description = "UUID of the project") @PathVariable("uuid")
          UUID projectUuid,
      @Parameter(example = "", description = "UUID of the digital object")
          @PathVariable("digitalObjectUuid")
          UUID digitalObjectUuid) {
    Project project = new Project();
    project.setUuid(projectUuid);

    DigitalObject digitalObject = new DigitalObject();
    digitalObject.setUuid(digitalObjectUuid);

    boolean successful = projectService.removeDigitalObject(project, digitalObject);

    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Save a newly created project")
  @PostMapping(
      value = {"/v5/projects", "/v2/projects", "/latest/projects"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Project save(@RequestBody Project project, BindingResult errors)
      throws IdentifiableServiceException {
    return projectService.save(project);
  }

  @Operation(summary = "Save existing digital objects into an existing project")
  @PutMapping(
      value = {
        "/v5/projects/{uuid}/digitalobjects",
        "/v3/projects/{uuid}/digitalobjects",
        "/latest/projects/{uuid}/digitalobjects"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity saveDigitalObjects(
      @Parameter(example = "", description = "UUID of the project") @PathVariable("uuid")
          UUID projectUuid,
      @Parameter(example = "", description = "List of the digital objects") @RequestBody
          List<DigitalObject> digitalObjects) {
    Project project = new Project();
    project.setUuid(projectUuid);

    boolean successful = projectService.saveDigitalObjects(project, digitalObjects);

    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Update an project")
  @PutMapping(
      value = {"/v5/projects/{uuid}", "/v2/projects/{uuid}", "/latest/projects/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Project update(@PathVariable UUID uuid, @RequestBody Project project, BindingResult errors)
      throws IdentifiableServiceException {
    assert Objects.equals(uuid, project.getUuid());
    return projectService.update(project);
  }

  @Operation(summary = "Get languages of all projects")
  @GetMapping(
      value = {"/v5/projects/languages", "/v3/projects/languages", "/latest/projects/languages"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Locale> getLanguages() {
    return projectService.getLanguages();
  }
}
