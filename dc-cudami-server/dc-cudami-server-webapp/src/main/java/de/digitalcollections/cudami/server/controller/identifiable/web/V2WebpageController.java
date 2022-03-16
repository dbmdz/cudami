package de.digitalcollections.cudami.server.controller.identifiable.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.web.WebpageService;
import de.digitalcollections.model.identifiable.web.Webpage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
public class V2WebpageController {

  private final ObjectMapper objectMapper;
  private final WebpageService webpageService;

  public V2WebpageController(WebpageService webpageService, ObjectMapper objectMapper) {
    this.webpageService = webpageService;
    this.objectMapper = objectMapper;
  }

  private JSONObject convertLocalizedStructuredContentJson(JSONObject json) {
    JSONObject localizedStructuredContent = new JSONObject();
    localizedStructuredContent.put("localizedStructuredContent", json);
    return localizedStructuredContent;
  }

  private JSONObject convertLocalizedTextJson(JSONObject json) {
    JSONObject result = new JSONObject();
    JSONArray translations = new JSONArray();
    json.keySet()
        .forEach(
            (locale) -> {
              JSONObject translation = new JSONObject();
              translation.put("locale", locale);
              translation.put("text", json.get(locale));
              translations.put(translation);
            });
    result.put("translations", translations);
    return result;
  }

  @Operation(
      summary = "Get a webpage in JSON format",
      description = "Get a webpage in JSON format (version 2)",
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
                              "https://github.com/dbmdz/cudami/raw/main/dc-cudami-server/dc-cudami-server-webapp/src/test/resources/json/v2/webpages/8f95bd0a-7095-44e7-9ab3-061f288741aa.json")
                    }))
      })
  @GetMapping(
      value = {"/v2/webpages/{uuid}.json", "/v2/webpages/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> getWebpageV2Json(
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
          Locale pLocale)
      throws IdentifiableServiceException, JsonProcessingException {
    Webpage webpage = loadWebpage(pLocale, uuid);
    if (webpage == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    webpage.setCreated(null);
    webpage.setLastModified(null);
    JSONObject result = new JSONObject(objectMapper.writeValueAsString(webpage));
    if (result.has("description")) {
      result.put(
          "description",
          convertLocalizedStructuredContentJson(result.getJSONObject("description")));
    }
    if (result.has("label")) {
      result.put("label", convertLocalizedTextJson(result.getJSONObject("label")));
    }
    if (result.has("text")) {
      result.put("text", convertLocalizedStructuredContentJson(result.getJSONObject("text")));
    }
    result.put("type", "RESOURCE");
    result.put("entityPartType", "WEBPAGE");
    return new ResponseEntity<>(result.toString(), HttpStatus.OK);
  }

  private Webpage loadWebpage(Locale pLocale, UUID uuid) throws IdentifiableServiceException {
    Webpage webpage;
    if (pLocale == null) {
      webpage = webpageService.getByUuid(uuid);
    } else {
      webpage = webpageService.getByUuidAndLocale(uuid, pLocale);
    }
    return webpage;
  }
}
