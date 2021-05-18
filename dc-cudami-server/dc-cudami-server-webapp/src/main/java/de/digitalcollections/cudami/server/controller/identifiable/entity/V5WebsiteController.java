package de.digitalcollections.cudami.server.controller.identifiable.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.WebsiteService;
import de.digitalcollections.model.identifiable.entity.Website;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.Iterator;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class V5WebsiteController {

  private final ObjectMapper objectMapper;
  private final WebsiteService websiteService;

  public V5WebsiteController(WebsiteService websiteService, ObjectMapper objectMapper) {
    this.websiteService = websiteService;
    this.objectMapper = objectMapper;
  }

  @Operation(
      summary = "Get a website",
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
                          name = "website",
                          externalValue =
                              "https://github.com/dbmdz/cudami/raw/main/dc-cudami-server/dc-cudami-server-webapp/src/test/resources/json/v5/websites/7a2f1935-c5b8-40fb-8622-c675de0a6242.json")
                    }))
      })
  @GetMapping(
      value = {"/v5/websites/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> findById(
      @Parameter(
              name = "uuid",
              description = "the UUID of the website",
              example = "7a2f1935-c5b8-40fb-8622-c675de0a6242",
              schema = @Schema(implementation = UUID.class))
          @PathVariable
          UUID uuid)
      throws JsonProcessingException {
    Website website = websiteService.get(uuid);

    if (website == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
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
}
