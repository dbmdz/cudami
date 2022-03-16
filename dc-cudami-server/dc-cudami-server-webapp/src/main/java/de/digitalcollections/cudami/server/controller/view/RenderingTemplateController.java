package de.digitalcollections.cudami.server.controller.view;

import de.digitalcollections.cudami.server.business.api.service.view.RenderingTemplateService;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.Sorting;
import de.digitalcollections.model.view.RenderingTemplate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Rendering template controller")
public class RenderingTemplateController {

  private final RenderingTemplateService renderingTemplateService;

  public RenderingTemplateController(RenderingTemplateService renderingTemplateService) {
    this.renderingTemplateService = renderingTemplateService;
  }

  @Operation(summary = "Get all rendering templates")
  @GetMapping(
      value = {"/v5/renderingtemplates"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<RenderingTemplate> findAll(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy) {
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    return renderingTemplateService.find(pageRequest);
  }

  @Operation(summary = "Get rendering template by uuid")
  @GetMapping(
      value = {
        "/v5/renderingtemplates/{uuid}",
        "/v3/renderingtemplates/{uuid}",
        "/latest/renderingtemplates/{uuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public RenderingTemplate getByUuid(@PathVariable UUID uuid) {
    return renderingTemplateService.getByUuid(uuid);
  }

  @Operation(summary = "Save a newly created rendering template")
  @PostMapping(
      value = {"/v5/renderingtemplates", "/v3/renderingtemplates", "/latest/renderingtemplates"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public RenderingTemplate save(@RequestBody RenderingTemplate template, BindingResult errors) {
    return renderingTemplateService.save(template);
  }

  @Operation(summary = "Update a rendering template")
  @PutMapping(
      value = {
        "/v5/renderingtemplates/{uuid}",
        "/v3/renderingtemplates/{uuid}",
        "/latest/renderingtemplates/{uuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public RenderingTemplate update(
      @PathVariable UUID uuid, @RequestBody RenderingTemplate template, BindingResult errors) {
    assert Objects.equals(uuid, template.getUuid());
    return renderingTemplateService.update(template);
  }
}
