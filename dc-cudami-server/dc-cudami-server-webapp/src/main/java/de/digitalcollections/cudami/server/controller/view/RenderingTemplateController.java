package de.digitalcollections.cudami.server.controller.view;

import de.digitalcollections.cudami.server.business.api.service.view.RenderingTemplateService;
import de.digitalcollections.model.api.paging.Order;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.Sorting;
import de.digitalcollections.model.impl.paging.PageRequestImpl;
import de.digitalcollections.model.impl.paging.SortingImpl;
import de.digitalcollections.model.impl.view.RenderingTemplate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
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
public class RenderingTemplateController {

  private final RenderingTemplateService service;

  public RenderingTemplateController(RenderingTemplateService service) {
    this.service = service;
  }

  @GetMapping(
      value = {"/latest/renderingtemplates", "/v3/renderingtemplates"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<RenderingTemplate> findAll(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy) {
    PageRequest pageRequest = new PageRequestImpl(pageNumber, pageSize, null);
    if (sortBy != null) {
      Sorting sorting =
          new SortingImpl(sortBy.stream().filter(Objects::nonNull).collect(Collectors.toList()));
      pageRequest.setSorting(sorting);
    }
    return service.find(pageRequest);
  }

  @GetMapping(
      value = {"/latest/renderingtemplates/{uuid}", "/v3/renderingtemplates/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public RenderingTemplate findOne(@PathVariable UUID uuid) {
    return service.findOne(uuid);
  }

  @PostMapping(
      value = {"/latest/renderingtemplates", "/v3/renderingtemplates"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public RenderingTemplate save(@RequestBody RenderingTemplate template, BindingResult errors) {
    return service.save(template);
  }

  @PutMapping(
      value = {"/latest/renderingtemplates/{uuid}", "/v3/renderingtemplates/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public RenderingTemplate update(
      @PathVariable UUID uuid, @RequestBody RenderingTemplate template, BindingResult errors) {
    assert Objects.equals(uuid, template.getUuid());
    return service.update(template);
  }
}
