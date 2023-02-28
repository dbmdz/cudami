package de.digitalcollections.cudami.server.controller.identifiable.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.ProjectService;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Sorting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Iterator;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Project controller")
public class V3ProjectController {

  private final ObjectMapper objectMapper;
  private final ProjectService projectService;

  public V3ProjectController(ProjectService projectService, ObjectMapper objectMapper) {
    this.projectService = projectService;
    this.objectMapper = objectMapper;
  }

  @Operation(
      summary = "Get digital objects of a project",
      description = "Get a paged list of digital objects of a project",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description =
                "PageResponse&lt;DigitalObject&gt; (<a href=\"https://raw.githubusercontent.com/dbmdz/digitalcollections-model/8.2.1/dc-model/src/main/java/de/digitalcollections/model/api/paging/PageResponse.java\">dc-model &lt; 9.0</a>)",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = String.class),
                    examples = {
                      @ExampleObject(
                          name = "example list",
                          externalValue =
                              "https://github.com/dbmdz/cudami/raw/main/dc-cudami-server/dc-cudami-server-webapp/src/test/resources/json/v3/projects/d0e3ce0f-f030-4c7f-8f78-5606173f1a11_digitalobjects.json")
                    }))
      })
  @GetMapping(
      value = {"/v3/projects/{uuid}/digitalobjects", "/latest/projects/{uuid}/digitalobjects"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> findDigitalObjects(
      @Parameter(
              name = "uuid",
              description = "the UUID of the collection",
              example = "d0e3ce0f-f030-4c7f-8f78-5606173f1a11",
              schema = @Schema(implementation = UUID.class))
          @PathVariable("uuid")
          UUID projectUuid,
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
          int pageSize)
      throws JsonProcessingException {
    PageRequest searchPageRequest = new PageRequest(null, pageNumber, pageSize, new Sorting());

    Project project = new Project();
    project.setUuid(projectUuid);
    PageResponse<DigitalObject> response =
        projectService.findDigitalObjects(project, searchPageRequest);

    // Fix the attributes, which are missing or different in new model
    JSONObject result = new JSONObject(objectMapper.writeValueAsString(response));
    JSONArray projects = (JSONArray) result.get("content");
    for (Iterator it = projects.iterator(); it.hasNext(); ) {
      JSONObject projectObject = (JSONObject) it.next();
      projectObject.put(
          "className", "de.digitalcollections.model.impl.identifiable.entity.DigitalObjectImpl");
      if (projectObject.isNull("fileResources"))
        projectObject.put("fileResources", new JSONArray());
    }

    String resultStr = result.toString();

    // TODO replace "query"
    return new ResponseEntity<>(resultStr, HttpStatus.OK);
  }
}
