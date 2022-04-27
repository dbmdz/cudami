package de.digitalcollections.cudami.server.controller.identifiable.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.DigitalObjectService;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.Sorting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Iterator;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Digital object controller")
public class V3DigitalObjectController {

  private final ObjectMapper objectMapper;
  private final DigitalObjectService digitalObjectService;

  public V3DigitalObjectController(
      DigitalObjectService digitalObjectService, ObjectMapper objectMapper) {
    this.digitalObjectService = digitalObjectService;
    this.objectMapper = objectMapper;
  }

  @Operation(
      summary = "Get collections for a digital object",
      description = "Get (active) paged collections of a digital objects",
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
                          name = "example list",
                          externalValue =
                              "https://github.com/dbmdz/cudami/raw/main/dc-cudami-server/dc-cudami-server-webapp/src/test/resources/json/v3/digitalobjects/6bfbe6dc-2c14-4e61-b88b-ce56cea712c7_collections.json")
                    }))
      })
  @GetMapping(
      value = {
        "/v3/digitalobjects/{uuid}/collections",
        "/latest/digitalobjects/{uuid}/collections"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> findCollections(
      @Parameter(
              name = "uuid",
              description = "the UUID of the digital object",
              example = "a014a33b-6803-4b17-a876-a8f68758f2a7",
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
              name = "active",
              description = "if set to true, only active collections are returned.",
              example = "true",
              schema = @Schema(type = "boolean"))
          @RequestParam(name = "active", required = false)
          String active)
      throws JsonProcessingException {
    SearchPageRequest searchPageRequest =
        new SearchPageRequest(null, pageNumber, pageSize, new Sorting());

    DigitalObject digitalObject = new DigitalObject();
    digitalObject.setUuid(uuid);
    PageResponse<Collection> response;
    if (active != null) {
      response = digitalObjectService.getActiveCollections(digitalObject, searchPageRequest);
    } else {
      response = digitalObjectService.getCollections(digitalObject, searchPageRequest);
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

  @Operation(
      summary = "Get projects for a digital object",
      description = "Get (active) paged projects of a digital objects",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description =
                "PageResponse&lt;Project&gt; (<a href=\"https://raw.githubusercontent.com/dbmdz/digitalcollections-model/8.2.1/dc-model/src/main/java/de/digitalcollections/model/api/paging/PageResponse.java\">dc-model &lt; 9.0</a>)",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = String.class),
                    examples = {
                      @ExampleObject(
                          name = "example list",
                          externalValue =
                              "https://github.com/dbmdz/cudami/raw/main/dc-cudami-server/dc-cudami-server-webapp/src/test/resources/json/v3/digitalobjects/6bfbe6dc-2c14-4e61-b88b-ce56cea712c7_projects.json")
                    }))
      })
  @GetMapping(
      value = {"/v3/digitalobjects/{uuid}/projects", "/latest/digitalobjects/{uuid}/projects"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> findProjects(
      @Parameter(
              name = "uuid",
              description = "the UUID of the digital object",
              example = "a014a33b-6803-4b17-a876-a8f68758f2a7",
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
          int pageSize)
      throws JsonProcessingException {
    SearchPageRequest searchPageRequest =
        new SearchPageRequest(null, pageNumber, pageSize, new Sorting());

    DigitalObject digitalObject = new DigitalObject();
    digitalObject.setUuid(uuid);
    PageResponse<Project> response =
        digitalObjectService.getProjects(digitalObject, searchPageRequest);

    // Fix the attributes, which are missing or different in new model
    JSONObject result = new JSONObject(objectMapper.writeValueAsString(response));
    JSONArray websites = (JSONArray) result.get("content");
    for (Iterator it = websites.iterator(); it.hasNext(); ) {
      JSONObject website = (JSONObject) it.next();
      website.put("className", "de.digitalcollections.model.impl.identifiable.entity.ProjectImpl");
    }

    return new ResponseEntity<>(result.toString(), HttpStatus.OK);
  }
}
