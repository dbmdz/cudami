package de.digitalcollections.cudami.server.controller.identifiable.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.CollectionService;
import de.digitalcollections.cudami.server.controller.AbstractLegacyController;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.Sorting;
import java.util.List;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiResponseObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(description = "The collection controller V2", name = "Collection controller V2")
public class V2CollectionController extends AbstractLegacyController {

  private final CollectionService collectionService;
  private final LocaleService localeService;

  public V2CollectionController(CollectionService collectionService, LocaleService localeService) {
    this.collectionService = collectionService;
    this.localeService = localeService;
  }

  @ApiMethod(description = "Get all collections (V2)")
  @GetMapping(
      value = {"/v2/collections"},
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

    return new ResponseEntity(fixPageResponse(response), HttpStatus.OK);
  }
}
