package de.digitalcollections.cudami.server.controller.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.ProjectService;
import de.digitalcollections.cudami.server.controller.identifiable.AbstractIdentifiableController;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
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
public class ProjectController extends AbstractIdentifiableController<Project> {

  private final ProjectService projectService;

  public ProjectController(ProjectService projectService) {
    this.projectService = projectService;
  }

  @Override
  protected IdentifiableService<Project> getService() {
    return projectService;
  }

  @Operation(summary = "Add an existing digital object to an existing project")
  @PostMapping(
      value = {
        "/v6/projects/{uuid}/digitalobjects/{digitalObjectUuid}",
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
        "/v6/projects/{uuid}/digitalobjects",
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
      value = {
        "/v6/projects/{uuid}",
        "/v5/projects/{uuid}",
        "/v3/projects/{uuid}",
        "/latest/projects/{uuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity delete(
      @Parameter(example = "", description = "UUID of the project") @PathVariable("uuid") UUID uuid)
      throws ConflictException {

    try {
      projectService.delete(uuid);
    } catch (IdentifiableServiceException e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Operation(summary = "Get all projects as (sorted, paged) list")
  @GetMapping(
      value = {"/v6/projects"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Project> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "searchTerm", required = false) String searchTerm,
      @RequestParam(name = "label", required = false) String labelTerm,
      @RequestParam(name = "labelLanguage", required = false) Locale labelLanguage) {
    return super.find(pageNumber, pageSize, sortBy, searchTerm, labelTerm, labelLanguage);
  }

  @Operation(summary = "Get paged digital objects of a project")
  @GetMapping(
      value = {"/v6/projects/{uuid}/digitalobjects"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<DigitalObject> findDigitalObjects(
      @Parameter(example = "", description = "UUID of the project") @PathVariable("uuid")
          UUID projectUuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "searchTerm", required = false) String searchTerm) {
    PageRequest searchPageRequest = new PageRequest(searchTerm, pageNumber, pageSize);

    Project project = new Project();
    project.setUuid(projectUuid);
    return projectService.findDigitalObjects(project, searchPageRequest);
  }

  @Override
  @Operation(
      summary = "Get a project by namespace and id",
      description =
          "Separate namespace and id with a colon, e.g. foo:bar. It is also possible, to add a .json suffix, which will be ignored then")
  @GetMapping(
      value = {
        "/v6/projects/identifier/**",
        "/v5/projects/identifier/**",
        "/v3/projects/identifier/**",
        "/latest/projects/identifier/**"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Project> getByIdentifier(HttpServletRequest request)
      throws IdentifiableServiceException, ValidationException {
    return super.getByIdentifier(request);
  }

  @Operation(summary = "Get a project by uuid")
  @GetMapping(
      value = {
        "/v6/projects/{uuid}",
        "/v5/projects/{uuid}",
        "/v2/projects/{uuid}",
        "/latest/projects/{uuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Project> getByUuid(
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
      project = projectService.getByUuid(uuid);
    } else {
      project = projectService.getByUuidAndLocale(uuid, pLocale);
    }
    return new ResponseEntity<>(project, HttpStatus.OK);
  }

  @Operation(summary = "Get languages of all projects")
  @GetMapping(
      value = {
        "/v6/projects/languages",
        "/v5/projects/languages",
        "/v3/projects/languages",
        "/latest/projects/languages"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Locale> getLanguages() {
    return projectService.getLanguages();
  }

  @Operation(summary = "Remove an existing digital object from an existing project")
  @DeleteMapping(
      value = {
        "/v6/projects/{uuid}/digitalobjects/{digitalObjectUuid}",
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
      value = {"/v6/projects", "/v5/projects", "/v2/projects", "/latest/projects"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Project save(@RequestBody Project project, BindingResult errors)
      throws IdentifiableServiceException, ValidationException {
    return projectService.save(project);
  }

  @Operation(summary = "Save existing digital objects into an existing project")
  @PutMapping(
      value = {
        "/v6/projects/{uuid}/digitalobjects",
        "/v5/projects/{uuid}/digitalobjects",
        "/v3/projects/{uuid}/digitalobjects",
        "/latest/projects/{uuid}/digitalobjects"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity setDigitalObjects(
      @Parameter(example = "", description = "UUID of the project") @PathVariable("uuid")
          UUID projectUuid,
      @Parameter(example = "", description = "List of the digital objects") @RequestBody
          List<DigitalObject> digitalObjects) {
    Project project = new Project();
    project.setUuid(projectUuid);

    boolean successful = projectService.setDigitalObjects(project, digitalObjects);

    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Update an project")
  @PutMapping(
      value = {
        "/v6/projects/{uuid}",
        "/v5/projects/{uuid}",
        "/v2/projects/{uuid}",
        "/latest/projects/{uuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Project update(@PathVariable UUID uuid, @RequestBody Project project, BindingResult errors)
      throws IdentifiableServiceException, ValidationException {
    assert Objects.equals(uuid, project.getUuid());
    return projectService.update(project);
  }
}
