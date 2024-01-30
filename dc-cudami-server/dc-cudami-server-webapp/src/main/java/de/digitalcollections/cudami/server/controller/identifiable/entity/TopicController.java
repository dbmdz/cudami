package de.digitalcollections.cudami.server.controller.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.EntityService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.TopicService;
import de.digitalcollections.cudami.server.controller.AbstractEntityController;
import de.digitalcollections.cudami.server.controller.ParameterHelper;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.Topic;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.validation.ValidationException;
import de.digitalcollections.model.view.BreadcrumbNavigation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Locale;
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
public class TopicController extends AbstractEntityController<Topic> {

  private final LocaleService localeService;
  private final TopicService service;

  public TopicController(LocaleService localeService, TopicService topicService) {
    this.localeService = localeService;
    this.service = topicService;
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
      throws ServiceException {
    boolean successful =
        service.addChild(buildExampleWithUuid(parentTopicUuid), buildExampleWithUuid(subtopicUuid));
    return successful
        ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
        : new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Add an existing entity to an existing topic")
  @PostMapping(
      value = {"/v6/topics/{uuid:" + ParameterHelper.UUID_PATTERN + "}/entities/{entityUuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity addEntity(
      @Parameter(example = "", description = "UUID of the topic") @PathVariable("uuid")
          UUID topicUuid,
      @Parameter(example = "", description = "UUID of the entity") @PathVariable("entityUuid")
          UUID entityUuid)
      throws ServiceException {
    Topic topic = new Topic();
    topic.setUuid(topicUuid);

    Entity entity = new Entity();
    entity.setUuid(entityUuid);

    boolean successful = service.addEntity(topic, entity);
    return successful
        ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
        : new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Add existing entities to an existing topic")
  @PostMapping(
      value = {"/v6/topics/{uuid:" + ParameterHelper.UUID_PATTERN + "}/entities"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity addEntities(
      @Parameter(example = "", description = "UUID of the topic") @PathVariable("uuid")
          UUID topicUuid,
      @Parameter(example = "", description = "List of the entities") @RequestBody
          List<Entity> entities)
      throws ServiceException {
    Topic topic = new Topic();
    topic.setUuid(topicUuid);

    boolean successful = service.addEntities(topic, entities);
    return successful
        ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
        : new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Add an existing filersource to an existing topic")
  @PostMapping(
      value = {
        "/v6/topics/{uuid:" + ParameterHelper.UUID_PATTERN + "}/fileresources/{fileResourceUuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity addFileResource(
      @Parameter(example = "", description = "UUID of the topic") @PathVariable("uuid")
          UUID topicUuid,
      @Parameter(example = "", description = "UUID of the fileResource")
          @PathVariable("fileResourceUuid")
          UUID fileResourceUuid)
      throws ServiceException {
    Topic topic = new Topic();
    topic.setUuid(topicUuid);

    FileResource fileResource = new FileResource();
    fileResource.setUuid(fileResourceUuid);

    boolean successful = service.addFileResource(topic, fileResource);
    return successful
        ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
        : new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Add existing fileresources to an existing topic")
  @PostMapping(
      value = {"/v6/topics/{uuid:" + ParameterHelper.UUID_PATTERN + "}/fileresources"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity addFileResources(
      @Parameter(example = "", description = "UUID of the topic") @PathVariable("uuid")
          UUID topicUuid,
      @Parameter(example = "", description = "List of the fileResources") @RequestBody
          List<FileResource> fileResources)
      throws ServiceException {
    Topic topic = new Topic();
    topic.setUuid(topicUuid);

    boolean successful = service.addFileResources(topic, fileResources);
    return successful
        ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
        : new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Get count of topics")
  @GetMapping(
      value = {"/v6/topics/count", "/v5/topics/count", "/v2/topics/count", "/latest/topics/count"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public long count() throws ServiceException {
    return super.count();
  }

  @Operation(summary = "Get all topics as (paged, sorted, filtered) list")
  @GetMapping(
      value = {"/v6/topics"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Topic> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "filter", required = false) List<FilterCriterion> filterCriteria,
      @RequestParam(name = "filtering", required = false) Filtering filtering)
      throws ServiceException {
    return super.find(pageNumber, pageSize, sortBy, filterCriteria, filtering);
  }

  @Operation(summary = "Get all entities of a topic as (paged, sorted, filtered) list")
  @GetMapping(
      value = {"/v6/topics/{uuid:" + ParameterHelper.UUID_PATTERN + "}/entities"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Entity> findEntities(
      @Parameter(example = "", description = "UUID of the topic") @PathVariable("uuid")
          UUID topicUuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "filter", required = false) List<FilterCriterion> filterCriteria,
      @RequestParam(name = "filtering", required = false) Filtering filtering)
      throws ServiceException {
    PageRequest pageRequest =
        createPageRequest(Entity.class, pageNumber, pageSize, sortBy, filterCriteria, filtering);
    return service.findEntities(buildExampleWithUuid(topicUuid), pageRequest);
  }

  @Operation(summary = "Get all file resources of a topic as (paged, sorted, filtered) list")
  @GetMapping(
      value = {"/v6/topics/{uuid:" + ParameterHelper.UUID_PATTERN + "}/fileresources"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<FileResource> findFileResources(
      @Parameter(example = "", description = "UUID of the topic") @PathVariable("uuid")
          UUID topicUuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "filter", required = false) List<FilterCriterion> filterCriteria,
      @RequestParam(name = "filtering", required = false) Filtering filtering)
      throws ServiceException {
    PageRequest pageRequest =
        createPageRequest(
            FileResource.class, pageNumber, pageSize, sortBy, filterCriteria, filtering);
    return service.findFileResources(buildExampleWithUuid(topicUuid), pageRequest);
  }

  @Operation(summary = "Get all subtopics of a topic as (paged, sorted, filtered) list")
  @GetMapping(
      value = {
        "/v6/topics/{uuid:" + ParameterHelper.UUID_PATTERN + "}/subtopics",
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Topic> findSubtopics(
      @Parameter(example = "", description = "UUID of the topic") @PathVariable("uuid")
          UUID topicUuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "filter", required = false) List<FilterCriterion> filterCriteria,
      @RequestParam(name = "filtering", required = false) Filtering filtering)
      throws ServiceException {
    PageRequest pageRequest =
        createPageRequest(Topic.class, pageNumber, pageSize, sortBy, filterCriteria, filtering);
    return service.findChildren(buildExampleWithUuid(topicUuid), pageRequest);
  }

  @Operation(summary = "Get all top topics as (paged, sorted, filtered) list")
  @GetMapping(
      value = {"/v6/topics/top"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Topic> findTopTopics(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "filter", required = false) List<FilterCriterion> filterCriteria,
      @RequestParam(name = "filtering", required = false) Filtering filtering)
      throws ServiceException {
    PageRequest pageRequest =
        createPageRequest(Topic.class, pageNumber, pageSize, sortBy, filterCriteria, filtering);
    return service.findRootNodes(pageRequest);
  }

  @Operation(summary = "Get the breadcrumb for a topic")
  @GetMapping(
      value = {
        "/v6/topics/{uuid:" + ParameterHelper.UUID_PATTERN + "}/breadcrumb",
        "/v5/topics/{uuid:" + ParameterHelper.UUID_PATTERN + "}/breadcrumb",
        "/v3/topics/{uuid:" + ParameterHelper.UUID_PATTERN + "}/breadcrumb",
        "/latest/topics/{uuid:" + ParameterHelper.UUID_PATTERN + "}/breadcrumb"
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
          Locale pLocale)
      throws ServiceException {

    BreadcrumbNavigation breadcrumbNavigation;
    Topic example = buildExampleWithUuid(uuid);

    if (pLocale == null) {
      breadcrumbNavigation = service.getBreadcrumbNavigation(example);
    } else {
      breadcrumbNavigation =
          service.getBreadcrumbNavigation(example, pLocale, localeService.getDefaultLocale());
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
      throws ServiceException {
    return super.getByRefId(refId);
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
      throws ServiceException {
    if (pLocale == null) {
      return super.getByUuid(uuid);
    } else {
      return super.getByUuidAndLocale(uuid, pLocale);
    }
  }

  @Operation(summary = "Get subtopics of topic")
  @GetMapping(
      value = {
        "/v6/topics/{uuid:" + ParameterHelper.UUID_PATTERN + "}/children",
        "/v5/topics/{uuid:" + ParameterHelper.UUID_PATTERN + "}/children",
        "/v3/topics/{uuid:" + ParameterHelper.UUID_PATTERN + "}/children",
        "/latest/topics/{uuid:" + ParameterHelper.UUID_PATTERN + "}/children"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Topic> getChildren(@PathVariable UUID uuid) throws ServiceException {
    return service.getChildren(buildExampleWithUuid(uuid));
  }

  @Operation(summary = "Get all languages of entities of a topic")
  @GetMapping(
      value = {
        "/v6/topics/{uuid:" + ParameterHelper.UUID_PATTERN + "}/entities/languages",
        "/v5/topics/{uuid:" + ParameterHelper.UUID_PATTERN + "}/entities/languages"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Locale> getLanguagesOfEntities(@PathVariable UUID uuid) throws ServiceException {
    return this.service.getLanguagesOfEntities(buildExampleWithUuid(uuid));
  }

  @Operation(summary = "Get all languages of file resources of a topic")
  @GetMapping(
      value = {
        "/v6/topics/{uuid:" + ParameterHelper.UUID_PATTERN + "}/fileresources/languages",
        "/v5/topics/{uuid:" + ParameterHelper.UUID_PATTERN + "}/fileresources/languages"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Locale> getLanguagesOfFileResources(@PathVariable UUID uuid) throws ServiceException {
    return this.service.getLanguagesOfFileResources(buildExampleWithUuid(uuid));
  }

  @Operation(summary = "Get parent topic of topic")
  @GetMapping(
      value = {
        "/v6/topics/{uuid:" + ParameterHelper.UUID_PATTERN + "}/parent",
        "/v5/topics/{uuid:" + ParameterHelper.UUID_PATTERN + "}/parent",
        "/v3/topics/{uuid:" + ParameterHelper.UUID_PATTERN + "}/parent",
        "/latest/topics/{uuid:" + ParameterHelper.UUID_PATTERN + "}/parent"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Topic getParent(@PathVariable UUID uuid) throws ServiceException {
    return service.getParent(buildExampleWithUuid(uuid));
  }

  @Override
  protected EntityService<Topic> getService() {
    return service;
  }

  @Operation(summary = "Get subtopics of topic")
  @GetMapping(
      value = {"/v2/topics/{uuid:" + ParameterHelper.UUID_PATTERN + "}/subtopics"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> getSubtopics(@PathVariable UUID uuid) {
    return new ResponseEntity<>(
        "no longer supported. use '/v3/topics/{uuid:"
            + ParameterHelper.UUID_PATTERN
            + "}/children' endpoint, returning list of child-topics",
        HttpStatus.GONE);
  }

  @Operation(summary = "Get topics an entity is linked to")
  @GetMapping(
      value = {
        "/v6/topics/entity/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/topics/entity/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v3/topics/entity/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/latest/topics/entity/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Topic> getTopicsOfEntity(@PathVariable UUID uuid) throws ServiceException {
    return service.getTopicsOfEntity(buildExampleWithUuid(uuid));
  }

  @Operation(summary = "Get topics a fileresource is linked to")
  @GetMapping(
      value = {
        "/v6/topics/fileresource/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/topics/fileresource/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v3/topics/fileresource/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/latest/topics/fileresource/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Topic> getTopicsOfFileResource(@PathVariable UUID uuid) throws ServiceException {
    return service.getTopicsOfFileResource(FileResource.builder().uuid(uuid).build());
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
  public List<Locale> getTopTopicsLanguages() throws ServiceException {
    return service.getRootNodesLanguages();
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
  public ResponseEntity<Boolean> removeChild(
      @Parameter(name = "parentTopicUuid", description = "The uuid of the parent topic")
          @PathVariable
          UUID parentTopicUuid,
      @Parameter(name = "subtopicUuid", description = "The uuid of the subtopic") @PathVariable
          UUID subtopicUuid)
      throws ServiceException {
    boolean successful =
        service.removeChild(
            buildExampleWithUuid(parentTopicUuid), buildExampleWithUuid(subtopicUuid));
    return successful
        ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
        : new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Remove an existing entity from an existing topic")
  @DeleteMapping(
      value = {"/v6/topics/{uuid:" + ParameterHelper.UUID_PATTERN + "}/entities/{entityUuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity removeEntity(
      @Parameter(example = "", description = "UUID of the topic") @PathVariable("uuid")
          UUID topicUuid,
      @Parameter(example = "", description = "UUID of the entity") @PathVariable("entityUuid")
          UUID entityUuid)
      throws ServiceException {
    Topic topic = new Topic();
    topic.setUuid(topicUuid);

    Entity entity = new Entity();
    entity.setUuid(entityUuid);

    boolean successful = service.removeEntity(topic, entity);
    return successful
        ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
        : new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Remove an existing fileResource from an existing topic")
  @DeleteMapping(
      value = {
        "/v6/topics/{uuid:" + ParameterHelper.UUID_PATTERN + "}/fileresources/{fileResourceUuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity removeFileResource(
      @Parameter(example = "", description = "UUID of the topic") @PathVariable("uuid")
          UUID topicUuid,
      @Parameter(example = "", description = "UUID of the fileResource")
          @PathVariable("fileResourceUuid")
          UUID fileResourceUuid)
      throws ServiceException {
    Topic topic = new Topic();
    topic.setUuid(topicUuid);

    FileResource fileResource = new FileResource();
    fileResource.setUuid(fileResourceUuid);

    boolean successful = service.removeFileResource(topic, fileResource);
    return successful
        ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
        : new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Save a newly created topic")
  @PostMapping(
      value = {"/v6/topics", "/v5/topics", "/v2/topics", "/latest/topics"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Topic save(@RequestBody Topic topic, BindingResult errors)
      throws ServiceException, ValidationException {
    return super.save(topic, errors);
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
      throws ServiceException, ValidationException {
    return service.saveWithParent(topic, buildExampleWithUuid(parentTopicUuid));
  }

  @Operation(summary = "Save entities of topic")
  @PutMapping(
      value = {
        "/v6/topics/{uuid:" + ParameterHelper.UUID_PATTERN + "}/entities",
        "/v5/topics/{uuid:" + ParameterHelper.UUID_PATTERN + "}/entities",
        "/v3/topics/{uuid:" + ParameterHelper.UUID_PATTERN + "}/entities",
        "/latest/topics/{uuid:" + ParameterHelper.UUID_PATTERN + "}/entities"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity setEntities(@PathVariable UUID uuid, @RequestBody List<Entity> entities)
      throws ServiceException {
    boolean successful = service.setEntities(buildExampleWithUuid(uuid), entities);
    return successful
        ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
        : new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Save fileresources of topic")
  @PutMapping(
      value = {
        "/v6/topics/{uuid:" + ParameterHelper.UUID_PATTERN + "}/fileresources",
        "/v5/topics/{uuid:" + ParameterHelper.UUID_PATTERN + "}/fileresources",
        "/v3/topics/{uuid:" + ParameterHelper.UUID_PATTERN + "}/fileresources",
        "/latest/topics/{uuid:" + ParameterHelper.UUID_PATTERN + "}/fileresources"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity setFileresources(
      @PathVariable UUID uuid, @RequestBody List<FileResource> fileResources)
      throws ServiceException {
    boolean successful = service.setFileResources(buildExampleWithUuid(uuid), fileResources);
    return successful
        ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
        : new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Update a topic")
  @PutMapping(
      value = {
        "/v6/topics/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/topics/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v2/topics/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/latest/topics/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Topic update(@PathVariable UUID uuid, @RequestBody Topic topic, BindingResult errors)
      throws ServiceException, ValidationException {
    return super.update(uuid, topic, errors);
  }
}
