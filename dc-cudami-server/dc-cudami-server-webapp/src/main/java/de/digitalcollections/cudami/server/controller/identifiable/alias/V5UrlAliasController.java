package de.digitalcollections.cudami.server.controller.identifiable.alias;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.controller.CudamiControllerException;
import de.digitalcollections.cudami.server.controller.ParameterHelper;
import de.digitalcollections.cudami.server.controller.legacy.V5MigrationHelper;
import de.digitalcollections.cudami.server.controller.legacy.model.LegacyPageRequest;
import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import de.digitalcollections.model.validation.ValidationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "UrlAlias controller")
public class V5UrlAliasController {

  private final UrlAliasService urlAliasService;

  private final ObjectMapper objectMapper;

  public V5UrlAliasController(UrlAliasService urlAliasService, ObjectMapper objectMapper) {
    this.urlAliasService = urlAliasService;
    this.objectMapper = objectMapper;
  }

  @Operation(summary = "Create and persist an UrlAlias")
  @PostMapping(
      value = {"/v5/urlaliases"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<UrlAlias> create(@RequestBody V5UrlAlias urlAlias)
      throws CudamiControllerException, ValidationException {
    if (urlAlias == null || urlAlias.getUuid() != null) {
      return new ResponseEntity("UUID must not be set", HttpStatus.UNPROCESSABLE_ENTITY);
    }

    try {
      urlAliasService.save(urlAlias);
    } catch (ServiceException e) {
      throw new CudamiControllerException(e);
    }

    return new ResponseEntity<>(urlAlias, HttpStatus.OK);
  }

  @Operation(
      summary =
          "Find limited amounts of LocalizedUrlAliases. If the searchTerm is used, the slugs to be returned have to match the searchTerm")
  @GetMapping(
      value = {"/v5/urlaliases"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "searchTerm", required = false) String searchTerm)
      throws CudamiControllerException {
    PageRequest pageRequest = new LegacyPageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      List<Order> migratedSortBy = V5MigrationHelper.migrate(sortBy);
      Sorting sorting = new Sorting(migratedSortBy);
      pageRequest.setSorting(sorting);
    }

    try {
      PageResponse<LocalizedUrlAliases> pageResponse =
          urlAliasService.findLocalizedUrlAliases(pageRequest);
      String result = V5MigrationHelper.migrate(pageResponse, objectMapper);
      return new ResponseEntity<>(result, HttpStatus.OK);
    } catch (ServiceException | JsonProcessingException e) {
      throw new CudamiControllerException(e);
    }
  }

  @Operation(summary = "update an UrlAlias")
  @PutMapping(
      value = {"/v5/urlaliases/{uuid:" + ParameterHelper.UUID_PATTERN + "}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<UrlAlias> update(
      @Parameter(
              description =
                  "UUID of the urlalias, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @RequestBody V5UrlAlias urlAlias)
      throws CudamiControllerException, ValidationException {

    if (uuid == null || urlAlias == null || !uuid.equals(urlAlias.getUuid())) {
      return new ResponseEntity(
          "UUID=" + uuid + " not set or does not match UUID of provided resource",
          HttpStatus.UNPROCESSABLE_ENTITY);
    }

    UrlAlias result;
    try {
      urlAliasService.update(UrlAlias.builder().uuid(uuid).build());
    } catch (ServiceException e) {
      throw new CudamiControllerException(e);
    }

    return new ResponseEntity<>(urlAlias, HttpStatus.OK);
  }
}
