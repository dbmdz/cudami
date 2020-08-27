package de.digitalcollections.cudami.server.controller.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.CollectionService;
import de.digitalcollections.model.api.identifiable.entity.Collection;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.SearchPageRequest;
import de.digitalcollections.model.api.paging.SearchPageResponse;
import de.digitalcollections.model.api.paging.Sorting;
import de.digitalcollections.model.api.paging.enums.Direction;
import de.digitalcollections.model.api.paging.enums.NullHandling;
import de.digitalcollections.model.api.view.BreadcrumbNavigation;
import de.digitalcollections.model.impl.identifiable.entity.CollectionImpl;
import de.digitalcollections.model.impl.identifiable.entity.DigitalObjectImpl;
import de.digitalcollections.model.impl.paging.OrderImpl;
import de.digitalcollections.model.impl.paging.PageRequestImpl;
import de.digitalcollections.model.impl.paging.SearchPageRequestImpl;
import de.digitalcollections.model.impl.paging.SortingImpl;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiPathParam;
import org.jsondoc.core.annotation.ApiQueryParam;
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
@Api(description = "The collection controller", name = "Collection controller")
public class CollectionController {

  private final CollectionService collectionService;

  @Autowired private LocaleService localeService;

  @Autowired
  public CollectionController(CollectionService collectionService) {
    this.collectionService = collectionService;
  }

  @ApiMethod(description = "Get all collections")
  @GetMapping(
      value = {"/latest/collections", "/v2/collections"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public PageResponse<Collection> findAll(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortField", required = false, defaultValue = "lastModified")
          String sortField,
      @RequestParam(name = "sortDirection", required = false, defaultValue = "DESC")
          Direction sortDirection,
      @RequestParam(name = "nullHandling", required = false, defaultValue = "NATIVE")
          NullHandling nullHandling) {
    OrderImpl order = new OrderImpl(sortDirection, sortField, nullHandling);
    Sorting sorting = new SortingImpl(order);
    PageRequest pageRequest = new PageRequestImpl(pageNumber, pageSize, sorting);
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
      @RequestParam(name = "sortField", required = false, defaultValue = "uuid") String sortField,
      @RequestParam(name = "sortDirection", required = false, defaultValue = "ASC")
          Direction sortDirection,
      @RequestParam(name = "nullHandling", required = false, defaultValue = "NATIVE")
          NullHandling nullHandling) {
    OrderImpl order = new OrderImpl(sortDirection, sortField, nullHandling);
    Sorting sorting = new SortingImpl(order);
    PageRequest pageRequest = new PageRequestImpl(pageNumber, pageSize, sorting);
    return collectionService.getTopCollections(pageRequest);
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

  // Test-URL: http://localhost:9000/latest/collections/599a120c-2dd5-11e8-b467-0ed5f89f718b
  @ApiMethod(
      description =
          "Get an collection as JSON or XML, depending on extension or <tt>format</tt> request parameter or accept header")
  @GetMapping(
      value = {"/latest/collections/{uuid}", "/v2/collections/{uuid}"},
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
          Locale pLocale)
      throws IdentifiableServiceException {

    Collection collection;
    if (pLocale == null) {
      collection = collectionService.get(uuid);
    } else {
      collection = collectionService.get(uuid, pLocale);
    }
    return new ResponseEntity<>(collection, HttpStatus.OK);
  }

  @ApiMethod(
      description =
          "Find limited amount of collections containing searchTerm in label or description")
  @GetMapping(
      value = {"/latest/collections/search", "/v3/collections/search"},
      produces = "application/json")
  @ApiResponseObject
  public SearchPageResponse<Collection> findCollections(
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
    return collectionService.find(pageRequest);
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
    return collectionService.saveWithParentCollection(collection, parentUuid);
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

  @ApiMethod(description = "Get count of collections")
  @GetMapping(
      value = {"/latest/collections/count", "/v2/collections/count"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public long count() {
    return collectionService.count();
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
    Collection collection = new CollectionImpl();
    collection.setUuid(collectionUuid);

    DigitalObject digitalObject = new DigitalObjectImpl();
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
    Collection collection = new CollectionImpl();
    collection.setUuid(collectionUuid);

    boolean successful = collectionService.addDigitalObjects(collection, digitalObjects);

    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
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
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize) {
    PageRequest pageRequest = new PageRequestImpl(pageNumber, pageSize, new SortingImpl());

    Collection collection = new CollectionImpl();
    collection.setUuid(collectionUuid);
    return collectionService.getDigitalObjects(collection, pageRequest);
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
    Collection collection = new CollectionImpl();
    collection.setUuid(collectionUuid);

    DigitalObject digitalObject = new DigitalObjectImpl();
    digitalObject.setUuid(digitalObjectUuid);

    boolean successful = collectionService.removeDigitalObject(collection, digitalObject);

    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
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
    Collection collection = new CollectionImpl();
    collection.setUuid(collectionUuid);

    boolean successful = collectionService.saveDigitalObjects(collection, digitalObjects);

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
    Collection collection = new CollectionImpl();
    collection.setUuid(uuid);

    Collection subcollection = new CollectionImpl();
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
    Collection collection = new CollectionImpl();
    collection.setUuid(uuid);

    boolean successful = collectionService.addChildren(collection, subcollections);

    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }

  @ApiMethod(description = "Get paged subcollections of a collection")
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
      @RequestParam(name = "sortField", required = false, defaultValue = "lastModified")
          String sortField,
      @RequestParam(name = "sortDirection", required = false, defaultValue = "DESC")
          Direction sortDirection,
      @RequestParam(name = "nullHandling", required = false, defaultValue = "NATIVE")
          NullHandling nullHandling) {
    OrderImpl order = new OrderImpl(sortDirection, sortField, nullHandling);
    Sorting sorting = new SortingImpl(order);
    PageRequest pageRequest = new PageRequestImpl(pageNumber, pageSize, sorting);
    return collectionService.getChildren(collectionUuid, pageRequest);
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
    Collection collection = new CollectionImpl();
    collection.setUuid(uuid);

    Collection subcollection = new CollectionImpl();
    subcollection.setUuid(subcollectionUuid);

    boolean successful = collectionService.removeChild(collection, subcollection);

    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }
}
