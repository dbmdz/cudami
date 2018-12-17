package de.digitalcollections.cudami.server.controller.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.ContentTreeService;
import de.digitalcollections.model.api.identifiable.entity.ContentTree;
import de.digitalcollections.model.api.identifiable.entity.parts.ContentNode;
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
@Api(description = "The content tree controller", name = "ContentTree controller")
public class ContentTreeController {

  @Autowired
  private ContentTreeService service;

  @ApiMethod(description = "get all content trees")
  @RequestMapping(value = "/latest/contenttrees",
          produces = "application/json", method = RequestMethod.GET)
  @ApiResponseObject
  public PageResponse<ContentTree> findAll(
          @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
          @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
          @RequestParam(name = "sortField", required = false, defaultValue = "uuid") String sortField,
          @RequestParam(name = "sortDirection", required = false, defaultValue = "ASC") Direction sortDirection,
          @RequestParam(name = "nullHandling", required = false, defaultValue = "NATIVE") NullHandling nullHandling
  ) {
    // FIXME add support for multiple sorting orders
    OrderImpl order = new OrderImpl(sortDirection, sortField, nullHandling);
    Sorting sorting = new SortingImpl(order);
    PageRequest pageRequest = new PageRequestImpl(pageNumber, pageSize, sorting);
    return service.find(pageRequest);
  }

  @ApiMethod(description = "get content tree by uuid")
  @RequestMapping(value = "/latest/contenttrees/{uuid}", produces = "application/json", method = RequestMethod.GET)
  @ApiResponseObject
  public ContentTree findById(@PathVariable UUID uuid) {
    return (ContentTree) service.get(uuid);
  }

  @ApiMethod(description = "save a newly created ContentTree")
  @RequestMapping(value = "/latest/contenttrees", produces = "application/json", method = RequestMethod.POST)
  @ApiResponseObject
  public ContentTree save(@RequestBody ContentTree contentTree, BindingResult errors) throws IdentifiableServiceException {
    return (ContentTree) service.save(contentTree);
  }

  @ApiMethod(description = "update a content tree")
  @RequestMapping(value = "/latest/contenttrees/{uuid}", produces = "application/json", method = RequestMethod.PUT)
  @ApiResponseObject
  public ContentTree update(@PathVariable UUID uuid, @RequestBody ContentTree contentTree, BindingResult errors) throws IdentifiableServiceException {
    assert Objects.equals(uuid, contentTree.getUuid());
    return (ContentTree) service.update(contentTree);
  }

  @ApiMethod(description = "get count of content trees")
  @RequestMapping(value = "/latest/contenttrees/count", produces = "application/json", method = RequestMethod.GET)
  @ApiResponseObject
  public long count() {
    return service.count();
  }

  @ApiMethod(description = "get root nodes of content tree")
  @RequestMapping(value = "/latest/contenttrees/{uuid}/rootNodes", produces = "application/json", method = RequestMethod.GET)
  @ApiResponseObject
  List<ContentNode> getRootNodes(@PathVariable UUID uuid) {
    return service.getRootNodes(uuid);
  }
}
