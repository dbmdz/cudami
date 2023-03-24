package de.digitalcollections.cudami.server.controller.identifiable.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.web.WebpageService;
import de.digitalcollections.cudami.server.controller.CudamiControllerException;
import de.digitalcollections.cudami.server.controller.legacy.V5MigrationHelper;
import de.digitalcollections.model.identifiable.web.Webpage;
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
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Webpage controller")
public class V3WebpageController {

  private final LocaleService localeService;
  private final WebpageService webpageService;

  private final ObjectMapper objectMapper;

  public V3WebpageController(
      LocaleService localeService, WebpageService webpageService, ObjectMapper objectMapper) {
    this.localeService = localeService;
    this.webpageService = webpageService;
    this.objectMapper = objectMapper;
  }

  @Operation(
      summary = "Get a webpage in JSON format",
      description =
          "Get a webpage in JSON format (version 3), depending on extension or <tt>format</tt> request parameter or accept header",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description =
                "Webpage (<a href=\"https://github.com/dbmdz/digitalcollections-model/raw/8.2.1/dc-model/src/main/java/de/digitalcollections/model/api/identifiable/entity/parts/Webpage.java\">dc-model &lt; 9.0</a>)",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = String.class),
                    examples = {
                      @ExampleObject(
                          name = "example list",
                          externalValue =
                              "https://github.com/dbmdz/cudami/raw/main/dc-cudami-server/dc-cudami-server-webapp/src/test/resources/de/digitalcollections/json/v3/webpages/8f95bd0a-7095-44e7-9ab3-061f288741aa.json")
                    }))
      })
  @GetMapping(
      value = {"/v3/webpages/{uuid}.json", "/v3/webpages/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> getByUuidV3Json(
      @Parameter(
              name = "uuid",
              description = "the UUID of the webpage",
              example = "599a120c-2dd5-11e8-b467-0ed5f89f718b",
              schema = @Schema(implementation = UUID.class))
          @PathVariable("uuid")
          UUID uuid,
      @Parameter(
              name = "pLocale",
              description =
                  "Desired locale in flattened form. If unset, contents in all languages will be returned",
              example = "de_DE",
              schema = @Schema(implementation = String.class))
          @RequestParam(name = "pLocale", required = false)
          Locale pLocale,
      @Parameter(
              name = "active",
              description = "Flag for only returning active webpages. If unset, returns all.",
              schema = @Schema(implementation = Boolean.class))
          @RequestParam(name = "active", required = false)
          String active)
      throws ServiceException, JsonProcessingException {
    Webpage webpage;
    if (active != null) {
      if (pLocale == null) {
        webpage = webpageService.getActive(uuid);
      } else {
        webpage = webpageService.getActive(uuid, pLocale);
      }
    } else {
      if (pLocale == null) {
        webpage = webpageService.getByUuid(uuid);
      } else {
        webpage = webpageService.getByUuidAndLocale(uuid, pLocale);
      }
    }

    if (webpage == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    JSONObject result = new JSONObject(objectMapper.writeValueAsString(webpage));
    result.put("type", "ENTITY_PART");
    result.put("entityPartType", "WEBPAGE");
    if (result.isNull("children")) {
      result.put("children", new JSONArray());
    }

    return new ResponseEntity<>(result.toString(), HttpStatus.OK);
  }

  @Operation(
      hidden = true,
      summary = "Get a webpage in XML format",
      description =
          "Get a webpage in XML format (version 3), depending on extension or <tt>format</tt> request parameter or accept header",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description =
                "Webpage (<a href=\"https://github.com/dbmdz/digitalcollections-model/raw/8.2.1/dc-model/src/main/java/de/digitalcollections/model/api/identifiable/entity/parts/Webpage.java\">dc-model &lt; 9.0</a>)",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_XML_VALUE,
                    schema = @Schema(implementation = String.class),
                    examples = {
                      @ExampleObject(
                          name = "example list",
                          externalValue =
                              "https://github.com/dbmdz/cudami/raw/main/dc-cudami-server/dc-cudami-server-webapp/src/test/resources/xml/v3/webpages/8f95bd0a-7095-44e7-9ab3-061f288741aa.xml")
                    }))
      })
  @GetMapping(
      value = {"/v3/webpages/{uuid}", "/v3/webpages/{uuid}.xml"},
      produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> getByUuidV3Xml(
      @Parameter(
              name = "uuid",
              description = "the UUID of the webpage",
              example = "599a120c-2dd5-11e8-b467-0ed5f89f718b",
              schema = @Schema(implementation = UUID.class))
          @PathVariable("uuid")
          UUID uuid,
      @Parameter(
              name = "pLocale",
              description =
                  "Desired locale in flattened form. If unset, contents in all languages will be returned",
              example = "de_DE",
              schema = @Schema(implementation = Locale.class))
          @RequestParam(name = "pLocale", required = false)
          Locale pLocale,
      @Parameter(
              name = "active",
              description = "Flag for only returning active webpages. If unset, returns all.",
              schema = @Schema(implementation = Boolean.class))
          @RequestParam(name = "active", required = false)
          String active)
      throws ServiceException, IOException {

    return new ResponseEntity<>(HttpStatus.UNSUPPORTED_MEDIA_TYPE);

    /*
    ResponseEntity<String> jsonResponse = getWebpageJson(uuid, pLocale, active);
    if (jsonResponse.getStatusCode() != HttpStatus.OK) {
      return jsonResponse;
    }

    //FIXME Unfortunately, this is not enough. The XML serialization is so much different due to
    // the new model, just fixing the two fields (done by the json method) is not enough
    JsonNode tree = objectMapper.readTree(jsonResponse.getBody());
    String xml = xmlMapper.writer().withRootName("webpage").writeValueAsString(tree);
    return new ResponseEntity<>(xml, HttpStatus.OK);
     */
  }

  @Operation(
      summary = "Get first level children of a webpage",
      description = "Get (active or all) paged children of a webpage as JSON",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description =
                "PageResponse&lt;Webpage&gt; (<a href=\"https://raw.githubusercontent.com/dbmdz/digitalcollections-model/8.2.1/dc-model/src/main/java/de/digitalcollections/model/api/paging/PageResponse.java\">dc-model &lt; 9.0</a>)",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = String.class),
                    examples = {
                      @ExampleObject(
                          name = "example list",
                          externalValue =
                              "https://github.com/dbmdz/cudami/raw/main/dc-cudami-server/dc-cudami-server-webapp/src/test/resources/json/v3/webpages/157f5428-5a5a-4d47-971e-f092f1836246_children.json")
                    }))
      })
  @GetMapping(
      value = {"/v3/webpages/{uuid}/children", "/latest/webpages/{uuid}/children"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> findChildren(
      @Parameter(
              name = "uuid",
              description = "the UUID of the webpage",
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
          List<Order> sortBy,
      @Parameter(
              name = "active",
              description = "Flag for only returning active webpages. If unset, returns all.",
              schema = @Schema(implementation = Boolean.class))
          @RequestParam(name = "active", required = false)
          String active)
      throws JsonProcessingException, CudamiControllerException {
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(V5MigrationHelper.migrate(sortBy));
      pageRequest.setSorting(sorting);
    }
    PageResponse<Webpage> pageResponse;

    if (active != null) {
      pageResponse = webpageService.findActiveChildren(uuid, pageRequest);
    } else {
      pageResponse = webpageService.findSubParts(uuid, pageRequest);
    }

    JSONObject result = new JSONObject(objectMapper.writeValueAsString(pageResponse));
    JSONArray contentSrc = result.getJSONArray("content");
    JSONArray contentDesc = new JSONArray();

    // Fix className, type and entityPartType for each element within content
    for (int i = 0; i < contentSrc.length(); i++) {
      JSONObject webpage = contentSrc.getJSONObject(i);
      webpage.put(
          "className", "de.digitalcollections.model.impl.identifiable.entity.parts.WebpageImpl");
      webpage.put("type", "ENTITY_PART");
      webpage.put("entityPartType", "WEBPAGE");
      contentDesc.put(i, webpage);
    }

    result.put("content", contentDesc);

    return new ResponseEntity<>(V5MigrationHelper.migrateToV5(result, objectMapper), HttpStatus.OK);
  }

  @Operation(
      summary = "Get all children of a webpage",
      description = "Get (active or all) children of a webpage recursivly as JSON",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "List&lt;Webpage&gt; (dc-model &lt; 9.0)",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = String.class),
                    examples = {
                      @ExampleObject(
                          name = "example list",
                          externalValue =
                              "https://github.com/dbmdz/cudami/raw/main/dc-cudami-server/dc-cudami-server-webapp/src/test/resources/json/v3/webpages/ead664b6-5fcc-414e-b3bb-133f0af1acb8_childrentree.json")
                    }))
      })
  @GetMapping(
      value = {"/v3/webpages/{uuid}/childrentree"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> getChildrenTree(
      @Parameter(
              name = "uuid",
              description = "the UUID of the root webpage",
              example = "599a120c-2dd5-11e8-b467-0ed5f89f718b",
              schema = @Schema(implementation = UUID.class))
          @PathVariable("uuid")
          UUID uuid,
      @Parameter(
              name = "active",
              description = "Flag for only returning active webpages. If unset, returns all.",
              schema = @Schema(implementation = Boolean.class))
          @RequestParam(name = "active", required = false)
          String active)
      throws JsonProcessingException {
    List<Webpage> childrenList;
    if (active != null) {
      childrenList = webpageService.getActiveChildrenTree(uuid);
    } else {
      childrenList = webpageService.getChildrenTree(uuid);
    }

    JSONArray resultList = new JSONArray(objectMapper.writeValueAsString(childrenList));
    resultList = fixWebpageChildren(resultList);
    return new ResponseEntity<>(resultList.toString(), HttpStatus.OK);
  }

  private JSONArray fixWebpageChildren(JSONArray webpages) {
    for (int i = 0; i < webpages.length(); i++) {
      JSONObject webpage = webpages.getJSONObject(i);
      webpage.put("type", "ENTITY_PART");
      webpage.put("entityPartType", "WEBPAGE");

      if (webpage.has("children")) {
        JSONArray children = webpage.getJSONArray("children");
        if (children != null && children.length() > 0) {
          children = fixWebpageChildren(children);
          webpage.put("children", children);
        }
      } else {
        webpage.put("children", List.of());
      }
    }
    return webpages;
  }
}
