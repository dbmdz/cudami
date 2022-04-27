package de.digitalcollections.cudami.server.controller.identifiable.entity.relation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.relation.EntityRelationService;
import de.digitalcollections.model.filter.FilterCriterion;
import de.digitalcollections.model.filter.Filtering;
import de.digitalcollections.model.identifiable.entity.relation.EntityRelation;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Iterator;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Entity relation controller")
public class V3EntityRelationController {

  private final EntityRelationService entityRelationService;
  private final ObjectMapper objectMapper;

  public V3EntityRelationController(
      EntityRelationService entityRelationservice, ObjectMapper objectMapper) {
    this.entityRelationService = entityRelationservice;
    this.objectMapper = objectMapper;
  }

  @Operation(
      summary = "Get filtered relations (deprecated, use /v5/entities/relations instead)",
      description = "Get a paged, sorted and filtered list of relations",
      deprecated = true,
      responses = {
        @ApiResponse(
            responseCode = "200",
            description =
                "PageResponse&lt;EntityRelation&gt; (<a href=\"https://raw.githubusercontent.com/dbmdz/digitalcollections-model/8.2.1/dc-model/src/main/java/de/digitalcollections/model/api/paging/PageResponse.java\">dc-model &lt; 9.0</a>)",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = String.class),
                    examples = {
                      @ExampleObject(
                          name = "example list",
                          externalValue =
                              "https://github.com/dbmdz/cudami/raw/main/dc-cudami-server/dc-cudami-server-webapp/src/test/resources/json/v3/entities/relations.json")
                    }))
      })
  @GetMapping(
      value = {"/v3/entities/relations", "/latest/entities/relations"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> findByPredicate(
      @Parameter(
              name = "pageNumber",
              description = "the page number (starting with 0); if unset, defaults to 0.",
              example = "0",
              schema = @Schema(type = "integer"))
          @RequestParam(name = "pageNumber", required = false, defaultValue = "0")
          int pageNumber,
      @Parameter(
              name = "pageSize",
              description = "the page size; if unset, defaults to 25",
              example = "25",
              schema = @Schema(type = "integer"))
          @RequestParam(name = "pageSize", required = false, defaultValue = "25")
          int pageSize,
      @Parameter(
              name = "predicate",
              description = "the predicate, e.g. <tt>is_sponsored_by</tt>",
              example = "is_sponsored_by",
              schema = @Schema(type = "string"))
          @RequestParam(name = "predicate", required = false)
          String predicate)
      throws JsonProcessingException {
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize);

    if (StringUtils.hasText(predicate)) {
      Filtering filtering =
          Filtering.builder()
              .add(
                  FilterCriterion.builder().withExpression("predicate").isEquals(predicate).build())
              .build();
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
