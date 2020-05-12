package de.digitalcollections.cudami.server.controller.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.DigitalObjectService;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.SearchPageRequest;
import de.digitalcollections.model.api.paging.SearchPageResponse;
import de.digitalcollections.model.api.paging.Sorting;
import de.digitalcollections.model.api.paging.enums.Direction;
import de.digitalcollections.model.api.paging.enums.NullHandling;
import de.digitalcollections.model.impl.paging.OrderImpl;
import de.digitalcollections.model.impl.paging.PageRequestImpl;
import de.digitalcollections.model.impl.paging.SearchPageRequestImpl;
import de.digitalcollections.model.impl.paging.SortingImpl;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiResponseObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(description = "The digital object controller", name = "Digital object controller")
public class DigitalObjectController {

  @Autowired private DigitalObjectService service;

  @ApiMethod(description = "Get count of digital objects")
  @GetMapping(
      value = {"/latest/digitalobjects/count", "/v2/digitalobjects/count"},
      produces = "application/json")
  @ApiResponseObject
  public long count() {
    return service.count();
  }

  @ApiMethod(description = "Get all digital objects")
  @GetMapping(
      value = {"/latest/digitalobjects", "/v2/digitalobjects"},
      produces = "application/json")
  @ApiResponseObject
  public PageResponse<DigitalObject> findAll(
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

  @ApiMethod(
      description =
          "Find limited amount of digital objects containing searchTerm in label or description")
  @GetMapping(
      value = {"/latest/digitalobjects/search", "/v3/digitalobjects/search"},
      produces = "application/json")
  @ApiResponseObject
  public SearchPageResponse<DigitalObject> findDigitalObjects(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortField", required = false, defaultValue = "uuid") String sortField,
      @RequestParam(name = "sortDirection", required = false, defaultValue = "ASC")
          Direction sortDirection,
      @RequestParam(name = "nullHandling", required = false, defaultValue = "NATIVE")
          NullHandling nullHandling,
      @RequestParam(name = "searchTerm", required = false) String searchTerm) {
    OrderImpl order = new OrderImpl(sortDirection, sortField, nullHandling);
    Sorting sorting = new SortingImpl(order);
    SearchPageRequest pageRequest =
        new SearchPageRequestImpl(searchTerm, pageNumber, pageSize, sorting);
    return service.find(pageRequest);
  }

  @ApiMethod(description = "Get digital object by uuid")
  @GetMapping(
      value = {"/latest/digitalobjects/{uuid}", "/v2/digitalobjects/{uuid}"},
      produces = "application/json")
  @ApiResponseObject
  public DigitalObject findById(@PathVariable UUID uuid) {
    return service.get(uuid);
  }

  @ApiMethod(description = "Get digital object by namespace and id")
  @GetMapping(
      value = {
        "/latest/digitalobjects/identifier/{namespace}:{id}",
        "/v2/digitalobjects/identifier/{namespace}:{id}"
      },
      produces = "application/json")
  @ApiResponseObject
  public DigitalObject findByIdentifier(@PathVariable String namespace, @PathVariable String id)
      throws IdentifiableServiceException {
    return service.getByIdentifier(namespace, id);
    //    if (digitalObject == null) {
    //      // FIXME throw resource not found http exception
    //      throw new IdentifiableServiceException(
    //          "DigitalObject " + namespace + ":" + id + " not found");
    //    }
    //    return digitalObject;
  }

  @ApiMethod(description = "Get image file resources of a digital object")
  @GetMapping(
      value = {
        "/latest/digitalobjects/{uuid}/fileresources/images",
        "/v2/digitalobjects/{uuid}/fileresources/images"
      },
      produces = "application/json")
  @ApiResponseObject
  public List<ImageFileResource> getImageFileResources(@PathVariable UUID uuid) {
    return service.getImageFileResources(uuid);
  }

  @ApiMethod(description = "Get file resources of a digital object")
  @GetMapping(
      value = {
        "/latest/digitalobjects/{uuid}/fileresources",
        "/v2/digitalobjects/{uuid}/fileresources"
      },
      produces = "application/json")
  @ApiResponseObject
  public List<FileResource> getFileResources(@PathVariable UUID uuid) {
    return service.getFileResources(uuid);
  }

  @ApiMethod(description = "Save a newly created digital object")
  @PostMapping(
      value = {"/latest/digitalobjects", "/v2/digitalobjects"},
      produces = "application/json")
  @ApiResponseObject
  public DigitalObject save(@RequestBody DigitalObject digitalObject, BindingResult errors)
      throws IdentifiableServiceException {
    return service.save(digitalObject);
  }

  @ApiMethod(description = "Save list of fileresources for a given digital object")
  @PostMapping(
      value = {
        "/latest/digitalobjects/{uuid}/fileresources",
        "/v3/digitalobjects/{uuid}/fileresources"
      },
      produces = "application/json")
  @ApiResponseObject
  public List<FileResource> saveFileResources(
      @PathVariable UUID uuid, @RequestBody List<FileResource> fileResources) {
    return service.saveFileResources(uuid, fileResources);
  }

  @ApiMethod(description = "Update a digital object")
  @PutMapping(
      value = {"/latest/digitalobjects/{uuid}", "/v2/digitalobjects/{uuid}"},
      produces = "application/json")
  @ApiResponseObject
  public DigitalObject update(
      @PathVariable UUID uuid, @RequestBody DigitalObject digitalObject, BindingResult errors)
      throws IdentifiableServiceException {
    assert Objects.equals(uuid, digitalObject.getUuid());
    return service.update(digitalObject);
  }
}
