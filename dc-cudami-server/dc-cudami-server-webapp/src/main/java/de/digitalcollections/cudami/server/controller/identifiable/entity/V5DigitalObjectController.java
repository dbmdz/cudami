package de.digitalcollections.cudami.server.controller.identifiable.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.DigitalObjectService;
import de.digitalcollections.cudami.server.controller.CudamiControllerException;
import de.digitalcollections.cudami.server.controller.legacy.V5MigrationHelper;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Digital object controller")
public class V5DigitalObjectController {

  private final DigitalObjectService digitalObjectService;

  private final ObjectMapper objectMapper;

  public V5DigitalObjectController(
      DigitalObjectService digitalObjectService, ObjectMapper objectMapper) {
    this.digitalObjectService = digitalObjectService;
    this.objectMapper = objectMapper;
  }

  @Operation(
      summary =
          "Find limited amount of digital objects containing searchTerm in label or description")
  @GetMapping(
      value = {
        "/v5/digitalobjects/search",
        "/v3/digitalobjects/search",
        "/latest/digitalobjects/search"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "searchTerm", required = false) String searchTerm,
      @RequestParam(name = "parent.uuid", required = false)
          FilterCriterion<UUID> parentUuidFilterCriterion)
      throws CudamiControllerException {
    PageRequest pageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(V5MigrationHelper.migrate(sortBy));
      pageRequest.setSorting(sorting);
    }
    if (parentUuidFilterCriterion != null) {
      parentUuidFilterCriterion.setExpression("parent.uuid");
      pageRequest.setFiltering(new Filtering(List.of(parentUuidFilterCriterion)));
    }

    PageResponse<DigitalObject> pageResponse = digitalObjectService.find(pageRequest);
    try {
      String result = V5MigrationHelper.migrate(pageResponse, objectMapper);
      return new ResponseEntity<>(result, HttpStatus.OK);
    } catch (JsonProcessingException e) {
      throw new CudamiControllerException(e);
    }
  }

  @Operation(summary = "Get paged projects of a digital objects")
  @GetMapping(
      value = {"/v5/digitalobjects/{uuid}/projects"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> findProjects(
      @Parameter(example = "", description = "UUID of the digital object") @PathVariable("uuid")
          UUID uuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "searchTerm", required = false) String searchTerm)
      throws CudamiControllerException {
    PageRequest pageRequest = new PageRequest(searchTerm, pageNumber, pageSize);

    DigitalObject digitalObject = new DigitalObject();
    digitalObject.setUuid(uuid);
    PageResponse<Project> pageResponse =
        digitalObjectService.findProjects(digitalObject, pageRequest);
    try {
      String result = V5MigrationHelper.migrate(pageResponse, objectMapper);
      return new ResponseEntity<>(result, HttpStatus.OK);
    } catch (JsonProcessingException e) {
      throw new CudamiControllerException(e);
    }
  }

  @Operation(summary = "Get (active or all) paged collections of a digital objects")
  @GetMapping(
      value = {"/v5/digitalobjects/{uuid}/collections"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> getCollections(
      @Parameter(example = "", description = "UUID of the digital object") @PathVariable("uuid")
          UUID uuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "active", required = false) String active,
      @RequestParam(name = "searchTerm", required = false) String searchTerm)
      throws CudamiControllerException {
    PageRequest searchPageRequest = new PageRequest(searchTerm, pageNumber, pageSize);

    DigitalObject digitalObject = new DigitalObject();
    digitalObject.setUuid(uuid);

    PageResponse<Collection> pageResponse;
    if (active != null) {
      pageResponse = digitalObjectService.findActiveCollections(digitalObject, searchPageRequest);
    } else {
      pageResponse = digitalObjectService.findCollections(digitalObject, searchPageRequest);
    }
    try {
      String result = V5MigrationHelper.migrate(pageResponse, objectMapper);
      return new ResponseEntity<>(result, HttpStatus.OK);
    } catch (JsonProcessingException e) {
      throw new CudamiControllerException(e);
    }
  }
}
