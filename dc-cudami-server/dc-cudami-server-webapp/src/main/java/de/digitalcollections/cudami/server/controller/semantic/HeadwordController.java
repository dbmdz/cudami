package de.digitalcollections.cudami.server.controller.semantic;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.semantic.HeadwordService;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.list.buckets.BucketsRequest;
import de.digitalcollections.model.list.buckets.BucketsResponse;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import de.digitalcollections.model.semantic.Headword;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Locale;
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
@Tag(name = "Headword controller")
public class HeadwordController {

  private final HeadwordService headwordService;

  public HeadwordController(HeadwordService headwordService) {
    this.headwordService = headwordService;
  }

  @Operation(summary = "Get count of headwords")
  @GetMapping(
      value = {"/v6/headwords/count", "/v5/headwords/count"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public long count() {
    return headwordService.count();
  }

  @Operation(summary = "Delete an headword with all its relations")
  @DeleteMapping(
      value = {"/v6/headwords/{uuid}", "/v5/headwords/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity delete(
      @Parameter(example = "", description = "UUID of the headword") @PathVariable("uuid")
          UUID uuid) {
    boolean successful = headwordService.delete(uuid);

    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Get all headwords as (filtered, sorted, paged) list")
  @GetMapping(
      value = {"/v6/headwords", "/v5/headwords"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Headword> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "label", required = false) FilterCriterion<String> labelCriterion,
      @RequestParam(name = "locale", required = false) FilterCriterion<String> localeCriterion) {
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    if (labelCriterion != null || localeCriterion != null) {
      Filtering filtering = new Filtering();
      if (labelCriterion != null) {
        filtering.add("label", labelCriterion);
      }
      if (localeCriterion != null) {
        filtering.add(
            Filtering.builder()
                .add(
                    new FilterCriterion<Locale>(
                        "locale",
                        localeCriterion.getOperation(),
                        Locale.forLanguageTag(localeCriterion.getValue().toString())))
                .build());
      }
      pageRequest.setFiltering(filtering);
    }
    return headwordService.find(pageRequest);
  }

  @Operation(summary = "Get lower and upper headword borders as equal sized buckets in a list")
  @GetMapping(
      value = {"/v6/headwords/buckets"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public BucketsResponse<Headword> find(
      @RequestParam(name = "numberOfBuckets", required = false, defaultValue = "25")
          int numberOfBuckets) {
    // TODO: until now only size is parametrizable:
    BucketsRequest<Headword> bucketsRequest =
        new BucketsRequest<>(numberOfBuckets, null, null, null);
    return headwordService.find(bucketsRequest);
  }

  @Operation(summary = "Find limited amount of random headwords")
  @GetMapping(
      value = {"/v6/headwords/random", "/v5/headwords/random"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Headword> getRandom(
      @RequestParam(name = "count", required = false, defaultValue = "5") int count) {
    return headwordService.getRandom(count);
  }

  @Operation(summary = "Get an headword by uuid")
  @GetMapping(
      value = {
        "/v6/headwords/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}",
        "/v5/headwords/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Headword getByUuid(@PathVariable UUID uuid) {
    return headwordService.getByUuid(uuid);
  }

  @Operation(summary = "Get related entities of an headword")
  @GetMapping(
      value = {"/v6/headwords/{uuid}/related/entities", "/v5/headwords/{uuid}/related/entities"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Entity> findRelatedEntities(
      @Parameter(example = "", description = "UUID of the headword") @PathVariable("uuid")
          UUID uuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize) {
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize);
    return headwordService.findRelatedEntities(uuid, pageRequest);
  }

  @Operation(summary = "Get related file resources of an headword")
  @GetMapping(
      value = {
        "/v6/headwords/{uuid}/related/fileresources",
        "/v5/headwords/{uuid}/related/fileresources"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<FileResource> findRelatedFileResources(
      @Parameter(example = "", description = "UUID of the headword") @PathVariable("uuid")
          UUID uuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize) {
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize);
    return headwordService.findRelatedFileResources(uuid, pageRequest);
  }

  @Operation(summary = "Save a newly created headword")
  @PostMapping(
      value = {"/v6/headwords", "/v5/headwords"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Headword save(@RequestBody Headword headword, BindingResult errors)
      throws IdentifiableServiceException, ServiceException {
    return headwordService.save(headword);
  }

  @Operation(summary = "Save list of related entities for a given headword")
  @PostMapping(
      value = {"/v6/headwords/{uuid}/entities", "/v5/headwords/{uuid}/entities"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Entity> setRelatedEntities(
      @Parameter(example = "", description = "UUID of the headword") @PathVariable("uuid")
          UUID uuid,
      @RequestBody List<Entity> entities) {
    return headwordService.setRelatedEntities(uuid, entities);
  }

  @Operation(summary = "Save list of related fileresources for a given headword")
  @PostMapping(
      value = {"/v6/headwords/{uuid}/fileresources", "/v5/headwords/{uuid}/fileresources"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<FileResource> setRelatedFileResources(
      @Parameter(example = "", description = "UUID of the headword") @PathVariable("uuid")
          UUID uuid,
      @RequestBody List<FileResource> fileResources) {
    return headwordService.setRelatedFileResources(uuid, fileResources);
  }

  @Operation(summary = "Update an headword")
  @PutMapping(
      value = {"/v6/headwords/{uuid}", "/v5/headwords/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Headword update(
      @Parameter(example = "", description = "UUID of the headword") @PathVariable("uuid")
          UUID uuid,
      @RequestBody Headword headword,
      BindingResult errors)
      throws IdentifiableServiceException, ServiceException {
    assert Objects.equals(uuid, headword.getUuid());
    return headwordService.update(headword);
  }
}
