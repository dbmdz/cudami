package de.digitalcollections.cudami.server.controller.identifiable.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.TopicService;
import de.digitalcollections.cudami.server.controller.CudamiControllerException;
import de.digitalcollections.cudami.server.controller.legacy.V5MigrationHelper;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.Topic;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Topic controller")
public class V5TopicController {

  private final LocaleService localeService;
  private final TopicService topicService;

  private final ObjectMapper objectMapper;

  public V5TopicController(
      LocaleService localeService, TopicService topicService, ObjectMapper objectMapper) {
    this.localeService = localeService;
    this.topicService = topicService;
    this.objectMapper = objectMapper;
  }

  @Operation(summary = "Get all topics")
  @GetMapping(
      value = {"/v5/topics", "/v2/topics", "/latest/topics"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "searchTerm", required = false) String searchTerm)
      throws CudamiControllerException, ServiceException {
    PageRequest pageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(V5MigrationHelper.migrate(sortBy));
      pageRequest.setSorting(sorting);
    }
    PageResponse<Topic> pageResponse = topicService.find(pageRequest);

    try {
      String result = V5MigrationHelper.migrate(pageResponse, objectMapper);
      return new ResponseEntity<>(result, HttpStatus.OK);
    } catch (JsonProcessingException e) {
      throw new CudamiControllerException(e);
    }
  }

  @Operation(summary = "Get paged entities of a topic")
  @GetMapping(
      value = {
        "/v5/topics/{uuid}/entities",
        "/v3/topics/{uuid}/entities",
        "/latest/topics/{uuid}/entities"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> findEntities(
      @Parameter(example = "", description = "UUID of the topic") @PathVariable("uuid")
          UUID topicUuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "entityType", required = false) FilterCriterion<String> entityType)
      throws CudamiControllerException, ServiceException {
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize, new Sorting());
    if (entityType != null) {
      Filtering filtering = Filtering.builder().add("entityType", entityType).build();
      pageRequest.setFiltering(filtering);
    }
    PageResponse<Entity> pageResponse =
        topicService.findEntities(Topic.builder().uuid(topicUuid).build(), pageRequest);

    try {
      String result = V5MigrationHelper.migrate(pageResponse, objectMapper);
      return new ResponseEntity<>(result, HttpStatus.OK);
    } catch (JsonProcessingException e) {
      throw new CudamiControllerException(e);
    }
  }

  @Operation(summary = "Get paged subtopics of a topic")
  @GetMapping(
      value = {
        "/v5/topics/{uuid}/subtopics",
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> findSubtopics(
      @Parameter(example = "", description = "UUID of the topic") @PathVariable("uuid")
          UUID topicUuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "searchTerm", required = false) String searchTerm)
      throws CudamiControllerException, ServiceException {
    PageRequest searchPageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(V5MigrationHelper.migrate(sortBy));
      searchPageRequest.setSorting(sorting);
    }
    PageResponse<Topic> pageResponse =
        topicService.findChildren(Topic.builder().uuid(topicUuid).build(), searchPageRequest);

    try {
      String result = V5MigrationHelper.migrate(pageResponse, objectMapper);
      return new ResponseEntity<>(result, HttpStatus.OK);
    } catch (JsonProcessingException e) {
      throw new CudamiControllerException(e);
    }
  }

  @Operation(summary = "Get file resources of topic")
  @GetMapping(
      value = {
        "/v5/topics/{uuid}/fileresources",
        "/v3/topics/{uuid}/fileresources",
        "/latest/topics/{uuid}/fileresources"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> findFileResources(
      @PathVariable UUID uuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize)
      throws CudamiControllerException, ServiceException {
    PageResponse<FileResource> pageResponse =
        topicService.findFileResources(
            Topic.builder().uuid(uuid).build(), new PageRequest(pageNumber, pageSize));

    try {
      String result = V5MigrationHelper.migrate(pageResponse, objectMapper);
      return new ResponseEntity<>(result, HttpStatus.OK);
    } catch (JsonProcessingException e) {
      throw new CudamiControllerException(e);
    }
  }

  @Operation(summary = "Get all top topics")
  @GetMapping(
      value = {"/v5/topics/top", "/v3/topics/top", "/latest/topics/top"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> findTopTopics(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "searchTerm", required = false) String searchTerm)
      throws CudamiControllerException, ServiceException {
    PageRequest searchPageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(V5MigrationHelper.migrate(sortBy));
      searchPageRequest.setSorting(sorting);
    }
    PageResponse<Topic> pageResponse = topicService.findRootNodes(searchPageRequest);

    try {
      String result = V5MigrationHelper.migrate(pageResponse, objectMapper);
      return new ResponseEntity<>(result, HttpStatus.OK);
    } catch (JsonProcessingException e) {
      throw new CudamiControllerException(e);
    }
  }
}
