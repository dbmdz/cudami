package de.digitalcollections.cudami.server.controller.identifiable.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.CollectionService;
import de.digitalcollections.cudami.server.controller.legacy.V5MigrationHelper;
import de.digitalcollections.model.identifiable.entity.Collection;
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
@Tag(name = "Collection controller")
public class V2CollectionController {

  private final ObjectMapper objectMapper;
  private final CollectionService collectionService;

  public V2CollectionController(CollectionService collectionService, ObjectMapper objectMapper) {
    this.collectionService = collectionService;
    this.objectMapper = objectMapper;
  }

  @Operation(
      summary = "Get all collections",
      description = "Get a paged list of all collections",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description =
                "PageResponse&lt;Collection&gt; (<a href=\"https://raw.githubusercontent.com/dbmdz/digitalcollections-model/8.2.1/dc-model/src/main/java/de/digitalcollections/model/api/paging/PageResponse.java\">dc-model &lt; 9.0</a>)",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = String.class),
                    examples = {
                      @ExampleObject(
                          name = "List with one element",
                          value =
                              "{\n"
                                  + "  \"content\": [\n"
                                  + "    {\n"
                                  + "      \"objectType\": \"COLLECTION\",\n"
                                  + "      \"created\": \"2020-03-03T16:12:08.686626\",\n"
                                  + "      \"identifiers\": [],\n"
                                  + "      \"label\": {\n"
                                  + "        \"de\": \"100(0) Schlüsseldokumente - zur deutschen Geschichte im 20. Jahrhundert sowie zur russischen und sowjetischen Geschichte (1917-1991)\",\n"
                                  + "        \"en\": \"100(0) Key Documents on German History of the 20th Century and of the Russian and Soviet History (1917-1991)\"\n"
                                  + "      },\n"
                                  + "      \"lastModified\": \"2020-10-02T12:38:03.069771\",\n"
                                  + "      \"previewImageRenderingHints\": {\n"
                                  + "        \"altText\": {\n"
                                  + "          \"de\": \"100(0) Schlüsseldokumente - zur deutschen Geschichte im 20. Jahrhundert sowie zur russischen und sowjetischen Geschichte (1917-1991)\",\n"
                                  + "          \"en\": \"100(0) Key Documents on German History of the 20th Century and of the Russian and Soviet History (1917-1991)\"\n"
                                  + "        },\n"
                                  + "        \"openLinkInNewWindow\": true,\n"
                                  + "        \"title\": {\n"
                                  + "          \"de\": \"100(0) Schlüsseldokumente - zur deutschen Geschichte im 20. Jahrhundert sowie zur russischen und sowjetischen Geschichte (1917-1991)\",\n"
                                  + "          \"en\": \"100(0) Key Documents on German History of the 20th Century and of the Russian and Soviet History (1917-1991)\"\n"
                                  + "        }\n"
                                  + "      },\n"
                                  + "      \"type\": \"ENTITY\",\n"
                                  + "      \"uuid\": \"0b0b89e1-3f8a-4928-b8f3-67a8c4b3ff57\",\n"
                                  + "      \"entityType\": \"COLLECTION\",\n"
                                  + "      \"refId\": 14,\n"
                                  + "      \"publicationStart\": \"2021-01-01\",\n"
                                  + "      \"className\": \"de.digitalcollections.model.impl.identifiable.entity.CollectionImpl\"\n"
                                  + "    }\n"
                                  + "  ],\n"
                                  + "  \"pageRequest\": {\n"
                                  + "    \"pageNumber\": 0,\n"
                                  + "    \"pageSize\": 1,\n"
                                  + "    \"sorting\": {\n"
                                  + "      \"orders\": [\n"
                                  + "        {\n"
                                  + "          \"direction\": \"ASC\",\n"
                                  + "          \"ignoreCase\": false,\n"
                                  + "          \"nullHandling\": \"NATIVE\",\n"
                                  + "          \"property\": \"label\",\n"
                                  + "          \"subProperty\": \"en\",\n"
                                  + "          \"descending\": false,\n"
                                  + "          \"ascending\": true\n"
                                  + "        },\n"
                                  + "        {\n"
                                  + "          \"direction\": \"ASC\",\n"
                                  + "          \"ignoreCase\": false,\n"
                                  + "          \"nullHandling\": \"NATIVE\",\n"
                                  + "          \"property\": \"label\",\n"
                                  + "          \"subProperty\": \"\",\n"
                                  + "          \"descending\": false,\n"
                                  + "          \"ascending\": true\n"
                                  + "        }\n"
                                  + "      ]\n"
                                  + "    }\n"
                                  + "  },\n"
                                  + "  \"totalElements\": 137\n"
                                  + "}")
                    }))
      })
  @GetMapping(
      value = {"/v2/collections"},
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
              name = "active",
              description =
                  "return only active values; if unset, all values are returned, active and non active ones",
              example = "",
              schema = @Schema(type = "boolean"))
          @RequestParam(name = "active", required = false)
          String active)
      throws JsonProcessingException {
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize);
    if (sortBy != null) {
      List<Order> migratedSortBy = V5MigrationHelper.migrate(sortBy);
      Sorting sorting = new Sorting(migratedSortBy);
      pageRequest.setSorting(sorting);
    }

    PageResponse<Collection> response;

    if (active != null) {
      response = collectionService.findActive(pageRequest);
    } else {
      response = collectionService.find(pageRequest);
    }

    // Fix the attributes, which are missing or different in new model
    JSONObject result = new JSONObject(objectMapper.writeValueAsString(response));
    JSONArray websites = (JSONArray) result.get("content");
    for (Iterator it = websites.iterator(); it.hasNext(); ) {
      JSONObject website = (JSONObject) it.next();
      website.put(
          "className", "de.digitalcollections.model.impl.identifiable.entity.CollectionImpl");
    }

    return new ResponseEntity<>(result.toString(), HttpStatus.OK);
  }
}
