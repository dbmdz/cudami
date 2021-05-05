package de.digitalcollections.cudami.server.controller.identifiable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierTypeService;
import de.digitalcollections.model.identifiable.IdentifierType;
import de.digitalcollections.model.jackson.DigitalCollectionsObjectMapper;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.Sorting;
import java.util.Iterator;
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
@Api(description = "The identifier types controller V2", name = "Identifier types controller V2")
public class V2IdentifierTypeController {

  private final DigitalCollectionsObjectMapper objectMapper = new DigitalCollectionsObjectMapper();
  private final IdentifierTypeService identifierTypeService;

  public V2IdentifierTypeController(IdentifierTypeService identifierTypeService) {
    this.identifierTypeService = identifierTypeService;
  }

  @ApiMethod(description = "Get all identifier types")
  @GetMapping(
      value = {"/v2/identifiertypes"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public ResponseEntity<String> findAll(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy)
      throws JsonProcessingException {
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }

    PageResponse<IdentifierType> response = identifierTypeService.find(pageRequest);

    JSONObject result = new JSONObject(objectMapper.writeValueAsString(response));
    JSONArray digitalobjects = (JSONArray) result.get("content");
    for (Iterator it = digitalobjects.iterator(); it.hasNext(); ) {
      JSONObject digitalobject = (JSONObject) it.next();
      digitalobject.put(
          "className", "de.digitalcollections.model.impl.identifiable.IdentifierTypeImpl");
    }

    return new ResponseEntity<>(result.toString(), HttpStatus.OK);
  }
}
