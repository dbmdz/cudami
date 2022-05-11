package de.digitalcollections.cudami.server.controller.identifiable.alias;

import com.github.openjson.JSONObject;
import de.digitalcollections.cudami.server.business.api.service.exceptions.CudamiServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.controller.CudamiControllerException;
import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
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
public class UrlAliasController {

  private final UrlAliasService urlAliasService;

  public UrlAliasController(UrlAliasService urlAliasService) {
    this.urlAliasService = urlAliasService;
  }

  @Operation(summary = "Create and persist an UrlAlias")
  @PostMapping(
      value = {"/v5/urlaliases"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<UrlAlias> create(@RequestBody UrlAlias urlAlias)
      throws CudamiControllerException {

    if (urlAlias == null || urlAlias.getUuid() != null) {
      return new ResponseEntity("UUID must not be set", HttpStatus.UNPROCESSABLE_ENTITY);
    }

    UrlAlias result;
    try {
      result = urlAliasService.save(urlAlias);
    } catch (CudamiServiceException e) {
      throw new CudamiControllerException(e);
    }

    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @Operation(summary = "Delete an UrlAlias by uuid")
  @DeleteMapping(
      value = {
        "/v5/urlaliases/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}"
      })
  public ResponseEntity<Void> delete(
      @Parameter(
              description =
                  "UUID of the urlalias, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid)
      throws CudamiControllerException {
    boolean isDeleted;
    try {
      isDeleted = urlAliasService.delete(uuid);
    } catch (CudamiServiceException e) {
      throw new CudamiControllerException(e);
    }

    if (!isDeleted) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @Operation(
      summary =
          "Find limited amounts of LocalizedUrlAliases. If the searchTerm is used, the slugs to be returned have to match the searchTerm")
  @GetMapping(
      value = {"/v6/urlaliases"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<PageResponse<LocalizedUrlAliases>> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "searchTerm", required = false) String searchTerm)
      throws CudamiControllerException {
    PageRequest pageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }

    PageResponse<LocalizedUrlAliases> result;
    try {
      result = urlAliasService.find(pageRequest);
    } catch (CudamiServiceException e) {
      throw new CudamiControllerException(e);
    }

    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @Operation(summary = "Get a slug for language and label and, if given, website_uuid")
  @GetMapping(
      value = {
        "/v5/urlaliases/slug/{pLocale}/{label}/{website_uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}",
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
      result = urlAliasService.generateSlug(pLocale, label, websiteUuid);
    } catch (CudamiServiceException e) {
      throw new CudamiControllerException(e);
    }

    if (result == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    return new ResponseEntity<>(JSONObject.quote(result), HttpStatus.OK);
  }

  @Operation(summary = "Get an UrlAlias by uuid")
  @GetMapping(
      value = {
        "/v5/urlaliases/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}"
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
      result = urlAliasService.getByUuid(uuid);
    } catch (CudamiServiceException e) {
      throw new CudamiControllerException(e);
    }

    if (result == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @Operation(
      summary =
          "Get the primary LocalizedUrlAliases for a given website uuid (null if empty) and slug, and optionally filtered by a locale")
  @GetMapping(
      value = {
        "/v5/urlaliases/primary/{slug}/{website_uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}",
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
      result = urlAliasService.getPrimaryUrlAliases(websiteUuid, slug, pLocale);
    } catch (CudamiServiceException e) {
      throw new CudamiControllerException(e);
    }

    if (result == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @Operation(summary = "update an UrlAlias")
  @PutMapping(
      value = {
        "/v5/urlaliases/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<UrlAlias> update(
      @Parameter(
              description =
                  "UUID of the urlalias, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @RequestBody UrlAlias urlAlias)
      throws CudamiControllerException {

    if (uuid == null || urlAlias == null || !uuid.equals(urlAlias.getUuid())) {
      return new ResponseEntity(
          "UUID=" + uuid + " not set or does not match UUID of provided resource",
          HttpStatus.UNPROCESSABLE_ENTITY);
    }

    UrlAlias result;
    try {
      result = urlAliasService.update(urlAlias);
    } catch (CudamiServiceException e) {
      throw new CudamiControllerException(e);
    }

    return new ResponseEntity<>(result, HttpStatus.OK);
  }
}
