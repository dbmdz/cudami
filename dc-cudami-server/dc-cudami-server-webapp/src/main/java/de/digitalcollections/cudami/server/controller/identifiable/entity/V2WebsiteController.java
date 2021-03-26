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
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * This controller is only responsible for the incompatible v2 endpoints, which differ from the
 * latest endpoint
 */
@RestController
@Api(description = "The website controller Version 2", name = "Website controller v2")
public class V2WebsiteController {

  private final DigitalCollectionsObjectMapper objectMapper = new DigitalCollectionsObjectMapper();

  private final WebsiteService websiteService;

  public V2WebsiteController(WebsiteService websiteService) {
    this.websiteService = websiteService;
  }

  @ApiMethod(description = "Get website by uuid")
  @GetMapping(
      value = {"/v2/websites/{uuid}", "/v2/websites/{uuid}.json"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public String findById(@PathVariable UUID uuid) throws JsonProcessingException {
    Website website = websiteService.get(uuid);
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
