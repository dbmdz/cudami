package de.digitalcollections.cudami.server.controller.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.TopicService;
import de.digitalcollections.model.identifiable.entity.Topic;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
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

  public V5TopicController(LocaleService localeService, TopicService topicService) {
    this.localeService = localeService;
    this.topicService = topicService;
  }

  @Operation(summary = "Get all topics")
  @GetMapping(
      value = {"/v5/topics", "/v2/topics", "/latest/topics"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "searchTerm", required = false) String searchTerm) {
    PageRequest pageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    PageResponse<Topic> response = topicService.find(pageRequest);
    // TODO
    return null;
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
      @RequestParam(name = "searchTerm", required = false) String searchTerm) {
    PageRequest searchPageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      searchPageRequest.setSorting(sorting);
    }
    PageResponse<Topic> response = topicService.findChildren(topicUuid, searchPageRequest);
    // TODO
    return null;
  }

  @Operation(summary = "Get all top topics")
  @GetMapping(
      value = {"/v5/topics/top", "/v3/topics/top", "/latest/topics/top"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> findTopTopics(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "searchTerm", required = false) String searchTerm) {
    PageRequest searchPageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      searchPageRequest.setSorting(sorting);
    }
    PageResponse<Topic> response = topicService.findRootNodes(searchPageRequest);
    // TODO
    return null;
  }
}
