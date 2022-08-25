package de.digitalcollections.cudami.server.controller.identifiable.entity.work;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.PublicationService;
import de.digitalcollections.model.identifiable.entity.work.Publication;
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
@Tag(name = "Publication Controller")
public class PublicationController {

  private final PublicationService service;

  public PublicationController(PublicationService service) {
    this.service = service;
  }

  @Operation(summary = "Get all publications")
  @GetMapping(
      value = {"/v6/publications"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Publication> find(
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

  @Operation(summary = "Get publication by UUID")
  @GetMapping(
      value = {"/v6/publications/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Publication getByUuid(@PathVariable UUID uuid) {
    return service.getByUuid(uuid);
  }

  @Operation(summary = "Save a newly created publication")
  @PostMapping(
      value = {"/v6/publications"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Publication save(@RequestBody Publication publication, BindingResult errors) {
    return service.save(publication);
  }

  @Operation(summary = "Update a publication")
  @PutMapping(
      value = {"/v6/publications/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Publication update(
      @PathVariable UUID uuid, @RequestBody Publication publication, BindingResult errors) {
    assert Objects.equals(uuid, publication.getUuid());
    return service.update(publication);
  }
}
