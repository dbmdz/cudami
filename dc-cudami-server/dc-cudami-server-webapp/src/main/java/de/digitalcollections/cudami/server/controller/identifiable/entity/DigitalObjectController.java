package de.digitalcollections.cudami.server.controller.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.DigitalObjectService;
import de.digitalcollections.model.api.identifiable.entity.Collection;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.entity.Project;
import de.digitalcollections.model.api.identifiable.entity.work.Item;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.api.paging.Order;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.SearchPageRequest;
import de.digitalcollections.model.api.paging.SearchPageResponse;
import de.digitalcollections.model.api.paging.Sorting;
import de.digitalcollections.model.impl.identifiable.entity.DigitalObjectImpl;
import de.digitalcollections.model.impl.paging.PageRequestImpl;
import de.digitalcollections.model.impl.paging.SearchPageRequestImpl;
import de.digitalcollections.model.impl.paging.SortingImpl;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiPathParam;
import org.jsondoc.core.annotation.ApiResponseObject;
import org.springframework.beans.factory.annotation.Autowired;
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
@Api(description = "The digital object controller", name = "Digital object controller")
public class DigitalObjectController {

  @Autowired private DigitalObjectService service;

  @ApiMethod(description = "Get count of digital objects")
  @GetMapping(
      value = {"/latest/digitalobjects/count", "/v2/digitalobjects/count"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public long count() {
    return service.count();
  }

  @ApiMethod(description = "Delete a digital object with all its relations")
  @DeleteMapping(
      value = {"/latest/digitalobjects/{uuid}", "/v2/digitalobjects/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public ResponseEntity delete(
      @ApiPathParam(description = "UUID of the digital object") @PathVariable("uuid") UUID uuid) {
    boolean successful = service.delete(uuid);

    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }

  @ApiMethod(description = "Get all digital objects")
  @GetMapping(
      value = {"/latest/digitalobjects", "/v2/digitalobjects"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public PageResponse<DigitalObject> findAll(
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

  @ApiMethod(
      description =
          "Get all digital objects, reduced to their metadata fields (only all identifiers and last modification date)")
  @GetMapping(
      value = {"/latest/digitalobjects/reduced", "/v3/digitalobjects/reduced"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public List<DigitalObject> findAllReduced() {
    return service.findAllReduced();
  }

  @ApiMethod(description = "Get digital object by uuid")
  @GetMapping(
      value = {"/latest/digitalobjects/{uuid}", "/v2/digitalobjects/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
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
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public DigitalObject findByIdentifier(
      @ApiPathParam(description = "Namespace of the identifier") @PathVariable("namespace")
          String namespace,
      @ApiPathParam(description = "value of the identifier") @PathVariable("id") String id)
      throws IdentifiableServiceException {
    return service.getByIdentifier(namespace, id);
    //    if (digitalObject == null) {
    //      // FIXME throw resource not found http exception
    //      throw new IdentifiableServiceException(
    //          "DigitalObject " + namespace + ":" + id + " not found");
    //    }
    //    return digitalObject;
  }

  @ApiMethod(
      description =
          "Find limited amount of digital objects containing searchTerm in label or description")
  @GetMapping(
      value = {"/latest/digitalobjects/search", "/v3/digitalobjects/search"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public SearchPageResponse<DigitalObject> findDigitalObjects(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "searchTerm", required = false) String searchTerm) {
    SearchPageRequest pageRequest = new SearchPageRequestImpl(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting =
          new SortingImpl(sortBy.stream().filter(Objects::nonNull).collect(Collectors.toList()));
      pageRequest.setSorting(sorting);
    }
    return service.find(pageRequest);
  }

  @ApiMethod(description = "Get item for digital object by digital object uuid")
  @GetMapping(
      value = {"/latest/digitalobjects/{uuid}/item", "/v2/digitalobjects/{uuid}/item"},
      produces = "application/json")
  @ApiResponseObject
  public Item findItemOfDigitalObject(@PathVariable UUID uuid) {
    return service.getItem(uuid);
  }

  @ApiMethod(description = "Get (active) paged collections of a digital objects")
  @GetMapping(
      value = {
        "/latest/digitalobjects/{uuid}/collections",
        "/v3/digitalobjects/{uuid}/collections"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public PageResponse<Collection> getCollections(
      @ApiPathParam(description = "UUID of the digital object") @PathVariable("uuid") UUID uuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "active", required = false) String active) {
    PageRequest pageRequest = new PageRequestImpl(pageNumber, pageSize, new SortingImpl());

    DigitalObject digitalObject = new DigitalObjectImpl();
    digitalObject.setUuid(uuid);
    if (active != null) {
      return service.getActiveCollections(digitalObject, pageRequest);
    }
    return service.getCollections(digitalObject, pageRequest);
  }

  @ApiMethod(description = "Get file resources of a digital object")
  @GetMapping(
      value = {
        "/latest/digitalobjects/{uuid}/fileresources",
        "/v2/digitalobjects/{uuid}/fileresources"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public List<FileResource> getFileResources(@PathVariable UUID uuid) {
    return service.getFileResources(uuid);
  }

  @ApiMethod(description = "Get image file resources of a digital object")
  @GetMapping(
      value = {
        "/latest/digitalobjects/{uuid}/fileresources/images",
        "/v2/digitalobjects/{uuid}/fileresources/images"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public List<ImageFileResource> getImageFileResources(@PathVariable UUID uuid) {
    return service.getImageFileResources(uuid);
  }

  @ApiMethod(description = "Get paged projects of a digital objects")
  @GetMapping(
      value = {"/latest/digitalobjects/{uuid}/projects", "/v3/digitalobjects/{uuid}/projects"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public PageResponse<Project> getProjects(
      @ApiPathParam(description = "UUID of the digital object") @PathVariable("uuid") UUID uuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize) {
    PageRequest pageRequest = new PageRequestImpl(pageNumber, pageSize, new SortingImpl());

    DigitalObject digitalObject = new DigitalObjectImpl();
    digitalObject.setUuid(uuid);
    return service.getProjects(digitalObject, pageRequest);
  }

  @ApiMethod(description = "Save a newly created digital object")
  @PostMapping(
      value = {"/latest/digitalobjects", "/v2/digitalobjects"},
      produces = MediaType.APPLICATION_JSON_VALUE)
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
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public List<FileResource> saveFileResources(
      @ApiPathParam(description = "UUID of the digital object") @PathVariable("uuid") UUID uuid,
      @RequestBody List<FileResource> fileResources) {
    return service.saveFileResources(uuid, fileResources);
  }

  @ApiMethod(description = "Update a digital object")
  @PutMapping(
      value = {"/latest/digitalobjects/{uuid}", "/v2/digitalobjects/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public DigitalObject update(
      @ApiPathParam(description = "UUID of the digital object") @PathVariable("uuid") UUID uuid,
      @RequestBody DigitalObject digitalObject,
      BindingResult errors)
      throws IdentifiableServiceException {
    assert Objects.equals(uuid, digitalObject.getUuid());
    return service.update(digitalObject);
  }
}
