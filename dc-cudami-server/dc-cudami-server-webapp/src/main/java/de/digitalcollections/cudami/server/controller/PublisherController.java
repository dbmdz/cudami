package de.digitalcollections.cudami.server.controller;

import de.digitalcollections.cudami.server.business.api.service.PublisherService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.CudamiServiceException;
import de.digitalcollections.model.identifiable.entity.work.Publisher;
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
import java.util.Objects;
import java.util.UUID;
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
@Tag(name = "Publisher controller")
public class PublisherController {

  private final PublisherService service;

  public PublisherController(PublisherService service) {
    this.service = service;
  }

  @Operation(summary = "Get all publishers")
  @GetMapping(
      value = {"/v6/publishers"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Publisher> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "searchTerm", required = false) String searchTerm,
      @RequestParam(name = "agent_uuid", required = false)
          FilterCriterion<UUID> filterCriterionAgentUuid,
      @RequestParam(name = "location_uuid", required = false)
          FilterCriterion<UUID> filterCriterionLocationUuid,
      @RequestParam(name = "publisherPresentation", required = false)
          FilterCriterion<String> filterCriterionPublisherPresentation)
      throws CudamiServiceException {
    PageRequest pageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }

    Filtering filtering = null;
    if (filterCriterionAgentUuid != null) {
      if (filtering == null) {
        filtering = new Filtering();
      }
      filterCriterionAgentUuid.setExpression("agent_uuid");
      filtering.add(filterCriterionAgentUuid);
    }

    if (filterCriterionLocationUuid != null) {
      if (filtering == null) {
        filtering = new Filtering();
      }
      filterCriterionLocationUuid.setExpression("location_uuid");
      filtering.add(filterCriterionLocationUuid);
    }

    if (filterCriterionPublisherPresentation != null) {
      if (filtering == null) {
        filtering = new Filtering();
      }
      filterCriterionPublisherPresentation.setExpression("publisherPresentation");
      filtering.add(filterCriterionPublisherPresentation);
    }

    if (filtering != null) {
      pageRequest.add(filtering);
    }

    System.out.println("pageRequest=" + pageRequest);

    var foo = service.find(pageRequest);
    return foo;
  }

  @Operation(summary = "Get a publisher by its UUID")
  @GetMapping(
      value = {"/v6/publishers/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Publisher> getByUuid(@PathVariable UUID uuid)
      throws CudamiServiceException {
    Publisher publisher = service.getByUuid(uuid);
    if (publisher == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(publisher, HttpStatus.OK);
  }

  @Operation(summary = "Save a newly created publisher")
  @PostMapping(
      value = {"/v6/publishers"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Publisher save(@RequestBody Publisher publisher, BindingResult errors)
      throws CudamiServiceException {
    return service.save(publisher);
  }

  @Operation(summary = "Update a publisher")
  @PutMapping(
      value = {"/v6/publishers/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Publisher update(
      @PathVariable UUID uuid, @RequestBody Publisher publisher, BindingResult errors)
      throws CudamiServiceException {
    assert Objects.equals(uuid, publisher.getUuid());
    return service.update(publisher);
  }

  @Operation(summary = "Delete a publisher")
  @DeleteMapping(
      value = {"/v6/publishers/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity delete(
      @Parameter(example = "", description = "UUID of the publisher") @PathVariable("uuid")
          UUID uuid)
      throws ConflictException, CudamiServiceException {
    boolean successful = service.delete(uuid);
    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.NO_CONTENT);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }
}
