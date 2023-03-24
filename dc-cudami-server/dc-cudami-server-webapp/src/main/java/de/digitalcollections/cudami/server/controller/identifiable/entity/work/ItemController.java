package de.digitalcollections.cudami.server.controller.identifiable.entity.work;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.DigitalObjectService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.ItemService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.WorkService;
import de.digitalcollections.cudami.server.controller.ParameterHelper;
import de.digitalcollections.cudami.server.controller.identifiable.AbstractIdentifiableController;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.entity.item.Item;
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
import java.util.Set;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Item controller")
public class ItemController extends AbstractIdentifiableController<Item> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ItemController.class);

  private final DigitalObjectService digitalObjectService;
  private final ItemService service;
  private final WorkService workService;

  public ItemController(
      DigitalObjectService digitalObjectService, ItemService itemService, WorkService workService) {
    this.digitalObjectService = digitalObjectService;
    this.service = itemService;
    this.workService = workService;
  }

  @Operation(summary = "Add digital object to an item")
  @PostMapping(
      value = {
        "/v6/items/{uuid:" + ParameterHelper.UUID_PATTERN + "}/digitalobjects/{digitalObjectUuid}",
        "/v2/items/{uuid:" + ParameterHelper.UUID_PATTERN + "}/digitalobjects/{digitalObjectUuid}",
        "/latest/items/{uuid:"
            + ParameterHelper.UUID_PATTERN
            + "}/digitalobjects/{digitalObjectUuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity addDigitalObject(
      @Parameter(name = "uuid", description = "UUID of the item") @PathVariable UUID uuid,
      @Parameter(name = "digitalObjectUuid", description = "UUID of the digital object")
          @PathVariable
          UUID digitalObjectUuid)
      throws ValidationException, ConflictException, ServiceException {

    Item item = service.getByUuid(uuid);
    boolean successful = digitalObjectService.addItemToDigitalObject(item, digitalObjectUuid);
    return successful
        ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
        : new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "count all items")
  @GetMapping(
      value = {"/v6/items/count", "/v5/items/count", "/v2/items/count", "/latest/items/count"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public long count() {
    return service.count();
  }

  @Operation(summary = "Delete an item")
  @DeleteMapping(
      value = {"/v6/items/{uuid:" + ParameterHelper.UUID_PATTERN + "}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity delete(
      @Parameter(example = "", description = "UUID of the item") @PathVariable("uuid") UUID uuid)
      throws ConflictException {
    boolean successful;
    try {
      successful = service.deleteByUuid(uuid);
    } catch (ServiceException e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
    return successful
        ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
        : new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Get all items as (paged, sorted, filtered) list")
  @GetMapping(
      value = {"/v6/items"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Item> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "filter", required = false) List<FilterCriterion> filterCriteria) {
    PageRequest pageRequest =
        createPageRequest(Item.class, pageNumber, pageSize, sortBy, filterCriteria);
    return service.find(pageRequest);
  }

  @Operation(summary = "Get paged list of digital objects of this item")
  @GetMapping(
      value = {
        "/v6/items/{uuid:" + ParameterHelper.UUID_PATTERN + "}/digitalobjects",
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<DigitalObject> findDigitalObjects(
      @Parameter(name = "uuid", description = "UUID of the item") @PathVariable UUID uuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy) {
    PageRequest pageRequest = new PageRequest(null, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    return service.findDigitalObjects(uuid, pageRequest);
  }

  @Operation(
      summary = "Get an item by namespace and id",
      description =
          "Separate namespace and id with a colon, e.g. foo:bar. It is also possible, to add a .json suffix, which will be ignored then")
  @GetMapping(
      value = {
        "/v6/items/identifier/**",
        "/v5/items/identifier/**",
        "/v2/items/identifier/**",
        "/latest/items/identifier/**"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Override
  public ResponseEntity<Item> getByIdentifier(HttpServletRequest request)
      throws ServiceException, ValidationException {
    return super.getByIdentifier(request);
  }

  @Operation(summary = "Get an item by namespace and id")
  @GetMapping(
      value = {
        "/v6/items/identifier",
        "/v5/items/identifier",
        "/v2/items/identifier",
        "/latest/items/identifier"
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

  @Operation(summary = "Get an item by uuid")
  @GetMapping(
      value = {"/v6/items/{uuid:" + ParameterHelper.UUID_PATTERN + "}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Item> getByUuid(
      @Parameter(
              name = "uuid",
              description = "UUID of the item, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @Parameter(
              name = "pLocale",
              description =
                  "Desired locale, e.g. <tt>de_DE</tt>. If unset, contents in all languages will be returned")
          @RequestParam(name = "pLocale", required = false)
          Locale pLocale)
      throws ServiceException {

    Item result;
    if (pLocale == null) {
      result = service.getByUuid(uuid);
    } else {
      result = service.getByUuidAndLocale(uuid, pLocale);
    }
    return new ResponseEntity<>(result, result != null ? HttpStatus.OK : HttpStatus.NOT_FOUND);
  }

  @Operation(
      summary = "Get languages of all items",
      description = "Get languages of all items",
      responses = {@ApiResponse(responseCode = "200", description = "List&lt;Locale&gt;")})
  @GetMapping(
      value = {"/v6/items/languages"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Locale> getLanguages() {
    return service.getLanguages();
  }

  @Operation(
      summary = "Get languages of all items",
      description = "Get languages of all items",
      responses = {@ApiResponse(responseCode = "200", description = "List&lt;Locale&gt;")})
  @GetMapping(
      value = {"/v6/items/{uuid:" + ParameterHelper.UUID_PATTERN + "}/digitalobjects/languages"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Locale> getLanguagesOfDigitalObjects(
      @Parameter(name = "uuid", description = "UUID of the item") @PathVariable UUID uuid) {
    return service.getLanguagesOfDigitalObjects(uuid);
  }

  @Override
  protected IdentifiableService<Item> getService() {
    return service;
  }

  @Operation(summary = "Get the work embodied in an item")
  @GetMapping(
      value = {
        "/v6/items/{uuid:" + ParameterHelper.UUID_PATTERN + "}/works",
        "/v2/items/{uuid:" + ParameterHelper.UUID_PATTERN + "}/works",
        "/latest/items/{uuid:" + ParameterHelper.UUID_PATTERN + "}/works"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Set<Work> getWorks(
      @Parameter(name = "uuid", description = "UUID of the item") @PathVariable UUID uuid) {
    return Set.of(workService.getForItem(uuid));
  }

  @Operation(summary = "save a newly created item")
  @PostMapping(
      value = {"/v6/items", "/v5/items", "/v2/items", "/latest/items"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Item save(@RequestBody Item item, BindingResult errors)
      throws ServiceException, ValidationException {
    service.save(item);
    return item;
  }

  @Operation(summary = "update an item")
  @PutMapping(
      value = {
        "/v6/items/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/items/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v2/items/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/latest/items/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Item update(@PathVariable("uuid") UUID uuid, @RequestBody Item item, BindingResult errors)
      throws ServiceException, ValidationException {
    if (uuid == null || item == null || !uuid.equals(item.getUuid())) {
      throw new IllegalArgumentException("UUID mismatch of new and existing item");
    }

    service.update(item);
    return item;
  }
}
