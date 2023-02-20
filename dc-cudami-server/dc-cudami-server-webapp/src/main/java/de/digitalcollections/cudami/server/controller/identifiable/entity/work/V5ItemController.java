package de.digitalcollections.cudami.server.controller.identifiable.entity.work;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.ItemService;
import de.digitalcollections.cudami.server.controller.CudamiControllerException;
import de.digitalcollections.cudami.server.controller.ParameterHelper;
import de.digitalcollections.cudami.server.controller.legacy.V5MigrationHelper;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.entity.item.Item;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Item controller")
public class V5ItemController {

  private final ItemService itemService;
  private final ObjectMapper objectMapper;

  public V5ItemController(ItemService itemService, ObjectMapper objectMapper) {
    this.itemService = itemService;
    this.objectMapper = objectMapper;
  }

  @Operation(summary = "get all items")
  @GetMapping(
      value = {"/v5/items", "/v2/items", "/latest/items"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "language", required = false, defaultValue = "de") String language,
      @RequestParam(name = "searchTerm", required = false) String searchTerm,
      @RequestParam(name = "initial", required = false) String initial)
      throws CudamiControllerException {
    PageRequest pageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }

    PageResponse<Item> pageResponse;
    if (initial == null) {
      pageResponse = itemService.find(pageRequest);
    } else {
      pageResponse = itemService.findByLanguageAndInitial(pageRequest, language, initial);
    }

    try {
      String result = V5MigrationHelper.migrate(pageResponse, objectMapper);
      return new ResponseEntity<>(result, HttpStatus.OK);
    } catch (JsonProcessingException e) {
      throw new CudamiControllerException(e);
    }
  }

  @Operation(summary = "Get an item by uuid")
  @GetMapping(
      value = {
        "/v5/items/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v2/items/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/latest/items/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
      },
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
      result = itemService.getByUuid(uuid);
    } else {
      result = itemService.getByUuidAndLocale(uuid, pLocale);
    }
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @Operation(summary = "Get digital objects of this item")
  @GetMapping(
      value = {
        "/v2/items/{uuid:" + ParameterHelper.UUID_PATTERN + "}/digitalobjects",
        "/latest/items/{uuid:" + ParameterHelper.UUID_PATTERN + "}/digitalobjects"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Set<DigitalObject> getDigitalObjects(
      @Parameter(name = "uuid", description = "UUID of the item") @PathVariable UUID uuid) {
    return new HashSet<>(
        itemService
            .findDigitalObjects(
                uuid, PageRequest.builder().pageNumber(0).pageSize(Integer.MAX_VALUE).build())
            .getContent());
  }
}
