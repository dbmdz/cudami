package de.digitalcollections.cudami.server.controller.identifiable.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.CollectionService;
import de.digitalcollections.cudami.server.controller.legacy.V5MigrationHelper;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
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

@RestController
@Tag(name = "Collection controller")
public class V3CollectionController {

  private final ObjectMapper objectMapper;
  private final CollectionService collectionService;
  private final LocaleService localeService;

  public V3CollectionController(
      CollectionService collectionService, LocaleService localeService, ObjectMapper objectMapper) {
    this.collectionService = collectionService;
    this.localeService = localeService;
    this.objectMapper = objectMapper;
  }

  @Operation(
      summary = "Get digital objects of a collection",
      description = "Get a paged and filtered list of digital objects of a collection",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description =
                "SearchPageResponse&lt;DigitalObject&gt; (<a href=\"https://raw.githubusercontent.com/dbmdz/digitalcollections-model/8.2.1/dc-model/src/main/java/de/digitalcollections/model/api/paging/SearchPageResponse.java\">dc-model &lt; 9.0</a>)",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = String.class),
                    examples = {
                      @ExampleObject(
                          name = "example list",
                          externalValue =
                              "https://github.com/dbmdz/cudami/raw/main/dc-cudami-server/dc-cudami-server-webapp/src/test/resources/json/v3/collections/a014a33b-6803-4b17-a876-a8f68758f2a7_digitalobjects.json")
                    }))
      })
  @GetMapping(
      value = {
        "/v3/collections/{uuid}/digitalobjects",
        "/latest/collections/{uuid}/digitalobjects"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> findDigitalObjects(
      @Parameter(
              name = "uuid",
              description = "the UUID of the collection",
              example = "a014a33b-6803-4b17-a876-a8f68758f2a7",
              schema = @Schema(implementation = UUID.class))
          @PathVariable("uuid")
          UUID collectionUuid,
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
              name = "searchTerm",
              description = "the search term, of which the result is filtered (substring match)",
              example = "Test",
              schema = @Schema(type = "string"))
          @RequestParam(name = "searchTerm", required = false)
          String searchTerm)
      throws JsonProcessingException {
    PageRequest searchPageRequest =
        new PageRequest(searchTerm, pageNumber, pageSize, new Sorting());

    Collection collection = new Collection();
    collection.setUuid(collectionUuid);
    PageResponse<DigitalObject> response =
        collectionService.findDigitalObjects(collection, searchPageRequest);

    // Fix the attributes, which are missing or different in new model
    JSONObject result =
        fixPageResponse(
            response, "de.digitalcollections.model.impl.identifiable.entity.DigitalObjectImpl");

    String resultStr = result.toString();

    // TODO replace "query"
    return new ResponseEntity<>(resultStr, HttpStatus.OK);
  }

  @Operation(
      summary = "Get subcollections of a collection",
      description =
          "Get a paged and filtered after 'active' state list of subcollections of a collection",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description =
                "PageResponse&lt;Collection&gt; (<a href=\"https://raw.githubusercontent.com/dbmdz/digitalcollections-model/8.2.1/dc-model/src/main/java/de/digitalcollections/model/api/paging/SearchPageResponse.java\">dc-model &lt; 9.0</a>)",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = String.class),
                    examples = {
                      @ExampleObject(
                          name = "example list",
                          externalValue =
                              "https://github.com/dbmdz/cudami/raw/main/dc-cudami-server/dc-cudami-server-webapp/src/test/resources/json/v3/collections/collectionlist.json")
                    }))
      })
  @GetMapping(
      value = {
        "/v3/collections/{uuid}/subcollections",
        "/latest/collections/{uuid}/subcollections"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> findSubcollections(
      @Parameter(
              name = "uuid",
              description = "the UUID of the collection",
              example = "a014a33b-6803-4b17-a876-a8f68758f2a7",
              schema = @Schema(implementation = UUID.class))
          @PathVariable("uuid")
          UUID collectionUuid,
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
              description = "the set to true, only active subcollections are returned.",
              example = "true",
              schema = @Schema(type = "boolean"))
          @RequestParam(name = "active", required = false)
          String active)
      throws JsonProcessingException {
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize);

    PageResponse<Collection> response;

    if (active != null) {
      response = collectionService.findActiveChildren(collectionUuid, pageRequest);
    } else {
      response = collectionService.findChildren(collectionUuid, pageRequest);
    }
    JSONObject result = fixPageResponse(response);

    return new ResponseEntity<>(result.toString(), HttpStatus.OK);
  }

  @Operation(
      summary = "Get collections",
      description = "Get a paged and filtered after 'active' state list of collections",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description =
                "PageResponse&lt;Collection&gt; (<a href=\"https://raw.githubusercontent.com/dbmdz/digitalcollections-model/8.2.1/dc-model/src/main/java/de/digitalcollections/model/api/paging/SearchPageResponse.java\">dc-model &lt; 9.0</a>)",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = String.class),
                    examples = {
                      @ExampleObject(
                          name = "example list",
                          externalValue =
                              "https://github.com/dbmdz/cudami/raw/main/dc-cudami-server/dc-cudami-server-webapp/src/test/resources/json/v3/collections/collectionlist.json")
                    }))
      })
  @GetMapping(
      value = {"/v3/collections", "/latest/collections"},
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
              description = "the set to true, only active subcollections are returned.",
              example = "true",
              schema = @Schema(type = "boolean"))
          @RequestParam(name = "active", required = false)
          String active)
      throws JsonProcessingException {
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(V5MigrationHelper.migrate(sortBy));
      pageRequest.setSorting(sorting);
    }
    PageResponse<Collection> response;
    if (active != null) {
      response = collectionService.findActive(pageRequest);
    } else {
      response = collectionService.find(pageRequest);
    }

    // Fix the attributes, which are missing or different in new model
    JSONObject result = fixPageResponse(response);
    return new ResponseEntity<>(result.toString(), HttpStatus.OK);
  }

  @Operation(
      summary =
          "Find limited amount of (active or all) collections containing searchTerm in label or description")
  @GetMapping(
      value = {"/v3/collections/search", "/latest/collections/search"},
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
          @RequestParam(name = "pageSize", required = false, defaultValue = "5")
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
          String searchTerm,
      @Parameter(
              name = "active",
              description = "the set to true, only active subcollections are returned.",
              example = "true",
              schema = @Schema(type = "boolean"))
          @RequestParam(name = "active", required = false)
          String active)
      throws JsonProcessingException {
    PageRequest pageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(V5MigrationHelper.migrate(sortBy));
      pageRequest.setSorting(sorting);
    }
    PageResponse<Collection> response;
    if (active != null) {
      response = collectionService.findActive(pageRequest);
    } else {
      response = collectionService.find(pageRequest);
    }

    // Fix the attributes, which are missing or different in new model
    JSONObject result = fixPageResponse(response);

    String resultStr = result.toString();

    // TODO replace "query"
    return new ResponseEntity<>(resultStr, HttpStatus.OK);
  }

  private JSONObject fixPageResponse(
      PageResponse<? extends Entity> response, String expectedClassName)
      throws JsonProcessingException {
    // Fix the attributes, which are missing or different in new model
    JSONObject result = new JSONObject(objectMapper.writeValueAsString(response));
    JSONArray digitalobjects = (JSONArray) result.get("content");
    for (Iterator it = digitalobjects.iterator(); it.hasNext(); ) {
      JSONObject digitalobject = (JSONObject) it.next();
      digitalobject.put("className", expectedClassName);
    }
    return result;
  }

  private JSONObject fixPageResponse(PageResponse<? extends Entity> response)
      throws JsonProcessingException {
    return fixPageResponse(
        response, "de.digitalcollections.model.impl.identifiable.entity.CollectionImpl");
  }
}
