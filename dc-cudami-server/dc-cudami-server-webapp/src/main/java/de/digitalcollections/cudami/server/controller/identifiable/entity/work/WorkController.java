package de.digitalcollections.cudami.server.controller.identifiable.entity.work;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.agent.AgentService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.ItemService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.ManifestationService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.WorkService;
import de.digitalcollections.cudami.server.controller.ParameterHelper;
import de.digitalcollections.cudami.server.controller.identifiable.AbstractIdentifiableController;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.identifiable.entity.item.Item;
import de.digitalcollections.model.identifiable.entity.manifestation.Manifestation;
import de.digitalcollections.model.identifiable.entity.work.Work;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
public class WorkController extends AbstractIdentifiableController<Work> {

  private static final Logger LOGGER = LoggerFactory.getLogger(WorkController.class);

  private final AgentService agentService;
  private final ItemService itemService;
  private final ManifestationService manifestationService;
  private final WorkService service;

  public WorkController(
      WorkService workService,
      ItemService itemService,
      AgentService agentService,
      ManifestationService manifestationService) {
    this.service = workService;
    this.itemService = itemService;
    this.agentService = agentService;
    this.manifestationService = manifestationService;
  }

  @Operation(summary = "count all works")
  @GetMapping(
      value = {"/v6/works/count", "/v5/works/count", "/v2/works/count", "/latest/works/count"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public long count() {
    return service.count();
  }

  @Operation(summary = "Get all works as (paged, sorted, filtered) list")
  @GetMapping(
      value = {"/v6/works"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Work> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "filter", required = false) List<FilterCriterion> filterCriteria) {
    PageRequest pageRequest =
        createPageRequest(Work.class, pageNumber, pageSize, sortBy, filterCriteria);
    return service.find(pageRequest);
  }

  @Operation(summary = "Find all children of a work")
  @GetMapping(
      value = {"/v6/works/{uuid:" + ParameterHelper.UUID_PATTERN + "}/children"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Work> findChildren(
      @Parameter(example = "", description = "UUID of the work") @PathVariable("uuid") UUID uuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy) {
    PageRequest pageRequest = new PageRequest(null, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    return service.findEmbedded(uuid, pageRequest);
  }

  @Operation(summary = "Find all manifestations of a work")
  @GetMapping(
      value = {"/v6/works/{uuid:" + ParameterHelper.UUID_PATTERN + "}/manifestations"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Manifestation> findManifestations(
      @Parameter(example = "", description = "UUID of the work") @PathVariable("uuid") UUID uuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy)
      throws ServiceException {
    PageRequest pageRequest = new PageRequest(null, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    return manifestationService.findManifestationsByWork(uuid, pageRequest);
  }

  @Override
  @Operation(
      summary = "Get a work by namespace and id",
      description =
          "Separate namespace and id with a colon, e.g. foo:bar. It is also possible, to add a .json suffix, which will be ignored then")
  @GetMapping(
      value = {
        "/v6/works/identifier/**",
        "/v5/works/identifier/**",
        "/v2/works/identifier/**",
        "/latest/works/identifier/**"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Work> getByIdentifier(HttpServletRequest request)
      throws ServiceException, ValidationException {
    return super.getByIdentifier(request);
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
      throws ServiceException {
    URI newLocation =
        URI.create(request.getRequestURI().concat(String.format("/%s:%s", namespace, id)));
    return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY).location(newLocation).build();
  }

  @Operation(summary = "Get a work by uuid")
  @GetMapping(
      value = {
        "/v6/works/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/works/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v2/works/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/latest/works/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
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
      throws ServiceException {

    Work result;
    if (pLocale == null) {
      result = service.getByUuid(uuid);
    } else {
      result = service.getByUuidAndLocale(uuid, pLocale);
    }
    return new ResponseEntity<>(result, result != null ? HttpStatus.OK : HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Get creators of a work")
  @GetMapping(
      value = {
        "/v6/works/{uuid:" + ParameterHelper.UUID_PATTERN + "}/creators",
        "/v5/works/{uuid:" + ParameterHelper.UUID_PATTERN + "}/creators",
        "/v2/works/{uuid:" + ParameterHelper.UUID_PATTERN + "}/creators",
        "/latest/works/{uuid:" + ParameterHelper.UUID_PATTERN + "}/creators"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Agent> getCreators(@PathVariable UUID uuid) {
    return agentService.getCreatorsForWork(uuid);
  }

  @Operation(summary = "Get items of a work")
  @GetMapping(
      value = {
        "/v6/works/{uuid:" + ParameterHelper.UUID_PATTERN + "}/items",
        "/v5/works/{uuid:" + ParameterHelper.UUID_PATTERN + "}/items",
        "/v2/works/{uuid:" + ParameterHelper.UUID_PATTERN + "}/items",
        "/latest/works/{uuid:" + ParameterHelper.UUID_PATTERN + "}/items"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Item> getItems(@PathVariable UUID uuid) {
    return itemService.getItemsForWork(uuid);
  }

  @Operation(
      summary = "Get languages of all works",
      description = "Get languages of all works",
      responses = {@ApiResponse(responseCode = "200", description = "List&lt;Locale&gt;")})
  @GetMapping(
      value = {"/v6/works/languages"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Locale> getLanguages() {
    return service.getLanguages();
  }

  @Operation(
      summary = "Get languages of all manifestations",
      description = "Get languages of all manifestations",
      responses = {@ApiResponse(responseCode = "200", description = "List&lt;Locale&gt;")})
  @GetMapping(
      value = {"/v6/works/{uuid:" + ParameterHelper.UUID_PATTERN + "}/manifestations/languages"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Locale> getLanguagesOfManifestations(
      @Parameter(name = "uuid", description = "UUID of the work") @PathVariable UUID uuid) {
    return manifestationService.getLanguagesOfManifestationsForWork(uuid);
  }

  @Override
  protected IdentifiableService<Work> getService() {
    return service;
  }

  @Operation(summary = "save a newly created work")
  @PostMapping(
      value = {"/v6/works", "/v5/works", "/v2/works", "/latest/works"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Work save(@RequestBody Work work, BindingResult errors)
      throws ServiceException, ValidationException {
    service.save(work);
    return work;
  }

  @Operation(summary = "update a work")
  @PutMapping(
      value = {
        "/v6/works/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/works/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v2/works/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/latest/works/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Work update(@PathVariable("uuid") UUID uuid, @RequestBody Work work, BindingResult errors)
      throws ServiceException, ValidationException {
    if (uuid == null || work == null || !uuid.equals(work.getUuid())) {
      throw new IllegalArgumentException("UUID mismatch of new and existing work");
    }

    service.update(work);
    return work;
  }
}
