package de.digitalcollections.cudami.server.controller.identifiable.entity.parts;

import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.parts.SubtopicService;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.Topic;
import de.digitalcollections.model.api.identifiable.entity.parts.Subtopic;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.Sorting;
import de.digitalcollections.model.api.paging.enums.Direction;
import de.digitalcollections.model.api.paging.enums.NullHandling;
import de.digitalcollections.model.api.view.BreadcrumbNavigation;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(description = "The subtopic controller", name = "Subtopic controller")
public class SubtopicController {

  @Autowired private LocaleService localeService;

  @Autowired private SubtopicService service;

  @ApiMethod(description = "Add an existing subtopic to an existing parent subtopic")
  @PostMapping(
      value = {
        "/latest/subtopics/{parentSubtopicUuid}/subtopic/{subtopicUuid}",
        "/v2/subtopics/{parentSubtopicUuid}/subtopic/{subtopicUuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public Subtopic addSubtopicToParentSubtopic(
      @ApiPathParam(name = "parentSubtopicUuid", description = "The uuid of the parent subtopic")
          @PathVariable
          UUID parentSubtopicUuid,
      @ApiPathParam(name = "subtopicUuid", description = "The uuid of the subtopic") @PathVariable
          UUID subtopicUuid)
      throws IdentifiableServiceException {
    return service.addSubtopicToParentSubtopic(subtopicUuid, parentSubtopicUuid);
  }

  @ApiMethod(description = "Add an existing subtopic to an existing topic")
  @PostMapping(
      value = {
        "/latest/topics/{parentTopicUuid}/subtopic/{subtopicUuid}",
        "/v2/topics/{parentTopicUuid}/subtopic/{subtopicUuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public Subtopic addSubtopicToParentTopic(
      @ApiPathParam(name = "parentTopicUuid", description = "The uuid of the parent topic")
          @PathVariable
          UUID parentTopicUuid,
      @ApiPathParam(name = "subtopicUuid", description = "The uuid of the subtopic") @PathVariable
          UUID subtopicUuid)
      throws IdentifiableServiceException {
    return service.addSubtopicToParentTopic(subtopicUuid, parentTopicUuid);
  }

  @ApiMethod(description = "Get count of subtopics")
  @GetMapping(
      value = {"/latest/subtopics/count", "/v2/subtopics/count"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public long count() {
    return service.count();
  }

  @ApiMethod(
      description = "Delete child-relation of the given subtopic to the given parent subtopic")
  @DeleteMapping(
      value = {
        "/latest/subtopics/{parentSubtopicUuid}/subtopic/{subtopicUuid}",
        "/v2/subtopics/{parentSubtopicUuid}/subtopic/{subtopicUuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  Integer deleteSubtopicFromParentSubtopic(
      @ApiPathParam(name = "parentSubtopicUuid", description = "The uuid of the parent subtopic")
          @PathVariable
          UUID parentSubtopicUuid,
      @ApiPathParam(name = "subtopicUuid", description = "The uuid of the subtopic") @PathVariable
          UUID subtopicUuid) {
    return service.deleteFromParentSubtopic(subtopicUuid, parentSubtopicUuid);
  }

  @ApiMethod(description = "Delete child-relation of the given subtopic to the given parent topic")
  @DeleteMapping(
      value = {
        "/latest/topic/{topicUuid}/subtopic/{subtopicUuid}",
        "/v2/topic/{topicUuid}/subtopic/{subtopicUuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  Integer deleteSubtopicFromParentTopic(
      @ApiPathParam(name = "topicUuid", description = "The uuid of the parent topic") @PathVariable
          UUID topicUuid,
      @ApiPathParam(name = "subtopicUuid", description = "The uuid of the subtopic") @PathVariable
          UUID subtopicUuid) {
    return service.deleteFromParentTopic(subtopicUuid, topicUuid);
  }

  @ApiMethod(description = "Get all subtopics")
  @GetMapping(
      value = {"/latest/subtopics", "/v2/subtopics"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public PageResponse<Subtopic> findAll(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortField", required = false) String sortField,
      @RequestParam(name = "sortDirection", required = false) Direction sortDirection,
      @RequestParam(name = "nullHandling", required = false, defaultValue = "NATIVE")
          NullHandling nullHandling) {
    Sorting sorting = null;
    if (sortField != null && sortDirection != null) {
      OrderImpl order = new OrderImpl(sortDirection, sortField, nullHandling);
      sorting = new SortingImpl(order);
    }
    PageRequest pageRequest = new PageRequestImpl(pageNumber, pageSize, sorting);
    return service.find(pageRequest);
  }

  @ApiMethod(description = "Get the breadcrumb for a subtopic")
  @GetMapping(
      value = {"/latest/subtopics/{uuid}/breadcrumb", "/v3/subtopics/{uuid}/breadcrumb"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public ResponseEntity<BreadcrumbNavigation> getBreadcrumb(
      @ApiPathParam(
              description =
                  "UUID of the subtopic, e.g. <tt>6119d8e9-9c92-4091-8dcb-bc4053385406</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @ApiQueryParam(
              name = "pLocale",
              description =
                  "Desired locale, e.g. <tt>de_DE</tt>. If unset, contents in all languages will be returned")
          @RequestParam(name = "pLocale", required = false)
          Locale pLocale) {

    BreadcrumbNavigation breadcrumbNavigation;

    if (pLocale == null) {
      breadcrumbNavigation = service.getBreadcrumbNavigation(uuid);
    } else {
      breadcrumbNavigation =
          service.getBreadcrumbNavigation(uuid, pLocale, localeService.getDefaultLocale());
    }

    if (breadcrumbNavigation == null || breadcrumbNavigation.getNavigationItems().isEmpty()) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    return new ResponseEntity<>(breadcrumbNavigation, HttpStatus.OK);
  }

  @ApiMethod(description = "Get child subtopics of subtopic")
  @GetMapping(
      value = {"/latest/subtopics/{uuid}/children", "/v2/subtopics/{uuid}/children"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  List<Subtopic> getChildren(@PathVariable UUID uuid) {
    return service.getChildren(uuid);
  }

  @ApiMethod(description = "Get entities of subtopic")
  @GetMapping(
      value = {"/latest/subtopics/{uuid}/entities", "/v2/subtopics/{uuid}/entities"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public List<Entity> getEntities(
      @ApiPathParam(name = "uuid", description = "The uuid of the subtopic") @PathVariable
          UUID uuid) {
    return service.getEntities(uuid);
  }

  @ApiMethod(description = "Get file resources of subtopic")
  @GetMapping(
      value = {"/latest/subtopics/{uuid}/fileresources", "/v2/subtopics/{uuid}/fileresources"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public List<FileResource> getFileResources(@PathVariable UUID uuid) {
    return service.getFileResources(uuid);
  }

  @ApiMethod(description = "Get parent subtopic of subtopic")
  @GetMapping(
      value = {"/latest/subtopics/{uuid}/parent", "/v2/subtopics/{uuid}/parent"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  Subtopic getParent(@PathVariable UUID uuid) {
    return service.getParent(uuid);
  }

  // Test-URL: http://localhost:9000/latest/subtopics/599a120c-2dd5-11e8-b467-0ed5f89f718b
  @ApiMethod(
      description =
          "Get a subtopic as JSON or XML, depending on extension or <tt>format</tt> request parameter or accept header")
  @GetMapping(
      value = {"/latest/subtopics/{uuid}", "/v2/subtopics/{uuid}"},
      produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ApiResponseObject
  public ResponseEntity<Subtopic> getSubtopic(
      @ApiPathParam(
              description =
                  "UUID of the subtopic, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
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

  @ApiMethod(description = "Get subtopics an entity is linked to")
  @GetMapping(
      value = {"/latest/subtopics/entity/{uuid}", "/v2/subtopics/entity/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  List<Subtopic> getSubtopicsOfEntity(@PathVariable UUID uuid) {
    return service.getSubtopicsOfEntity(uuid);
  }

  @ApiMethod(description = "Get subtopics a fileresource is linked to")
  @GetMapping(
      value = {"/latest/subtopics/fileresource/{uuid}", "/v2/subtopics/fileresource/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  List<Subtopic> getSubtopicsOfFileResource(@PathVariable UUID uuid) {
    return service.getSubtopicsOfFileResource(uuid);
  }

  @ApiMethod(description = "Get the topic of a subtopic")
  @GetMapping(
      value = {"/latest/subtopics/{uuid}/topic", "/v3/subtopics/{uuid}/topic"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public Topic getTopic(
      @ApiPathParam(
              description =
                  "UUID of the subtopic, e.g. <tt>6119d8e9-9c92-4091-8dcb-bc4053385406</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @ApiQueryParam(
              name = "pLocale",
              description =
                  "Desired locale, e.g. <tt>de_DE</tt>. If unset, contents in all languages will be returned")
          @RequestParam(name = "pLocale", required = false)
          Locale pLocale) {
    return service.getTopic(uuid);
  }

  // FIXME
  @ApiMethod(description = "Save entities of subtopic")
  @PostMapping(
      value = {"/latest/subtopics/{uuid}/entities", "/v2/subtopics/{uuid}/entities"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public List<Entity> saveEntities(@PathVariable UUID uuid, @RequestBody List<Entity> entities) {
    return service.saveEntities(uuid, entities);
  }

  // FIXME
  @ApiMethod(description = "Save fileresources of subtopic")
  @PostMapping(
      value = {"/latest/subtopics/{uuid}/fileresources", "/v2/subtopics/{uuid}/fileresources"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public List<FileResource> saveFileresources(
      @PathVariable UUID uuid, @RequestBody List<FileResource> fileResources) {
    return service.saveFileResources(uuid, fileResources);
  }

  @ApiMethod(description = "Save a newly created subtopic")
  @PostMapping(
      value = {
        "/latest/subtopics/{parentSubtopicUuid}/subtopic",
        "/v2/subtopics/{parentSubtopicUuid}/subtopic"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public Subtopic saveWithParentSubtopic(
      @ApiPathParam(name = "parentSubtopicUuid", description = "The uuid of the parent subtopic")
          @PathVariable
          UUID parentSubtopicUuid,
      @RequestBody Subtopic subtopic)
      throws IdentifiableServiceException {
    return service.saveWithParent(subtopic, parentSubtopicUuid);
  }

  @ApiMethod(description = "Save a newly created top-level subtopic")
  @PostMapping(
      value = {
        "/latest/topics/{parentTopicUuid}/subtopic",
        "/v2/topics/{parentTopicUuid}/subtopic"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public Subtopic saveWithParentTopic(
      @ApiPathParam(name = "parentTopicUuid", description = "The uuid of the parent topic")
          @PathVariable
          UUID parentTopicUuid,
      @RequestBody Subtopic subtopic,
      BindingResult errors)
      throws IdentifiableServiceException {
    return service.saveWithParentTopic(subtopic, parentTopicUuid);
  }

  @ApiMethod(description = "Update a subtopic")
  @PutMapping(
      value = {"/latest/subtopics/{uuid}", "/v2/subtopics/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public Subtopic update(
      @ApiPathParam(name = "uuid", description = "The uuid of the subtopic") @PathVariable
          UUID uuid,
      @RequestBody Subtopic subtopic,
      BindingResult errors)
      throws IdentifiableServiceException {
    assert Objects.equals(uuid, subtopic.getUuid());
    return service.update(subtopic);
  }
}
