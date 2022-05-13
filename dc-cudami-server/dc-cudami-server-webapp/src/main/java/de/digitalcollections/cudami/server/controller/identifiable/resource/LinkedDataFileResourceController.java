package de.digitalcollections.cudami.server.controller.identifiable.resource;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.LinkedDataFileResourceService;
import de.digitalcollections.model.identifiable.resource.LinkedDataFileResource;
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
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "LinkedData Fileresource controller")
public class LinkedDataFileResourceController {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(LinkedDataFileResourceController.class);

  private final LinkedDataFileResourceService service;

  public LinkedDataFileResourceController(LinkedDataFileResourceService service) {
    this.service = service;
  }

  @Operation(summary = "Get a paged list of all linkedDataFileResources")
  @GetMapping(
      value = {"/v6/linkeddatafileresources"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<LinkedDataFileResource> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "uri", required = false)
          FilterCriterion<String> encodedUriFilterCriterion) {

    PageRequest pageRequest = new PageRequest(pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    if (encodedUriFilterCriterion != null) {
      FilterCriterion<String> uri =
          new FilterCriterion<>(
              "uri",
              encodedUriFilterCriterion.getOperation(),
              URLDecoder.decode(
                  (String) encodedUriFilterCriterion.getValue(), StandardCharsets.UTF_8));
      Filtering filtering = Filtering.builder().add("uri", uri).build();
      pageRequest.setFiltering(filtering);
    }

    return service.find(pageRequest);
  }

  @Operation(summary = "Find a limited and filtered amount of LinkedDataFileResources")
  @GetMapping(
      value = {"/v6/linkeddatafileresources/search"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<LinkedDataFileResource> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "searchTerm", required = false) String searchTerm,
      @RequestParam(name = "uri", required = false)
          FilterCriterion<String> encodedUriFilterCriterion) {
    PageRequest searchPageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
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

    return service.find(searchPageRequest);
  }

  @Operation(summary = "Get a linkedDataFileResource by uuid")
  @GetMapping(
      value = {"/v6/linkeddatafileresources/{uuid}", "/v5/linkeddatafileresources/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<LinkedDataFileResource> getByUuid(
      @Parameter(
              example = "",
              description =
                  "UUID of the linkeDataFileResource, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @Parameter(
              name = "pLocale",
              description =
                  "Desired locale, e.g. <tt>de_DE</tt>. If unset, contents in all languages will be returned")
          @RequestParam(name = "pLocale", required = false)
          Locale pLocale)
      throws IdentifiableServiceException {
    LinkedDataFileResource linkedDataFileResource;
    if (pLocale == null) {
      linkedDataFileResource = service.getByUuid(uuid);
    } else {
      linkedDataFileResource = service.getByUuidAndLocale(uuid, pLocale);
    }
    return new ResponseEntity<>(linkedDataFileResource, HttpStatus.OK);
  }

  @Operation(summary = "Save a newly created linkedDataFileResource")
  @PostMapping(
      value = {"/v6/linkeddatafileresources", "/v5/linkeddatafileresources"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public LinkedDataFileResource save(@RequestBody LinkedDataFileResource linkedDataFileResource)
      throws IdentifiableServiceException, ValidationException {
    return service.save(linkedDataFileResource);
  }

  @Operation(summary = "Update a linkedDataFileResource")
  @PutMapping(
      value = {"/v5/linkeddatafileresources/{uuid}", "/v6/linkeddatafileresources/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public LinkedDataFileResource update(
      @PathVariable UUID uuid,
      @RequestBody LinkedDataFileResource linkedDataFileResource,
      BindingResult errors)
      throws IdentifiableServiceException, ValidationException {
    assert Objects.equals(uuid, linkedDataFileResource.getUuid());
    return service.update(linkedDataFileResource);
  }
}
