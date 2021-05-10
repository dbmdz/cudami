package de.digitalcollections.cudami.server.controller.identifiable.entity.relation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.relation.EntityRelationService;
import de.digitalcollections.model.filter.Filtering;
import de.digitalcollections.model.identifiable.entity.relation.EntityRelation;
import de.digitalcollections.model.jackson.DigitalCollectionsObjectMapper;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import java.util.Iterator;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiResponseObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(description = "The entity relations controller V3", name = "Entity relations controller v3")
public class V3EntityRelationController {

  private final EntityRelationService entityRelationService;

  private final DigitalCollectionsObjectMapper objectMapper = new DigitalCollectionsObjectMapper();

  public V3EntityRelationController(EntityRelationService entityRelationservice) {
    this.entityRelationService = entityRelationservice;
  }

  @ApiMethod(description = "Get paged, sorted, filtered relations")
  @GetMapping(
      value = {"/v3/entities/relations"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public ResponseEntity<String> getEntitiesRelations(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "predicate", required = false) String predicate)
      throws JsonProcessingException {
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize);

    if (StringUtils.hasText(predicate)) {
      Filtering filtering =
          Filtering.defaultBuilder().filter("predicate").isEquals(predicate).build();
      pageRequest.add(filtering);
    }
    PageResponse<EntityRelation> response = entityRelationService.find(pageRequest);

    // Fix the attributes, which are missing or different in new model
    JSONObject result = new JSONObject(objectMapper.writeValueAsString(response));
    JSONArray websites = (JSONArray) result.get("content");
    for (Iterator it = websites.iterator(); it.hasNext(); ) {
      JSONObject website = (JSONObject) it.next();
      website.put(
          "className",
          "de.digitalcollections.model.impl.identifiable.entity.relation.EntityRelationImpl");
    }

    return new ResponseEntity<>(result.toString(), HttpStatus.OK);
  }
}
