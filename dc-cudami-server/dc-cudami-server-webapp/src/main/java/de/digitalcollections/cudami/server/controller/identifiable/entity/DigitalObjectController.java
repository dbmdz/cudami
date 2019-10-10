package de.digitalcollections.cudami.server.controller.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.DigitalObjectService;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.Sorting;
import de.digitalcollections.model.api.paging.enums.Direction;
import de.digitalcollections.model.api.paging.enums.NullHandling;
import de.digitalcollections.model.impl.paging.OrderImpl;
import de.digitalcollections.model.impl.paging.PageRequestImpl;
import de.digitalcollections.model.impl.paging.SortingImpl;
import java.util.LinkedHashSet;
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
@Api(description = "The digital object controller", name = "Digital object controller")
public class DigitalObjectController {

  @Autowired
  private DigitalObjectService service;

  @ApiMethod(description = "get all digital objects")
  @RequestMapping(value = {"/latest/digitalobjects", "/v2/digitalobjects"},
                  produces = "application/json", method = RequestMethod.GET)
  @ApiResponseObject
  public PageResponse<DigitalObject> findAll(
    @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
    @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
    @RequestParam(name = "sortField", required = false, defaultValue = "uuid") String sortField,
    @RequestParam(name = "sortDirection", required = false, defaultValue = "ASC") Direction sortDirection,
    @RequestParam(name = "nullHandling", required = false, defaultValue = "NATIVE") NullHandling nullHandling) {
    OrderImpl order = new OrderImpl(sortDirection, sortField, nullHandling);
    Sorting sorting = new SortingImpl(order);
    PageRequest pageRequest = new PageRequestImpl(pageNumber, pageSize, sorting);
    return service.find(pageRequest);
  }

  @ApiMethod(description = "get digital object by uuid")
  @RequestMapping(value = {"/latest/digitalobjects/{uuid}", "/v2/digitalobjects/{uuid}"}, produces = "application/json", method = RequestMethod.GET)
  @ApiResponseObject
  public DigitalObject findById(@PathVariable UUID uuid) {
    return service.get(uuid);
  }

  @ApiMethod(description = "get digital object by namespace and id")
  @RequestMapping(value = {"/latest/digitalobjects/identifier/{namespace}:{id}", "/v2/digitalobjects/identifier/{namespace}:{id}"}, produces = "application/json", method = RequestMethod.GET)
  @ApiResponseObject
  public DigitalObject findByIdIdentifier(@PathVariable String namespace, @PathVariable String id) throws IdentifiableServiceException {
    DigitalObject digitalObject = service.getByIdentifier(namespace, id);
    if (digitalObject == null) {
      // FIXME throw resource not found http exception
      throw new IdentifiableServiceException("DigitalObject " + namespace + ":" + id + " not found");
    }
    return digitalObject;
  }

  @ApiMethod(description = "save a newly created digital object")
  @RequestMapping(value = {"/latest/digitalobjects", "/v2/digitalobjects"}, produces = "application/json", method = RequestMethod.POST)
  @ApiResponseObject
  public DigitalObject save(@RequestBody DigitalObject digitalObject, BindingResult errors) throws IdentifiableServiceException {
    return service.save(digitalObject);
  }

  @ApiMethod(description = "update a digital object")
  @RequestMapping(value = {"/latest/digitalobjects/{uuid}", "/v2/digitalobjects/{uuid}"}, produces = "application/json", method = RequestMethod.PUT)
  @ApiResponseObject
  public DigitalObject update(@PathVariable UUID uuid, @RequestBody DigitalObject digitalObject, BindingResult errors) throws IdentifiableServiceException {
    assert Objects.equals(uuid, digitalObject.getUuid());
    return service.update(digitalObject);
  }

  @ApiMethod(description = "get image file resources of a digital object")
  @RequestMapping(value = {"/latest/digitalobjects/{uuid}/fileresources/images", "/v2/digitalobjects/{uuid}/fileresources/images"}, produces = "application/json", method = RequestMethod.GET)
  @ApiResponseObject
  public LinkedHashSet<ImageFileResource> getImageFileResources(@PathVariable UUID uuid) {
    return service.getImageFileResources(uuid);
  }

  @ApiMethod(description = "get count of digital objects")
  @RequestMapping(value = {"/latest/digitalobjects/count", "/v2/digitalobjects/count"}, produces = "application/json", method = RequestMethod.GET)
  @ApiResponseObject
  public long count() {
    return service.count();
  }
}
