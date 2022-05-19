package de.digitalcollections.cudami.server.controller.identifiable.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.WebsiteService;
import de.digitalcollections.cudami.server.controller.legacy.V5MigrationHelper;
import de.digitalcollections.model.identifiable.entity.Website;
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
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * This controller is only responsible for the incompatible v2 endpoints, which differ from the
 * latest endpoint
 */
@RestController
@Tag(name = "Website controller")
public class V2WebsiteController {

  private final ObjectMapper objectMapper;
  private final WebsiteService websiteService;

  public V2WebsiteController(WebsiteService websiteService, ObjectMapper objectMapper) {
    this.websiteService = websiteService;
    this.objectMapper = objectMapper;
  }

  @Operation(
      summary = "Get websites",
      description = "Get a paged and filtered list of websites",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description =
                "SearchPageResponse&lt;Website&gt; (<a href=\"https://raw.githubusercontent.com/dbmdz/digitalcollections-model/8.2.1/dc-model/src/main/java/de/digitalcollections/model/api/paging/SearchPageResponse.java\">dc-model &lt; 9.0</a>)",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = String.class),
                    examples = {
                      @ExampleObject(
                          name = "empty list",
                          externalValue =
                              "https://github.com/dbmdz/cudami/raw/main/dc-cudami-server/dc-cudami-server-webapp/src/test/resources/json/v2/websites.json")
                    }))
      })
  @GetMapping(
      value = {"/v2/websites", "/latest/websites"},
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
              example = "Test",
              schema = @Schema(type = "string"))
          @RequestParam(name = "searchTerm", required = false)
          String searchTerm)
      throws JsonProcessingException {
    PageRequest searchPageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(V5MigrationHelper.migrate(sortBy));
      searchPageRequest.setSorting(sorting);
    }
    PageResponse response = websiteService.find(searchPageRequest);
    if (response == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Fix the attributes, which are missing or different in new model
    JSONObject result = new JSONObject(objectMapper.writeValueAsString(response));
    JSONArray websites = (JSONArray) result.get("content");
    for (Iterator it = websites.iterator(); it.hasNext(); ) {
      JSONObject website = (JSONObject) it.next();
      website.put("className", "de.digitalcollections.model.impl.identifiable.entity.WebsiteImpl");
    }

    String resultStr = result.toString();

    // TODO replace "query"
    return new ResponseEntity<>(resultStr, HttpStatus.OK);
  }

  @Operation(
      summary = "Get website by uuid",
      description = "Get a website by its uuid",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description =
                "Website (<a href=\"https://github.com/dbmdz/digitalcollections-model/raw/8.2.1/dc-model/src/main/java/de/digitalcollections/model/api/identifiable/entity/Website.java\">dc-model &lt; 9.0</a>)",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = String.class),
                    examples = {
                      @ExampleObject(
                          name = "single website",
                          externalValue =
                              "https://github.com/dbmdz/cudami/raw/main/dc-cudami-server/dc-cudami-server-webapp/src/test/resources/json/v2/websites/7ebaf4b1-cf5a-491b-991c-4fd082677ff9.json"),
                      @ExampleObject(
                          name = "nonexisting website",
                          value = "",
                          description = "even for non-existing websites, a status 200 is returned!")
                    })),
        @ApiResponse(
            responseCode = "404",
            description = "is never returned, even when no website was found!")
      })
  @GetMapping(
      value = {"/v2/websites/{uuid}", "/v2/websites/{uuid}.json"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public String getByUuid(
      @Parameter(
              name = "uuid",
              description = "the UUID of the website",
              example = "7ebaf4b1-cf5a-491b-991c-4fd082677ff9",
              schema = @Schema(implementation = UUID.class))
          @PathVariable
          UUID uuid)
      throws JsonProcessingException {
    Website website = websiteService.getByUuid(uuid);
    if (website == null) {
      return null;
    }

    // Fix the attributes, which are missing or different in new model
    JSONObject result = new JSONObject(objectMapper.writeValueAsString(website));
    result.put("type", "ENTITY");
    result.put("entityType", "WEBSITE");
    JSONArray rootPages = (JSONArray) result.get("rootPages");
    for (Iterator it = rootPages.iterator(); it.hasNext(); ) {
      JSONObject rootPage = (JSONObject) it.next();
      rootPage.put("type", "ENTITY_PART");
      rootPage.put("entityPartType", "WEBPAGE");
    }

    return result.toString();
  }
}
