package de.digitalcollections.cudami.server.controller.semantic;

import de.digitalcollections.cudami.server.business.api.service.UniqueObjectService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
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
import de.digitalcollections.model.list.sorting.Sorting;
import de.digitalcollections.model.semantic.Headword;
import de.digitalcollections.model.validation.ValidationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
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
  public long count() throws ServiceException {
    return super.count();
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
          UUID uuid)
      throws ConflictException, ServiceException {
    return super.delete(uuid);
  }

  @Operation(summary = "Get all headwords as (filtered, sorted, paged) list")
  @GetMapping(
      value = {"/v6/headwords", "/v5/headwords"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Headword> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "filter", required = false) List<FilterCriterion> filterCriteria,
      @RequestParam(name = "filtering", required = false) Filtering filtering)
      throws ServiceException {
    PageRequest pageRequest =
        createPageRequest(Headword.class, pageNumber, pageSize, sortBy, filterCriteria, filtering);
    if (filterCriteria != null) {
      Optional<FilterCriterion> localeCriterionOpt =
          filterCriteria.stream().filter(p -> "locale".equals(p.getExpression())).findAny();
      if (localeCriterionOpt.isPresent()) {
        FilterCriterion localeCriterion = localeCriterionOpt.get();
        String value = (String) localeCriterion.getValue();
        localeCriterion.setValue(Locale.forLanguageTag(value));
      }
      pageRequest.setFiltering(new Filtering(filterCriteria));
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
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "startId", required = true) UUID startId,
      @RequestParam(name = "endId", required = true) UUID endId)
      throws ServiceException {
    Headword startHeadword = new Headword();
    startHeadword.setUuid(startId);
    Headword endHeadword = new Headword();
    endHeadword.setUuid(endId);
    Bucket<Headword> bucket = new Bucket<>(startHeadword, endHeadword);

    // TODO add filtering (e.g. on label or locale) to bucketObjectsRequest

    BucketObjectsRequest<Headword> bucketObjectsRequest =
        new BucketObjectsRequest<>(bucket, pageNumber, pageSize, null, null);

    // add sorting
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      bucketObjectsRequest.setSorting(sorting);
    }

    return service.find(bucketObjectsRequest);
  }

  @Operation(summary = "Get lower and upper headword borders as equal sized buckets in a list")
  @GetMapping(
      value = {"/v6/headwords/buckets"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public BucketsResponse<Headword> findBuckets(
      @RequestParam(name = "numberOfBuckets", required = false, defaultValue = "25")
          int numberOfBuckets,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "startId", required = false) UUID startId,
      @RequestParam(name = "endId", required = false) UUID endId)
      throws ServiceException {
    Bucket<Headword> parentBucket = null;
    if (startId != null && endId != null) {
      Headword startHeadword = new Headword();
      startHeadword.setUuid(startId);
      Headword endHeadword = new Headword();
      endHeadword.setUuid(endId);
      parentBucket = new Bucket<>(startHeadword, endHeadword);
    }

    // TODO add filtering (e.g. on label or locale) to bucketsRequest

    BucketsRequest<Headword> bucketsRequest =
        new BucketsRequest<>(numberOfBuckets, parentBucket, null, null);

    // add sorting
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      bucketsRequest.setSorting(sorting);
    }

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
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize)
      throws ServiceException {
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize);
    return service.findRelatedEntities(buildExampleWithUuid(uuid), pageRequest);
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
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize)
      throws ServiceException {
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize);
    return service.findRelatedFileResources(buildExampleWithUuid(uuid), pageRequest);
  }

  @Operation(summary = "Get an headword by uuid")
  @GetMapping(
      value = {
        "/v6/headwords/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/headwords/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Headword> getByUuid(@PathVariable UUID uuid) throws ServiceException {
    return super.getByUuid(uuid);
  }

  @Operation(summary = "Find limited amount of random headwords")
  @GetMapping(
      value = {"/v6/headwords/random", "/v5/headwords/random"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Headword> getRandom(
      @RequestParam(name = "count", required = false, defaultValue = "5") int count)
      throws ServiceException {
    return service.getRandom(count);
  }

  @Operation(summary = "Save a newly created headword")
  @PostMapping(
      value = {"/v6/headwords", "/v5/headwords"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Headword save(@RequestBody Headword headword, BindingResult errors)
      throws ServiceException, ValidationException {
    return super.save(headword, errors);
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
      @RequestBody List<Entity> entities)
      throws ServiceException {
    return service.setRelatedEntities(buildExampleWithUuid(uuid), entities);
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
      @RequestBody List<FileResource> fileResources)
      throws ServiceException {
    return service.setRelatedFileResources(buildExampleWithUuid(uuid), fileResources);
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
      throws ServiceException, ValidationException {
    return super.update(uuid, headword, errors);
  }

  @Override
  protected UniqueObjectService<Headword> getService() {
    return service;
  }
}
