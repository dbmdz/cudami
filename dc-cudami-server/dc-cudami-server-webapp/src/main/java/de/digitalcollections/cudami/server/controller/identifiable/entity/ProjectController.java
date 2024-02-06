package de.digitalcollections.cudami.server.controller.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.ProjectService;
import de.digitalcollections.cudami.server.controller.ParameterHelper;
import de.digitalcollections.cudami.server.controller.identifiable.AbstractIdentifiableController;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.validation.ValidationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Project controller")
public class ProjectController extends AbstractIdentifiableController<Project> {

  private final ProjectService service;

  public ProjectController(ProjectService projectService) {
    this.service = projectService;
  }

  @Operation(summary = "Add an existing digital object to an existing project")
  @PostMapping(
      value = {
        "/v6/projects/{uuid:"
            + ParameterHelper.UUID_PATTERN
            + "}/digitalobjects/{digitalObjectUuid}",
        "/v5/projects/{uuid:"
            + ParameterHelper.UUID_PATTERN
            + "}/digitalobjects/{digitalObjectUuid}",
        "/v3/projects/{uuid:"
            + ParameterHelper.UUID_PATTERN
            + "}/digitalobjects/{digitalObjectUuid}",
        "/latest/projects/{uuid:"
            + ParameterHelper.UUID_PATTERN
            + "}/digitalobjects/{digitalObjectUuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity addDigitalObject(
      @Parameter(example = "", description = "UUID of the project") @PathVariable("uuid")
          UUID projectUuid,
      @Parameter(example = "", description = "UUID of the digital object")
          @PathVariable("digitalObjectUuid")
          UUID digitalObjectUuid)
      throws ServiceException {
    DigitalObject digitalObject = new DigitalObject();
    digitalObject.setUuid(digitalObjectUuid);

    boolean successful = service.addDigitalObject(buildExampleWithUuid(projectUuid), digitalObject);
    return successful
        ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
        : new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Add existing digital objects to an existing project")
  @PostMapping(
      value = {
        "/v6/projects/{uuid:" + ParameterHelper.UUID_PATTERN + "}/digitalobjects",
        "/v5/projects/{uuid:" + ParameterHelper.UUID_PATTERN + "}/digitalobjects",
        "/v3/projects/{uuid:" + ParameterHelper.UUID_PATTERN + "}/digitalobjects",
        "/latest/projects/{uuid:" + ParameterHelper.UUID_PATTERN + "}/digitalobjects"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity addDigitalObjects(
      @Parameter(example = "", description = "UUID of the project") @PathVariable("uuid")
          UUID projectUuid,
      @Parameter(example = "", description = "List of the digital objects") @RequestBody
          List<DigitalObject> digitalObjects)
      throws ServiceException {
    boolean successful =
        service.addDigitalObjects(buildExampleWithUuid(projectUuid), digitalObjects);
    return successful
        ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
        : new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @Operation(
      summary = "Delete an existing project and the identifiers, which belong to this project")
  @DeleteMapping(
      value = {
        "/v6/projects/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/projects/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v3/projects/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/latest/projects/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity delete(
      @Parameter(example = "", description = "UUID of the project") @PathVariable("uuid") UUID uuid)
      throws ConflictException, ServiceException {
    return super.delete(uuid);
  }

  @Operation(summary = "Get all projects as (paged, sorted, filtered) list")
  @GetMapping(
      value = {"/v6/projects"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Project> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "filter", required = false) List<FilterCriterion> filterCriteria,
      @RequestParam(name = "filtering", required = false) Filtering filtering)
      throws ServiceException {
    return super.find(pageNumber, pageSize, sortBy, filterCriteria, filtering);
  }

  @Operation(summary = "Get all digital objects of a project as (paged, sorted, filtered) list")
  @GetMapping(
      value = {"/v6/projects/{uuid:" + ParameterHelper.UUID_PATTERN + "}/digitalobjects"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<DigitalObject> findDigitalObjects(
      @Parameter(example = "", description = "UUID of the project") @PathVariable("uuid")
          UUID projectUuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "filter", required = false) List<FilterCriterion> filterCriteria,
      @RequestParam(name = "filtering", required = false) Filtering filtering)
      throws ServiceException {
    PageRequest pageRequest =
        createPageRequest(
            DigitalObject.class, pageNumber, pageSize, sortBy, filterCriteria, filtering);
    return service.findDigitalObjects(buildExampleWithUuid(projectUuid), pageRequest);
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
      throws ServiceException, ValidationException {
    return super.getByIdentifier(request);
  }

  @Operation(summary = "Get a project by uuid")
  @GetMapping(
      value = {
        "/v6/projects/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/projects/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v2/projects/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/latest/projects/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
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
      throws ServiceException {
    if (pLocale == null) {
      return super.getByUuid(uuid);
    } else {
      return super.getByUuidAndLocale(uuid, pLocale);
    }
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
  public List<Locale> getLanguages() throws ServiceException {
    return super.getLanguages();
  }

  @Override
  protected IdentifiableService<Project> getService() {
    return service;
  }

  @Operation(summary = "Remove an existing digital object from an existing project")
  @DeleteMapping(
      value = {
        "/v6/projects/{uuid:"
            + ParameterHelper.UUID_PATTERN
            + "}/digitalobjects/{digitalObjectUuid}",
        "/v5/projects/{uuid:"
            + ParameterHelper.UUID_PATTERN
            + "}/digitalobjects/{digitalObjectUuid}",
        "/v3/projects/{uuid:"
            + ParameterHelper.UUID_PATTERN
            + "}/digitalobjects/{digitalObjectUuid}",
        "/latest/projects/{uuid:"
            + ParameterHelper.UUID_PATTERN
            + "}/digitalobjects/{digitalObjectUuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity removeDigitalObject(
      @Parameter(example = "", description = "UUID of the project") @PathVariable("uuid")
          UUID projectUuid,
      @Parameter(example = "", description = "UUID of the digital object")
          @PathVariable("digitalObjectUuid")
          UUID digitalObjectUuid)
      throws ServiceException {
    DigitalObject digitalObject = new DigitalObject();
    digitalObject.setUuid(digitalObjectUuid);

    boolean successful =
        service.removeDigitalObject(buildExampleWithUuid(projectUuid), digitalObject);
    return successful
        ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
        : new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Save a newly created project")
  @PostMapping(
      value = {"/v6/projects", "/v5/projects", "/v2/projects", "/latest/projects"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Project save(@RequestBody Project project, BindingResult errors)
      throws ServiceException, ValidationException {
    return super.save(project, errors);
  }

  @Operation(summary = "Save existing digital objects into an existing project")
  @PutMapping(
      value = {
        "/v6/projects/{uuid:" + ParameterHelper.UUID_PATTERN + "}/digitalobjects",
        "/v5/projects/{uuid:" + ParameterHelper.UUID_PATTERN + "}/digitalobjects",
        "/v3/projects/{uuid:" + ParameterHelper.UUID_PATTERN + "}/digitalobjects",
        "/latest/projects/{uuid:" + ParameterHelper.UUID_PATTERN + "}/digitalobjects"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity setDigitalObjects(
      @Parameter(example = "", description = "UUID of the project") @PathVariable("uuid")
          UUID projectUuid,
      @Parameter(example = "", description = "List of the digital objects") @RequestBody
          List<DigitalObject> digitalObjects)
      throws ServiceException {
    boolean successful =
        service.setDigitalObjects(buildExampleWithUuid(projectUuid), digitalObjects);
    return successful
        ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
        : new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Update an project")
  @PutMapping(
      value = {
        "/v6/projects/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/projects/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v2/projects/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/latest/projects/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Project update(@PathVariable UUID uuid, @RequestBody Project project, BindingResult errors)
      throws ServiceException, ValidationException {
    return super.update(uuid, project, errors);
  }
}
