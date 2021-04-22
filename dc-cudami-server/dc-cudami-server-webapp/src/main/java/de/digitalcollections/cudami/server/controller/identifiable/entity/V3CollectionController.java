package de.digitalcollections.cudami.server.controller.identifiable.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.CollectionService;
import de.digitalcollections.cudami.server.controller.AbstractLegacyController;
import de.digitalcollections.model.filter.FilterCriterion;
import de.digitalcollections.model.filter.Filtering;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import de.digitalcollections.model.paging.Sorting;
import java.util.List;
import java.util.UUID;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiPathParam;
import org.jsondoc.core.annotation.ApiResponseObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(description = "The collection controller V3", name = "Collection controller V3")
public class V3CollectionController extends AbstractLegacyController {

  private final CollectionService collectionService;
  private final LocaleService localeService;

  public V3CollectionController(CollectionService collectionService, LocaleService localeService) {
    this.collectionService = collectionService;
    this.localeService = localeService;
  }

  @ApiMethod(description = "Get paged digital objects of a collection")
  @GetMapping(
      value = {"/v3/collections/{uuid}/digitalobjects"},
      produces = "application/json")
  @ApiResponseObject
  public ResponseEntity<String> getDigitalObjects(
      @ApiPathParam(description = "UUID of the collection") @PathVariable("uuid")
          UUID collectionUuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "searchTerm", required = false) String searchTerm)
      throws JsonProcessingException {
    SearchPageRequest searchPageRequest =
        new SearchPageRequest(searchTerm, pageNumber, pageSize, new Sorting());

    Collection collection = new Collection();
    collection.setUuid(collectionUuid);
    SearchPageResponse<DigitalObject> response =
        collectionService.getDigitalObjects(collection, searchPageRequest);

    return new ResponseEntity<>(fixPageResponse(response), HttpStatus.OK);

    /*
    // Fix the attributes, which are missing or different in new model
    JSONObject result =
        fixPageResponse(
            response, "de.digitalcollections.model.impl.identifiable.entity.DigitalObjectImpl");

    return new ResponseEntity<>(result.toString(), HttpStatus.OK);

     */
  }

  @ApiMethod(description = "Get (active or all) paged subcollections of a collection")
  @GetMapping(
      value = {"/v3/collections/{uuid}/subcollections"},
      produces = "application/json")
  @ApiResponseObject
  public ResponseEntity<String> getSubcollections(
      @ApiPathParam(description = "UUID of the collection") @PathVariable("uuid")
          UUID collectionUuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "active", required = false) String active)
      throws JsonProcessingException {
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize);

    PageResponse<Collection> response;

    if (active != null) {
      response = collectionService.getActiveChildren(collectionUuid, pageRequest);
    } else {
      response = collectionService.getChildren(collectionUuid, pageRequest);
    }
    // JSONObject result = oldfixPageResponse(response);

    return new ResponseEntity<>(fixPageResponse(response), HttpStatus.OK);
  }

  @ApiMethod(description = "Get all collections")
  @GetMapping(
      value = {"/v3/collections"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public ResponseEntity<String> findAll(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "active", required = false) String active)
      throws JsonProcessingException {
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    PageResponse<Collection> response;
    if (active != null) {
      response = collectionService.findActive(pageRequest);
    } else {
      response = collectionService.find(pageRequest);
    }

    // Fix the attributes, which are missing or different in new model
    // JSONObject result = oldfixPageResponse(response);
    return new ResponseEntity<>(fixPageResponse(response), HttpStatus.OK);
  }

  @ApiMethod(
      description = "Get all related - by the given predicate - corporate bodies of a collection")
  @GetMapping(
      value = {"/v3/collections/{uuid}/related/corporatebodies"},
      produces = "application/json")
  @ApiResponseObject
  public ResponseEntity<String> getRelatedCorporateBodies(
      @ApiPathParam(description = "UUID of the collection") @PathVariable("uuid") UUID uuid,
      @RequestParam(name = "predicate", required = true) FilterCriterion<String> predicate)
      throws JsonProcessingException {
    Filtering filtering = Filtering.defaultBuilder().add("predicate", predicate).build();
    List<CorporateBody> result = collectionService.getRelatedCorporateBodies(uuid, filtering);
    if (result == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // FIXME: A plain list is again something, which is not covered yet
    return new ResponseEntity(fixSimpleObjectList(result), HttpStatus.OK);
  }
}
