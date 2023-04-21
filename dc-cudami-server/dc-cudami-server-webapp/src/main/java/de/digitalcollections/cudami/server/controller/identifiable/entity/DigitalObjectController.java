package de.digitalcollections.cudami.server.controller.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.DigitalObjectService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.EntityService;
import de.digitalcollections.cudami.server.controller.AbstractEntityController;
import de.digitalcollections.cudami.server.controller.ParameterHelper;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.entity.item.Item;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.FilterOperation;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Digital object controller")
public class DigitalObjectController extends AbstractEntityController<DigitalObject> {

  private final DigitalObjectService service;

  public DigitalObjectController(DigitalObjectService digitalObjectService) {
    this.service = digitalObjectService;
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
  public long count() throws ServiceException {
    return super.count();
  }

  @Operation(summary = "Delete a digital object with all its relations")
  @DeleteMapping(
      value = {
        "/v6/digitalobjects/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/digitalobjects/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v2/digitalobjects/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/latest/digitalobjects/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity delete(
      @Parameter(example = "", description = "UUID of the digital object") @PathVariable("uuid")
          UUID uuid)
      throws ConflictException, ServiceException {
    return super.delete(uuid);
  }

  @Operation(summary = "Get all digital objects as (paged, sorted, filtered) list")
  @GetMapping(
      value = {"/v6/digitalobjects"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<DigitalObject> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "filter", required = false) List<FilterCriterion> filterCriteria)
      throws ServiceException {
    return super.find(pageNumber, pageSize, sortBy, filterCriteria);
  }

  @Operation(summary = "Get all projects of a digital object as (paged, sorted, filtered) list")
  @GetMapping(
      value = {"/v6/digitalobjects/{uuid:" + ParameterHelper.UUID_PATTERN + "}/projects"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Project> findProjects(
      @Parameter(example = "", description = "UUID of the digital object") @PathVariable("uuid")
          UUID uuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "filter", required = false) List<FilterCriterion> filterCriteria)
      throws ServiceException {
    PageRequest pageRequest =
        createPageRequest(Project.class, pageNumber, pageSize, sortBy, filterCriteria);
    return service.findProjects(buildExampleWithUuid(uuid), pageRequest);
  }

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
  public ResponseEntity<DigitalObject> getByIdentifier(
      HttpServletRequest request,
      @RequestParam(name = "fill-wemi", required = false, defaultValue = "false") boolean fillWemi)
      throws ServiceException, ValidationException {

    Pair<String, String> namespaceAndId = extractNamespaceAndId(request);
    Identifier identifier =
        Identifier.builder()
            .namespace(namespaceAndId.getLeft())
            .id(namespaceAndId.getRight())
            .build();

    DigitalObject digitalObject =
        fillWemi
            ? service.getByIdentifierWithWEMI(identifier)
            : service.getByIdentifier(identifier);
    return new ResponseEntity<>(
        digitalObject, digitalObject != null ? HttpStatus.OK : HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Get a digital object by refId")
  @GetMapping(
      value = {"/v5/digitalobjects/{refId:[0-9]+}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<DigitalObject> getByRefId(@PathVariable long refId)
      throws ServiceException {
    return super.getByRefId(refId);
  }

  @Operation(summary = "Get a digital object by uuid")
  @GetMapping(
      value = {
        "/v6/digitalobjects/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/digitalobjects/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v2/digitalobjects/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/latest/digitalobjects/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<DigitalObject> getByUuid(@PathVariable UUID uuid) throws ServiceException {
    return super.getByUuid(uuid);
  }

  @Operation(
      summary =
          "Get all (active) collections of a digital object as (paged, sorted, filtered) list")
  @GetMapping(
      value = {"/v6/digitalobjects/{uuid:" + ParameterHelper.UUID_PATTERN + "}/collections"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Collection> getCollections(
      @Parameter(example = "", description = "UUID of the digital object") @PathVariable("uuid")
          UUID uuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "filter", required = false) List<FilterCriterion> filterCriteria,
      @RequestParam(name = "active", required = false) String active)
      throws ServiceException {
    PageRequest pageRequest =
        createPageRequest(Collection.class, pageNumber, pageSize, sortBy, filterCriteria);
    DigitalObject example = buildExampleWithUuid(uuid);
    if (active != null) {
      return service.findActiveCollections(example, pageRequest);
    }
    return service.findCollections(example, pageRequest);
  }

  @Operation(summary = "Get file resources of a digital object")
  @GetMapping(
      value = {
        "/v6/digitalobjects/{uuid:" + ParameterHelper.UUID_PATTERN + "}/fileresources",
        "/v5/digitalobjects/{uuid:" + ParameterHelper.UUID_PATTERN + "}/fileresources",
        "/v2/digitalobjects/{uuid:" + ParameterHelper.UUID_PATTERN + "}/fileresources",
        "/latest/digitalobjects/{uuid:" + ParameterHelper.UUID_PATTERN + "}/fileresources"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<FileResource> getFileResources(@PathVariable UUID uuid) throws ServiceException {
    return service.getFileResources(buildExampleWithUuid(uuid));
  }

  @Operation(summary = "Get image file resources of a digital object")
  @GetMapping(
      value = {
        "/v6/digitalobjects/{uuid:" + ParameterHelper.UUID_PATTERN + "}/fileresources/images",
        "/v5/digitalobjects/{uuid:" + ParameterHelper.UUID_PATTERN + "}/fileresources/images",
        "/v2/digitalobjects/{uuid:" + ParameterHelper.UUID_PATTERN + "}/fileresources/images",
        "/latest/digitalobjects/{uuid:" + ParameterHelper.UUID_PATTERN + "}/fileresources/images"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<ImageFileResource> getImageFileResources(@PathVariable UUID uuid)
      throws ServiceException {
    return service.getImageFileResources(buildExampleWithUuid(uuid));
  }

  @Operation(summary = "Get item for digital object by digital object uuid")
  @GetMapping(
      value = {
        "/v6/digitalobjects/{uuid:" + ParameterHelper.UUID_PATTERN + "}/item",
        "/v5/digitalobjects/{uuid:" + ParameterHelper.UUID_PATTERN + "}/item",
        "/v2/digitalobjects/{uuid:" + ParameterHelper.UUID_PATTERN + "}/item",
        "/latest/digitalobjects/{uuid:" + ParameterHelper.UUID_PATTERN + "}/item"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Item getItem(@PathVariable UUID uuid) throws ServiceException {
    return service.getItem(buildExampleWithUuid(uuid));
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
      @RequestParam(name = "filter", required = false) List<FilterCriterion> filterCriteria)
      throws ServiceException {
    if (filterCriteria != null) {
      Optional<FilterCriterion> parentUuidCriterion =
          filterCriteria.stream()
              .filter(
                  p ->
                      "parent.uuid".equals(p.getExpression())
                          && p.getOperation() == FilterOperation.EQUALS)
              .findAny();
      if (parentUuidCriterion.isPresent()) {
        return service.getLanguagesOfContainedDigitalObjects(
            buildExampleWithUuid(UUID.fromString((String) parentUuidCriterion.get().getValue())));
      }
    }
    return super.getLanguages();
  }

  @Operation(summary = "Get all languages of a digital object's collections")
  @GetMapping(
      value = {
        "/v6/digitalobjects/{uuid:" + ParameterHelper.UUID_PATTERN + "}/collections/languages",
        "/v5/digitalobjects/{uuid:" + ParameterHelper.UUID_PATTERN + "}/collections/languages"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Locale> getLanguagesOfCollections(@PathVariable UUID uuid) throws ServiceException {
    return this.service.getLanguagesOfCollections(buildExampleWithUuid(uuid));
  }

  @Operation(summary = "Get all languages of a digital object's projects")
  @GetMapping(
      value = {
        "/v6/digitalobjects/{uuid:" + ParameterHelper.UUID_PATTERN + "}/projects/languages",
        "/v5/digitalobjects/{uuid:" + ParameterHelper.UUID_PATTERN + "}/projects/languages"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Locale> getLanguagesOfProjects(@PathVariable UUID uuid) throws ServiceException {
    return this.service.getLanguagesOfProjects(buildExampleWithUuid(uuid));
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
      @RequestParam(name = "count", required = false, defaultValue = "5") int count)
      throws ServiceException {
    return service.getRandom(count);
  }

  @Override
  protected EntityService<DigitalObject> getService() {
    return service;
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
      throws ServiceException, ValidationException {
    return super.save(digitalObject, errors);
  }

  @Operation(summary = "Save list of fileresources for a given digital object")
  @PostMapping(
      value = {
        "/v6/digitalobjects/{uuid:" + ParameterHelper.UUID_PATTERN + "}/fileresources",
        "/v5/digitalobjects/{uuid:" + ParameterHelper.UUID_PATTERN + "}/fileresources",
        "/v3/digitalobjects/{uuid:" + ParameterHelper.UUID_PATTERN + "}/fileresources",
        "/latest/digitalobjects/{uuid:" + ParameterHelper.UUID_PATTERN + "}/fileresources"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<FileResource> setFileResources(
      @Parameter(example = "", description = "UUID of the digital object") @PathVariable("uuid")
          UUID uuid,
      @RequestBody List<FileResource> fileResources)
      throws ServiceException {
    return service.setFileResources(buildExampleWithUuid(uuid), fileResources);
  }

  @Operation(summary = "Update a digital object")
  @PutMapping(
      value = {
        "/v6/digitalobjects/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/digitalobjects/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v2/digitalobjects/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/latest/digitalobjects/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public DigitalObject update(
      @Parameter(example = "", description = "UUID of the digital object") @PathVariable("uuid")
          UUID uuid,
      @RequestBody DigitalObject digitalObject,
      BindingResult errors)
      throws ServiceException, ValidationException {
    return super.update(uuid, digitalObject, errors);
  }
}
