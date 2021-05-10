package de.digitalcollections.cudami.server.controller.identifiable.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.WebsiteService;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.jackson.DigitalCollectionsObjectMapper;
import java.util.Iterator;
import java.util.UUID;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiResponseObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(description = "The website controller Version 5", name = "Website controller v5")
public class V5WebsiteController {

  private final DigitalCollectionsObjectMapper objectMapper = new DigitalCollectionsObjectMapper();

  private final WebsiteService websiteService;

  public V5WebsiteController(WebsiteService websiteService) {
    this.websiteService = websiteService;
  }

  @ApiMethod(description = "Get website by uuid")
  @GetMapping(
      value = {"/v5/websites/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public ResponseEntity<String> findById(@PathVariable UUID uuid) throws JsonProcessingException {
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
