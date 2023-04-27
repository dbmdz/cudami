package de.digitalcollections.cudami.server.controller.identifiable.entity.work;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.EntityService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.ItemService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.ManifestationService;
import de.digitalcollections.cudami.server.controller.AbstractEntityController;
import de.digitalcollections.cudami.server.controller.ParameterHelper;
import de.digitalcollections.model.identifiable.entity.item.Item;
import de.digitalcollections.model.identifiable.entity.manifestation.Manifestation;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Manifestation controller")
public class ManifestationController extends AbstractEntityController<Manifestation> {

  private ItemService itemService;
  private ManifestationService service;

  public ManifestationController(
      ManifestationService manifestationService, ItemService itemService) {
    service = manifestationService;
    this.itemService = itemService;
  }

  @Operation(summary = "Count all manifestations")
  @GetMapping(
      value = {"/v6/manifestations/count"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public long count() throws ServiceException {
    return super.count();
  }

  @Operation(summary = "Delete a manifestation")
  @DeleteMapping(
      value = {"/v6/manifestations/{uuid:" + ParameterHelper.UUID_PATTERN + "}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity delete(
      @Parameter(example = "", description = "UUID of the manifestation") @PathVariable("uuid")
          UUID uuid)
      throws ConflictException, ServiceException {
    return super.delete(uuid);
  }

  @Operation(summary = "Get all manifestations as (paged, sorted, filtered) list")
  @GetMapping(
      value = {"/v6/manifestations"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Manifestation> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "filter", required = false) List<FilterCriterion> filterCriteria)
      throws ServiceException {
    return super.find(pageNumber, pageSize, sortBy, filterCriteria);
  }

  @Operation(summary = "Find all items of a manifestation")
  @GetMapping(
      value = {"/v6/manifestations/{uuid:" + ParameterHelper.UUID_PATTERN + "}/items"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Item> findItems(
      @Parameter(example = "", description = "UUID of the manifestation") @PathVariable("uuid")
          UUID uuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy)
      throws ServiceException {
    PageRequest pageRequest = new PageRequest(null, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    return itemService.findItemsByManifestation(buildExampleWithUuid(uuid), pageRequest);
  }

  @Operation(summary = "Find all children of a manifestation")
  @GetMapping(
      value = {"/v6/manifestations/{uuid:" + ParameterHelper.UUID_PATTERN + "}/children"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Manifestation> findSubcollections(
      @Parameter(example = "", description = "UUID of the manifestation") @PathVariable("uuid")
          UUID uuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy)
      throws ServiceException {
    PageRequest pageRequest = new PageRequest(null, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    return service.findSubParts(buildExampleWithUuid(uuid), pageRequest);
  }

  @Operation(
      summary = "Get a manifestation by namespace and id",
      description =
          "Separate namespace and id with a colon, e.g. foo:bar. It is also possible, to add a .json suffix, which will be ignored then")
  @GetMapping(
      value = {"/v6/manifestations/identifier/**"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Override
  public ResponseEntity<Manifestation> getByIdentifier(HttpServletRequest request)
      throws ServiceException, ValidationException {
    return super.getByIdentifier(request);
  }

  @Operation(summary = "Get a manifestation by uuid")
  @GetMapping(
      value = {"/v6/manifestations/{uuid:" + ParameterHelper.UUID_PATTERN + "}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Manifestation> getByUuid(
      @Parameter(
              name = "uuid",
              description =
                  "UUID of the manifestation, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable
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

  @Operation(summary = "Get languages of all manifestations")
  @GetMapping(
      value = {"/v6/manifestations/languages"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Locale> getLanguages() throws ServiceException {
    return super.getLanguages();
  }

  @Operation(
      summary = "Get languages of all items",
      description = "Get languages of all items",
      responses = {@ApiResponse(responseCode = "200", description = "List&lt;Locale&gt;")})
  @GetMapping(
      value = {"/v6/manifestations/{uuid:" + ParameterHelper.UUID_PATTERN + "}/items/languages"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Locale> getLanguagesOfItems(
      @Parameter(name = "uuid", description = "UUID of the manifestation") @PathVariable UUID uuid)
      throws ServiceException {
    return itemService.getLanguagesOfItemsForManifestation(buildExampleWithUuid(uuid));
  }

  @Override
  protected EntityService<Manifestation> getService() {
    return service;
  }

  @Operation(summary = "Save a newly created manifestation")
  @PostMapping(
      value = {"/v6/manifestations"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Manifestation save(@RequestBody Manifestation manifestation, BindingResult errors)
      throws ServiceException, ValidationException {
    return super.save(manifestation, errors);
  }

  @Operation(summary = "update an manifestation")
  @PutMapping(
      value = {"/v6/manifestations/{uuid:" + ParameterHelper.UUID_PATTERN + "}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Manifestation update(
      @PathVariable UUID uuid, @RequestBody Manifestation manifestation, BindingResult errors)
      throws ServiceException, ValidationException {
    return super.update(uuid, manifestation, errors);
  }
}
