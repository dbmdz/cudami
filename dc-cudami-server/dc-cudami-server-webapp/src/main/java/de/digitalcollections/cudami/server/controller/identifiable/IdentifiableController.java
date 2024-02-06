package de.digitalcollections.cudami.server.controller.identifiable;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.controller.CudamiControllerException;
import de.digitalcollections.cudami.server.controller.ParameterHelper;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.validation.ValidationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Identifiable controller")
public class IdentifiableController extends AbstractIdentifiableController<Identifiable> {

  private final IdentifiableService<Identifiable> service;
  private final UrlAliasService urlAliasService;

  public IdentifiableController(
      @Qualifier("identifiableService") IdentifiableService identifiableService,
      UrlAliasService urlAliasService) {
    this.service = identifiableService;
    this.urlAliasService = urlAliasService;
  }

  @Override
  @Operation(summary = "Get a list of identifiables by UUID")
  @GetMapping(
      value = {
        "/v6/identifiables/list/{uuids}", // no REGEX possible here!
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Identifiable> getByUuids(@PathVariable List<UUID> uuids)
      throws ServiceException {
    return super.getByUuids(uuids);
  }

  @Operation(summary = "Get a list of identifiables by UUID")
  @PostMapping(
      value = {"/v6/identifiables/list"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Identifiable> getByManyUuids(@RequestBody List<UUID> uuids)
      throws ServiceException {
    return super.getByUuids(uuids);
  }

  @Override
  @Operation(summary = "Get all identifiables as (paged, sorted, filtered) list")
  @GetMapping(
      value = {
        "/v6/identifiables",
        "/v5/identifiables",
        "/v2/identifiables",
        "/latest/identifiables"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Identifiable> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "filter", required = false) List<FilterCriterion> filterCriteria,
      @RequestParam(name = "filtering", required = false) Filtering filtering)
      throws ServiceException {
    return super.find(pageNumber, pageSize, sortBy, filterCriteria, filtering);
  }

  @Operation(
      summary = "Get an identifiable by namespace and id",
      description =
          "Separate namespace and id with a colon, e.g. foo:bar. It is also possible, to add a .json suffix, which will be ignored then")
  @GetMapping(
      value = {
        "/v6/identifiables/identifier/**",
        "/v5/identifiables/identifier/**",
        "/v2/identifiables/identifier/**",
        "/latest/identifiables/identifier/**"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Override
  public ResponseEntity<Identifiable> getByIdentifier(HttpServletRequest request)
      throws ServiceException, ValidationException {
    return super.getByIdentifier(request);
  }

  @Override
  @Operation(summary = "Get identifiable by uuid")
  @GetMapping(
      value = {
        "/v6/identifiables/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/identifiables/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v2/identifiables/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/latest/identifiables/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Identifiable> getByUuid(@PathVariable UUID uuid) throws ServiceException {
    return super.getByUuid(uuid);
  }

  @Operation(summary = "Get the LocalizedUrlAliases for an identifiable by its UUID")
  @GetMapping(
      value = {
        "/v6/identifiables/{uuid:" + ParameterHelper.UUID_PATTERN + "}/localizedUrlAliases",
        "/v5/identifiables/{uuid:" + ParameterHelper.UUID_PATTERN + "}/localizedUrlAliases"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<LocalizedUrlAliases> getLocalizedUrlAliases(
      @Parameter(
              description =
                  "UUID of the urlalias, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid)
      throws CudamiControllerException {

    try {
      if (service.getByExamples(List.of(Identifiable.builder().uuid(uuid).build())).isEmpty()) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
    } catch (Exception e) {
      throw new CudamiControllerException(e);
    }

    try {
      return new ResponseEntity<>(
          urlAliasService.getByIdentifiable(Identifiable.builder().uuid(uuid).build()),
          HttpStatus.OK);
    } catch (ServiceException e) {
      throw new CudamiControllerException(e);
    }
  }

  @Override
  protected IdentifiableService<Identifiable> getService() {
    return service;
  }
}
