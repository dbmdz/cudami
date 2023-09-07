package de.digitalcollections.cudami.admin.controller.identifiable.resource;

import de.digitalcollections.cudami.admin.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.admin.business.i18n.LanguageService;
import de.digitalcollections.cudami.admin.controller.ParameterHelper;
import de.digitalcollections.cudami.admin.controller.identifiable.AbstractIdentifiablesController;
import de.digitalcollections.cudami.admin.model.bootstraptable.BTResponse;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.resource.CudamiFileResourcesMetadataClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.FilterLogicalOperator;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.endpoint.InvalidEndpointRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Controller for all public "FileResources" endpoints (API). */
@RestController
public class FileResourcesMetadataAPIController
    extends AbstractIdentifiablesController<FileResource, CudamiFileResourcesMetadataClient> {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(FileResourcesMetadataAPIController.class);

  public FileResourcesMetadataAPIController(CudamiClient client, LanguageService languageService) {
    super(client.forFileResourcesMetadata(), client, languageService);
  }

  @GetMapping("/api/fileresources/new")
  @ResponseBody
  public FileResource create() throws TechnicalException {
    return service.create();
  }

  @SuppressFBWarnings
  @GetMapping("/api/fileresources")
  @ResponseBody
  public BTResponse<FileResource> find(
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(name = "limit", required = false, defaultValue = "10") int limit,
      @RequestParam(name = "search", required = false) String searchTerm,
      @RequestParam(name = "sort", required = false, defaultValue = "label") String sortProperty,
      @RequestParam(name = "order", required = false, defaultValue = "asc") String sortOrder,
      @RequestParam(name = "dataLanguage", required = false) String dataLanguage)
      throws TechnicalException, ServiceException {
    return find(
        FileResource.class,
        offset,
        limit,
        sortProperty,
        sortOrder,
        "label",
        searchTerm,
        dataLanguage);
  }

  /*
   * Used in templates/topics/view.html as param for
   * templates/fragments/modals/select-fileresources.html
   */
  @GetMapping("/api/fileresources/search")
  @ResponseBody
  public PageResponse<FileResource> find(
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
            Entity.class, pageNumber, pageSize, sortBy, searchField, searchTerm, dataLanguage);
    PageResponse<FileResource> pageResponse = search(searchField, searchTerm, pageRequest);
    if (pageResponse == null) {
      throw new InvalidEndpointRequestException("invalid request param", searchField);
    }
    return pageResponse;
  }

  @GetMapping("/api/fileresources/type/{type}")
  @ResponseBody
  public PageResponse<FileResource> findByType(
      @PathVariable String type,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "searchTerm", required = false) String searchTerm,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy)
      throws TechnicalException {
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize);
    if (searchTerm != null) {
      Filtering filtering =
          Filtering.builder()
              .filterCriterion(
                  FilterLogicalOperator.OR,
                  FilterCriterion.builder().contains(searchTerm).withExpression("label").build())
              .filterCriterion(
                  FilterLogicalOperator.OR,
                  FilterCriterion.builder()
                      .contains(searchTerm)
                      .withExpression("description")
                      .build())
              .filterCriterion(
                  FilterLogicalOperator.OR,
                  FilterCriterion.builder().contains(searchTerm).withExpression("filename").build())
              .build();
      pageRequest.setFiltering(filtering);
    }
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    return ((CudamiFileResourcesMetadataClient) service).findByType(pageRequest, type);
  }

  @GetMapping("/api/fileresources/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  @ResponseBody
  public FileResource getByUuid(@PathVariable UUID uuid) throws TechnicalException {
    return service.getByUuid(uuid);
  }

  @PostMapping("/api/fileresources")
  public ResponseEntity save(@RequestBody FileResource fileResource) {
    try {
      FileResource fileResourceDb = service.save(fileResource);
      return ResponseEntity.status(HttpStatus.CREATED).body(fileResourceDb);
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save fileresource: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @PutMapping("/api/fileresources/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  public ResponseEntity update(@PathVariable UUID uuid, @RequestBody FileResource fileResource) {
    try {
      FileResource fileResourceDb = service.update(uuid, fileResource);
      return ResponseEntity.ok(fileResourceDb);
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save fileresource with uuid={}", uuid, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }
}
