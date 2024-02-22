package io.github.dbmdz.cudami.controller.identifiable.resource;

import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.resource.CudamiImageFileResourcesClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import io.github.dbmdz.cudami.business.api.service.exceptions.ServiceException;
import io.github.dbmdz.cudami.business.i18n.LanguageService;
import io.github.dbmdz.cudami.controller.ParameterHelper;
import io.github.dbmdz.cudami.controller.identifiable.AbstractIdentifiablesController;
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
public class ImageFileResourcesAPIController
    extends AbstractIdentifiablesController<ImageFileResource, CudamiImageFileResourcesClient> {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(ImageFileResourcesAPIController.class);

  public ImageFileResourcesAPIController(CudamiClient client, LanguageService languageService) {
    super(client.forImageFileResources(), client, languageService);
  }

  @GetMapping("/api/imagefileresources/new")
  @ResponseBody
  public ImageFileResource create() throws TechnicalException {
    return service.create();
  }

  @GetMapping("/api/imagefileresources")
  @ResponseBody
  public PageResponse<ImageFileResource> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "searchTerm", required = false) String searchTerm,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy)
      throws TechnicalException, ServiceException {
    PageRequest pageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    return service.find(pageRequest);
  }

  @GetMapping("/api/imagefileresources/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  @ResponseBody
  public ImageFileResource getByUuid(@PathVariable UUID uuid) throws TechnicalException {
    return service.getByUuid(uuid);
  }

  @PostMapping("/api/imagefileresources")
  public ResponseEntity save(@RequestBody ImageFileResource imageFileResource) {
    try {
      ImageFileResource fileResourceDb = service.save(imageFileResource);
      return ResponseEntity.status(HttpStatus.CREATED).body(fileResourceDb);
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save image fileresource: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @PutMapping("/api/imagefileresources/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  public ResponseEntity update(
      @PathVariable UUID uuid, @RequestBody ImageFileResource imageFileResource) {
    try {
      ImageFileResource fileResourceDb = service.update(uuid, imageFileResource);
      return ResponseEntity.ok(fileResourceDb);
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save image fileresource with uuid={}", uuid, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }
}
