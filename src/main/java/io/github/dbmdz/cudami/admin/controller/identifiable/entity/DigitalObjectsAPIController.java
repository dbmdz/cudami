package io.github.dbmdz.cudami.admin.controller.identifiable.entity;

import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.CudamiIdentifiablesClient;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiDigitalObjectsClient;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiEntitiesClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.dbmdz.cudami.admin.business.i18n.LanguageService;
import io.github.dbmdz.cudami.admin.controller.ParameterHelper;
import io.github.dbmdz.cudami.admin.model.bootstraptable.BTRequest;
import io.github.dbmdz.cudami.admin.model.bootstraptable.BTResponse;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.endpoint.InvalidEndpointRequestException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/** Controller for all public "DigitalObjects" endpoints (API). */
@RestController
public class DigitalObjectsAPIController
    extends AbstractEntitiesController<DigitalObject, CudamiDigitalObjectsClient> {

  private static final Logger LOGGER = LoggerFactory.getLogger(DigitalObjectsAPIController.class);

  public DigitalObjectsAPIController(CudamiClient client, LanguageService languageService) {
    super(client.forDigitalObjects(), client, languageService);
  }

  /*
   * Used in templates/collections/view.html as param for
   * templates/fragments/modals/select-entities.html
   */
  @GetMapping("/api/digitalobjects/search")
  @ResponseBody
  public PageResponse<DigitalObject> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "searchField", required = false) String searchField,
      @RequestParam(name = "searchTerm", required = false) String searchTerm,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy)
      throws TechnicalException {
    // TODO ?: add datalanguage as request param to allow search / autocompletion in
    // selected data language
    String dataLanguage = null;
    PageRequest pageRequest =
        createPageRequest(
            DigitalObject.class,
            pageNumber,
            pageSize,
            sortBy,
            searchField,
            searchTerm,
            dataLanguage);
    PageResponse<DigitalObject> pageResponse = search(searchField, searchTerm, pageRequest);
    if (pageResponse == null) {
      throw new InvalidEndpointRequestException("invalid request param", searchField);
    }
    return pageResponse;
  }

  @SuppressFBWarnings
  @GetMapping("/api/digitalobjects")
  @ResponseBody
  public BTResponse<DigitalObject> find(
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(name = "limit", required = false, defaultValue = "10") int limit,
      @RequestParam(name = "search", required = false) String searchTerm,
      @RequestParam(name = "sort", required = false, defaultValue = "label") String sortProperty,
      @RequestParam(name = "order", required = false, defaultValue = "asc") String sortOrder,
      @RequestParam(name = "dataLanguage", required = false) String dataLanguage)
      throws TechnicalException {
    return find(
        DigitalObject.class,
        offset,
        limit,
        sortProperty,
        sortOrder,
        "label",
        searchTerm,
        dataLanguage);
  }

  @GetMapping(
      value = "/api/digitalobjects/{uuid:" + ParameterHelper.UUID_PATTERN + "}/collections",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public BTResponse<Collection> findAssociatedCollections(
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
        ((CudamiDigitalObjectsClient) service).findCollections(uuid, btRequest);
    return new BTResponse<>(pageResponse);
  }

  @GetMapping(
      value = "/api/digitalobjects/{uuid:" + ParameterHelper.UUID_PATTERN + "}/projects",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public BTResponse<Project> findAssociatedProjects(
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
            Project.class,
            offset,
            limit,
            sortProperty,
            sortOrder,
            "label",
            searchTerm,
            dataLanguage);
    PageResponse<Project> pageResponse =
        ((CudamiDigitalObjectsClient) service).findProjects(uuid, btRequest);
    return new BTResponse<>(pageResponse);
  }

  @GetMapping("/api/digitalobjects/{uuid:" + ParameterHelper.UUID_PATTERN + "}/digitalobjects")
  @ResponseBody
  public BTResponse<DigitalObject> findContainedDigitalObjects(
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
        ((CudamiDigitalObjectsClient) service)
            .getAllForParent(DigitalObject.builder().uuid(uuid).build(), btRequest);
    return new BTResponse<>(pageResponse);
  }

  @GetMapping("/api/digitalobjects/identifier/{namespace}:{id}")
  @ResponseBody
  public DigitalObject getByIdentifier(@PathVariable String namespace, @PathVariable String id)
      throws TechnicalException {
    return ((CudamiIdentifiablesClient<DigitalObject>) service).getByIdentifier(namespace, id);
  }

  @GetMapping("/api/digitalobjects/{refId:[0-9]+}")
  @ResponseBody
  public DigitalObject getByRefId(@PathVariable long refId) throws TechnicalException {
    return ((CudamiEntitiesClient<DigitalObject>) service).getByRefId(refId);
  }

  @GetMapping("/api/digitalobjects/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  @ResponseBody
  public DigitalObject getByUuid(@PathVariable UUID uuid) throws TechnicalException {
    return service.getByUuid(uuid);
  }
}
