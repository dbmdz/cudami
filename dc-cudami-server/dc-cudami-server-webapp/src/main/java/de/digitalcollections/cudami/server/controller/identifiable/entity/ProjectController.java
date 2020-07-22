package de.digitalcollections.cudami.server.controller.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.ProjectService;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.entity.Project;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.Sorting;
import de.digitalcollections.model.api.paging.enums.Direction;
import de.digitalcollections.model.api.paging.enums.NullHandling;
import de.digitalcollections.model.impl.identifiable.entity.DigitalObjectImpl;
import de.digitalcollections.model.impl.identifiable.entity.ProjectImpl;
import de.digitalcollections.model.impl.paging.OrderImpl;
import de.digitalcollections.model.impl.paging.PageRequestImpl;
import de.digitalcollections.model.impl.paging.SortingImpl;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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

  @ApiMethod(description = "Get all projects")
  @GetMapping(
      value = {"/latest/projects", "/v2/projects"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public PageResponse<Project> findAll(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortField", required = false, defaultValue = "uuid") String sortField,
      @RequestParam(name = "sortDirection", required = false, defaultValue = "ASC")
          Direction sortDirection,
      @RequestParam(name = "nullHandling", required = false, defaultValue = "NATIVE")
          NullHandling nullHandling) {
    OrderImpl order = new OrderImpl(sortDirection, sortField, nullHandling);
    Sorting sorting = new SortingImpl(order);
    PageRequest pageRequest = new PageRequestImpl(pageNumber, pageSize, sorting);
    return projectService.find(pageRequest);
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

  @ApiMethod(description = "Save a newly created project")
  @PostMapping(
      value = {"/latest/projects", "/v2/projects"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public Project save(@RequestBody Project project, BindingResult errors)
      throws IdentifiableServiceException {
    return projectService.save(project);
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

  @ApiMethod(description = "Add an existing digital object to an existing project")
  @PatchMapping(
      value = {
        "/latest/project/{uuid}/digitalobject/{digitalObjectUuid}",
        "/v3/projects/{uuid}/digitalobject/{digitalObjectUuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public ResponseEntity addDigitalObject(
      @ApiPathParam(description = "UUID of the project") @PathVariable("uuid") UUID projectUuid,
      @ApiPathParam(description = "UUID of the digital object") @PathVariable("digitalObjectUuid")
          UUID digitalObjectUuid) {
    ProjectImpl project = new ProjectImpl();
    project.setUuid(projectUuid);

    DigitalObjectImpl digitalObject = new DigitalObjectImpl();
    digitalObject.setUuid(digitalObjectUuid);

    boolean successful = projectService.addDigitalObject(project, digitalObject);

    if (successful) {
      return new ResponseEntity<>(HttpStatus.OK);
    }
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @ApiMethod(description = "Add existing digital objects to an existing project")
  @PatchMapping(
      value = {"/latest/projects/{uuid}/digitalobjects", "/v3/projects/{uuid}/digitalobjects"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public ResponseEntity addDigitalObjects(
      @ApiPathParam(description = "UUID of the project") @PathVariable("uuid") UUID projectUuid,
      @ApiPathParam(description = "List of the digital objects") @RequestBody
          List<DigitalObject> digitalObjects) {
    ProjectImpl project = new ProjectImpl();
    project.setUuid(projectUuid);

    boolean successful = projectService.addDigitalObjects(project, digitalObjects);

    if (successful) {
      return new ResponseEntity<>(HttpStatus.OK);
    }
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
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
    PageRequest pageRequest = new PageRequestImpl(pageNumber, pageSize, new SortingImpl());

    ProjectImpl project = new ProjectImpl();
    project.setUuid(projectUuid);
    return projectService.getDigitalObjects(project, pageRequest);
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
    ProjectImpl project = new ProjectImpl();
    project.setUuid(projectUuid);

    boolean successful = projectService.saveDigitalObjects(project, digitalObjects);

    if (successful) {
      return new ResponseEntity<>(HttpStatus.OK);
    }
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }
}
