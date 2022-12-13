package de.digitalcollections.cudami.server.controller.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
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
import de.digitalcollections.model.list.sorting.Sorting;
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

  private final CollectionService collectionService;
  private final LocaleService localeService;

  public CollectionController(CollectionService collectionService, LocaleService localeService) {
    this.collectionService = collectionService;
    this.localeService = localeService;
  }

  @Override
  protected IdentifiableService<Collection> getService() {
    return collectionService;
  }

  @Operation(summary = "Add an existing digital object to an existing collection")
  @PostMapping(
      value = {
        "/v6/collections/{uuid}/digitalobjects/{digitalObjectUuid}",
        "/v5/collections/{uuid}/digitalobjects/{digitalObjectUuid}",
        "/v3/collections/{uuid}/digitalobjects/{digitalObjectUuid}",
        "/latest/collections/{uuid}/digitalobjects/{digitalObjectUuid}"
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

    boolean successful = collectionService.addDigitalObject(collection, digitalObject);
    return successful
        ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
        : new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Add existing digital objects to an existing collection")
  @PostMapping(
      value = {
        "/v6/collections/{uuid}/digitalobjects",
        "/v5/collections/{uuid}/digitalobjects",
        "/v3/collections/{uuid}/digitalobjects",
        "/latest/collections/{uuid}/digitalobjects"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity addDigitalObjects(
      @Parameter(example = "", description = "UUID of the collection") @PathVariable("uuid")
          UUID collectionUuid,
      @Parameter(example = "", description = "List of the digital objects") @RequestBody
          List<DigitalObject> digitalObjects) {
    Collection collection = new Collection();
    collection.setUuid(collectionUuid);

    boolean successful = collectionService.addDigitalObjects(collection, digitalObjects);
    return successful
        ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
        : new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Add an existing collection to an existing collection")
  @PostMapping(
      value = {
        "/v6/collections/{uuid}/subcollections/{subcollectionUuid}",
        "/v5/collections/{uuid}/subcollections/{subcollectionUuid}",
        "/v3/collections/{uuid}/subcollections/{subcollectionUuid}",
        "/latest/collections/{uuid}/subcollections/{subcollectionUuid}"
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

    boolean successful = collectionService.addChild(collection, subcollection);
    return successful
        ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
        : new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Add existing collections to an existing collection")
  @PostMapping(
      value = {
        "/v6/collections/{uuid}/subcollections",
        "/v5/collections/{uuid}/subcollections",
        "/v3/collections/{uuid}/subcollections",
        "/latest/collections/{uuid}/subcollections"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity addSubcollections(
      @Parameter(example = "", description = "UUID of the collection") @PathVariable("uuid")
          UUID uuid,
      @Parameter(example = "", description = "List of the subcollections") @RequestBody
          List<Collection> subcollections) {
    Collection collection = new Collection();
    collection.setUuid(uuid);

    boolean successful = collectionService.addChildren(collection, subcollections);
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
    return collectionService.count();
  }

  @Operation(summary = "Delete an existing collection")
  @DeleteMapping(
      value = {
        "/v6/collections/{uuid}",
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity delete(
      @Parameter(example = "", description = "UUID of the collection") @PathVariable("uuid")
          UUID uuid)
      throws ConflictException {

    boolean successful;
    try {
      successful = collectionService.delete(uuid);
    } catch (IdentifiableServiceException e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
    return successful
        ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
        : new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @Operation(
      summary =
          "Find limited amount of (active or all) collections containing searchTerm in label or description")
  @GetMapping(
      value = {"/v6/collections"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  // should work with v5-clients, too, as only searchTerm field is added (will be ignored by jackson
  // if unknown)
  public PageResponse<Collection> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "searchTerm", required = false) String searchTerm,
      @RequestParam(name = "label", required = false) String labelTerm,
      @RequestParam(name = "labelLanguage", required = false) Locale labelLanguage,
      @RequestParam(name = "active", required = false) String active) {
    PageRequest pageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    addLabelFilter(pageRequest, labelTerm, labelLanguage);
    if (active != null) {
      return collectionService.findActive(pageRequest);
    }
    return collectionService.find(pageRequest);
  }

  @Operation(summary = "Get paged digital objects of a collection")
  @GetMapping(
      value = {"/v6/collections/{uuid}/digitalobjects"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<DigitalObject> findDigitalObjects(
      @Parameter(example = "", description = "UUID of the collection") @PathVariable("uuid")
          UUID collectionUuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "searchTerm", required = false) String searchTerm) {
    PageRequest searchPageRequest = new PageRequest(searchTerm, pageNumber, pageSize);

    Collection collection = new Collection();
    collection.setUuid(collectionUuid);
    return collectionService.findDigitalObjects(collection, searchPageRequest);
  }

  @Operation(
      summary = "Get all related - by the given predicate - corporate bodies of a collection")
  @GetMapping(
      value = {
        "/v6/collections/{uuid}/related/corporatebodies",
        "/v5/collections/{uuid}/related/corporatebodies",
        "/v3/collections/{uuid}/related/corporatebodies",
        "/latest/collections/{uuid}/related/corporatebodies"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<CorporateBody> findRelatedCorporateBodies(
      @Parameter(example = "", description = "UUID of the collection") @PathVariable("uuid")
          UUID uuid,
      @RequestParam(name = "predicate", required = true) FilterCriterion<String> predicate) {
    Filtering filtering = Filtering.builder().add("predicate", predicate).build();
    return collectionService.findRelatedCorporateBodies(uuid, filtering);
  }

  @Operation(summary = "Get (active or all) paged subcollections of a collection")
  @GetMapping(
      value = {"/v6/collections/{uuid}/subcollections"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Collection> findSubcollections(
      @Parameter(example = "", description = "UUID of the collection") @PathVariable("uuid")
          UUID collectionUuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "active", required = false) String active,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "searchTerm", required = false) String searchTerm) {
    PageRequest searchPageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      searchPageRequest.setSorting(sorting);
    }
    if (active != null) {
      return collectionService.findActiveChildren(collectionUuid, searchPageRequest);
    }
    return collectionService.findChildren(collectionUuid, searchPageRequest);
  }

  @Operation(summary = "Get all top collections")
  @GetMapping(
      value = {"/v6/collections/top"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Collection> findTopCollections(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "searchTerm", required = false) String searchTerm) {
    PageRequest pageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    return collectionService.findRootNodes(pageRequest);
  }

  @Operation(summary = "Get the breadcrumb for a collection")
  @GetMapping(
      value = {
        "/v6/collections/{uuid}/breadcrumb",
        "/v5/collections/{uuid}/breadcrumb",
        "/v3/collections/{uuid}/breadcrumb",
        "/latest/collections/{uuid}/breadcrumb"
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
      breadcrumbNavigation = collectionService.getBreadcrumbNavigation(uuid);
    } else {
      breadcrumbNavigation =
          collectionService.getBreadcrumbNavigation(
              uuid, pLocale, localeService.getDefaultLocale());
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
      throws IdentifiableServiceException, ValidationException {
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
      throws IdentifiableServiceException {
    Collection collection = collectionService.getByRefId(refId);
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
      throws IdentifiableServiceException {
    Collection collection;
    if (active != null) {
      if (pLocale == null) {
        collection = collectionService.getActive(uuid);
      } else {
        collection = collectionService.getActive(uuid, pLocale);
      }
    } else {
      if (pLocale == null) {
        collection = collectionService.getByUuid(uuid);
      } else {
        collection = collectionService.getByUuidAndLocale(uuid, pLocale);
      }
    }
    return new ResponseEntity<>(
        collection, collection != null ? HttpStatus.OK : HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Get the first created parent of a collection")
  @GetMapping(
      value = {
        "/v6/collections/{uuid}/parent",
        "/v5/collections/{uuid}/parent",
        "/v3/collections/{uuid}/parent",
        "/latest/collections/{uuid}/parent"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Collection getParent(@PathVariable UUID uuid) {
    return collectionService.getParent(uuid);
  }

  @Operation(summary = "Get parent collections")
  @GetMapping(
      value = {
        "/v6/collections/{uuid}/parents",
        "/v5/collections/{uuid}/parents",
        "/v3/collections/{uuid}/parents",
        "/latest/collections/{uuid}/parents"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Collection> getParents(
      @Parameter(example = "", description = "UUID of the collection") @PathVariable("uuid")
          UUID collectionUuid) {
    return collectionService.getParents(collectionUuid);
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
    return collectionService.getRootNodesLanguages();
  }

  @Operation(summary = "Remove an existing digital object from an existing collection")
  @DeleteMapping(
      value = {
        "/v6/collections/{uuid}/digitalobjects/{digitalObjectUuid}",
        "/v5/collections/{uuid}/digitalobjects/{digitalObjectUuid}",
        "/v3/collections/{uuid}/digitalobjects/{digitalObjectUuid}",
        "/latest/collections/{uuid}/digitalobjects/{digitalObjectUuid}"
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

    boolean successful = collectionService.removeDigitalObject(collection, digitalObject);
    return successful
        ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
        : new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Remove an existing collection from an existing collection")
  @DeleteMapping(
      value = {
        "/v6/collections/{uuid}/subcollections/{subcollectionUuid}",
        "/v5/collections/{uuid}/subcollections/{subcollectionUuid}",
        "/v3/collections/{uuid}/subcollections/{subcollectionUuid}",
        "/latest/collections/{uuid}/subcollections/{subcollectionUuid}"
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

    boolean successful = collectionService.removeChild(collection, subcollection);
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
    collectionService.save(collection);
    return collection;
  }

  @Operation(summary = "Save existing digital objects into an existing collection")
  @PutMapping(
      value = {
        "/v6/collections/{uuid}/digitalobjects",
        "/v5/collections/{uuid}/digitalobjects",
        "/v3/collections/{uuid}/digitalobjects",
        "/latest/collections/{uuid}/digitalobjects"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity saveDigitalObjects(
      @Parameter(example = "", description = "UUID of the collection") @PathVariable("uuid")
          UUID collectionUuid,
      @Parameter(example = "", description = "List of the digital objects") @RequestBody
          List<DigitalObject> digitalObjects) {
    Collection collection = new Collection();
    collection.setUuid(collectionUuid);

    boolean successful = collectionService.saveDigitalObjects(collection, digitalObjects);
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
    return collectionService.saveWithParent(collection, parentUuid);
  }

  @Operation(summary = "Update a collection")
  @PutMapping(
      value = {
        "/v6/collections/{uuid}",
        "/v5/collections/{uuid}",
        "/v2/collections/{uuid}",
        "/latest/collections/{uuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Collection update(
      @PathVariable UUID uuid, @RequestBody Collection collection, BindingResult errors)
      throws ServiceException, ValidationException {
    assert Objects.equals(uuid, collection.getUuid());
    collectionService.update(collection);
    return collection;
  }
}
