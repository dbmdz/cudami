package de.digitalcollections.cudami.server.controller.identifiable.entity.semantic;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.semantic.SubjectService;
import de.digitalcollections.model.identifiable.entity.semantic.Subject;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
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
@Tag(name = "Subject controller")
public class SubjectController {

  private final SubjectService service;

  public SubjectController(SubjectService service) {
    this.service = service;
  }

  @Operation(summary = "Get all subjects")
  @GetMapping(
      value = {"/v6/expressiontypes"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Subject> find(
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

  @Operation(summary = "Get subject by UUID")
  @GetMapping(
      value = {"/v6/expressiontypes/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Subject getByUuid(@PathVariable UUID uuid) {
    return service.getByUuid(uuid);
  }

  @Operation(summary = "Save a newly created subject")
  @PostMapping(
      value = {"/v6/expressiontypes"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Subject save(@RequestBody Subject subject, BindingResult errors) {
    return service.save(subject);
  }

  @Operation(summary = "Update a subject")
  @PutMapping(
      value = {"/v6/expressiontypes/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Subject update(
      @PathVariable UUID uuid, @RequestBody Subject subject, BindingResult errors) {
    assert Objects.equals(uuid, subject.getUuid());
    return service.update(subject);
  }
}
