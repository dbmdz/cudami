package de.digitalcollections.cudami.server.controller.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
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
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiPathParam;
import org.jsondoc.core.annotation.ApiQueryParam;
import org.jsondoc.core.annotation.ApiResponseObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
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
@Api(description = "The collection controller", name = "Collection controller")
public class CollectionController {

  private static final Logger LOGGER = LoggerFactory.getLogger(CollectionController.class);

  private final CollectionService collectionService;

  @Autowired private LocaleService localeService;

  @Autowired
  public CollectionController(CollectionService collectionService) {
    this.collectionService = collectionService;
  }

  @ApiMethod(description = "Add an existing digital object to an existing collection")
  @PostMapping(
      value = {
        "/latest/collections/{uuid}/digitalobjects/{digitalObjectUuid}",
        "/v3/collections/{uuid}/digitalobjects/{digitalObjectUuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public ResponseEntity addDigitalObject(
      @ApiPathParam(description = "UUID of the collection") @PathVariable("uuid")
          UUID collectionUuid,
      @ApiPathParam(description = "UUID of the digital object") @PathVariable("digitalObjectUuid")
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

  @ApiMethod(description = "Add existing digital objects to an existing collection")
  @PostMapping(
      value = {
        "/latest/collections/{uuid}/digitalobjects",
        "/v3/collections/{uuid}/digitalobjects"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public ResponseEntity addDigitalObjects(
      @ApiPathParam(description = "UUID of the collection") @PathVariable("uuid")
          UUID collectionUuid,
      @ApiPathParam(description = "List of the digital objects") @RequestBody
          List<DigitalObject> digitalObjects) {
    Collection collection = new Collection();
    collection.setUuid(collectionUuid);

    boolean successful = collectionService.addDigitalObjects(collection, digitalObjects);

    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }

  @ApiMethod(description = "Add an existing collection to an existing collection")
  @PostMapping(
      value = {
        "/latest/collections/{uuid}/subcollections/{subcollectionUuid}",
        "/v3/collections/{uuid}/subcollections/{subcollectionUuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public ResponseEntity addSubcollection(
      @ApiPathParam(description = "UUID of the collection") @PathVariable("uuid") UUID uuid,
      @ApiPathParam(description = "UUID of the subcollection") @PathVariable("subcollectionUuid")
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

  @ApiMethod(description = "Add existing collections to an existing collection")
  @PostMapping(
      value = {
        "/latest/collections/{uuid}/subcollections",
        "/v3/collections/{uuid}/subcollections"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public ResponseEntity addSubcollections(
      @ApiPathParam(description = "UUID of the collection") @PathVariable("uuid") UUID uuid,
      @ApiPathParam(description = "List of the subcollections") @RequestBody
          List<Collection> subcollections) {
    Collection collection = new Collection();
    collection.setUuid(uuid);

    boolean successful = collectionService.addChildren(collection, subcollections);

    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }

  @ApiMethod(description = "Get count of collections")
  @GetMapping(
      value = {"/latest/collections/count", "/v2/collections/count"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public long count() {
    return collectionService.count();
  }

  @ApiMethod(description = "Get all collections")
  @GetMapping(
      value = {"/latest/collections", "/v2/collections"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public PageResponse<Collection> findAll(
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

  @ApiMethod(description = "Get all top collections")
  @GetMapping(
      value = {"/latest/collections/top", "/v2/collections/top"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public PageResponse<Collection> findAllTop(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "searchTerm", required = false) String searchTerm) {
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }

    if (StringUtils.hasText(searchTerm)) {
      SearchPageRequest searchPageRequest =
          new SearchPageRequest(
              searchTerm,
              pageRequest.getPageNumber(),
              pageRequest.getPageSize(),
              pageRequest.getSorting());
      return collectionService.findRootNodes(searchPageRequest);
    } else {
      return collectionService.getRootNodes(pageRequest);
    }
  }

  @ApiMethod(description = "Get collection by namespace and id")
  @GetMapping(
      value = {
        "/latest/collections/identifier/{namespace}:{id}",
        "/v2/collections/identifier/{namespace}:{id}"
      },
      produces = "application/json")
  @ApiResponseObject
  public Collection findByIdentifier(@PathVariable String namespace, @PathVariable String id)
      throws IdentifiableServiceException {
    return collectionService.getByIdentifier(namespace, id);
  }

  @ApiMethod(description = "Get collection by refId")
  @GetMapping(value = {"/latest/collections/{refId:[0-9]+}"})
  @ApiResponseObject
  public ResponseEntity<Collection> findByRefId(
      @ApiPathParam(description = "refId of the collection, e.g. <tt>42</tt>") @PathVariable
          long refId)
      throws IdentifiableServiceException {
    Collection collection = collectionService.getByRefId(refId);
    return findByUuid(collection.getUuid(), null, null);
  }

  // Test-URL: http://localhost:9000/latest/collections/599a120c-2dd5-11e8-b467-0ed5f89f718b
  @ApiMethod(
      description =
          "Get an collection as JSON or XML, depending on extension or <tt>format</tt> request parameter or accept header")
  @GetMapping(
      value = {
        "/latest/collections/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}",
        "/v2/collections/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}"
      },
      produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ApiResponseObject
  public ResponseEntity<Collection> findByUuid(
      @ApiPathParam(
              description =
                  "UUID of the collection, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @ApiQueryParam(
              name = "pLocale",
              description =
                  "Desired locale, e.g. <tt>de_DE</tt>. If unset, contents in all languages will be returned")
          @RequestParam(name = "pLocale", required = false)
          Locale pLocale,
      @ApiQueryParam(
              name = "active",
              description = "If set, object will only be returned if active")
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
        collection = collectionService.get(uuid);
      } else {
        collection = collectionService.get(uuid, pLocale);
      }
    }
    return new ResponseEntity<>(collection, HttpStatus.OK);
  }

  @ApiMethod(
      description =
          "Find limited amount of (active or all) collections containing searchTerm in label or description")
  @GetMapping(
      value = {"/latest/collections/search", "/v3/collections/search"},
      produces = "application/json")
  @ApiResponseObject
  public SearchPageResponse<Collection> findCollections(
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

  @ApiMethod(description = "Get the breadcrumb for a collection")
  @GetMapping(
      value = {"/latest/collections/{uuid}/breadcrumb", "/v3/collections/{uuid}/breadcrumb"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public ResponseEntity<BreadcrumbNavigation> getBreadcrumb(
      @ApiPathParam(
              description =
                  "UUID of the collection, e.g. <tt>6119d8e9-9c92-4091-8dcb-bc4053385406</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @ApiQueryParam(
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

  @ApiMethod(description = "Get paged digital objects of a collection")
  @GetMapping(
      value = {
        "/latest/collections/{uuid}/digitalobjects",
        "/v3/collections/{uuid}/digitalobjects"
      },
      produces = "application/json")
  @ApiResponseObject
  public PageResponse<DigitalObject> getDigitalObjects(
      @ApiPathParam(description = "UUID of the collection") @PathVariable("uuid")
          UUID collectionUuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "searchTerm", required = false) String searchTerm) {
    SearchPageRequest searchPageRequest = new SearchPageRequest(searchTerm, pageNumber, pageSize, new Sorting());

    Collection collection = new Collection();
    collection.setUuid(collectionUuid);
    return collectionService.getDigitalObjects(collection, searchPageRequest);
  }

  @ApiMethod(description = "Get the first created parent of a collection")
  @GetMapping(
      value = {"/latest/collections/{uuid}/parent", "/v3/collections/{uuid}/parent"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public Collection getParent(@PathVariable UUID uuid) {
    return collectionService.getParent(uuid);
  }

  @ApiMethod(description = "Get parent collections")
  @GetMapping(
      value = {"/latest/collections/{uuid}/parents", "/v3/collections/{uuid}/parents"},
      produces = "application/json")
  @ApiResponseObject
  public List<Collection> getParents(
      @ApiPathParam(description = "UUID of the collection") @PathVariable("uuid")
          UUID collectionUuid) {
    return collectionService.getParents(collectionUuid);
  }

  @ApiMethod(
      description = "Get all related - by the given predicate - corporate bodies of a collection")
  @GetMapping(
      value = {
        "/latest/collections/{uuid}/related/corporatebodies",
        "/v3/collections/{uuid}/related/corporatebodies"
      },
      produces = "application/json")
  @ApiResponseObject
  public List<CorporateBody> getRelatedCorporateBodies(
      @ApiPathParam(description = "UUID of the collection") @PathVariable("uuid") UUID uuid,
      @RequestParam(name = "predicate", required = true) FilterCriterion<String> predicate) {
    Filtering filtering = Filtering.defaultBuilder().add("predicate", predicate).build();
    return collectionService.getRelatedCorporateBodies(uuid, filtering);
  }

  @ApiMethod(description = "Get (active or all) paged subcollections of a collection")
  @GetMapping(
      value = {
        "/latest/collections/{uuid}/subcollections",
        "/v3/collections/{uuid}/subcollections"
      },
      produces = "application/json")
  @ApiResponseObject
  public PageResponse<Collection> getSubcollections(
      @ApiPathParam(description = "UUID of the collection") @PathVariable("uuid")
          UUID collectionUuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "active", required = false) String active) {
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize);
    if (active != null) {
      return collectionService.getActiveChildren(collectionUuid, pageRequest);
    }
    return collectionService.getChildren(collectionUuid, pageRequest);
  }

  @ApiMethod(description = "Get languages of all top collections")
  @GetMapping(
      value = {"/latest/collections/top/languages", "/v2/collections/top/languages"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public List<Locale> getTopCollectionsLanguages() {
    return collectionService.getRootNodesLanguages();
  }

  @ApiMethod(description = "Remove an existing digital object from an existing collection")
  @DeleteMapping(
      value = {
        "/latest/collections/{uuid}/digitalobjects/{digitalObjectUuid}",
        "/v3/collections/{uuid}/digitalobjects/{digitalObjectUuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public ResponseEntity removeDigitalObject(
      @ApiPathParam(description = "UUID of the collection") @PathVariable("uuid")
          UUID collectionUuid,
      @ApiPathParam(description = "UUID of the digital object") @PathVariable("digitalObjectUuid")
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

  @ApiMethod(description = "Remove an existing collection from an existing collection")
  @DeleteMapping(
      value = {
        "/latest/collections/{uuid}/subcollections/{subcollectionUuid}",
        "/v3/collections/{uuid}/subcollections/{subcollectionUuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public ResponseEntity removeSubcollection(
      @ApiPathParam(description = "UUID of the collection") @PathVariable("uuid") UUID uuid,
      @ApiPathParam(description = "UUID of the subcollection") @PathVariable("subcollectionUuid")
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

  @ApiMethod(description = "Save a newly created collection")
  @PostMapping(
      value = {"/latest/collections", "/v2/collections"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public Collection save(@RequestBody Collection collection, BindingResult errors)
      throws IdentifiableServiceException {
    return collectionService.save(collection);
  }

  @ApiMethod(description = "Save existing digital objects into an existing collection")
  @PutMapping(
      value = {
        "/latest/collections/{uuid}/digitalobjects",
        "/v3/collections/{uuid}/digitalobjects"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public ResponseEntity saveDigitalObjects(
      @ApiPathParam(description = "UUID of the collection") @PathVariable("uuid")
          UUID collectionUuid,
      @ApiPathParam(description = "List of the digital objects") @RequestBody
          List<DigitalObject> digitalObjects) {
    Collection collection = new Collection();
    collection.setUuid(collectionUuid);

    boolean successful = collectionService.saveDigitalObjects(collection, digitalObjects);

    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }

  @ApiMethod(description = "Save a newly created collection with parent collection")
  @PostMapping(
      value = {
        "/latest/collections/{parentUuid}/collection",
        "/v2/collections/{parentUuid}/collection"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public Collection saveWithParentCollection(
      @ApiPathParam(name = "parentUuid", description = "The uuid of the parent collection")
          @PathVariable
          UUID parentUuid,
      @RequestBody Collection collection)
      throws IdentifiableServiceException {
    return collectionService.saveWithParent(collection, parentUuid);
  }

  @ApiMethod(description = "Update an collection")
  @PutMapping(
      value = {"/latest/collections/{uuid}", "/v2/collections/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public Collection update(
      @PathVariable UUID uuid, @RequestBody Collection collection, BindingResult errors)
      throws IdentifiableServiceException {
    assert Objects.equals(uuid, collection.getUuid());
    return collectionService.update(collection);
  }
}
