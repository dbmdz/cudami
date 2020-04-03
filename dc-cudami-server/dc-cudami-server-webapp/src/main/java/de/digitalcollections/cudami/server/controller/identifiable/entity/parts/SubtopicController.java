package de.digitalcollections.cudami.server.controller.identifiable.entity.parts;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.parts.SubtopicService;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.parts.Subtopic;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.Sorting;
import de.digitalcollections.model.api.paging.enums.Direction;
import de.digitalcollections.model.api.paging.enums.NullHandling;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(description = "The subtopic controller", name = "Subtopic controller")
public class SubtopicController {

  @Autowired private SubtopicService service;

  @ApiMethod(description = "Get all subtopics")
  @RequestMapping(
      value = {"/latest/subtopics", "/v2/subtopics"},
      produces = "application/json",
      method = RequestMethod.GET)
  @ApiResponseObject
  public PageResponse<Subtopic> findAll(
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
    return service.find(pageRequest);
  }

  // Test-URL: http://localhost:9000/latest/subtopics/599a120c-2dd5-11e8-b467-0ed5f89f718b
  @ApiMethod(
      description =
          "Get a subtopic as JSON or XML, depending on extension or <tt>format</tt> request parameter or accept header")
  @RequestMapping(
      value = {"/latest/subtopics/{uuid}", "/v2/subtopics/{uuid}"},
      produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
      method = RequestMethod.GET)
  @ApiResponseObject
  public ResponseEntity<Subtopic> getSubtopic(
      @ApiPathParam(
              description =
                  "UUID of the content node, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @ApiQueryParam(
              name = "pLocale",
              description =
                  "Desired locale, e.g. <tt>de_DE</tt>. If unset, contents in all languages will be returned")
          @RequestParam(name = "pLocale", required = false)
          Locale pLocale)
      throws IdentifiableServiceException {

    Subtopic subtopic;
    if (pLocale == null) {
      subtopic = service.get(uuid);
    } else {
      subtopic = service.get(uuid, pLocale);
    }
    return new ResponseEntity<>(subtopic, HttpStatus.OK);
  }

  @ApiMethod(description = "Save a newly created top-level subtopic")
  @RequestMapping(
      value = {
        "/latest/topics/{parentTopicUuid}/subtopic",
        "/v2/topics/{parentTopicUuid}/subtopic"
      },
      produces = "application/json",
      method = RequestMethod.POST)
  @ApiResponseObject
  public Subtopic saveWithParentTopic(
      @PathVariable UUID parentTopicUuid, @RequestBody Subtopic subtopic, BindingResult errors)
      throws IdentifiableServiceException {
    return service.saveWithParentTopic(subtopic, parentTopicUuid);
  }

  @ApiMethod(description = "Save a newly created subtopic")
  @RequestMapping(
      value = {
        "/latest/subtopics/{parentSubtopicUuid}/subtopic",
        "/v2/subtopics/{parentSubtopicUuid}/subtopic"
      },
      produces = "application/json",
      method = RequestMethod.POST)
  @ApiResponseObject
  public Subtopic saveWithParentSubtopic(
      @PathVariable UUID parentSubtopicUuid, @RequestBody Subtopic subtopic, BindingResult errors)
      throws IdentifiableServiceException {
    return service.saveWithParentSubtopic(subtopic, parentSubtopicUuid);
  }

  @ApiMethod(description = "Update a content node")
  @RequestMapping(
      value = {"/latest/subtopics/{uuid}", "/v2/subtopics/{uuid}"},
      produces = "application/json",
      method = RequestMethod.PUT)
  @ApiResponseObject
  public Subtopic update(
      @PathVariable UUID uuid, @RequestBody Subtopic subtopic, BindingResult errors)
      throws IdentifiableServiceException {
    assert Objects.equals(uuid, subtopic.getUuid());
    return service.update(subtopic);
  }

  @ApiMethod(description = "Get count of content nodes")
  @RequestMapping(
      value = {"/latest/subtopics/count", "/v2/subtopics/count"},
      produces = "application/json",
      method = RequestMethod.GET)
  @ApiResponseObject
  public long count() {
    return service.count();
  }

  @ApiMethod(description = "Get child content nodes of content node")
  @RequestMapping(
      value = {"/latest/subtopics/{uuid}/children", "/v2/subtopics/{uuid}/children"},
      produces = "application/json",
      method = RequestMethod.GET)
  @ApiResponseObject
  List<Subtopic> getChildren(@PathVariable UUID uuid) {
    return service.getChildren(uuid);
  }

  @ApiMethod(description = "Get entities of content node")
  @RequestMapping(
      value = {"/latest/subtopics/{uuid}/entities", "/v2/subtopics/{uuid}/entities"},
      produces = "application/json",
      method = RequestMethod.GET)
  @ApiResponseObject
  public List<Entity> getEntities(@PathVariable UUID uuid) {
    return service.getEntities(uuid);
  }

  // FIXME
  @ApiMethod(description = "Save entities of content node")
  @PostMapping(
      value = {"/latest/subtopics/{uuid}/entities", "/v2/subtopics/{uuid}/entities"},
      produces = "application/json")
  @ApiResponseObject
  public List<Entity> saveEntities(@PathVariable UUID uuid, @RequestBody List<Entity> entities) {
    return service.saveEntities(uuid, entities);
  }

  @ApiMethod(description = "Get file resources of content node")
  @RequestMapping(
      value = {"/latest/subtopics/{uuid}/fileresources", "/v2/subtopics/{uuid}/fileresources"},
      produces = "application/json",
      method = RequestMethod.GET)
  @ApiResponseObject
  public List<FileResource> getFileResources(@PathVariable UUID uuid) {
    return service.getFileResources(uuid);
  }

  // FIXME
  @ApiMethod(description = "Save fileresources of content node")
  @PostMapping(
      value = {"/latest/subtopics/{uuid}/fileresources", "/v2/subtopics/{uuid}/fileresources"},
      produces = "application/json")
  @ApiResponseObject
  public List<FileResource> saveFileresources(
      @PathVariable UUID uuid, @RequestBody List<FileResource> fileResources) {
    return service.saveFileResources(uuid, fileResources);
  }

  @ApiMethod(description = "Get parent content node of content node")
  @RequestMapping(
      value = {"/latest/subtopics/{uuid}/parent", "/v2/subtopics/{uuid}/parent"},
      produces = "application/json",
      method = RequestMethod.GET)
  @ApiResponseObject
  Subtopic getParent(@PathVariable UUID uuid) {
    return service.getParent(uuid);
  }

  //  @ApiMethod(description = "add identifiable to content node")
  //  @PostMapping(value = {"/latest/subtopics/{uuid}/identifiables/{identifiableUuid}",
  // "/v2/subtopics/{uuid}/identifiables/{identifiableUuid}"})
  //  @ResponseStatus(value = HttpStatus.OK)
  //  @ApiResponseObject
  //  public void addIdentifiable(@PathVariable UUID uuid, @PathVariable UUID identifiableUuid) {
  //    service.addIdentifiable(uuid, identifiableUuid);
  //  }
}
