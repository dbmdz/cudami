package de.digitalcollections.cudami.server.controller.identifiable.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.WebsiteService;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
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
 * This controller is only responsible for the incompatible v3 endpoints, which differ from the
 * latest endpoint
 */
@RestController
@Tag(name = "Website controller")
public class V3WebsiteController {

  private final ObjectMapper objectMapper;
  private final WebsiteService websiteService;

  public V3WebsiteController(WebsiteService websiteService, ObjectMapper objectMapper) {
    this.websiteService = websiteService;
    this.objectMapper = objectMapper;
  }

  @Operation(
      deprecated = true,
      summary = "Get a website by its uuid",
      description = "Use /v5/websites/{uuid} instead",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Website or empty value (instead of a 404 error)",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Website.class))),
        @ApiResponse(responseCode = "404", description = "never returned!")
      })
  @GetMapping(
      value = {"/latest/websites/{uuid}", "/v3/websites/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> getByUuid(
      @Parameter(
              name = "uuid",
              description = "the UUID of the website",
              example = "7a2f1935-c5b8-40fb-8622-c675de0a6242",
              schema = @Schema(implementation = UUID.class))
          @PathVariable
          UUID uuid)
      throws JsonProcessingException {
    Website website = websiteService.getByUuid(uuid);

    if (website == null) {
      // For compatibility reasons, we must return 200 with empty body
      return new ResponseEntity<>("", HttpStatus.OK);
    }

    // Fix the attributes, which are missing or different in new model
    JSONObject result = new JSONObject(objectMapper.writeValueAsString(website));
    JSONArray rootPages = (JSONArray) result.get("rootPages");
    for (Iterator it = rootPages.iterator(); it.hasNext(); ) {
      JSONObject rootPage = (JSONObject) it.next();
      rootPage.put("type", "ENTITY_PART");
      rootPage.put("entityPartType", "WEBPAGE");
    }

    return new ResponseEntity<>(result.toString(), HttpStatus.OK);
  }

  @Operation(
      summary = "Get root pages of a website",
      description = "Get a paged and sorted list of root pages of a website",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description =
                "SearchPageResponse&lt;Webpage&gt; (<a href=\"https://raw.githubusercontent.com/dbmdz/digitalcollections-model/8.2.1/dc-model/src/main/java/de/digitalcollections/model/api/paging/SearchPageResponse.java\">dc-model &lt; 9.0</a>)",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = String.class),
                    examples = {
                      @ExampleObject(
                          name = "example list",
                          externalValue =
                              "https://github.com/dbmdz/cudami/raw/main/dc-cudami-server/dc-cudami-server-webapp/src/test/resources/json/v3/websites/7a2f1935-c5b8-40fb-8622-c675de0a6242_rootpages.json")
                    }))
      })
  @GetMapping(
      value = {"/v3/websites/{uuid}/rootpages", "/latest/websites/{uuid}/rootpages"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> findRootPages(
      @Parameter(
              name = "uuid",
              description = "the UUID of the collection",
              example = "599a120c-2dd5-11e8-b467-0ed5f89f718b",
              schema = @Schema(implementation = UUID.class))
          @PathVariable("uuid")
          UUID uuid,
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
          List<Order> sortBy)
      throws JsonProcessingException {
    SearchPageRequest searchPageRequest = new SearchPageRequest(null, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      searchPageRequest.setSorting(sorting);
    }

    SearchPageResponse<Webpage> response = websiteService.findRootWebpages(uuid, searchPageRequest);
    if (response == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Fix the attributes, which are missing or different in new model
    JSONObject result = new JSONObject(objectMapper.writeValueAsString(response));
    JSONArray rootPages = (JSONArray) result.get("content");
    for (Iterator it = rootPages.iterator(); it.hasNext(); ) {
      JSONObject rootPage = (JSONObject) it.next();
      rootPage.put(
          "className", "de.digitalcollections.model.impl.identifiable.entity.parts.WebpageImpl");
      rootPage.put("type", "ENTITY_PART");
      rootPage.put("entityPartType", "WEBPAGE");
    }

    return new ResponseEntity<>(result.toString(), HttpStatus.OK);
  }
}
