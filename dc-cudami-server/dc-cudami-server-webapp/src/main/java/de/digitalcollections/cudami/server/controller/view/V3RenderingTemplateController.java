package de.digitalcollections.cudami.server.controller.view;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import de.digitalcollections.cudami.server.business.api.service.view.RenderingTemplateService;
import de.digitalcollections.model.jackson.DigitalCollectionsObjectMapper;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.Sorting;
import de.digitalcollections.model.view.RenderingTemplate;
import java.util.Iterator;
import java.util.List;
import org.jsondoc.core.annotation.Api;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(
    description = "The rendering template controller Version 3",
    name = "Rendering template controller v3")
public class V3RenderingTemplateController {

  private final DigitalCollectionsObjectMapper objectMapper = new DigitalCollectionsObjectMapper();
  private final RenderingTemplateService renderingTemplateService;

  public V3RenderingTemplateController(RenderingTemplateService renderingTemplateService) {
    this.renderingTemplateService = renderingTemplateService;
  }

  @GetMapping(
      value = {"/v3/renderingtemplates"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> findAll(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy)
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
