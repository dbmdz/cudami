package de.digitalcollections.cudami.server.controller.identifiable.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.ProjectService;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.jackson.DigitalCollectionsObjectMapper;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.Sorting;
import java.util.Iterator;
import java.util.UUID;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiPathParam;
import org.jsondoc.core.annotation.ApiResponseObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(description = "The V3 project controller", name = "Project controller version 3")
public class V3ProjectController {

  private final DigitalCollectionsObjectMapper objectMapper = new DigitalCollectionsObjectMapper();

  private final ProjectService projectService;

  public V3ProjectController(ProjectService projectService) {
    this.projectService = projectService;
  }

  @ApiMethod(description = "Get paged digital objects of a project")
  @GetMapping(
      value = {"/v3/projects/{uuid}/digitalobjects"},
      produces = "application/json")
  @ApiResponseObject
  public ResponseEntity<String> getDigitalObjects(
      @ApiPathParam(description = "UUID of the project") @PathVariable("uuid") UUID projectUuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize)
      throws JsonProcessingException {
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize, new Sorting());

    Project project = new Project();
    project.setUuid(projectUuid);
    PageResponse<DigitalObject> response = projectService.getDigitalObjects(project, pageRequest);

    // Fix the attributes, which are missing or different in new model
    JSONObject result = new JSONObject(objectMapper.writeValueAsString(response));
    JSONArray projects = (JSONArray) result.get("content");
    for (Iterator it = projects.iterator(); it.hasNext(); ) {
      JSONObject projectObject = (JSONObject) it.next();
      projectObject.put(
          "className", "de.digitalcollections.model.impl.identifiable.entity.DigitalObjectImpl");
    }

    return new ResponseEntity<>(result.toString(), HttpStatus.OK);
  }
}
