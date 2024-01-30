package de.digitalcollections.cudami.server.controller.identifiable.alias;

import com.github.openjson.JSONObject;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.controller.AbstractPagingAndSortingController;
import de.digitalcollections.cudami.server.controller.CudamiControllerException;
import de.digitalcollections.cudami.server.controller.ParameterHelper;
import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.validation.ValidationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "UrlAlias controller")
public class UrlAliasController extends AbstractPagingAndSortingController {

  private final UrlAliasService service;

  public UrlAliasController(UrlAliasService urlAliasService) {
    this.service = urlAliasService;
  }

  @Operation(summary = "Create and persist an UrlAlias")
  @PostMapping(
      value = {"/v6/urlaliases"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<UrlAlias> create(@RequestBody UrlAlias urlAlias)
      throws CudamiControllerException, ValidationException {

    if (urlAlias == null || urlAlias.getUuid() != null) {
      return new ResponseEntity("UUID must not be set", HttpStatus.UNPROCESSABLE_ENTITY);
    }

    try {
      service.save(urlAlias);
    } catch (ServiceException e) {
      throw new CudamiControllerException(e);
    }

    return new ResponseEntity<>(urlAlias, HttpStatus.OK);
  }

  @Operation(summary = "Delete an UrlAlias by uuid")
  @DeleteMapping(
      value = {
        "/v6/urlaliases/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/urlaliases/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
      })
  public ResponseEntity<Void> delete(
      @Parameter(
              description =
                  "UUID of the urlalias, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid)
      throws CudamiControllerException, ConflictException {
    boolean isDeleted;
    try {
      isDeleted = service.delete(UrlAlias.builder().uuid(uuid).build());
    } catch (ServiceException e) {
      throw new CudamiControllerException(e);
    }

    return isDeleted
        ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
        : new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Get all LocalizedUrlAliases as (paged, sorted, filtered) list")
  @GetMapping(
      value = {"/v6/urlaliases"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<PageResponse<LocalizedUrlAliases>> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "filter", required = false) List<FilterCriterion> filterCriteria,
      @RequestParam(name = "filtering", required = false) Filtering filtering)
      throws CudamiControllerException {
    PageRequest pageRequest =
        createPageRequest(
            LocalizedUrlAliases.class, pageNumber, pageSize, sortBy, filterCriteria, filtering);
    PageResponse<LocalizedUrlAliases> result;
    try {
      result = service.findLocalizedUrlAliases(pageRequest);
    } catch (ServiceException e) {
      throw new CudamiControllerException(e);
    }

    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @Operation(summary = "Get a slug for language and label and, if given, website_uuid")
  @GetMapping(
      value = {
        "/v6/urlaliases/slug/{pLocale}/{label}/{website_uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v6/urlaliases/slug/{pLocale}/{label}",
        "/v5/urlaliases/slug/{pLocale}/{label}/{website_uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/urlaliases/slug/{pLocale}/{label}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> generateSlug(
      @Parameter(name = "pLocale", description = "Desired locale, e.g. <tt>de_DE</tt>.")
          @PathVariable(name = "pLocale")
          Locale pLocale,
      @Parameter(
              name = "label",
              description =
                  "The label, from which the slug shall be constructed, e.g. <tt>Impressum</tt>")
          @PathVariable("label")
          String label,
      @Parameter(
              description =
                  "UUID of the website (or not provided, if the default website shall be used), e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>",
              required = false)
          @PathVariable(value = "website_uuid", required = false)
          UUID websiteUuid)
      throws CudamiControllerException {

    String result;
    try {
      Website website = null;
      if (websiteUuid != null) {
        website = Website.builder().uuid(websiteUuid).build();
      }
      result = service.generateSlug(pLocale, label, website);
    } catch (ServiceException e) {
      throw new CudamiControllerException(e);
    }

    return new ResponseEntity<>(
        JSONObject.quote(result), result != null ? HttpStatus.OK : HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Get an UrlAlias by uuid")
  @GetMapping(
      value = {
        "/v6/urlaliases/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/urlaliases/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<UrlAlias> getByUuid(
      @Parameter(
              description =
                  "UUID of the urlalias, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid)
      throws CudamiControllerException {

    UrlAlias result;
    try {
      result = service.getByExample(UrlAlias.builder().uuid(uuid).build());
    } catch (ServiceException e) {
      throw new CudamiControllerException(e);
    }

    return new ResponseEntity<>(result, result != null ? HttpStatus.OK : HttpStatus.NOT_FOUND);
  }

  @Operation(
      summary =
          "Get the primary LocalizedUrlAliases for a given website uuid (null if empty) and slug, and optionally filtered by a locale")
  @GetMapping(
      value = {
        "/v6/urlaliases/primary/{slug}/{website_uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v6/urlaliases/primary/{slug}",
        "/v5/urlaliases/primary/{slug}/{website_uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/urlaliases/primary/{slug}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<LocalizedUrlAliases> getPrimaryUrlAliases(
      @Parameter(description = "the slug of the URL, e.g. <tt>imprint</tt>") @PathVariable("slug")
          String slug,
      @Parameter(
              description =
                  "UUID of the website if given (otherwise not set), e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable(value = "website_uuid", required = false)
          UUID websiteUuid,
      @Parameter(
              name = "pLocale",
              description =
                  "Desired language locale in flattened form. If unset, contents in all languages will be returned",
              example = "de",
              schema = @Schema(implementation = Locale.class))
          @RequestParam(name = "pLocale", required = false)
          Locale pLocale)
      throws CudamiControllerException {
    LocalizedUrlAliases result;
    try {
      Website website = null;
      if (websiteUuid != null) {
        website = Website.builder().uuid(websiteUuid).build();
      }
      result = service.getPrimaryUrlAliases(website, slug, pLocale);
    } catch (ServiceException e) {
      throw new CudamiControllerException(e);
    }

    return new ResponseEntity<>(result, result != null ? HttpStatus.OK : HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "update an UrlAlias")
  @PutMapping(
      value = {"/v6/urlaliases/{uuid:" + ParameterHelper.UUID_PATTERN + "}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<UrlAlias> update(
      @Parameter(
              description =
                  "UUID of the urlalias, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @RequestBody UrlAlias urlAlias)
      throws CudamiControllerException, ValidationException {

    if (uuid == null || urlAlias == null || !uuid.equals(urlAlias.getUuid())) {
      return new ResponseEntity(
          "UUID=" + uuid + " not set or does not match UUID of provided resource",
          HttpStatus.UNPROCESSABLE_ENTITY);
    }

    UrlAlias result;
    try {
      service.update(UrlAlias.builder().uuid(uuid).build());
    } catch (ServiceException e) {
      throw new CudamiControllerException(e);
    }

    return new ResponseEntity<>(urlAlias, HttpStatus.OK);
  }
}
