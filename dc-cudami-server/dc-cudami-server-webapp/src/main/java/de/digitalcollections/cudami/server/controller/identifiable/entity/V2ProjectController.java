package de.digitalcollections.cudami.server.controller.identifiable.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.ProjectService;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import de.digitalcollections.model.paging.Sorting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Iterator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Project controller")
public class V2ProjectController {

  private final ObjectMapper objectMapper;
  private final ProjectService projectService;

  public V2ProjectController(ProjectService projectService, ObjectMapper objectMapper) {
    this.projectService = projectService;
    this.objectMapper = objectMapper;
  }

  @Operation(
      summary = "Get projects in reduced form (no identifiers)",
      description = "Get a paged and filtered list of projects in reduced form (no identifiers)",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description =
                "SearchPageResponse&lt;Collection&gt; (<a href=\"https://raw.githubusercontent.com/dbmdz/digitalcollections-model/8.2.1/dc-model/src/main/java/de/digitalcollections/model/api/paging/SearchPageResponse.java\">dc-model &lt; 9.0</a>)",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = String.class),
                    examples = {
                      @ExampleObject(
                          name = "empty list",
                          externalValue =
                              "https://github.com/dbmdz/cudami/raw/main/dc-cudami-server/dc-cudami-server-webapp/src/test/resources/json/v2/projects/projects.json")
                    }))
      })
  @GetMapping(
      value = {"/v2/projects", "/latest/projects"},
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
              name = "searchTerm",
              description = "the search term, of which the result is filtered (substring match)",
              example = "Druck",
              schema = @Schema(type = "string"))
          @RequestParam(name = "searchTerm", required = false)
          String searchTerm)
      throws JsonProcessingException {
    SearchPageRequest searchPageRequest = new SearchPageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      searchPageRequest.setSorting(sorting);
    }
    SearchPageResponse<Project> response = projectService.find(searchPageRequest);

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
