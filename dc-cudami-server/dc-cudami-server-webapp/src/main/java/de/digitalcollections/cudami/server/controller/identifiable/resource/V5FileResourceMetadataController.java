package de.digitalcollections.cudami.server.controller.identifiable.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.FileResourceMetadataService;
import de.digitalcollections.cudami.server.controller.CudamiControllerException;
import de.digitalcollections.cudami.server.controller.legacy.V5MigrationHelper;
import de.digitalcollections.cudami.server.controller.legacy.model.LegacyPageRequest;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Fileresource controller")
public class V5FileResourceMetadataController {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(V5FileResourceMetadataController.class);

  private final FileResourceMetadataService<FileResource> metadataService;

  private final ObjectMapper objectMapper;

  public V5FileResourceMetadataController(
      @Qualifier("fileResourceMetadataService")
          FileResourceMetadataService<FileResource> metadataService,
      ObjectMapper objectMapper) {
    this.metadataService = metadataService;
    this.objectMapper = objectMapper;
  }

  @Operation(summary = "Get all fileresources")
  @GetMapping(
      value = {"/v5/fileresources", "/v2/fileresources", "/latest/fileresources"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "searchTerm", required = false) String searchTerm,
      @RequestParam(name = "uri", required = false)
          FilterCriterion<String> encodedUriFilterCriterion)
      throws CudamiControllerException, ServiceException {
    PageRequest searchPageRequest = new LegacyPageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(V5MigrationHelper.migrate(sortBy));
      searchPageRequest.setSorting(sorting);
    }
    if (encodedUriFilterCriterion != null) {
      FilterCriterion<String> uri =
          new FilterCriterion<>(
              "uri",
              encodedUriFilterCriterion.getOperation(),
              URLDecoder.decode(
                  (String) encodedUriFilterCriterion.getValue(), StandardCharsets.UTF_8));
      Filtering filtering = Filtering.builder().add("uri", uri).build();
      searchPageRequest.setFiltering(filtering);
    }

    PageResponse<FileResource> pageResponse = metadataService.find(searchPageRequest);
    try {
      String result = V5MigrationHelper.migrate(pageResponse, objectMapper);
      return new ResponseEntity<>(result, HttpStatus.OK);
    } catch (JsonProcessingException e) {
      throw new CudamiControllerException(e);
    }
  }

  @Operation(
      summary =
          "Find limited amount of fileresources of given type containing searchTerm in label or description")
  @GetMapping(
      value = {
        "/v5/fileresources/type/{type}",
        "/v2/fileresources/type/{type}",
        "/latest/fileresources/type/{type}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> findByType(
      @Parameter(example = "", description = "Type of the fileresource, e.g. <tt>image</tt>")
          @PathVariable("type")
          String type,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "searchTerm", required = false) String searchTerm)
      throws CudamiControllerException, ServiceException {
    PageRequest pageRequest = new LegacyPageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(V5MigrationHelper.migrate(sortBy));
      pageRequest.setSorting(sorting);
    }

    String prefix;
    switch (type) {
      case "application":
        prefix = "application/";
        break;
      case "audio":
        prefix = "audio/";
        break;
      case "image":
        prefix = "image/";
        break;
      case "linkeddata":
        prefix = "application/ld";
        break;
      case "text":
        prefix = "text/";
        break;
      case "video":
        prefix = "video/";
        break;
      default:
        LOGGER.warn("Unsupported mimeType for type='{}'", type);
        prefix = null;
    }
    if (prefix != null) {
      Filtering filtering =
          Filtering.builder()
              .add(FilterCriterion.builder().withExpression("mimeType").startsWith(prefix).build())
              .build();
      pageRequest.add(filtering);
    }

    PageResponse<FileResource> pageResponse = metadataService.find(pageRequest);
    try {
      String result = V5MigrationHelper.migrate(pageResponse, objectMapper);
      return new ResponseEntity<>(result, HttpStatus.OK);
    } catch (JsonProcessingException e) {
      throw new CudamiControllerException(e);
    }
  }
}
