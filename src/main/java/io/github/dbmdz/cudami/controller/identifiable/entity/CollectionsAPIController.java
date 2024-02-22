package io.github.dbmdz.cudami.controller.identifiable.entity;

import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.CudamiIdentifiablesClient;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiCollectionsClient;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiEntitiesClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.dbmdz.cudami.business.i18n.LanguageService;
import io.github.dbmdz.cudami.controller.ParameterHelper;
import io.github.dbmdz.cudami.model.bootstraptable.BTRequest;
import io.github.dbmdz.cudami.model.bootstraptable.BTResponse;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.endpoint.InvalidEndpointRequestException;
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
public class CollectionsAPIController
    extends AbstractEntitiesController<Collection, CudamiCollectionsClient> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CollectionsAPIController.class);

  public CollectionsAPIController(CudamiClient client, LanguageService languageService) {
    super(client.forCollections(), client, languageService);
  }

  @PostMapping("/api/collections/{uuid:" + ParameterHelper.UUID_PATTERN + "}/digitalobjects")
  public ResponseEntity addDigitalObjects(
      @PathVariable UUID uuid, @RequestBody List<DigitalObject> digitalObjects)
      throws TechnicalException {
    boolean successful =
        ((CudamiCollectionsClient) service).addDigitalObjects(uuid, digitalObjects);
    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }

  /*
   * Used in templates/collections/view.html
   */
  @PostMapping("/api/collections/{collectionUuid}/collections/{subcollectionUuid}")
  public ResponseEntity addSubcollection(
      @PathVariable UUID collectionUuid, @PathVariable UUID subcollectionUuid)
      throws TechnicalException {
    boolean successful =
        ((CudamiCollectionsClient) service).addSubcollection(collectionUuid, subcollectionUuid);
    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }

  @PostMapping("/api/collections/{collectionUuid}/collections")
  public ResponseEntity addSubcollections(
      @PathVariable UUID collectionUuid, @RequestBody List<Collection> subcollections)
      throws TechnicalException {
    boolean successful =
        ((CudamiCollectionsClient) service).addSubcollections(collectionUuid, subcollections);
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

  /*
   * Used in templates/collections/view.html as param for templates/fragments/modals/select-entities.html
   */
  @GetMapping("/api/collections/search")
  @ResponseBody
  public PageResponse<Collection> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "searchField", required = false) String searchField,
      @RequestParam(name = "searchTerm", required = false) String searchTerm,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy)
      throws TechnicalException {
    // TODO ?: add datalanguage as request param to allow search / autocompletion in selected data
    // language
    String dataLanguage = null;
    PageRequest pageRequest =
        createPageRequest(
            Collection.class, pageNumber, pageSize, sortBy, searchField, searchTerm, dataLanguage);
    PageResponse<Collection> pageResponse = search(searchField, searchTerm, pageRequest);
    if (pageResponse == null) {
      throw new InvalidEndpointRequestException("invalid request param", searchField);
    }
    return pageResponse;
  }

  /*
   * Used in templates/collections/view.html
   */
  @GetMapping("/api/collections/{uuid:" + ParameterHelper.UUID_PATTERN + "}/digitalobjects")
  @ResponseBody
  public BTResponse<DigitalObject> findDigitalObjects(
      @PathVariable UUID uuid,
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(name = "limit", required = false, defaultValue = "10") int limit,
      @RequestParam(name = "search", required = false) String searchTerm,
      @RequestParam(name = "sort", required = false, defaultValue = "label") String sortProperty,
      @RequestParam(name = "order", required = false, defaultValue = "asc") String sortOrder,
      @RequestParam(name = "dataLanguage", required = false) String dataLanguage)
      throws TechnicalException {
    BTRequest btRequest =
        createBTRequest(
            DigitalObject.class,
            offset,
            limit,
            sortProperty,
            sortOrder,
            "label",
            searchTerm,
            dataLanguage);
    PageResponse<DigitalObject> pageResponse =
        ((CudamiCollectionsClient) service).findDigitalObjects(uuid, btRequest);
    return new BTResponse<>(pageResponse);
  }

  /*
   * Used in templates/collections/view.html
   */
  @GetMapping("/api/collections/{uuid:" + ParameterHelper.UUID_PATTERN + "}/collections")
  @ResponseBody
  public BTResponse<Collection> findSubcollections(
      @PathVariable UUID uuid,
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(name = "limit", required = false, defaultValue = "10") int limit,
      @RequestParam(name = "search", required = false) String searchTerm,
      @RequestParam(name = "sort", required = false, defaultValue = "label") String sortProperty,
      @RequestParam(name = "order", required = false, defaultValue = "asc") String sortOrder,
      @RequestParam(name = "dataLanguage", required = false) String dataLanguage)
      throws TechnicalException {
    BTRequest btRequest =
        createBTRequest(
            Collection.class,
            offset,
            limit,
            sortProperty,
            sortOrder,
            "label",
            searchTerm,
            dataLanguage);
    PageResponse<Collection> pageResponse =
        ((CudamiCollectionsClient) service).findSubcollections(uuid, btRequest);
    return new BTResponse<>(pageResponse);
  }

  /*
   * Used in templates/collections/list.html
   */
  @SuppressFBWarnings
  @GetMapping("/api/collections")
  @ResponseBody
  public BTResponse<Collection> findTop(
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(name = "limit", required = false, defaultValue = "10") int limit,
      @RequestParam(name = "search", required = false) String searchTerm,
      @RequestParam(name = "sort", required = false, defaultValue = "label") String sortProperty,
      @RequestParam(name = "order", required = false, defaultValue = "asc") String sortOrder,
      @RequestParam(name = "dataLanguage", required = false) String dataLanguage)
      throws TechnicalException {
    BTRequest btRequest =
        createBTRequest(
            Collection.class,
            offset,
            limit,
            sortProperty,
            sortOrder,
            "label",
            searchTerm,
            dataLanguage);
    PageResponse<Collection> pageResponse =
        ((CudamiCollectionsClient) service).findTopCollections(btRequest);
    return new BTResponse<>(pageResponse);
  }

  @GetMapping("/api/collections/identifier/{namespace}:{id}")
  @ResponseBody
  public Collection getByIdentifier(@PathVariable String namespace, @PathVariable String id)
      throws TechnicalException {
    return ((CudamiIdentifiablesClient<Collection>) service).getByIdentifier(namespace, id);
  }

  @GetMapping("/api/collections/{refId:[0-9]+}")
  @ResponseBody
  public Collection getByRefId(@PathVariable long refId) throws TechnicalException {
    return ((CudamiEntitiesClient<Collection>) service).getByRefId(refId);
  }

  @GetMapping("/api/collections/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  @ResponseBody
  public Collection getByUuid(@PathVariable UUID uuid) throws TechnicalException {
    return service.getByUuid(uuid);
  }

  /*
   * Used in templates/collections/view.html
   */
  @DeleteMapping("/api/collections/{collectionUuid}/digitalobjects/{digitalobjectUuid}")
  @ResponseBody
  public ResponseEntity removeDigitalObject(
      @PathVariable UUID collectionUuid, @PathVariable UUID digitalobjectUuid)
      throws TechnicalException {
    boolean successful =
        ((CudamiCollectionsClient) service).removeDigitalObject(collectionUuid, digitalobjectUuid);
    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }

  /*
   * Used in templates/collections/view.html
   */
  @DeleteMapping("/api/collections/{collectionUuid}/collections/{subcollectionUuid}")
  public ResponseEntity removeSubcollection(
      @PathVariable UUID collectionUuid, @PathVariable UUID subcollectionUuid)
      throws TechnicalException {
    boolean successful =
        ((CudamiCollectionsClient) service).removeSubcollection(collectionUuid, subcollectionUuid);
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
        collectionDb =
            ((CudamiCollectionsClient) service).saveWithParentCollection(collection, parentUuid);
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
