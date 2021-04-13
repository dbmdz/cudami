package de.digitalcollections.cudami.server.controller.identifiable.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.ProjectService;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.jackson.DigitalCollectionsObjectMapper;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import de.digitalcollections.model.paging.Sorting;
import java.util.Iterator;
import java.util.List;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiResponseObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(description = "The V2 project controller", name = "Project controller version 2")
public class V2ProjectController {

  private final DigitalCollectionsObjectMapper objectMapper = new DigitalCollectionsObjectMapper();

  private final ProjectService projectService;

  public V2ProjectController(ProjectService projectService) {
    this.projectService = projectService;
  }

  @ApiMethod(description = "Get all projects in reduced form (no identifiers)")
  @GetMapping(
      value = {"/v2/projects"},
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
    SearchPageResponse<Project> response = projectService.find(searchPageRequest);

    // Fix the attributes, which are missing or different in new model
    // Fix the attributes, which are missing or different in new model
    JSONObject result = new JSONObject(objectMapper.writeValueAsString(response));
    JSONArray projects = (JSONArray) result.get("content");
    for (Iterator it = projects.iterator(); it.hasNext(); ) {
      JSONObject project = (JSONObject) it.next();
      project.put("className", "de.digitalcollections.model.impl.identifiable.entity.ProjectImpl");
    }

    return new ResponseEntity<>(result.toString(), HttpStatus.OK);
  }
}
