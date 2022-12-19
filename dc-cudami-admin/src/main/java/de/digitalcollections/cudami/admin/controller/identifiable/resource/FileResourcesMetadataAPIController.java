package de.digitalcollections.cudami.admin.controller.identifiable.resource;

import de.digitalcollections.cudami.admin.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.admin.controller.AbstractPagingAndSortingController;
import de.digitalcollections.cudami.admin.model.bootstraptable.BTResponse;
import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.CudamiLocalesClient;
import de.digitalcollections.cudami.client.identifiable.resource.CudamiFileResourcesMetadataClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.resource.FileResource;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/** Controller for all public "FileResources" endpoints (API). */
@RestController
public class FileResourcesMetadataAPIController
    extends AbstractPagingAndSortingController<FileResource> {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(FileResourcesMetadataAPIController.class);

  private final LanguageSortingHelper languageSortingHelper;
  private final CudamiLocalesClient localeService;
  private final CudamiFileResourcesMetadataClient service;

  public FileResourcesMetadataAPIController(
      LanguageSortingHelper languageSortingHelper, CudamiClient client) {
    this.languageSortingHelper = languageSortingHelper;
    this.localeService = client.forLocales();
    this.service = client.forFileResourcesMetadata();
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
      @RequestParam(name = "limit", required = false, defaultValue = "1") int limit,
      @RequestParam(name = "search", required = false) String searchTerm,
      @RequestParam(name = "sort", required = false, defaultValue = "url") String sort,
      @RequestParam(name = "order", required = false, defaultValue = "asc") String order,
      @RequestParam(name = "dataLanguage", required = false) String dataLanguage)
      throws TechnicalException, ServiceException {
    PageResponse<FileResource> pageResponse =
        super.find(localeService, service, offset, limit, searchTerm, sort, order, dataLanguage);
    return new BTResponse<>(pageResponse);
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
    PageRequest pageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    return service.findByType(pageRequest, type);
  }

  @GetMapping("/api/fileresources/{uuid}")
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

  @PutMapping("/api/fileresources/{uuid}")
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
