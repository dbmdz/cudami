package de.digitalcollections.cudami.server.controller.v2.identifiable.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.web.WebpageService;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.jackson.DigitalCollectionsObjectMapper;
import java.util.Locale;
import java.util.UUID;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiPathParam;
import org.jsondoc.core.annotation.ApiQueryParam;
import org.jsondoc.core.annotation.ApiResponseObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(description = "The V2 webpage controller", name = "V2 Webpage controller")
public class V2WebpageController {

  private final DigitalCollectionsObjectMapper objectMapper = new DigitalCollectionsObjectMapper();

  private final WebpageService webpageService;

  public V2WebpageController(WebpageService webpageService) {
    this.webpageService = webpageService;
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

  @ApiMethod(description = "Get a webpage as JSON (Version 2)")
  @GetMapping(
      value = {"/v2/webpages/{uuid}.json", "/v2/webpages/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public ResponseEntity<String> getWebpageV2Json(
      @ApiPathParam(
              description =
                  "UUID of the webpage, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @ApiQueryParam(
              name = "pLocale",
              description =
                  "Desired locale, e.g. <tt>de_DE</tt>. If unset, contents in all languages will be returned")
          @RequestParam(name = "pLocale", required = false)
          Locale pLocale)
      throws IdentifiableServiceException, JsonProcessingException {
    Webpage webpage = loadWebpage(pLocale, uuid);
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
    return new ResponseEntity<>(result.toString(), HttpStatus.OK);
  }

  private Webpage loadWebpage(Locale pLocale, UUID uuid) throws IdentifiableServiceException {
    Webpage webpage;
    if (pLocale == null) {
      webpage = webpageService.get(uuid);
    } else {
      webpage = webpageService.get(uuid, pLocale);
    }
    return webpage;
  }
}
