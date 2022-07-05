package de.digitalcollections.cudami.server.controller.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.DigitalObjectService;
import de.digitalcollections.cudami.server.controller.identifiable.AbstractIdentifiableController;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.identifiable.entity.work.Item;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
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
@Tag(name = "Digital object controller")
public class DigitalObjectController extends AbstractIdentifiableController<DigitalObject> {

  private final DigitalObjectService digitalObjectService;

  public DigitalObjectController(DigitalObjectService digitalObjectService) {
    this.digitalObjectService = digitalObjectService;
  }

  @Override
  protected IdentifiableService<DigitalObject> getService() {
    return digitalObjectService;
  }

  @Operation(summary = "Get count of digital objects")
  @GetMapping(
      value = {
        "/v6/digitalobjects/count",
        "/v5/digitalobjects/count",
        "/v2/digitalobjects/count",
        "/latest/digitalobjects/count"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public long count() {
    return digitalObjectService.count();
  }

  @Operation(summary = "Delete a digital object with all its relations")
  @DeleteMapping(
      value = {
        "/v6/digitalobjects/{uuid}",
        "/v5/digitalobjects/{uuid}",
        "/v2/digitalobjects/{uuid}",
        "/latest/digitalobjects/{uuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity delete(
      @Parameter(example = "", description = "UUID of the digital object") @PathVariable("uuid")
          UUID uuid)
      throws ConflictException {
    boolean successful;
    try {
      successful = digitalObjectService.delete(uuid);
    } catch (IdentifiableServiceException e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }

  @Operation(
      summary =
          "Find limited amount of digital objects containing searchTerm in label or description")
  @GetMapping(
      value = {"/v6/digitalobjects"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<DigitalObject> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "searchTerm", required = false) String searchTerm,
      @RequestParam(name = "parent.uuid", required = false)
          FilterCriterion<UUID> parentUuidFilterCriterion,
      @RequestParam(name = "label", required = false) String labelTerm,
      @RequestParam(name = "labelLanguage", required = false) Locale labelLanguage) {
    return super.find(
        pageNumber,
        pageSize,
        sortBy,
        searchTerm,
        labelTerm,
        labelLanguage,
        Map.of("parent.uuid", parentUuidFilterCriterion));
  }

  @Operation(summary = "Get paged projects of a digital objects")
  @GetMapping(
      value = {"/v6/digitalobjects/{uuid}/projects"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Project> findProjects(
      @Parameter(example = "", description = "UUID of the digital object") @PathVariable("uuid")
          UUID uuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "searchTerm", required = false) String searchTerm) {
    PageRequest searchPageRequest = new PageRequest(searchTerm, pageNumber, pageSize);

    DigitalObject digitalObject = new DigitalObject();
    digitalObject.setUuid(uuid);
    return digitalObjectService.findProjects(digitalObject, searchPageRequest);
  }

  @Operation(
      summary =
          "Get all digital objects, reduced to their metadata fields (only all identifiers and last modification date)")
  @GetMapping(
      value = {
        "/v6/digitalobjects/reduced",
        "/v5/digitalobjects/reduced",
        "/v3/digitalobjects/reduced",
        "/latest/digitalobjects/reduced"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<DigitalObject> getAllReduced() {
    return digitalObjectService.getAllReduced();
  }

  @Override
  @Operation(
      summary = "Get a digital object by namespace and id",
      description =
          "Separate namespace and id with a colon, e.g. foo:bar. It is also possible, to add a .json suffix, which will be ignored then")
  @GetMapping(
      value = {
        "/v6/digitalobjects/identifier/**",
        "/v5/digitalobjects/identifier/**",
        "/v2/digitalobjects/identifier/**",
        "/latest/digitalobjects/identifier/**"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<DigitalObject> getByIdentifier(HttpServletRequest request)
      throws IdentifiableServiceException, ValidationException {
    return super.getByIdentifier(request);
  }

  @Operation(summary = "Get a digital object by refId")
  @GetMapping(
      value = {"/v5/digitalobjects/{refId:[0-9]+}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public DigitalObject getByRefId(@PathVariable long refId) {
    return digitalObjectService.getByRefId(refId);
  }

  @Operation(summary = "Get a digital object by uuid")
  @GetMapping(
      value = {
        "/v6/digitalobjects/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}",
        "/v5/digitalobjects/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}",
        "/v2/digitalobjects/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}",
        "/latest/digitalobjects/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public DigitalObject getByUuid(@PathVariable UUID uuid) {
    return digitalObjectService.getByUuid(uuid);
  }

  @Operation(summary = "Get (active or all) paged collections of a digital objects")
  @GetMapping(
      value = {"/v6/digitalobjects/{uuid}/collections"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Collection> getCollections(
      @Parameter(example = "", description = "UUID of the digital object") @PathVariable("uuid")
          UUID uuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "active", required = false) String active,
      @RequestParam(name = "searchTerm", required = false) String searchTerm) {
    PageRequest searchPageRequest = new PageRequest(searchTerm, pageNumber, pageSize);

    DigitalObject digitalObject = new DigitalObject();
    digitalObject.setUuid(uuid);
    if (active != null) {
      return digitalObjectService.findActiveCollections(digitalObject, searchPageRequest);
    }
    return digitalObjectService.findCollections(digitalObject, searchPageRequest);
  }

  @Operation(summary = "Get file resources of a digital object")
  @GetMapping(
      value = {
        "/v6/digitalobjects/{uuid}/fileresources",
        "/v5/digitalobjects/{uuid}/fileresources",
        "/v2/digitalobjects/{uuid}/fileresources",
        "/latest/digitalobjects/{uuid}/fileresources"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<FileResource> getFileResources(@PathVariable UUID uuid) {
    return digitalObjectService.getFileResources(uuid);
  }

  @Operation(summary = "Get image file resources of a digital object")
  @GetMapping(
      value = {
        "/v6/digitalobjects/{uuid}/fileresources/images",
        "/v5/digitalobjects/{uuid}/fileresources/images",
        "/v2/digitalobjects/{uuid}/fileresources/images",
        "/latest/digitalobjects/{uuid}/fileresources/images"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<ImageFileResource> getImageFileResources(@PathVariable UUID uuid) {
    return digitalObjectService.getImageFileResources(uuid);
  }

  @Operation(summary = "Get item for digital object by digital object uuid")
  @GetMapping(
      value = {
        "/v6/digitalobjects/{uuid}/item",
        "/v5/digitalobjects/{uuid}/item",
        "/v2/digitalobjects/{uuid}/item",
        "/latest/digitalobjects/{uuid}/item"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Item getItem(@PathVariable UUID uuid) {
    return digitalObjectService.getItem(uuid);
  }

  @Operation(summary = "Get languages of all digital objects")
  @GetMapping(
      value = {
        "/v6/digitalobjects/languages",
        "/v5/digitalobjects/languages",
        "/v3/digitalobjects/languages",
        "/latest/digitalobjects/languages"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Locale> getLanguages(
      @RequestParam(name = "parent.uuid", required = false)
          FilterCriterion<UUID> parentUuidFilterCriterion) {
    if (parentUuidFilterCriterion != null) {
      return digitalObjectService.getLanguagesOfContainedDigitalObjects(
          (UUID) parentUuidFilterCriterion.getValue());
    }
    return digitalObjectService.getLanguages();
  }

  @Operation(summary = "Get all languages of a digital object's collections")
  @GetMapping(
      value = {
        "/v6/digitalobjects/{uuid}/collections/languages",
        "/v5/digitalobjects/{uuid}/collections/languages"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Locale> getLanguagesOfCollections(@PathVariable UUID uuid) {
    return this.digitalObjectService.getLanguagesOfCollections(uuid);
  }

  @Operation(summary = "Get all languages of a digital object's projects")
  @GetMapping(
      value = {
        "/v6/digitalobjects/{uuid}/projects/languages",
        "/v5/digitalobjects/{uuid}/projects/languages"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Locale> getLanguagesOfProjects(@PathVariable UUID uuid) {
    return this.digitalObjectService.getLanguagesOfProjects(uuid);
  }

  @Operation(summary = "Find limited amount of random digital objects")
  @GetMapping(
      value = {
        "/v6/digitalobjects/random",
        "/v5/digitalobjects/random",
        "/v2/digitalobjects/random",
        "/latest/digitalobjects/random"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<DigitalObject> getRandomDigitalObjects(
      @RequestParam(name = "count", required = false, defaultValue = "5") int count) {
    return digitalObjectService.getRandom(count);
  }

  @Operation(summary = "Save a newly created digital object")
  @PostMapping(
      value = {
        "/v6/digitalobjects",
        "/v5/digitalobjects",
        "/v2/digitalobjects",
        "/latest/digitalobjects"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public DigitalObject save(@RequestBody DigitalObject digitalObject, BindingResult errors)
      throws IdentifiableServiceException, ValidationException {
    return digitalObjectService.save(digitalObject);
  }

  @Operation(summary = "Save list of fileresources for a given digital object")
  @PostMapping(
      value = {
        "/v6/digitalobjects/{uuid}/fileresources",
        "/v5/digitalobjects/{uuid}/fileresources",
        "/v3/digitalobjects/{uuid}/fileresources",
        "/latest/digitalobjects/{uuid}/fileresources"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<FileResource> setFileResources(
      @Parameter(example = "", description = "UUID of the digital object") @PathVariable("uuid")
          UUID uuid,
      @RequestBody List<FileResource> fileResources) {
    return digitalObjectService.setFileResources(uuid, fileResources);
  }

  @Operation(summary = "Update a digital object")
  @PutMapping(
      value = {
        "/v6/digitalobjects/{uuid}",
        "/v5/digitalobjects/{uuid}",
        "/v2/digitalobjects/{uuid}",
        "/latest/digitalobjects/{uuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public DigitalObject update(
      @Parameter(example = "", description = "UUID of the digital object") @PathVariable("uuid")
          UUID uuid,
      @RequestBody DigitalObject digitalObject,
      BindingResult errors)
      throws IdentifiableServiceException, ValidationException {
    assert Objects.equals(uuid, digitalObject.getUuid());
    return digitalObjectService.update(digitalObject);
  }
}
