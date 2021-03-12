package de.digitalcollections.cudami.server.controller.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.TopicService;
import de.digitalcollections.model.filter.FilterCriterion;
import de.digitalcollections.model.filter.Filtering;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.Topic;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.Sorting;
import de.digitalcollections.model.view.BreadcrumbNavigation;
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
import org.springframework.util.StringUtils;
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
@Api(description = "The topic controller", name = "Topic controller")
public class TopicController {

  @Autowired private LocaleService localeService;

  @Autowired private TopicService service;

  @ApiMethod(description = "Add an existing topic to an existing parent topic")
  @PostMapping(
      value = {
        "/latest/topics/{parentTopicUuid}/subtopic/{subtopicUuid}",
        "/v3/topics/{parentTopicUuid}/subtopic/{subtopicUuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public ResponseEntity<Boolean> addChild(
      @ApiPathParam(name = "parentTopicUuid", description = "The uuid of the parent topic")
          @PathVariable
          UUID parentTopicUuid,
      @ApiPathParam(name = "subtopicUuid", description = "The uuid of the subtopic") @PathVariable
          UUID subtopicUuid)
      throws IdentifiableServiceException {
    boolean successful = service.addChild(parentTopicUuid, subtopicUuid);

    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }

  @ApiMethod(description = "Get count of topics")
  @GetMapping(
      value = {"/latest/topics/count", "/v2/topics/count"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public long count() {
    return service.count();
  }

  @ApiMethod(description = "Get all topics")
  @GetMapping(
      value = {"/latest/topics", "/v2/topics"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public PageResponse<Topic> findAll(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy) {
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    return service.find(pageRequest);
  }

  @ApiMethod(description = "Get all top topics")
  @GetMapping(
      value = {"/latest/topics/top", "/v3/topics/top"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public PageResponse<Topic> findAllTop(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "searchTerm", required = false) String searchTerm) {
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }

    if (StringUtils.hasText(searchTerm)) {
      SearchPageRequest searchPageRequest =
          new SearchPageRequest(
              searchTerm,
              pageRequest.getPageNumber(),
              pageRequest.getPageSize(),
              pageRequest.getSorting());
      return service.findRootNodes(searchPageRequest);
    } else {
      return service.getRootNodes(pageRequest);
    }
  }

  @ApiMethod(description = "Get topic by uuid (and optional locale)")
  @GetMapping(
      value = {
        "/latest/topics/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}",
        "/v2/topics/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public ResponseEntity<Topic> findById(
      @ApiPathParam(
              description = "UUID of the topic, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @ApiQueryParam(
              name = "pLocale",
              description =
                  "Desired locale, e.g. <tt>de_DE</tt>. If unset, contents in all languages will be returned")
          @RequestParam(name = "pLocale", required = false)
          Locale pLocale)
      throws IdentifiableServiceException {
    Topic topic;
    if (pLocale == null) {
      topic = service.get(uuid);
    } else {
      topic = service.get(uuid, pLocale);
    }
    return new ResponseEntity<>(topic, HttpStatus.OK);
  }

  @ApiMethod(description = "Get topic by refId")
  @GetMapping(value = {"/latest/topics/{refId:[0-9]+}", "/v3/topics/{refId:[0-9]+}"})
  @ApiResponseObject
  public ResponseEntity<Topic> findByRefId(
      @ApiPathParam(description = "refId of the topic, e.g. <tt>42</tt>") @PathVariable long refId)
      throws IdentifiableServiceException {
    Topic topic = service.getByRefId(refId);
    return findById(topic.getUuid(), null);
  }

  @ApiMethod(description = "Get the breadcrumb for a topic")
  @GetMapping(
      value = {"/latest/topics/{uuid}/breadcrumb", "/v3/topics/{uuid}/breadcrumb"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public ResponseEntity<BreadcrumbNavigation> getBreadcrumb(
      @ApiPathParam(
              description = "UUID of the topic, e.g. <tt>6119d8e9-9c92-4091-8dcb-bc4053385406</tt>")
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

  @ApiMethod(description = "Get subtopics of topic")
  @GetMapping(
      value = {"/latest/topics/{uuid}/children", "/v3/topics/{uuid}/children"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  List<Topic> getChildren(@PathVariable UUID uuid) {
    return service.getChildren(uuid);
  }

  @ApiMethod(description = "Get all entities of topic")
  @GetMapping(
      value = {"/latest/topics/{uuid}/entities/all", "/v3/topics/{uuid}/entities/all"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public List<Entity> getEntities(
      @ApiPathParam(name = "uuid", description = "The uuid of the topic") @PathVariable UUID uuid) {
    return service.getAllEntities(uuid);
  }

  @ApiMethod(description = "Get paged entities of a topic")
  @GetMapping(
      value = {"/latest/topics/{uuid}/entities", "/v3/topics/{uuid}/entities"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public PageResponse<Entity> getEntities(
      @ApiPathParam(description = "UUID of the topic") @PathVariable("uuid") UUID topicUuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "entityType", required = false) FilterCriterion<String> entityType) {
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize, new Sorting());
    if (entityType != null) {
      Filtering filtering = Filtering.defaultBuilder().add("entityType", entityType).build();
      pageRequest.setFiltering(filtering);
    }
    return service.getEntities(topicUuid, pageRequest);
  }

  @ApiMethod(description = "Get file resources of topic")
  @GetMapping(
      value = {"/latest/topics/{uuid}/fileresources", "/v3/topics/{uuid}/fileresources"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public List<FileResource> getFileResources(@PathVariable UUID uuid) {
    return service.getFileResources(uuid);
  }

  @ApiMethod(description = "Get parent topic of topic")
  @GetMapping(
      value = {"/latest/topics/{uuid}/parent", "/v3/topics/{uuid}/parent"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  Topic getParent(@PathVariable UUID uuid) {
    return service.getParent(uuid);
  }

  @ApiMethod(description = "Get subtopics of topic")
  @GetMapping(
      value = {"/v2/topics/{uuid}/subtopics"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  ResponseEntity<String> getSubtopics(@PathVariable UUID uuid) {
    return new ResponseEntity<>(
        "no longer supported. use '/v3/topics/{uuid}/children' endpoint, returning list of child-topics",
        HttpStatus.GONE);
  }

  @ApiMethod(description = "Get topics an entity is linked to")
  @GetMapping(
      value = {"/latest/topics/entity/{uuid}", "/v3/topics/entity/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  List<Topic> getTopicsOfEntity(@PathVariable UUID uuid) {
    return service.getTopicsOfEntity(uuid);
  }

  @ApiMethod(description = "Get topics a fileresource is linked to")
  @GetMapping(
      value = {"/latest/topics/fileresource/{uuid}", "/v3/topics/fileresource/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  List<Topic> getTopicsOfFileResource(@PathVariable UUID uuid) {
    return service.getTopicsOfFileResource(uuid);
  }

  @ApiMethod(description = "Get languages of all top topics")
  @GetMapping(
      value = {"/latest/topics/top/languages", "/v3/topics/top/languages"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public List<Locale> getTopTopicsLanguages() {
    return service.getRootNodesLanguages();
  }

  @ApiMethod(description = "Remove child-relation of the given subtopic to the given parent topic")
  @DeleteMapping(
      value = {
        "/latest/topics/{parentTopicUuid}/children/{subtopicUuid}",
        "/v3/topics/{parentTopicUuid}/children/{subtopicUuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  ResponseEntity<Boolean> removeChild(
      @ApiPathParam(name = "parentTopicUuid", description = "The uuid of the parent topic")
          @PathVariable
          UUID parentTopicUuid,
      @ApiPathParam(name = "subtopicUuid", description = "The uuid of the subtopic") @PathVariable
          UUID subtopicUuid) {
    boolean successful = service.removeChild(parentTopicUuid, subtopicUuid);
    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }

  @ApiMethod(description = "Save a newly created topic")
  @PostMapping(
      value = {"/latest/topics", "/v2/topics"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public Topic save(@RequestBody Topic topic, BindingResult errors)
      throws IdentifiableServiceException {
    return service.save(topic);
  }

  @ApiMethod(description = "Save entities of topic")
  @PostMapping(
      value = {"/latest/topics/{uuid}/entities", "/v3/topics/{uuid}/entities"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public List<Entity> saveEntities(@PathVariable UUID uuid, @RequestBody List<Entity> entities) {
    return service.saveEntities(uuid, entities);
  }

  @ApiMethod(description = "Save fileresources of topic")
  @PostMapping(
      value = {"/latest/topics/{uuid}/fileresources", "/v3/topics/{uuid}/fileresources"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public List<FileResource> saveFileresources(
      @PathVariable UUID uuid, @RequestBody List<FileResource> fileResources) {
    return service.saveFileResources(uuid, fileResources);
  }

  @ApiMethod(description = "Save a newly created topic and add it to parent")
  @PostMapping(
      value = {
        "/latest/topics/{parentTopicUuid}/subtopic",
        "/v3/topics/{parentTopicUuid}/subtopic"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public Topic saveWithParentTopic(
      @ApiPathParam(name = "parentTopicUuid", description = "The uuid of the parent topic")
          @PathVariable
          UUID parentTopicUuid,
      @RequestBody Topic topic,
      BindingResult errors)
      throws IdentifiableServiceException {
    return service.saveWithParent(topic, parentTopicUuid);
  }

  @ApiMethod(description = "Update a topic")
  @PutMapping(
      value = {"/latest/topics/{uuid}", "/v2/topics/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public Topic update(@PathVariable UUID uuid, @RequestBody Topic topic, BindingResult errors)
      throws IdentifiableServiceException {
    assert Objects.equals(uuid, topic.getUuid());
    return service.update(topic);
  }
}
