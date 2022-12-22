package de.digitalcollections.cudami.admin.controller.identifiable.entity;

import de.digitalcollections.cudami.admin.controller.AbstractPagingAndSortingController;
import de.digitalcollections.cudami.admin.controller.ParameterHelper;
import de.digitalcollections.cudami.admin.model.bootstraptable.BTResponse;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.CudamiLocalesClient;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiDigitalObjectsClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/** Controller for all public "DigitalObjects" endpoints (API). */
@RestController
public class DigitalObjectsAPIController extends AbstractPagingAndSortingController<DigitalObject> {

  private static final Logger LOGGER = LoggerFactory.getLogger(DigitalObjectsAPIController.class);

  private final CudamiLocalesClient localeService;
  private final CudamiDigitalObjectsClient service;

  public DigitalObjectsAPIController(CudamiClient client) {
    this.localeService = client.forLocales();
    this.service = client.forDigitalObjects();
  }

  @SuppressFBWarnings
  @GetMapping({"/api/digitalobjects", "/api/digitalobjects/search"})
  @ResponseBody
  public BTResponse<DigitalObject> find(
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(name = "limit", required = false, defaultValue = "1") int limit,
      @RequestParam(name = "search", required = false) String searchTerm,
      @RequestParam(name = "sort", required = false, defaultValue = "label") String sort,
      @RequestParam(name = "order", required = false, defaultValue = "asc") String order,
      @RequestParam(name = "dataLanguage", required = false) String dataLanguage)
      throws TechnicalException {

    PageResponse<DigitalObject> pageResponse =
        super.find(localeService, service, offset, limit, searchTerm, sort, order, dataLanguage);
    return new BTResponse<>(pageResponse);
  }

  @GetMapping(
      value = "/api/digitalobjects/{uuid:" + ParameterHelper.UUID_PATTERN + "}/collections",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public BTResponse<Collection> findAssociatedCollections(
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

    if ("label".equals(sort)) {
      if (dataLanguage == null) {
        dataLanguage = localeService.getDefaultLanguage().getLanguage();
      }
      Sorting sorting =
          Sorting.builder()
              .order(Order.builder().property("label").subProperty(dataLanguage).build())
              .build();
      pageRequest.setSorting(sorting);
    }
    PageResponse<Collection> pageResponse = service.findCollections(uuid, pageRequest);
    return new BTResponse<>(pageResponse);
  }

  @GetMapping(
      value = "/api/digitalobjects/{uuid:" + ParameterHelper.UUID_PATTERN + "}/projects",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public BTResponse<Project> findAssociatedProjects(
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

    if ("label".equals(sort)) {
      if (dataLanguage == null) {
        dataLanguage = localeService.getDefaultLanguage().getLanguage();
      }
      Sorting sorting =
          Sorting.builder()
              .order(Order.builder().property("label").subProperty(dataLanguage).build())
              .build();
      pageRequest.setSorting(sorting);
    }
    PageResponse<Project> pageResponse = service.findProjects(uuid, pageRequest);
    return new BTResponse<>(pageResponse);
  }

  @GetMapping("/api/digitalobjects/{uuid:" + ParameterHelper.UUID_PATTERN + "}/digitalobjects")
  @ResponseBody
  public BTResponse<DigitalObject> findContainedDigitalObjects(
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

    if ("label".equals(sort)) {
      if (dataLanguage == null) {
        dataLanguage = localeService.getDefaultLanguage().getLanguage();
      }
      Sorting sorting =
          Sorting.builder()
              .order(Order.builder().property("label").subProperty(dataLanguage).build())
              .build();
      pageRequest.setSorting(sorting);
    }
    PageResponse<DigitalObject> pageResponse =
        service.getAllForParent(DigitalObject.builder().uuid(uuid).build(), pageRequest);
    return new BTResponse<>(pageResponse);
  }

  @GetMapping("/api/digitalobjects/identifier/{namespace}:{id}")
  @ResponseBody
  public DigitalObject getByIdentifier(@PathVariable String namespace, @PathVariable String id)
      throws TechnicalException {
    return service.getByIdentifier(namespace, id);
  }

  @GetMapping("/api/digitalobjects/{refId:[0-9]+}")
  @ResponseBody
  public DigitalObject getByRefId(@PathVariable long refId) throws TechnicalException {
    return service.getByRefId(refId);
  }

  @GetMapping("/api/digitalobjects/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  @ResponseBody
  public DigitalObject getByUuid(@PathVariable UUID uuid) throws TechnicalException {
    return service.getByUuid(uuid);
  }
}
