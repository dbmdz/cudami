package de.digitalcollections.cudami.server.controller.identifiable.entity.work;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.WorkService;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.identifiable.entity.work.Item;
import de.digitalcollections.model.identifiable.entity.work.Work;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
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
@Tag(name = "Work controller")
public class WorkController {

  private static final Logger LOGGER = LoggerFactory.getLogger(WorkController.class);

  private final WorkService workService;

  public WorkController(WorkService workService) {
    this.workService = workService;
  }

  @Operation(summary = "count all works")
  @GetMapping(
      value = {"/v6/works/count", "/v5/works/count", "/v2/works/count", "/latest/works/count"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public long count() {
    return workService.count();
  }

  @Operation(summary = "get all works")
  @GetMapping(
      value = {"/v6/works"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Work> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "language", required = false, defaultValue = "de") String language,
      @RequestParam(name = "searchTerm", required = false) String searchTerm,
      @RequestParam(name = "initial", required = false) String initial) {
    PageRequest pageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    if (initial == null) {
      return workService.find(pageRequest);
    }
    return workService.findByLanguageAndInitial(pageRequest, language, initial);
  }

  @Operation(summary = "Get a work by namespace and id")
  @GetMapping(
      value = {
        "/v6/works/identifier/{namespace}:{id}",
        "/v6/works/identifier/{namespace}:{id}.json",
        "/v5/works/identifier/{namespace}:{id}",
        "/v5/works/identifier/{namespace}:{id}.json",
        "/v2/works/identifier/{namespace}:{id}",
        "/v2/works/identifier/{namespace}:{id}.json",
        "/latest/works/identifier/{namespace}:{id}",
        "/latest/works/identifier/{namespace}:{id}.json"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Work> getByIdentifier(
      @PathVariable String namespace, @PathVariable String id) throws IdentifiableServiceException {
    Work result = workService.getByIdentifier(namespace, id);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @Operation(summary = "Get a work by namespace and id")
  @GetMapping(
      value = {
        "/v6/works/identifier",
        "/v5/works/identifier",
        "/v2/works/identifier",
        "/latest/works/identifier",
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> getByIdentifier(
      @RequestParam(name = "namespace", required = true) String namespace,
      @RequestParam(name = "id", required = true) String id,
      HttpServletRequest request)
      throws IdentifiableServiceException {
    URI newLocation =
        URI.create(request.getRequestURI().concat(String.format("/%s:%s", namespace, id)));
    return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY).location(newLocation).build();
  }

  @Operation(summary = "Get a work by uuid")
  @GetMapping(
      value = {
        "/v6/works/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}",
        "/v5/works/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}",
        "/v2/works/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}",
        "/latest/works/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Work> getByUuid(
      @Parameter(
              example = "",
              description = "UUID of the work, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @Parameter(
              name = "pLocale",
              description =
                  "Desired locale, e.g. <tt>de_DE</tt>. If unset, contents in all languages will be returned")
          @RequestParam(name = "pLocale", required = false)
          Locale pLocale)
      throws IdentifiableServiceException {

    Work result;
    if (pLocale == null) {
      result = workService.getByUuid(uuid);
    } else {
      result = workService.getByUuidAndLocale(uuid, pLocale);
    }
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @Operation(summary = "Get creators of a work")
  @GetMapping(
      value = {
        "/v6/works/{uuid}/creators",
        "/v5/works/{uuid}/creators",
        "/v2/works/{uuid}/creators",
        "/latest/works/{uuid}/creators"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Agent> getCreators(@PathVariable UUID uuid) {
    return workService.getCreators(uuid);
  }

  @Operation(summary = "Get items of a work")
  @GetMapping(
      value = {
        "/v6/works/{uuid}/items",
        "/v5/works/{uuid}/items",
        "/v2/works/{uuid}/items",
        "/latest/works/{uuid}/items"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Item> getItems(@PathVariable UUID uuid) {
    return workService.getItems(uuid);
  }

  @Operation(summary = "save a newly created work")
  @PostMapping(
      value = {"/v6/works", "/v5/works", "/v2/works", "/latest/works"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Work save(@RequestBody Work work, BindingResult errors)
      throws IdentifiableServiceException, ValidationException {
    return workService.save(work);
  }

  @Operation(summary = "update a work")
  @PutMapping(
      value = {"/v6/works/{uuid}", "/v5/works/{uuid}", "/v2/works/{uuid}", "/latest/works/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Work update(@PathVariable("uuid") UUID uuid, @RequestBody Work work, BindingResult errors)
      throws IdentifiableServiceException, ValidationException {
    if (uuid == null || work == null || !uuid.equals(work.getUuid())) {
      throw new IllegalArgumentException("UUID mismatch of new and existing work");
    }

    return workService.update(work);
  }
}
