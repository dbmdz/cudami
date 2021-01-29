package de.digitalcollections.cudami.server.controller.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.TopicService;
import de.digitalcollections.model.api.identifiable.entity.Topic;
import de.digitalcollections.model.api.identifiable.entity.parts.Subtopic;
import de.digitalcollections.model.api.paging.Order;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.Sorting;
import de.digitalcollections.model.impl.paging.PageRequestImpl;
import de.digitalcollections.model.impl.paging.SortingImpl;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiResponseObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
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

  @Autowired private TopicService service;

  @ApiMethod(description = "Get all topics")
  @GetMapping(
      value = {"/latest/topics", "/v2/topics"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public PageResponse<Topic> findAll(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy) {
    PageRequest pageRequest = new PageRequestImpl(pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting =
          new SortingImpl(sortBy.stream().filter(Objects::nonNull).collect(Collectors.toList()));
      pageRequest.setSorting(sorting);
    }
    return service.find(pageRequest);
  }

  @ApiMethod(description = "Get topic by uuid")
  @GetMapping(
      value = {"/latest/topics/{uuid}", "/v2/topics/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public Topic findById(@PathVariable UUID uuid) {
    return (Topic) service.get(uuid);
  }

  @ApiMethod(description = "Save a newly created topic")
  @PostMapping(
      value = {"/latest/topics", "/v2/topics"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public Topic save(@RequestBody Topic topic, BindingResult errors)
      throws IdentifiableServiceException {
    return (Topic) service.save(topic);
  }

  @ApiMethod(description = "Update a topic")
  @PutMapping(
      value = {"/latest/topics/{uuid}", "/v2/topics/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public Topic update(@PathVariable UUID uuid, @RequestBody Topic topic, BindingResult errors)
      throws IdentifiableServiceException {
    assert Objects.equals(uuid, topic.getUuid());
    return (Topic) service.update(topic);
  }

  @ApiMethod(description = "Get count of topics")
  @GetMapping(
      value = {"/latest/topics/count", "/v2/topics/count"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public long count() {
    return service.count();
  }

  @ApiMethod(description = "Get subtopics of topic")
  @GetMapping(
      value = {"/latest/topics/{uuid}/subtopics", "/v2/topics/{uuid}/subtopics"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  List<Subtopic> getSubtopics(@PathVariable UUID uuid) {
    return service.getSubtopics(uuid);
  }
}
