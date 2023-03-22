package de.digitalcollections.cudami.server.controller.semantic;

import de.digitalcollections.cudami.server.business.api.service.UniqueObjectService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.semantic.HeadwordService;
import de.digitalcollections.cudami.server.controller.AbstractUniqueObjectController;
import de.digitalcollections.cudami.server.controller.ParameterHelper;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.list.buckets.Bucket;
import de.digitalcollections.model.list.buckets.BucketObjectsRequest;
import de.digitalcollections.model.list.buckets.BucketObjectsResponse;
import de.digitalcollections.model.list.buckets.BucketsRequest;
import de.digitalcollections.model.list.buckets.BucketsResponse;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
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
public class HeadwordController extends AbstractUniqueObjectController<Headword> {

  private final HeadwordService service;

  public HeadwordController(HeadwordService headwordService) {
    this.service = headwordService;
  }

  @Operation(summary = "Get count of headwords")
  @GetMapping(
      value = {"/v6/headwords/count", "/v5/headwords/count"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public long count() {
    return service.count();
  }

  @Operation(summary = "Delete an headword with all its relations")
  @DeleteMapping(
      value = {
        "/v6/headwords/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/headwords/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity delete(
      @Parameter(example = "", description = "UUID of the headword") @PathVariable("uuid")
          UUID uuid) {
    boolean successful = service.delete(uuid);
    return successful
        ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
        : new ResponseEntity<>(HttpStatus.NOT_FOUND);
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
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize, sortBy);
    if (labelCriterion != null || localeCriterion != null) {
      Filtering filtering = new Filtering();
      if (labelCriterion != null) {
        filtering.add("label", labelCriterion);
      }
      if (localeCriterion != null) {
        filtering.add(
            new FilterCriterion<Locale>(
                "locale",
                localeCriterion.getOperation(),
                Locale.forLanguageTag(localeCriterion.getValue().toString())));
      }
      pageRequest.setFiltering(filtering);
    }
    return service.find(pageRequest);
  }

  @Operation(
      summary = "Get paged list of headwords in a bucket (defined by lower and upper border UUIDs)")
  @GetMapping(
      value = {"/v6/headwords/bucketobjects"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public BucketObjectsResponse<Headword> findBucketObjects(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "startId", required = true) UUID startId,
      @RequestParam(name = "endId", required = true) UUID endId) {
    Headword startHeadword = new Headword();
    startHeadword.setUuid(startId);
    Headword endHeadword = new Headword();
    endHeadword.setUuid(endId);
    Bucket<Headword> bucket = new Bucket<>(startHeadword, endHeadword);
    // TODO: sorting is fix (on label), no filtering (e.g. on locale) available:
    BucketObjectsRequest<Headword> bucketObjectsRequest =
        new BucketObjectsRequest<>(bucket, pageNumber, pageSize, null, null);
    return service.find(bucketObjectsRequest);
  }

  @Operation(summary = "Get lower and upper headword borders as equal sized buckets in a list")
  @GetMapping(
      value = {"/v6/headwords/buckets"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public BucketsResponse<Headword> findBuckets(
      @RequestParam(name = "numberOfBuckets", required = false, defaultValue = "25")
          int numberOfBuckets,
      @RequestParam(name = "startId", required = false) UUID startId,
      @RequestParam(name = "endId", required = false) UUID endId) {
    Bucket<Headword> parentBucket = null;
    if (startId != null && endId != null) {
      Headword startHeadword = new Headword();
      startHeadword.setUuid(startId);
      Headword endHeadword = new Headword();
      endHeadword.setUuid(endId);
      parentBucket = new Bucket<>(startHeadword, endHeadword);
    }
    // TODO: sorting is fix (on label), no filtering (e.g. on locale) available:
    BucketsRequest<Headword> bucketsRequest =
        new BucketsRequest<>(numberOfBuckets, parentBucket, null, null);
    return service.find(bucketsRequest);
  }

  @Operation(summary = "Get related entities of an headword")
  @GetMapping(
      value = {
        "/v6/headwords/{uuid:" + ParameterHelper.UUID_PATTERN + "}/related/entities",
        "/v5/headwords/{uuid:" + ParameterHelper.UUID_PATTERN + "}/related/entities"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Entity> findRelatedEntities(
      @Parameter(example = "", description = "UUID of the headword") @PathVariable("uuid")
          UUID uuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize) {
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize);
    return service.findRelatedEntities(uuid, pageRequest);
  }

  @Operation(summary = "Get related file resources of an headword")
  @GetMapping(
      value = {
        "/v6/headwords/{uuid:" + ParameterHelper.UUID_PATTERN + "}/related/fileresources",
        "/v5/headwords/{uuid:" + ParameterHelper.UUID_PATTERN + "}/related/fileresources"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<FileResource> findRelatedFileResources(
      @Parameter(example = "", description = "UUID of the headword") @PathVariable("uuid")
          UUID uuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize) {
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize);
    return service.findRelatedFileResources(uuid, pageRequest);
  }

  @Operation(summary = "Get an headword by uuid")
  @GetMapping(
      value = {
        "/v6/headwords/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/headwords/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Headword> getByUuid(@PathVariable UUID uuid) {
    Headword result = service.getByUuid(uuid);
    return new ResponseEntity<>(result, result != null ? HttpStatus.OK : HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Find limited amount of random headwords")
  @GetMapping(
      value = {"/v6/headwords/random", "/v5/headwords/random"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Headword> getRandom(
      @RequestParam(name = "count", required = false, defaultValue = "5") int count) {
    return service.getRandom(count);
  }

  @Operation(summary = "Save a newly created headword")
  @PostMapping(
      value = {"/v6/headwords", "/v5/headwords"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Headword save(@RequestBody Headword headword, BindingResult errors)
      throws ServiceException {
    return service.save(headword);
  }

  @Operation(summary = "Save list of related entities for a given headword")
  @PostMapping(
      value = {
        "/v6/headwords/{uuid:" + ParameterHelper.UUID_PATTERN + "}/entities",
        "/v5/headwords/{uuid:" + ParameterHelper.UUID_PATTERN + "}/entities"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Entity> setRelatedEntities(
      @Parameter(example = "", description = "UUID of the headword") @PathVariable("uuid")
          UUID uuid,
      @RequestBody List<Entity> entities) {
    return service.setRelatedEntities(uuid, entities);
  }

  @Operation(summary = "Save list of related fileresources for a given headword")
  @PostMapping(
      value = {
        "/v6/headwords/{uuid:" + ParameterHelper.UUID_PATTERN + "}/fileresources",
        "/v5/headwords/{uuid:" + ParameterHelper.UUID_PATTERN + "}/fileresources"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<FileResource> setRelatedFileResources(
      @Parameter(example = "", description = "UUID of the headword") @PathVariable("uuid")
          UUID uuid,
      @RequestBody List<FileResource> fileResources) {
    return service.setRelatedFileResources(uuid, fileResources);
  }

  @Operation(summary = "Update an headword")
  @PutMapping(
      value = {
        "/v6/headwords/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/headwords/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Headword update(
      @Parameter(example = "", description = "UUID of the headword") @PathVariable("uuid")
          UUID uuid,
      @RequestBody Headword headword,
      BindingResult errors)
      throws ServiceException {
    assert Objects.equals(uuid, headword.getUuid());
    return service.update(headword);
  }

  @Override
  protected UniqueObjectService<Headword> getService() {
    return service;
  }
}
