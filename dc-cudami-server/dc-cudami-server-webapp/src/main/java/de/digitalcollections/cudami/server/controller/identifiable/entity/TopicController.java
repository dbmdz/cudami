package de.digitalcollections.cudami.server.controller.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.TopicService;
import de.digitalcollections.cudami.server.controller.ParameterHelper;
import de.digitalcollections.cudami.server.controller.identifiable.AbstractIdentifiableController;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.Topic;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import de.digitalcollections.model.view.BreadcrumbNavigation;
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
@Tag(name = "Topic controller")
public class TopicController extends AbstractIdentifiableController<Topic> {

  private final LocaleService localeService;
  private final TopicService topicService;

  public TopicController(LocaleService localeService, TopicService topicService) {
    this.localeService = localeService;
    this.topicService = topicService;
  }

  @Override
  protected IdentifiableService<Topic> getService() {
    return topicService;
  }

  @Operation(summary = "Add an existing topic to an existing parent topic")
  @PostMapping(
      value = {
        "/v6/topics/{parentTopicUuid}/subtopic/{subtopicUuid}",
        "/v5/topics/{parentTopicUuid}/subtopic/{subtopicUuid}",
        "/v3/topics/{parentTopicUuid}/subtopic/{subtopicUuid}",
        "/latest/topics/{parentTopicUuid}/subtopic/{subtopicUuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Boolean> addChild(
      @Parameter(name = "parentTopicUuid", description = "The uuid of the parent topic")
          @PathVariable
          UUID parentTopicUuid,
      @Parameter(name = "subtopicUuid", description = "The uuid of the subtopic") @PathVariable
          UUID subtopicUuid)
      throws IdentifiableServiceException {
    boolean successful = topicService.addChild(parentTopicUuid, subtopicUuid);

    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Get count of topics")
  @GetMapping(
      value = {"/v6/topics/count", "/v5/topics/count", "/v2/topics/count", "/latest/topics/count"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public long count() {
    return topicService.count();
  }

  @Operation(summary = "Get all topics")
  @GetMapping(
      value = {"/v6/topics"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Topic> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "searchTerm", required = false) String searchTerm,
      @RequestParam(name = "label", required = false) String labelTerm,
      @RequestParam(name = "labelLanguage", required = false) Locale labelLanguage) {
    return super.find(pageNumber, pageSize, sortBy, searchTerm, labelTerm, labelLanguage);
  }

  @Operation(summary = "Get paged entities of a topic")
  @GetMapping(
      value = {"/v6/topics/{uuid}/entities"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Entity> findEntities(
      @Parameter(example = "", description = "UUID of the topic") @PathVariable("uuid")
          UUID topicUuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "entityType", required = false) FilterCriterion<String> entityType) {
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize, new Sorting());
    if (entityType != null) {
      Filtering filtering = Filtering.builder().add("entityType", entityType).build();
      pageRequest.setFiltering(filtering);
    }
    return topicService.findEntities(topicUuid, pageRequest);
  }

  @Operation(summary = "Get file resources of topic")
  @GetMapping(
      value = {"/v6/topics/{uuid}/fileresources"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<FileResource> findFileResources(
      @PathVariable UUID uuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize) {
    return topicService.findFileResources(uuid, new PageRequest(pageNumber, pageSize));
  }

  @Operation(summary = "Get paged subtopics of a topic")
  @GetMapping(
      value = {
        "/v6/topics/{uuid}/subtopics",
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Topic> findSubtopics(
      @Parameter(example = "", description = "UUID of the topic") @PathVariable("uuid")
          UUID topicUuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "searchTerm", required = false) String searchTerm) {
    PageRequest searchPageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      searchPageRequest.setSorting(sorting);
    }
    return topicService.findChildren(topicUuid, searchPageRequest);
  }

  @Operation(summary = "Get all top topics")
  @GetMapping(
      value = {"/v6/topics/top"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Topic> findTopTopics(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "searchTerm", required = false) String searchTerm) {
    PageRequest searchPageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      searchPageRequest.setSorting(sorting);
    }
    return topicService.findRootNodes(searchPageRequest);
  }

  @Operation(summary = "Get the breadcrumb for a topic")
  @GetMapping(
      value = {
        "/v6/topics/{uuid}/breadcrumb",
        "/v5/topics/{uuid}/breadcrumb",
        "/v3/topics/{uuid}/breadcrumb",
        "/latest/topics/{uuid}/breadcrumb"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<BreadcrumbNavigation> getBreadcrumbNavigation(
      @Parameter(
              example = "",
              description = "UUID of the topic, e.g. <tt>6119d8e9-9c92-4091-8dcb-bc4053385406</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @Parameter(
              name = "pLocale",
              description =
                  "Desired locale, e.g. <tt>de_DE</tt>. If unset, contents in all languages will be returned")
          @RequestParam(name = "pLocale", required = false)
          Locale pLocale) {

    BreadcrumbNavigation breadcrumbNavigation;

    if (pLocale == null) {
      breadcrumbNavigation = topicService.getBreadcrumbNavigation(uuid);
    } else {
      breadcrumbNavigation =
          topicService.getBreadcrumbNavigation(uuid, pLocale, localeService.getDefaultLocale());
    }

    if (breadcrumbNavigation == null || breadcrumbNavigation.getNavigationItems().isEmpty()) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    return new ResponseEntity<>(breadcrumbNavigation, HttpStatus.OK);
  }

  @Operation(summary = "Get topic by refId")
  @GetMapping(
      value = {
        "/v6/topics/{refId:[0-9]+}",
        "/v5/topics/{refId:[0-9]+}",
        "/v3/topics/{refId:[0-9]+}",
        "/latest/topics/{refId:[0-9]+}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Topic> getByRefId(
      @Parameter(name = "refId", example = "", description = "refId of the topic, e.g. <tt>42</tt>")
          @PathVariable
          long refId)
      throws IdentifiableServiceException {
    Topic topic = topicService.getByRefId(refId);
    return getByUuid(topic.getUuid(), null);
  }

  @Operation(summary = "Get topic by uuid (and optional locale)")
  @GetMapping(
      value = {
        "/v6/topics/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/topics/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v2/topics/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/latest/topics/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Topic> getByUuid(
      @Parameter(
              example = "",
              description = "UUID of the topic, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @Parameter(
              name = "pLocale",
              description =
                  "Desired locale, e.g. <tt>de_DE</tt>. If unset, contents in all languages will be returned")
          @RequestParam(name = "pLocale", required = false)
          Locale pLocale)
      throws IdentifiableServiceException {
    Topic topic;
    if (pLocale == null) {
      topic = topicService.getByUuid(uuid);
    } else {
      topic = topicService.getByUuidAndLocale(uuid, pLocale);
    }
    return new ResponseEntity<>(topic, HttpStatus.OK);
  }

  @Operation(summary = "Get subtopics of topic")
  @GetMapping(
      value = {
        "/v6/topics/{uuid}/children",
        "/v5/topics/{uuid}/children",
        "/v3/topics/{uuid}/children",
        "/latest/topics/{uuid}/children"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  List<Topic> getChildren(@PathVariable UUID uuid) {
    return topicService.getChildren(uuid);
  }

  @Operation(summary = "Get all entities of topic")
  @GetMapping(
      value = {
        "/v6/topics/{uuid}/entities/all",
        "/v5/topics/{uuid}/entities/all",
        "/v3/topics/{uuid}/entities/all",
        "/latest/topics/{uuid}/entities/all"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Entity> getEntities(
      @Parameter(name = "uuid", description = "The uuid of the topic") @PathVariable UUID uuid) {
    return topicService.getEntities(uuid);
  }

  @Operation(summary = "Get all languages of entities of a topic")
  @GetMapping(
      value = {"/v6/topics/{uuid}/entities/languages", "/v5/topics/{uuid}/entities/languages"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Locale> getLanguagesOfEntities(@PathVariable UUID uuid) {
    return this.topicService.getLanguagesOfEntities(uuid);
  }

  @Operation(summary = "Get all languages of file resources of a topic")
  @GetMapping(
      value = {
        "/v6/topics/{uuid}/fileresources/languages",
        "/v5/topics/{uuid}/fileresources/languages"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Locale> getLanguagesOfFileResources(@PathVariable UUID uuid) {
    return this.topicService.getLanguagesOfFileResources(uuid);
  }

  @Operation(summary = "Get parent topic of topic")
  @GetMapping(
      value = {
        "/v6/topics/{uuid}/parent",
        "/v5/topics/{uuid}/parent",
        "/v3/topics/{uuid}/parent",
        "/latest/topics/{uuid}/parent"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  Topic getParent(@PathVariable UUID uuid) {
    return topicService.getParent(uuid);
  }

  @Operation(summary = "Get subtopics of topic")
  @GetMapping(
      value = {"/v2/topics/{uuid}/subtopics"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity<String> getSubtopics(@PathVariable UUID uuid) {
    return new ResponseEntity<>(
        "no longer supported. use '/v3/topics/{uuid}/children' endpoint, returning list of child-topics",
        HttpStatus.GONE);
  }

  @Operation(summary = "Get languages of all top topics")
  @GetMapping(
      value = {
        "/v6/topics/top/languages",
        "/v5/topics/top/languages",
        "/v3/topics/top/languages",
        "/latest/topics/top/languages"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Locale> getTopTopicsLanguages() {
    return topicService.getRootNodesLanguages();
  }

  @Operation(summary = "Get topics an entity is linked to")
  @GetMapping(
      value = {
        "/v6/topics/entity/{uuid}",
        "/v5/topics/entity/{uuid}",
        "/v3/topics/entity/{uuid}",
        "/latest/topics/entity/{uuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  List<Topic> getTopicsOfEntity(@PathVariable UUID uuid) {
    return topicService.getTopicsOfEntity(uuid);
  }

  @Operation(summary = "Get topics a fileresource is linked to")
  @GetMapping(
      value = {
        "/v6/topics/fileresource/{uuid}",
        "/v5/topics/fileresource/{uuid}",
        "/v3/topics/fileresource/{uuid}",
        "/latest/topics/fileresource/{uuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  List<Topic> getTopicsOfFileResource(@PathVariable UUID uuid) {
    return topicService.getTopicsOfFileResource(uuid);
  }

  @Operation(summary = "Remove child-relation of the given subtopic to the given parent topic")
  @DeleteMapping(
      value = {
        "/v6/topics/{parentTopicUuid}/children/{subtopicUuid}",
        "/v5/topics/{parentTopicUuid}/children/{subtopicUuid}",
        "/v3/topics/{parentTopicUuid}/children/{subtopicUuid}",
        "/latest/topics/{parentTopicUuid}/children/{subtopicUuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity<Boolean> removeChild(
      @Parameter(name = "parentTopicUuid", description = "The uuid of the parent topic")
          @PathVariable
          UUID parentTopicUuid,
      @Parameter(name = "subtopicUuid", description = "The uuid of the subtopic") @PathVariable
          UUID subtopicUuid) {
    boolean successful = topicService.removeChild(parentTopicUuid, subtopicUuid);
    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Save a newly created topic")
  @PostMapping(
      value = {"/v6/topics", "/v5/topics", "/v2/topics", "/latest/topics"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Topic save(@RequestBody Topic topic, BindingResult errors)
      throws IdentifiableServiceException, ValidationException {
    return topicService.save(topic);
  }

  @Operation(summary = "Save a newly created topic and add it to parent")
  @PostMapping(
      value = {
        "/v6/topics/{parentTopicUuid}/subtopic",
        "/v5/topics/{parentTopicUuid}/subtopic",
        "/v3/topics/{parentTopicUuid}/subtopic",
        "/latest/topics/{parentTopicUuid}/subtopic"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Topic saveWithParentTopic(
      @Parameter(name = "parentTopicUuid", description = "The uuid of the parent topic")
          @PathVariable
          UUID parentTopicUuid,
      @RequestBody Topic topic,
      BindingResult errors)
      throws IdentifiableServiceException, ValidationException {
    return topicService.saveWithParent(topic, parentTopicUuid);
  }

  @Operation(summary = "Save entities of topic")
  @PostMapping(
      value = {
        "/v6/topics/{uuid}/entities",
        "/v5/topics/{uuid}/entities",
        "/v3/topics/{uuid}/entities",
        "/latest/topics/{uuid}/entities"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Entity> setEntities(@PathVariable UUID uuid, @RequestBody List<Entity> entities) {
    return topicService.setEntities(uuid, entities);
  }

  @Operation(summary = "Save fileresources of topic")
  @PostMapping(
      value = {
        "/v6/topics/{uuid}/fileresources",
        "/v5/topics/{uuid}/fileresources",
        "/v3/topics/{uuid}/fileresources",
        "/latest/topics/{uuid}/fileresources"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<FileResource> setFileresources(
      @PathVariable UUID uuid, @RequestBody List<FileResource> fileResources) {
    return topicService.setFileResources(uuid, fileResources);
  }

  @Operation(summary = "Update a topic")
  @PutMapping(
      value = {
        "/v6/topics/{uuid}",
        "/v5/topics/{uuid}",
        "/v2/topics/{uuid}",
        "/latest/topics/{uuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Topic update(@PathVariable UUID uuid, @RequestBody Topic topic, BindingResult errors)
      throws IdentifiableServiceException, ValidationException {
    assert Objects.equals(uuid, topic.getUuid());
    return topicService.update(topic);
  }
}
