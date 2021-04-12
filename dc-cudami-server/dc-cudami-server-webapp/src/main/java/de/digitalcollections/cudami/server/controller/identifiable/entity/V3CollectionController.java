package de.digitalcollections.cudami.server.controller.identifiable.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.CollectionService;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.jackson.DigitalCollectionsObjectMapper;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import de.digitalcollections.model.paging.Sorting;
import java.util.Iterator;
import java.util.UUID;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiPathParam;
import org.jsondoc.core.annotation.ApiResponseObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(description = "The collection controller V3", name = "Collection controller V3")
public class V3CollectionController {

  private final DigitalCollectionsObjectMapper objectMapper = new DigitalCollectionsObjectMapper();

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

    // Fix the attributes, which are missing or different in new model
    JSONObject result = new JSONObject(objectMapper.writeValueAsString(response));
    JSONArray digitalobjects = (JSONArray) result.get("content");
    for (Iterator it = digitalobjects.iterator(); it.hasNext(); ) {
      JSONObject digitalobject = (JSONObject) it.next();
      digitalobject.put(
          "className", "de.digitalcollections.model.impl.identifiable.entity.DigitalObjectImpl");
    }

    return new ResponseEntity<>(result.toString(), HttpStatus.OK);
  }
}
