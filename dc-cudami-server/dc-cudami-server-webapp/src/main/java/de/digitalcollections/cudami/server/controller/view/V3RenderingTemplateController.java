package de.digitalcollections.cudami.server.controller.view;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import de.digitalcollections.cudami.server.business.api.service.view.RenderingTemplateService;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.Sorting;
import de.digitalcollections.model.view.RenderingTemplate;
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
@Tag(name = "Rendering template controller")
public class V3RenderingTemplateController {

  private final ObjectMapper objectMapper;
  private final RenderingTemplateService renderingTemplateService;

  public V3RenderingTemplateController(
      RenderingTemplateService renderingTemplateService, ObjectMapper objectMapper) {
    this.renderingTemplateService = renderingTemplateService;
    this.objectMapper = objectMapper;
  }

  @Operation(
      summary = "Get all rendering templates",
      description = "Get a paged and sorted list of all rendering templates",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description =
                "PageResponse&lt;RenderingTemplate&gt; (<a href=\"https://raw.githubusercontent.com/dbmdz/digitalcollections-model/8.2.1/dc-model/src/main/java/de/digitalcollections/model/api/paging/PageResponse.java\">dc-model &lt; 9.0</a>)",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = String.class),
                    examples = {
                      @ExampleObject(
                          name = "example list",
                          externalValue =
                              "https://github.com/dbmdz/cudami/raw/main/dc-cudami-server/dc-cudami-server-webapp/src/test/resources/de/digitalcollections/json/v3/renderingtemplates.json")
                    }))
      })
  @GetMapping(
      value = {"/v3/renderingtemplates", "/latest/renderingtemplates"},
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
          List<Order> sortBy)
      throws JsonProcessingException {
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    PageResponse<RenderingTemplate> response = renderingTemplateService.find(pageRequest);

    JSONObject result = new JSONObject(objectMapper.writeValueAsString(response));
    JSONArray digitalobjects = (JSONArray) result.get("content");
    for (Iterator it = digitalobjects.iterator(); it.hasNext(); ) {
      JSONObject digitalobject = (JSONObject) it.next();
      digitalobject.put("className", "de.digitalcollections.model.impl.view.RenderingTemplate");
    }

    return new ResponseEntity<>(result.toString(), HttpStatus.OK);
  }
}
