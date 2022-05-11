package de.digitalcollections.cudami.server.controller.identifiable.entity.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.agent.CorporateBodyService;
import de.digitalcollections.cudami.server.controller.legacy.V5MigrationHelper;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Iterator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Corporate body controller")
public class V2CorporateBodyController {

  private final ObjectMapper objectMapper;
  private final CorporateBodyService corporateBodyService;

  public V2CorporateBodyController(
      CorporateBodyService corporateBodyservice, ObjectMapper objectMapper) {
    this.corporateBodyService = corporateBodyservice;
    this.objectMapper = objectMapper;
  }

  @Operation(
      summary = "find corporate bodies (deprecated, use /v5/corporatebodies instead)",
      description = "get a paged and filtered list of corporate bodies",
      deprecated = true,
      responses = {
        @ApiResponse(
            responseCode = "200",
            description =
                "SearchPageResponse&lt;CorporateBody&gt; (<a href=\"https://raw.githubusercontent.com/dbmdz/digitalcollections-model/8.2.1/dc-model/src/main/java/de/digitalcollections/model/api/paging/SearchPageResponse.java\">dc-model &lt; 9.0</a>)",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = String.class),
                    examples = {
                      @ExampleObject(
                          name = "empty list",
                          externalValue =
                              "https://github.com/dbmdz/cudami/raw/main/dc-cudami-server/dc-cudami-server-webapp/src/test/resources/json/v2/corporatebodies/corporatebodies.json")
                    }))
      })
  @GetMapping(
      value = {"/v2/corporatebodies", "/latest/corporatebodies"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> find(
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
              name = "sortBy",
              description =
                  "the sorting specification; if unset, default to alphabetically ascending sorting of the field 'label')",
              example = "label_de.desc.nullsfirst",
              schema = @Schema(type = "string"))
          @RequestParam(name = "sortBy", required = false)
          List<Order> sortBy,
      @Parameter(
              name = "searchTerm",
              description = "the search term, of which the result is filtered (substring match)",
              example = "Archiv",
              schema = @Schema(type = "string"))
          @RequestParam(name = "searchTerm", required = false)
          String searchTerm)
      throws JsonProcessingException {
    PageRequest pageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    PageResponse<CorporateBody> response = corporateBodyService.find(pageRequest);

    // Fix the attributes, which are missing or different in new model
    JSONObject result = new JSONObject(objectMapper.writeValueAsString(response));
    JSONArray websites = (JSONArray) result.get("content");
    for (Iterator it = websites.iterator(); it.hasNext(); ) {
      JSONObject website = (JSONObject) it.next();
      website.put(
          "className",
          "de.digitalcollections.model.impl.identifiable.entity.agent.CorporateBodyImpl");
    }

    String resultStr = V5MigrationHelper.migrateToV5(result.toString());
    return new ResponseEntity<>(resultStr, HttpStatus.OK);
  }
}
