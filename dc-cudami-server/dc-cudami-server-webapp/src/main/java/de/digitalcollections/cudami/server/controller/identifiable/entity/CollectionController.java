package de.digitalcollections.cudami.server.controller.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.CollectionService;
import de.digitalcollections.model.filter.FilterCriterion;
import de.digitalcollections.model.filter.Filtering;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import de.digitalcollections.model.paging.Sorting;
import de.digitalcollections.model.view.BreadcrumbNavigation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
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
public class CollectionController {

  private static final Logger LOGGER = LoggerFactory.getLogger(CollectionController.class);

  private final CollectionService collectionService;
  private final LocaleService localeService;

  public CollectionController(CollectionService collectionService, LocaleService localeService) {
    this.collectionService = collectionService;
    this.localeService = localeService;
  }

  @Operation(summary = "Add an existing digital object to an existing collection")
  @PostMapping(
      value = {
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

    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Add existing digital objects to an existing collection")
  @PostMapping(
      value = {
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

    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Add an existing collection to an existing collection")
  @PostMapping(
      value = {
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

    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Add existing collections to an existing collection")
  @PostMapping(
      value = {
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

    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Get count of collections")
  @GetMapping(
      value = {"/v5/collections/count", "/v2/collections/count", "/latest/collections/count"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public long count() {
    return collectionService.count();
  }

  @Operation(summary = "Get all collections")
  @GetMapping(
      value = {"/v5/collections"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Collection> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "active", required = false) String active) {
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    if (active != null) {
      return collectionService.findActive(pageRequest);
    }
    return collectionService.find(pageRequest);
  }

  @Operation(summary = "Get all top collections")
  @GetMapping(
      value = {"/v5/collections/top", "/v2/collections/top", "/latest/collections/top"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Collection> findTopCollections(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "searchTerm", required = false) String searchTerm) {
    SearchPageRequest searchPageRequest = new SearchPageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      searchPageRequest.setSorting(sorting);
    }
    return collectionService.findRootNodes(searchPageRequest);
  }

  @Operation(
      summary =
          "Find limited amount of (active or all) collections containing searchTerm in label or description")
  @GetMapping(
      value = {"/v5/collections/search"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public SearchPageResponse<Collection> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "searchTerm", required = false) String searchTerm,
      @RequestParam(name = "active", required = false) String active) {
    SearchPageRequest pageRequest = new SearchPageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    if (active != null) {
      return collectionService.findActive(pageRequest);
    }
    return collectionService.find(pageRequest);
  }

  @Operation(summary = "Get the breadcrumb for a collection")
  @GetMapping(
      value = {
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

  @Operation(summary = "Get a collection by namespace and id")
  @GetMapping(
      value = {
        "/v5/collections/identifier/{namespace}:{id}",
        "/v5/collections/identifier/{namespace}:{id}.json",
        "/v2/collections/identifier/{namespace}:{id}",
        "/v2/collections/identifier/{namespace}:{id}.json",
        "/latest/collections/identifier/{namespace}:{id}",
        "/latest/collections/identifier/{namespace}:{id}.json"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Collection getByIdentifier(@PathVariable String namespace, @PathVariable String id)
      throws IdentifiableServiceException {
    return collectionService.getByIdentifier(namespace, id);
  }

  @Operation(summary = "Get a collection by refId")
  @GetMapping(
      value = {"/v5/collections/{refId:[0-9]+}", "/latest/collections/{refId:[0-9]+}"},
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
        "/v5/collections/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}",
        "/v2/collections/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}",
        "/latest/collections/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}"
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
    return new ResponseEntity<>(collection, HttpStatus.OK);
  }

  @Operation(summary = "Get paged digital objects of a collection")
  @GetMapping(
      value = {"/v5/collections/{uuid}/digitalobjects"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public SearchPageResponse<DigitalObject> findDigitalObjects(
      @Parameter(example = "", description = "UUID of the collection") @PathVariable("uuid")
          UUID collectionUuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "searchTerm", required = false) String searchTerm) {
    SearchPageRequest searchPageRequest = new SearchPageRequest(searchTerm, pageNumber, pageSize);

    Collection collection = new Collection();
    collection.setUuid(collectionUuid);
    return collectionService.findDigitalObjects(collection, searchPageRequest);
  }

  @Operation(summary = "Get the first created parent of a collection")
  @GetMapping(
      value = {
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

  @Operation(
      summary = "Get all related - by the given predicate - corporate bodies of a collection")
  @GetMapping(
      value = {
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
      value = {"/v5/collections/{uuid}/subcollections"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Collection> findSubcollections(
      @Parameter(example = "", description = "UUID of the collection") @PathVariable("uuid")
          UUID collectionUuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "active", required = false) String active,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "searchTerm", required = false) String searchTerm) {
    SearchPageRequest searchPageRequest = new SearchPageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      searchPageRequest.setSorting(sorting);
    }
    if (active != null) {
      return collectionService.findActiveChildren(collectionUuid, searchPageRequest);
    }
    return collectionService.findChildren(collectionUuid, searchPageRequest);
  }

  @Operation(summary = "Get languages of all top collections")
  @GetMapping(
      value = {
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

    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Remove an existing collection from an existing collection")
  @DeleteMapping(
      value = {
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

    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Save a newly created collection")
  @PostMapping(
      value = {"/v5/collections", "/v2/collections", "/latest/collections"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Collection save(@RequestBody Collection collection, BindingResult errors)
      throws IdentifiableServiceException, ValidationException {
    return collectionService.save(collection);
  }

  @Operation(summary = "Save existing digital objects into an existing collection")
  @PutMapping(
      value = {
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

    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Save a newly created collection with parent collection")
  @PostMapping(
      value = {
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
      throws IdentifiableServiceException, ValidationException {
    return collectionService.saveWithParent(collection, parentUuid);
  }

  @Operation(summary = "Update a collection")
  @PutMapping(
      value = {"/v5/collections/{uuid}", "/v2/collections/{uuid}", "/latest/collections/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Collection update(
      @PathVariable UUID uuid, @RequestBody Collection collection, BindingResult errors)
      throws IdentifiableServiceException, ValidationException {
    assert Objects.equals(uuid, collection.getUuid());
    return collectionService.update(collection);
  }
}
