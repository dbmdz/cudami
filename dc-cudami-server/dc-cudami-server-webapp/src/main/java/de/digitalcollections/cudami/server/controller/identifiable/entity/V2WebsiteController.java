package de.digitalcollections.cudami.server.controller.identifiable.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.openjson.JSONObject;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.WebsiteService;
import de.digitalcollections.cudami.server.controller.AbstractLegacyController;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import de.digitalcollections.model.paging.Sorting;
import java.util.List;
import java.util.UUID;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiResponseObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * This controller is only responsible for the incompatible v2 endpoints, which differ from the
 * latest endpoint
 */
@RestController
@Api(description = "The website controller Version 2", name = "Website controller v2")
public class V2WebsiteController extends AbstractLegacyController {

  private final WebsiteService websiteService;

  public V2WebsiteController(WebsiteService websiteService) {
    this.websiteService = websiteService;
  }

  @ApiMethod(description = "Get all websites")
  @GetMapping(
      value = {"/v2/websites"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public ResponseEntity<String> findAll(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "searchTerm", required = false) String searchTerm)
      throws JsonProcessingException {
    SearchPageRequest searchPageRequest = new SearchPageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      searchPageRequest.setSorting(sorting);
    }
    SearchPageResponse response = websiteService.find(searchPageRequest);
    if (response == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    return new ResponseEntity<>(fixPageResponse(response), HttpStatus.OK);

    /*
    // Fix the attributes, which are missing or different in new model
    JSONObject result = new JSONObject(objectMapper.writeValueAsString(response));
    JSONArray websites = (JSONArray) result.get("content");
    for (Iterator it = websites.iterator(); it.hasNext(); ) {
      JSONObject website = (JSONObject) it.next();
      website.put("className", "de.digitalcollections.model.impl.identifiable.entity.WebsiteImpl");
    }

    return new ResponseEntity<>(result.toString(), HttpStatus.OK);

     */
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

    return fixEmbeddedObject(new JSONObject(objectMapper.writeValueAsString(website))).toString();

    /*
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

     */
  }
}
