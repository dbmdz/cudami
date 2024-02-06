package de.digitalcollections.cudami.server.controller.identifiable.entity.work;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.DigitalObjectService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.EntityService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.ItemService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.WorkService;
import de.digitalcollections.cudami.server.controller.AbstractEntityController;
import de.digitalcollections.cudami.server.controller.ParameterHelper;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.entity.item.Item;
import de.digitalcollections.model.identifiable.entity.work.Work;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import de.digitalcollections.model.validation.ValidationException;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Item controller")
public class ItemController extends AbstractEntityController<Item> {

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

    Item item = service.getByExample(buildExampleWithUuid(uuid));
    boolean successful =
        digitalObjectService.setItem(DigitalObject.builder().uuid(digitalObjectUuid).build(), item);
    return successful
        ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
        : new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @Override
  @Operation(summary = "count all items")
  @GetMapping(
      value = {"/v6/items/count", "/v5/items/count", "/v2/items/count", "/latest/items/count"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public long count() throws ServiceException {
    return super.count();
  }

  @Override
  @Operation(summary = "Delete an item")
  @DeleteMapping(
      value = {"/v6/items/{uuid:" + ParameterHelper.UUID_PATTERN + "}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity delete(
      @Parameter(example = "", description = "UUID of the item") @PathVariable("uuid") UUID uuid)
      throws ConflictException, ServiceException {
    return super.delete(uuid);
  }

  @Override
  @Operation(summary = "Get all items as (paged, sorted, filtered) list")
  @GetMapping(
      value = {"/v6/items"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Item> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "filter", required = false) List<FilterCriterion> filterCriteria,
      @RequestParam(name = "filtering", required = false) Filtering filtering)
      throws ServiceException {
    return super.find(pageNumber, pageSize, sortBy, filterCriteria, filtering);
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
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy)
      throws ServiceException {
    PageRequest pageRequest = new PageRequest(null, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    return digitalObjectService.findDigitalObjectsByItem(buildExampleWithUuid(uuid), pageRequest);
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
    if (pLocale == null) {
      return super.getByUuid(uuid);
    } else {
      return super.getByUuidAndLocale(uuid, pLocale);
    }
  }

  @Override
  @Operation(summary = "Get a list of items by their UUIDs")
  @GetMapping(
      value = {
        "/v6/items/list/{uuids}", // no REGEX possible here!
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Item> getByUuids(@PathVariable List<UUID> uuids) throws ServiceException {
    return super.getByUuids(uuids);
  }

  @Operation(summary = "Get a list of items objects by UUID")
  @PostMapping(
      value = {"/v6/items/list"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Item> getByManyUuids(@RequestBody List<UUID> uuids) throws ServiceException {
    return super.getByUuids(uuids);
  }

  @Override
  @Operation(
      summary = "Get languages of all items",
      description = "Get languages of all items",
      responses = {@ApiResponse(responseCode = "200", description = "List&lt;Locale&gt;")})
  @GetMapping(
      value = {"/v6/items/languages"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Locale> getLanguages() throws ServiceException {
    return super.getLanguages();
  }

  @Operation(
      summary = "Get languages of all items",
      description = "Get languages of all items",
      responses = {@ApiResponse(responseCode = "200", description = "List&lt;Locale&gt;")})
  @GetMapping(
      value = {"/v6/items/{uuid:" + ParameterHelper.UUID_PATTERN + "}/digitalobjects/languages"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Locale> getLanguagesOfDigitalObjects(
      @Parameter(name = "uuid", description = "UUID of the item") @PathVariable UUID uuid)
      throws ServiceException {
    return service.getLanguagesOfDigitalObjects(buildExampleWithUuid(uuid));
  }

  @Override
  protected EntityService<Item> getService() {
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
      @Parameter(name = "uuid", description = "UUID of the item") @PathVariable UUID uuid)
      throws ServiceException {
    return Set.of(workService.getByItem(buildExampleWithUuid(uuid)));
  }

  @Operation(
      summary = "Remove an existing parent item (attribute partOfItem) from an existing item")
  @DeleteMapping(
      value = {
        "/v6/items/{uuid:"
            + ParameterHelper.UUID_PATTERN
            + "}/parent/{parentItemUuid:"
            + ParameterHelper.UUID_PATTERN
            + "}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity removeParentItem(
      @Parameter(example = "", description = "UUID of the item") @PathVariable("uuid") UUID uuid,
      @Parameter(example = "", description = "UUID of the parent item")
          @PathVariable("parentItemUuid")
          UUID parentItemUuid)
      throws ServiceException {
    Item item = buildExampleWithUuid(uuid);
    Item parentItem = buildExampleWithUuid(parentItemUuid);

    boolean successful = service.clearPartOfItem(item, parentItem);
    return successful
        ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
        : new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @Operation(
      summary = "Remove the parent item relation(attribute partOfItem) from all of its children")
  @DeleteMapping(
      value = {"/v6/items/{parentItemUuid:" + ParameterHelper.UUID_PATTERN + "}/children/all"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity removeAllChildrenOfParentItem(
      @Parameter(example = "", description = "UUID of the parent item")
          @PathVariable("parentItemUuid")
          UUID parentItemUuid)
      throws ServiceException {
    Item parentItem = buildExampleWithUuid(parentItemUuid);

    boolean successful = service.removeParentItemChildren(parentItem);
    return successful
        ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
        : new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @Override
  @Operation(summary = "save a newly created item")
  @PostMapping(
      value = {"/v6/items", "/v5/items", "/v2/items", "/latest/items"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Item save(@RequestBody Item item, BindingResult errors)
      throws ServiceException, ValidationException {
    return super.save(item, errors);
  }

  @Override
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
    return super.update(uuid, item, errors);
  }
}
