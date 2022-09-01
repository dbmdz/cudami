package de.digitalcollections.cudami.server.controller.semantic;

import de.digitalcollections.cudami.server.business.api.service.semantic.TagService;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import de.digitalcollections.model.semantic.Tag;
import io.swagger.v3.oas.annotations.Operation;
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
@io.swagger.v3.oas.annotations.tags.Tag(name = "Tag controller")
public class TagController {

  private final TagService service;

  public TagController(TagService service) {
    this.service = service;
  }

  @Operation(summary = "Get all tags")
  @GetMapping(
      value = {"/v6/tags"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Tag> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "searchTerm", required = false) String searchTerm) {
    PageRequest pageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    return service.find(pageRequest);
  }

  @Operation(summary = "Get tag by UUID")
  @GetMapping(
      value = {"/v6/tags/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Tag getByUuid(@PathVariable UUID uuid) {
    return service.getByUuid(uuid);
  }

  @Operation(summary = "Save a newly created tag")
  @PostMapping(
      value = {"/v6/tags"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Tag save(@RequestBody Tag tag, BindingResult errors) {
    return service.save(tag);
  }

  @Operation(summary = "Update a tag")
  @PutMapping(
      value = {"/v6/tags/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Tag update(@PathVariable UUID uuid, @RequestBody Tag tag, BindingResult errors) {
    assert Objects.equals(uuid, tag.getUuid());
    return service.update(tag);
  }
}
