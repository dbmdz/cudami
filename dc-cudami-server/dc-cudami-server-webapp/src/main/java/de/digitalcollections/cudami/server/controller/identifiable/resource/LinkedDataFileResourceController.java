package de.digitalcollections.cudami.server.controller.identifiable.resource;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.LinkedDataFileResourceService;
import de.digitalcollections.cudami.server.controller.ParameterHelper;
import de.digitalcollections.cudami.server.controller.identifiable.AbstractIdentifiableController;
import de.digitalcollections.model.identifiable.resource.LinkedDataFileResource;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.validation.ValidationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
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
@Tag(name = "LinkedData Fileresource controller")
public class LinkedDataFileResourceController
    extends AbstractIdentifiableController<LinkedDataFileResource> {

  private final LinkedDataFileResourceService service;

  public LinkedDataFileResourceController(LinkedDataFileResourceService service) {
    this.service = service;
  }

  @Operation(summary = "Get all linkedDataFileResources as (paged, sorted, filtered) list")
  @GetMapping(
      value = {"/v6/linkeddatafileresources", "/v6/linkeddatafileresources/search"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<LinkedDataFileResource> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "filter", required = false) List<FilterCriterion> filterCriteria,
      @RequestParam(name = "filtering", required = false) Filtering filtering)
      throws ServiceException {
    return super.find(pageNumber, pageSize, sortBy, filterCriteria, filtering);
  }

  @Operation(summary = "Get a linkedDataFileResource by uuid")
  @GetMapping(
      value = {
        "/v6/linkeddatafileresources/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/linkeddatafileresources/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<LinkedDataFileResource> getByUuid(
      @Parameter(
              example = "",
              description =
                  "UUID of the linkeDataFileResource, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
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
  protected IdentifiableService<LinkedDataFileResource> getService() {
    return service;
  }

  @Operation(summary = "Save a newly created linkedDataFileResource")
  @PostMapping(
      value = {"/v6/linkeddatafileresources", "/v5/linkeddatafileresources"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public LinkedDataFileResource save(
      @RequestBody LinkedDataFileResource linkedDataFileResource, BindingResult bindingResult)
      throws ServiceException, ValidationException {
    return super.save(linkedDataFileResource, bindingResult);
  }

  @Operation(summary = "Update a linkedDataFileResource")
  @PutMapping(
      value = {
        "/v6/linkeddatafileresources/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/linkeddatafileresources/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public LinkedDataFileResource update(
      @PathVariable UUID uuid,
      @RequestBody LinkedDataFileResource linkedDataFileResource,
      BindingResult errors)
      throws ServiceException, ValidationException {
    return super.update(uuid, linkedDataFileResource, errors);
  }
}
