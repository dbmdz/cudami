package de.digitalcollections.cudami.server.controller.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.ProjectService;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.Sorting;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiPathParam;
import org.jsondoc.core.annotation.ApiQueryParam;
import org.jsondoc.core.annotation.ApiResponseObject;
import org.springframework.beans.factory.annotation.Autowired;
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
@Api(description = "The project controller", name = "Project controller")
public class ProjectController {

  private ProjectService projectService;

  @Autowired
  public ProjectController(ProjectService projectService) {
    this.projectService = projectService;
  }

  @ApiMethod(description = "Add an existing digital object to an existing project")
  @PostMapping(
      value = {
        "/latest/projects/{uuid}/digitalobjects/{digitalObjectUuid}",
        "/v3/projects/{uuid}/digitalobjects/{digitalObjectUuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public ResponseEntity addDigitalObject(
      @ApiPathParam(description = "UUID of the project") @PathVariable("uuid") UUID projectUuid,
      @ApiPathParam(description = "UUID of the digital object") @PathVariable("digitalObjectUuid")
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

  @ApiMethod(description = "Add existing digital objects to an existing project")
  @PostMapping(
      value = {"/latest/projects/{uuid}/digitalobjects", "/v3/projects/{uuid}/digitalobjects"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public ResponseEntity addDigitalObjects(
      @ApiPathParam(description = "UUID of the project") @PathVariable("uuid") UUID projectUuid,
      @ApiPathParam(description = "List of the digital objects") @RequestBody
          List<DigitalObject> digitalObjects) {
    Project project = new Project();
    project.setUuid(projectUuid);

    boolean successful = projectService.addDigitalObjects(project, digitalObjects);

    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }

  @ApiMethod(
      description = "Delete an existing project and the identifiers, which belong to this project")
  @DeleteMapping(
      value = {"/latest/projects/{uuid}", "/v3/projects/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public ResponseEntity deleteProject(
      @ApiPathParam(description = "UUID of the project") @PathVariable("uuid") UUID uuid) {

    projectService.delete(uuid);

    return new ResponseEntity<>(HttpStatus.OK);
  }

  @ApiMethod(description = "Get all projects in reduced form (no identifiers)")
  @GetMapping(
      value = {"/latest/projects", "/v2/projects"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
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

  @ApiMethod(description = "Get all projects as list")
  @GetMapping(
      value = {"/latest/projectlist", "/v2/projectlist"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public List<Project> findAllReducedAsList() {
    // TODO test if reduced is sufficient for use cases...
    return projectService.findAllReduced();
  }

  @ApiMethod(description = "Get project by namespace and id")
  @GetMapping(
      value = {
        "/latest/projects/identifier/{namespace}:{id}",
        "/v3/projects/identifier/{namespace}:{id}"
      },
      produces = "application/json")
  @ApiResponseObject
  public Project findByIdentifier(@PathVariable String namespace, @PathVariable String id)
      throws IdentifiableServiceException {
    return projectService.getByIdentifier(namespace, id);
  }

  // Test-URL: http://localhost:9000/latest/projects/599a120c-2dd5-11e8-b467-0ed5f89f718b
  @ApiMethod(
      description =
          "Get an project as JSON or XML, depending on extension or <tt>format</tt> request parameter or accept header")
  @GetMapping(
      value = {"/latest/projects/{uuid}", "/v2/projects/{uuid}"},
      produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ApiResponseObject
  public ResponseEntity<Project> findByUuid(
      @ApiPathParam(
              description =
                  "UUID of the project, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @ApiQueryParam(
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

  @ApiMethod(description = "Get paged digital objects of a project")
  @GetMapping(
      value = {"/latest/projects/{uuid}/digitalobjects", "/v3/projects/{uuid}/digitalobjects"},
      produces = "application/json")
  @ApiResponseObject
  public PageResponse<DigitalObject> getDigitalObjects(
      @ApiPathParam(description = "UUID of the project") @PathVariable("uuid") UUID projectUuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize) {
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize, new Sorting());

    Project project = new Project();
    project.setUuid(projectUuid);
    return projectService.getDigitalObjects(project, pageRequest);
  }

  @ApiMethod(description = "Remove an existing digital object from an existing project")
  @DeleteMapping(
      value = {
        "/latest/projects/{uuid}/digitalobjects/{digitalObjectUuid}",
        "/v3/projects/{uuid}/digitalobjects/{digitalObjectUuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public ResponseEntity removeDigitalObject(
      @ApiPathParam(description = "UUID of the project") @PathVariable("uuid") UUID projectUuid,
      @ApiPathParam(description = "UUID of the digital object") @PathVariable("digitalObjectUuid")
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

  @ApiMethod(description = "Save a newly created project")
  @PostMapping(
      value = {"/latest/projects", "/v2/projects"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public Project save(@RequestBody Project project, BindingResult errors)
      throws IdentifiableServiceException {
    return projectService.save(project);
  }

  @ApiMethod(description = "Save existing digital objects into an existing project")
  @PutMapping(
      value = {"/latest/projects/{uuid}/digitalobjects", "/v3/projects/{uuid}/digitalobjects"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public ResponseEntity saveDigitalObjects(
      @ApiPathParam(description = "UUID of the project") @PathVariable("uuid") UUID projectUuid,
      @ApiPathParam(description = "List of the digital objects") @RequestBody
          List<DigitalObject> digitalObjects) {
    Project project = new Project();
    project.setUuid(projectUuid);

    boolean successful = projectService.saveDigitalObjects(project, digitalObjects);

    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }

  @ApiMethod(description = "Update an project")
  @PutMapping(
      value = {"/latest/projects/{uuid}", "/v2/projects/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public Project update(@PathVariable UUID uuid, @RequestBody Project project, BindingResult errors)
      throws IdentifiableServiceException {
    assert Objects.equals(uuid, project.getUuid());
    return projectService.update(project);
  }

  @ApiMethod(description = "Get languages of all projects")
  @GetMapping(
      value = {"/latest/projects/languages", "/v3/projects/languages"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public List<Locale> getLanguages() {
    return projectService.getLanguages();
  }
}
