package de.digitalcollections.cudami.admin.controller.identifiable.entity;

import de.digitalcollections.cudami.admin.controller.AbstractPagingAndSortingController;
import de.digitalcollections.cudami.admin.controller.ParameterHelper;
import de.digitalcollections.cudami.admin.model.bootstraptable.BTResponse;
import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.CudamiLocalesClient;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiCollectionsClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/** Controller for all public "Collections" endpoints (API). */
@RestController
public class CollectionsAPIController extends AbstractPagingAndSortingController<Collection> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CollectionsAPIController.class);

  private final LanguageSortingHelper languageSortingHelper;
  private final CudamiLocalesClient localeService;
  private final CudamiCollectionsClient service;

  public CollectionsAPIController(
      LanguageSortingHelper languageSortingHelper, CudamiClient client) {
    this.languageSortingHelper = languageSortingHelper;
    this.localeService = client.forLocales();
    this.service = client.forCollections();
  }

  @PostMapping("/api/collections/{uuid:" + ParameterHelper.UUID_PATTERN + "}/digitalobjects")
  public ResponseEntity addDigitalObjects(
      @PathVariable UUID uuid, @RequestBody List<DigitalObject> digitalObjects)
      throws TechnicalException {
    boolean successful = service.addDigitalObjects(uuid, digitalObjects);
    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }

  @PostMapping("/api/collections/{collectionUuid}/collections/{subcollectionUuid}")
  public ResponseEntity addSubcollection(
      @PathVariable UUID collectionUuid, @PathVariable UUID subcollectionUuid)
      throws TechnicalException {
    boolean successful = service.addSubcollection(collectionUuid, subcollectionUuid);
    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }

  @PostMapping("/api/collections/{collectionUuid}/collections")
  public ResponseEntity addSubcollections(
      @PathVariable UUID collectionUuid, @RequestBody List<Collection> subcollections)
      throws TechnicalException {
    boolean successful = service.addSubcollections(collectionUuid, subcollections);
    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }

  @GetMapping("/api/collections/new")
  @ResponseBody
  public Collection create() throws TechnicalException {
    return service.create();
  }

  @GetMapping("/api/collections/search")
  @ResponseBody
  public PageResponse<Collection> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "searchTerm", required = false) String searchTerm,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy)
      throws TechnicalException {
    PageRequest pageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    return service.find(pageRequest);
  }

  @GetMapping("/api/collections/{uuid:" + ParameterHelper.UUID_PATTERN + "}/digitalobjects")
  @ResponseBody
  public BTResponse<DigitalObject> findDigitalObjects(
      @PathVariable UUID uuid,
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(name = "limit", required = false, defaultValue = "1") int limit,
      @RequestParam(name = "search", required = false) String searchTerm,
      @RequestParam(name = "sort", required = false, defaultValue = "label") String sort,
      @RequestParam(name = "order", required = false, defaultValue = "asc") String order,
      @RequestParam(name = "dataLanguage", required = false) String dataLanguage)
      throws TechnicalException {
    PageRequest pageRequest =
        createPageRequest(sort, order, dataLanguage, localeService, offset, limit, searchTerm);
    PageResponse<DigitalObject> pageResponse = service.findDigitalObjects(uuid, pageRequest);
    return new BTResponse<>(pageResponse);
  }

  @GetMapping("/api/collections/{uuid:" + ParameterHelper.UUID_PATTERN + "}/collections")
  @ResponseBody
  public BTResponse<Collection> findSubcollections(
      @PathVariable UUID uuid,
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(name = "limit", required = false, defaultValue = "1") int limit,
      @RequestParam(name = "search", required = false) String searchTerm,
      @RequestParam(name = "sort", required = false, defaultValue = "label") String sort,
      @RequestParam(name = "order", required = false, defaultValue = "asc") String order,
      @RequestParam(name = "dataLanguage", required = false) String dataLanguage)
      throws TechnicalException {
    PageRequest pageRequest =
        createPageRequest(sort, order, dataLanguage, localeService, offset, limit, searchTerm);
    PageResponse<Collection> pageResponse = service.findSubcollections(uuid, pageRequest);
    return new BTResponse<>(pageResponse);
  }

  @SuppressFBWarnings
  @GetMapping("/api/collections")
  @ResponseBody
  public BTResponse<Collection> findTop(
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(name = "limit", required = false, defaultValue = "1") int limit,
      @RequestParam(name = "search", required = false) String searchTerm,
      @RequestParam(name = "sort", required = false, defaultValue = "label") String sort,
      @RequestParam(name = "order", required = false, defaultValue = "asc") String order,
      @RequestParam(name = "dataLanguage", required = false) String dataLanguage)
      throws TechnicalException {
    PageResponse<Collection> pageResponse =
        service.findTopCollections(
            createPageRequest(sort, order, dataLanguage, localeService, offset, limit, searchTerm));
    return new BTResponse<>(pageResponse);
  }

  @GetMapping("/api/collections/identifier/{namespace}:{id}")
  @ResponseBody
  public Collection getByIdentifier(@PathVariable String namespace, @PathVariable String id)
      throws TechnicalException {
    return service.getByIdentifier(namespace, id);
  }

  @GetMapping("/api/collections/{refId:[0-9]+}")
  @ResponseBody
  public Collection getByRefId(@PathVariable long refId) throws TechnicalException {
    return service.getByRefId(refId);
  }

  @GetMapping("/api/collections/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  @ResponseBody
  public Collection getByUuid(@PathVariable UUID uuid) throws TechnicalException {
    return service.getByUuid(uuid);
  }

  @DeleteMapping("/api/collections/{collectionUuid}/digitalobjects/{digitalobjectUuid}")
  @ResponseBody
  public ResponseEntity removeDigitalObject(
      @PathVariable UUID collectionUuid, @PathVariable UUID digitalobjectUuid)
      throws TechnicalException {
    boolean successful = service.removeDigitalObject(collectionUuid, digitalobjectUuid);
    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }

  @DeleteMapping("/api/collections/{collectionUuid}/collections/{subcollectionUuid}")
  public ResponseEntity removeSubcollection(
      @PathVariable UUID collectionUuid, @PathVariable UUID subcollectionUuid)
      throws TechnicalException {
    boolean successful = service.removeSubcollection(collectionUuid, subcollectionUuid);
    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }

  @PostMapping("/api/collections")
  public ResponseEntity save(
      @RequestBody Collection collection,
      @RequestParam(name = "parentType", required = false) String parentType,
      @RequestParam(name = "parentUuid", required = false) UUID parentUuid) {
    try {
      Collection collectionDb = null;
      if ("collection".equals(parentType)) {
        collectionDb = service.saveWithParentCollection(collection, parentUuid);
      } else {
        collectionDb = service.save(collection);
      }
      return ResponseEntity.status(HttpStatus.CREATED).body(collectionDb);
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save collection: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @PutMapping("/api/collections/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  public ResponseEntity update(@PathVariable UUID uuid, @RequestBody Collection collection) {
    try {
      Collection collectionDb = service.update(uuid, collection);
      return ResponseEntity.ok(collectionDb);
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save collection with uuid={}", uuid, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }
}
