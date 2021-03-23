package de.digitalcollections.cudami.server.controller.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.DigitalObjectService;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.identifiable.entity.work.Item;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import de.digitalcollections.model.paging.Sorting;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiPathParam;
import org.jsondoc.core.annotation.ApiResponseObject;
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

  private final DigitalObjectService digitalObjectService;

  public DigitalObjectController(DigitalObjectService digitalObjectService) {
    this.digitalObjectService = digitalObjectService;
  }

  @ApiMethod(description = "Get count of digital objects")
  @GetMapping(
      value = {"/latest/digitalobjects/count", "/v2/digitalobjects/count"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public long count() {
    return digitalObjectService.count();
  }

  @ApiMethod(description = "Delete a digital object with all its relations")
  @DeleteMapping(
      value = {"/latest/digitalobjects/{uuid}", "/v2/digitalobjects/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public ResponseEntity delete(
      @ApiPathParam(description = "UUID of the digital object") @PathVariable("uuid") UUID uuid) {
    boolean successful = digitalObjectService.delete(uuid);

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
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    return digitalObjectService.find(pageRequest);
  }

  @ApiMethod(
      description =
          "Get all digital objects, reduced to their metadata fields (only all identifiers and last modification date)")
  @GetMapping(
      value = {"/latest/digitalobjects/reduced", "/v3/digitalobjects/reduced"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public List<DigitalObject> findAllReduced() {
    return digitalObjectService.findAllReduced();
  }

  @ApiMethod(description = "Get digital object by uuid")
  @GetMapping(
      value = {"/latest/digitalobjects/{uuid}", "/v2/digitalobjects/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public DigitalObject findById(@PathVariable UUID uuid) {
    return digitalObjectService.get(uuid);
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
    return digitalObjectService.getByIdentifier(namespace, id);
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
    SearchPageRequest pageRequest = new SearchPageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    return digitalObjectService.find(pageRequest);
  }

  @ApiMethod(description = "Get item for digital object by digital object uuid")
  @GetMapping(
      value = {"/latest/digitalobjects/{uuid}/item", "/v2/digitalobjects/{uuid}/item"},
      produces = "application/json")
  @ApiResponseObject
  public Item findItemOfDigitalObject(@PathVariable UUID uuid) {
    return digitalObjectService.getItem(uuid);
  }

  @ApiMethod(description = "Find limited amount of random digital objects")
  @GetMapping(
      value = {"/latest/digitalobjects/random", "/v2/digitalobjects/random"},
      produces = "application/json")
  @ApiResponseObject
  public List<DigitalObject> findRandomDigitalObjects(
      @RequestParam(name = "count", required = false, defaultValue = "5") int count) {
    return digitalObjectService.getRandom(count);
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
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize, new Sorting());

    DigitalObject digitalObject = new DigitalObject();
    digitalObject.setUuid(uuid);
    if (active != null) {
      return digitalObjectService.getActiveCollections(digitalObject, pageRequest);
    }
    return digitalObjectService.getCollections(digitalObject, pageRequest);
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
    return digitalObjectService.getFileResources(uuid);
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
    return digitalObjectService.getImageFileResources(uuid);
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
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize, new Sorting());

    DigitalObject digitalObject = new DigitalObject();
    digitalObject.setUuid(uuid);
    return digitalObjectService.getProjects(digitalObject, pageRequest);
  }

  @ApiMethod(description = "Save a newly created digital object")
  @PostMapping(
      value = {"/latest/digitalobjects", "/v2/digitalobjects"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public DigitalObject save(@RequestBody DigitalObject digitalObject, BindingResult errors)
      throws IdentifiableServiceException {
    return digitalObjectService.save(digitalObject);
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
    return digitalObjectService.saveFileResources(uuid, fileResources);
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
    return digitalObjectService.update(digitalObject);
  }

  @ApiMethod(description = "Get languages of all digital objects")
  @GetMapping(
      value = {"/latest/digitalobjects/languages", "/v3/digitalobjects/languages"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public List<Locale> getLanguages() {
    return digitalObjectService.getLanguages();
  }
}
