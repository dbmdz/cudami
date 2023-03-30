package de.digitalcollections.cudami.server.controller.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.CollectionService;
import de.digitalcollections.cudami.server.controller.ParameterHelper;
import de.digitalcollections.cudami.server.controller.identifiable.AbstractIdentifiableController;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.view.BreadcrumbNavigation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@Tag(name = "Collection controller")
public class CollectionController extends AbstractIdentifiableController<Collection> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CollectionController.class);

  private final LocaleService localeService;
  private final CollectionService service;

  public CollectionController(CollectionService collectionService, LocaleService localeService) {
    this.service = collectionService;
    this.localeService = localeService;
  }

  @Operation(summary = "Add an existing digital object to an existing collection")
  @PostMapping(
      value = {
        "/v6/collections/{uuid:"
            + ParameterHelper.UUID_PATTERN
            + "}/digitalobjects/{digitalObjectUuid}",
        "/v5/collections/{uuid:"
            + ParameterHelper.UUID_PATTERN
            + "}/digitalobjects/{digitalObjectUuid}",
        "/v3/collections/{uuid:"
            + ParameterHelper.UUID_PATTERN
            + "}/digitalobjects/{digitalObjectUuid}",
        "/latest/collections/{uuid:"
            + ParameterHelper.UUID_PATTERN
            + "}/digitalobjects/{digitalObjectUuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity addDigitalObject(
      @Parameter(example = "", description = "UUID of the collection") @PathVariable("uuid")
          UUID collectionUuid,
      @Parameter(example = "", description = "UUID of the digital object")
          @PathVariable("digitalObjectUuid")
          UUID digitalObjectUuid) {
    Collection collection = new Collection();
    collection.setUuid(collectionUuid);

    DigitalObject digitalObject = new DigitalObject();
    digitalObject.setUuid(digitalObjectUuid);

    boolean successful = service.addDigitalObject(collection, digitalObject);
    return successful
        ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
        : new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Add existing digital objects to an existing collection")
  @PostMapping(
      value = {
        "/v6/collections/{uuid:" + ParameterHelper.UUID_PATTERN + "}/digitalobjects",
        "/v5/collections/{uuid:" + ParameterHelper.UUID_PATTERN + "}/digitalobjects",
        "/v3/collections/{uuid:" + ParameterHelper.UUID_PATTERN + "}/digitalobjects",
        "/latest/collections/{uuid:" + ParameterHelper.UUID_PATTERN + "}/digitalobjects"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity addDigitalObjects(
      @Parameter(example = "", description = "UUID of the collection") @PathVariable("uuid")
          UUID collectionUuid,
      @Parameter(example = "", description = "List of the digital objects") @RequestBody
          List<DigitalObject> digitalObjects) {
    Collection collection = new Collection();
    collection.setUuid(collectionUuid);

    boolean successful = service.addDigitalObjects(collection, digitalObjects);
    return successful
        ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
        : new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Add an existing collection to an existing collection")
  @PostMapping(
      value = {
        "/v6/collections/{uuid:"
            + ParameterHelper.UUID_PATTERN
            + "}/subcollections/{subcollectionUuid}",
        "/v5/collections/{uuid:"
            + ParameterHelper.UUID_PATTERN
            + "}/subcollections/{subcollectionUuid}",
        "/v3/collections/{uuid:"
            + ParameterHelper.UUID_PATTERN
            + "}/subcollections/{subcollectionUuid}",
        "/latest/collections/{uuid:"
            + ParameterHelper.UUID_PATTERN
            + "}/subcollections/{subcollectionUuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity addSubcollection(
      @Parameter(example = "", description = "UUID of the collection") @PathVariable("uuid")
          UUID uuid,
      @Parameter(example = "", description = "UUID of the subcollection")
          @PathVariable("subcollectionUuid")
          UUID subcollectionUuid) {
    Collection collection = new Collection();
    collection.setUuid(uuid);

    Collection subcollection = new Collection();
    subcollection.setUuid(subcollectionUuid);

    boolean successful = service.addChild(collection, subcollection);
    return successful
        ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
        : new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Add existing collections to an existing collection")
  @PostMapping(
      value = {
        "/v6/collections/{uuid:" + ParameterHelper.UUID_PATTERN + "}/subcollections",
        "/v5/collections/{uuid:" + ParameterHelper.UUID_PATTERN + "}/subcollections",
        "/v3/collections/{uuid:" + ParameterHelper.UUID_PATTERN + "}/subcollections",
        "/latest/collections/{uuid:" + ParameterHelper.UUID_PATTERN + "}/subcollections"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity addSubcollections(
      @Parameter(example = "", description = "UUID of the collection") @PathVariable("uuid")
          UUID uuid,
      @Parameter(example = "", description = "List of the subcollections") @RequestBody
          List<Collection> subcollections) {
    Collection collection = new Collection();
    collection.setUuid(uuid);

    boolean successful = service.addChildren(collection, subcollections);
    return successful
        ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
        : new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Get count of collections")
  @GetMapping(
      value = {
        "/v6/collections/count",
        "/v5/collections/count",
        "/v2/collections/count",
        "/latest/collections/count"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public long count() {
    return service.count();
  }

  @Operation(summary = "Delete an existing collection")
  @DeleteMapping(
      value = {
        "/v6/collections/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity delete(
      @Parameter(example = "", description = "UUID of the collection") @PathVariable("uuid")
          UUID uuid)
      throws ConflictException {

    boolean successful;
    try {
      successful = service.deleteByUuid(uuid);
    } catch (ServiceException e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
    return successful
        ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
        : new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Get (active or all) collections as (paged, sorted, filtered) list")
  @GetMapping(
      value = {"/v6/collections"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Collection> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "filter", required = false) List<FilterCriterion> filterCriteria,
      @RequestParam(name = "active", required = false) String active) {
    PageRequest pageRequest =
        createPageRequest(Collection.class, pageNumber, pageSize, sortBy, filterCriteria);
    if (active != null) {
      return service.findActive(pageRequest);
    }
    return service.find(pageRequest);
  }

  @Operation(summary = "Get all digital objects of a collection as (paged, sorted, filtered) list")
  @GetMapping(
      value = {"/v6/collections/{uuid:" + ParameterHelper.UUID_PATTERN + "}/digitalobjects"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<DigitalObject> findDigitalObjects(
      @Parameter(example = "", description = "UUID of the collection") @PathVariable("uuid")
          UUID collectionUuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "filter", required = false) List<FilterCriterion> filterCriteria) {
    PageRequest pageRequest =
        createPageRequest(DigitalObject.class, pageNumber, pageSize, sortBy, filterCriteria);
    Collection collection = new Collection();
    collection.setUuid(collectionUuid);
    return service.findDigitalObjects(collection, pageRequest);
  }

  @Operation(
      summary = "Get all related - by the given predicate - corporate bodies of a collection")
  @GetMapping(
      value = {
        "/v6/collections/{uuid:" + ParameterHelper.UUID_PATTERN + "}/related/corporatebodies",
        "/v5/collections/{uuid:" + ParameterHelper.UUID_PATTERN + "}/related/corporatebodies",
        "/v3/collections/{uuid:" + ParameterHelper.UUID_PATTERN + "}/related/corporatebodies",
        "/latest/collections/{uuid:" + ParameterHelper.UUID_PATTERN + "}/related/corporatebodies"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<CorporateBody> findRelatedCorporateBodies(
      @Parameter(example = "", description = "UUID of the collection") @PathVariable("uuid")
          UUID uuid,
      @RequestParam(name = "predicate", required = true) FilterCriterion<String> predicate) {
    Filtering filtering = Filtering.builder().add("predicate", predicate).build();
    return service.findRelatedCorporateBodies(uuid, filtering);
  }

  @Operation(
      summary = "Get all (active) subcollections of a collection as (paged, sorted, filtered) list")
  @GetMapping(
      value = {"/v6/collections/{uuid:" + ParameterHelper.UUID_PATTERN + "}/subcollections"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Collection> findSubcollections(
      @Parameter(example = "", description = "UUID of the collection") @PathVariable("uuid")
          UUID collectionUuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "filter", required = false) List<FilterCriterion> filterCriteria,
      @RequestParam(name = "active", required = false) String active) {
    PageRequest pageRequest =
        createPageRequest(Collection.class, pageNumber, pageSize, sortBy, filterCriteria);
    if (active != null) {
      return service.findActiveChildren(collectionUuid, pageRequest);
    }
    return service.findSubParts(collectionUuid, pageRequest);
  }

  @Operation(summary = "Get all (active) top collections as (paged, sorted, filtered) list")
  @GetMapping(
      value = {"/v6/collections/top"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Collection> findTopCollections(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "filter", required = false) List<FilterCriterion> filterCriteria,
      @RequestParam(name = "active", required = false) String active) {
    PageRequest pageRequest =
        createPageRequest(Collection.class, pageNumber, pageSize, sortBy, filterCriteria);
    return service.findRootNodes(pageRequest);
  }

  @Operation(summary = "Get the breadcrumb for a collection")
  @GetMapping(
      value = {
        "/v6/collections/{uuid:" + ParameterHelper.UUID_PATTERN + "}/breadcrumb",
        "/v5/collections/{uuid:" + ParameterHelper.UUID_PATTERN + "}/breadcrumb",
        "/v3/collections/{uuid:" + ParameterHelper.UUID_PATTERN + "}/breadcrumb",
        "/latest/collections/{uuid:" + ParameterHelper.UUID_PATTERN + "}/breadcrumb"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<BreadcrumbNavigation> getBreadcrumbNavigation(
      @Parameter(
              example = "",
              description =
                  "UUID of the collection, e.g. <tt>6119d8e9-9c92-4091-8dcb-bc4053385406</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @Parameter(
              name = "pLocale",
              description =
                  "Desired locale, e.g. <tt>de_DE</tt>. If unset, contents in all languages will be returned")
          @RequestParam(name = "pLocale", required = false)
          Locale pLocale) {

    BreadcrumbNavigation breadcrumbNavigation;

    if (pLocale == null) {
      breadcrumbNavigation = service.getBreadcrumbNavigation(uuid);
    } else {
      breadcrumbNavigation =
          service.getBreadcrumbNavigation(uuid, pLocale, localeService.getDefaultLocale());
    }

    if (breadcrumbNavigation == null || breadcrumbNavigation.getNavigationItems().isEmpty()) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    return new ResponseEntity<>(breadcrumbNavigation, HttpStatus.OK);
  }

  @Operation(
      summary = "Get a collection by namespace and id",
      description =
          "Separate namespace and id with a colon, e.g. foo:bar. It is also possible, to add a .json suffix, which will be ignored then")
  @GetMapping(
      value = {
        "/v6/collections/identifier/**",
        "/v5/collections/identifier/**",
        "/v2/collections/identifier/**",
        "/latest/collections/identifier/**"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Override
  public ResponseEntity<Collection> getByIdentifier(HttpServletRequest request)
      throws ServiceException, ValidationException {
    return super.getByIdentifier(request);
  }

  @Operation(summary = "Get a collection by refId")
  @GetMapping(
      value = {
        "/v6/collections/{refId:[0-9]+}",
        "/v5/collections/{refId:[0-9]+}",
        "/latest/collections/{refId:[0-9]+}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Collection> getByRefId(
      @Parameter(example = "", description = "refId of the collection, e.g. <tt>42</tt>")
          @PathVariable
          long refId)
      throws ServiceException {
    Collection collection = service.getByRefId(refId);
    if (collection == null) {
      return ResponseEntity.notFound().build();
    }
    return getByUuid(collection.getUuid(), null, null);
  }

  @Operation(summary = "Get a collection by uuid")
  @GetMapping(
      value = {
        "/v6/collections/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/collections/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v2/collections/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/latest/collections/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Collection> getByUuid(
      @Parameter(
              example = "",
              description =
                  "UUID of the collection, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @Parameter(
              name = "pLocale",
              description =
                  "Desired locale, e.g. <tt>de_DE</tt>. If unset, contents in all languages will be returned")
          @RequestParam(name = "pLocale", required = false)
          Locale pLocale,
      @Parameter(name = "active", description = "If set, object will only be returned if active")
          @RequestParam(name = "active", required = false)
          String active)
      throws ServiceException {
    Collection collection;
    if (active != null) {
      if (pLocale == null) {
        collection = service.getByExampleAndActive(uuid);
      } else {
        collection = service.getByExampleAndActiveAndLocale(uuid, pLocale);
      }
    } else {
      if (pLocale == null) {
        collection = service.getByUuid(uuid);
      } else {
        collection = service.getByUuidAndLocale(uuid, pLocale);
      }
    }
    return new ResponseEntity<>(
        collection, collection != null ? HttpStatus.OK : HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Get the first created parent of a collection")
  @GetMapping(
      value = {
        "/v6/collections/{uuid:" + ParameterHelper.UUID_PATTERN + "}/parent",
        "/v5/collections/{uuid:" + ParameterHelper.UUID_PATTERN + "}/parent",
        "/v3/collections/{uuid:" + ParameterHelper.UUID_PATTERN + "}/parent",
        "/latest/collections/{uuid:" + ParameterHelper.UUID_PATTERN + "}/parent"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Collection getParent(@PathVariable UUID uuid) {
    return service.getParent(uuid);
  }

  @Operation(summary = "Get parent collections")
  @GetMapping(
      value = {
        "/v6/collections/{uuid:" + ParameterHelper.UUID_PATTERN + "}/parents",
        "/v5/collections/{uuid:" + ParameterHelper.UUID_PATTERN + "}/parents",
        "/v3/collections/{uuid:" + ParameterHelper.UUID_PATTERN + "}/parents",
        "/latest/collections/{uuid:" + ParameterHelper.UUID_PATTERN + "}/parents"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Collection> getParents(
      @Parameter(example = "", description = "UUID of the collection") @PathVariable("uuid")
          UUID collectionUuid) {
    return service.getParents(collectionUuid);
  }

  @Override
  protected IdentifiableService<Collection> getService() {
    return service;
  }

  @Operation(summary = "Get languages of all top collections")
  @GetMapping(
      value = {
        "/v6/collections/top/languages",
        "/v5/collections/top/languages",
        "/v2/collections/top/languages",
        "/latest/collections/top/languages"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Locale> getTopCollectionsLanguages() {
    return service.getRootNodesLanguages();
  }

  @Operation(summary = "Remove an existing digital object from an existing collection")
  @DeleteMapping(
      value = {
        "/v6/collections/{uuid:"
            + ParameterHelper.UUID_PATTERN
            + "}/digitalobjects/{digitalObjectUuid}",
        "/v5/collections/{uuid:"
            + ParameterHelper.UUID_PATTERN
            + "}/digitalobjects/{digitalObjectUuid}",
        "/v3/collections/{uuid:"
            + ParameterHelper.UUID_PATTERN
            + "}/digitalobjects/{digitalObjectUuid}",
        "/latest/collections/{uuid:"
            + ParameterHelper.UUID_PATTERN
            + "}/digitalobjects/{digitalObjectUuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity removeDigitalObject(
      @Parameter(example = "", description = "UUID of the collection") @PathVariable("uuid")
          UUID collectionUuid,
      @Parameter(example = "", description = "UUID of the digital object")
          @PathVariable("digitalObjectUuid")
          UUID digitalObjectUuid) {
    Collection collection = new Collection();
    collection.setUuid(collectionUuid);

    DigitalObject digitalObject = new DigitalObject();
    digitalObject.setUuid(digitalObjectUuid);

    boolean successful = service.removeDigitalObject(collection, digitalObject);
    return successful
        ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
        : new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Remove an existing collection from an existing collection")
  @DeleteMapping(
      value = {
        "/v6/collections/{uuid:"
            + ParameterHelper.UUID_PATTERN
            + "}/subcollections/{subcollectionUuid}",
        "/v5/collections/{uuid:"
            + ParameterHelper.UUID_PATTERN
            + "}/subcollections/{subcollectionUuid}",
        "/v3/collections/{uuid:"
            + ParameterHelper.UUID_PATTERN
            + "}/subcollections/{subcollectionUuid}",
        "/latest/collections/{uuid:"
            + ParameterHelper.UUID_PATTERN
            + "}/subcollections/{subcollectionUuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity removeSubcollection(
      @Parameter(example = "", description = "UUID of the collection") @PathVariable("uuid")
          UUID uuid,
      @Parameter(example = "", description = "UUID of the subcollection")
          @PathVariable("subcollectionUuid")
          UUID subcollectionUuid) {
    Collection collection = new Collection();
    collection.setUuid(uuid);

    Collection subcollection = new Collection();
    subcollection.setUuid(subcollectionUuid);

    boolean successful = service.removeChild(collection, subcollection);
    return successful
        ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
        : new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Save a newly created collection")
  @PostMapping(
      value = {"/v6/collections", "/v5/collections", "/v2/collections", "/latest/collections"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Collection save(@RequestBody Collection collection, BindingResult errors)
      throws ServiceException, ValidationException {
    service.save(collection);
    return collection;
  }

  @Operation(summary = "Save existing digital objects into an existing collection")
  @PutMapping(
      value = {
        "/v6/collections/{uuid:" + ParameterHelper.UUID_PATTERN + "}/digitalobjects",
        "/v5/collections/{uuid:" + ParameterHelper.UUID_PATTERN + "}/digitalobjects",
        "/v3/collections/{uuid:" + ParameterHelper.UUID_PATTERN + "}/digitalobjects",
        "/latest/collections/{uuid:" + ParameterHelper.UUID_PATTERN + "}/digitalobjects"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity saveDigitalObjects(
      @Parameter(example = "", description = "UUID of the collection") @PathVariable("uuid")
          UUID collectionUuid,
      @Parameter(example = "", description = "List of the digital objects") @RequestBody
          List<DigitalObject> digitalObjects) {
    Collection collection = new Collection();
    collection.setUuid(collectionUuid);

    boolean successful = service.saveDigitalObjects(collection, digitalObjects);
    return successful
        ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
        : new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Save a newly created collection with parent collection")
  @PostMapping(
      value = {
        "/v6/collections/{parentUuid}/collection",
        "/v5/collections/{parentUuid}/collection",
        "/v2/collections/{parentUuid}/collection",
        "/latest/collections/{parentUuid}/collection"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Collection saveWithParentCollection(
      @Parameter(name = "parentUuid", description = "The uuid of the parent collection")
          @PathVariable
          UUID parentUuid,
      @RequestBody Collection collection)
      throws ServiceException, ValidationException {
    return service.saveWithParent(collection, parentUuid);
  }

  @Operation(summary = "Update a collection")
  @PutMapping(
      value = {
        "/v6/collections/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/collections/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v2/collections/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/latest/collections/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Collection update(
      @PathVariable UUID uuid, @RequestBody Collection collection, BindingResult errors)
      throws ServiceException, ValidationException {
    assert Objects.equals(uuid, collection.getUuid());
    service.update(collection);
    return collection;
  }
}
