package de.digitalcollections.cudami.server.controller.view;

import de.digitalcollections.cudami.server.business.api.service.UniqueObjectService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.view.RenderingTemplateService;
import de.digitalcollections.cudami.server.controller.AbstractUniqueObjectController;
import de.digitalcollections.cudami.server.controller.ParameterHelper;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.validation.ValidationException;
import de.digitalcollections.model.view.RenderingTemplate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
public class RenderingTemplateController extends AbstractUniqueObjectController<RenderingTemplate> {

  private final RenderingTemplateService service;

  public RenderingTemplateController(RenderingTemplateService renderingTemplateService) {
    this.service = renderingTemplateService;
  }

  @Operation(summary = "Get all rendering templates as (paged, sorted, filtered) list")
  @GetMapping(
      value = {"/v6/renderingtemplates", "/v5/renderingtemplates"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<RenderingTemplate> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "filter", required = false) List<FilterCriterion> filterCriteria,
      @RequestParam(name = "filtering", required = false) Filtering filtering)
      throws ServiceException {
    return super.find(pageNumber, pageSize, sortBy, filterCriteria, filtering);
  }

  @Operation(summary = "Get rendering template by uuid")
  @GetMapping(
      value = {
        "/v6/renderingtemplates/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/renderingtemplates/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v3/renderingtemplates/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/latest/renderingtemplates/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<RenderingTemplate> getByUuid(@PathVariable UUID uuid)
      throws ServiceException {
    return super.getByUuid(uuid);
  }

  @Operation(summary = "Get languages of all rendering templates")
  @GetMapping(
      value = {"/v6/renderingtemplates/languages"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Locale> getLanguages() throws ServiceException {
    return service.getLanguages();
  }

  @Override
  protected UniqueObjectService<RenderingTemplate> getService() {
    return service;
  }

  @Operation(summary = "Save a newly created rendering template")
  @PostMapping(
      value = {
        "/v6/renderingtemplates",
        "/v5/renderingtemplates",
        "/v3/renderingtemplates",
        "/latest/renderingtemplates"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public RenderingTemplate save(@RequestBody RenderingTemplate template, BindingResult errors)
      throws ValidationException, ServiceException {
    return super.save(template, errors);
  }

  @Operation(summary = "Update a rendering template")
  @PutMapping(
      value = {
        "/v6/renderingtemplates/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/renderingtemplates/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v3/renderingtemplates/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/latest/renderingtemplates/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public RenderingTemplate update(
      @PathVariable UUID uuid, @RequestBody RenderingTemplate template, BindingResult errors)
      throws ValidationException, ServiceException {
    return super.update(uuid, template, errors);
  }
}
