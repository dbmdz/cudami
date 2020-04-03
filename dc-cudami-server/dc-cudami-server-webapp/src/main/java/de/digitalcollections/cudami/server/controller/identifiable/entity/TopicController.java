package de.digitalcollections.cudami.server.controller.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.TopicService;
import de.digitalcollections.model.api.identifiable.entity.Topic;
import de.digitalcollections.model.api.identifiable.entity.parts.Subtopic;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.Sorting;
import de.digitalcollections.model.api.paging.enums.Direction;
import de.digitalcollections.model.api.paging.enums.NullHandling;
import de.digitalcollections.model.impl.paging.OrderImpl;
import de.digitalcollections.model.impl.paging.PageRequestImpl;
import de.digitalcollections.model.impl.paging.SortingImpl;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiResponseObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(description = "The topic controller", name = "Topic controller")
public class TopicController {

  @Autowired private TopicService service;

  @ApiMethod(description = "Get all topics")
  @RequestMapping(
      value = {"/latest/topics", "/v2/topics"},
      produces = "application/json",
      method = RequestMethod.GET)
  @ApiResponseObject
  public PageResponse<Topic> findAll(
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

  @ApiMethod(description = "Get topic by uuid")
  @RequestMapping(
      value = {"/latest/topics/{uuid}", "/v2/topics/{uuid}"},
      produces = "application/json",
      method = RequestMethod.GET)
  @ApiResponseObject
  public Topic findById(@PathVariable UUID uuid) {
    return (Topic) service.get(uuid);
  }

  @ApiMethod(description = "Save a newly created topic")
  @RequestMapping(
      value = {"/latest/topics", "/v2/topics"},
      produces = "application/json",
      method = RequestMethod.POST)
  @ApiResponseObject
  public Topic save(@RequestBody Topic topic, BindingResult errors)
      throws IdentifiableServiceException {
    return (Topic) service.save(topic);
  }

  @ApiMethod(description = "Update a topic")
  @RequestMapping(
      value = {"/latest/topics/{uuid}", "/v2/topics/{uuid}"},
      produces = "application/json",
      method = RequestMethod.PUT)
  @ApiResponseObject
  public Topic update(@PathVariable UUID uuid, @RequestBody Topic topic, BindingResult errors)
      throws IdentifiableServiceException {
    assert Objects.equals(uuid, topic.getUuid());
    return (Topic) service.update(topic);
  }

  @ApiMethod(description = "Get count of topics")
  @RequestMapping(
      value = {"/latest/topics/count", "/v2/topics/count"},
      produces = "application/json",
      method = RequestMethod.GET)
  @ApiResponseObject
  public long count() {
    return service.count();
  }

  @ApiMethod(description = "Get subtopics of topic")
  @RequestMapping(
      value = {"/latest/topics/{uuid}/rootNodes", "/v2/topics/{uuid}/subtopics"},
      produces = "application/json",
      method = RequestMethod.GET)
  @ApiResponseObject
  List<Subtopic> getRootNodes(@PathVariable UUID uuid) {
    return service.getSubtopics(uuid);
  }
}
