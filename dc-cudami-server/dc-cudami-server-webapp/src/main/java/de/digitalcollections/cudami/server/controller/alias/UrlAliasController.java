package de.digitalcollections.cudami.server.controller.alias;

import de.digitalcollections.cudami.server.business.api.service.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.CudamiServiceException;
import de.digitalcollections.cudami.server.controller.ControllerException;
import de.digitalcollections.model.alias.UrlAlias;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import de.digitalcollections.model.paging.Sorting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

  @Operation(summary = "Get an UrlAlias by uuid")
  @GetMapping(
      value = {"/v5/urlaliases/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<UrlAlias> get(
      @Parameter(
              description =
                  "UUID of the urlalias, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid)
      throws ControllerException {

    UrlAlias result;
    try {
      result = urlAliasService.findOne(uuid);
    } catch (CudamiServiceException e) {
      throw new ControllerException(e);
    }

    if (result == null) {
      return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @Operation(summary = "Delete an UrlAlias by uuid")
  @DeleteMapping(value = {"/v5/urlaliases/{uuid}"})
  public ResponseEntity<Void> delete(
      @Parameter(
              description =
                  "UUID of the urlalias, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid)
      throws ControllerException {
    boolean isDeleted;
    try {
      isDeleted = urlAliasService.delete(uuid);
    } catch (CudamiServiceException e) {
      throw new ControllerException(e);
    }

    if (!isDeleted) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @Operation(summary = "Save a newly created UrlAlias")
  @PostMapping(
      value = {"/v5/urlaliases"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<UrlAlias> save(@RequestBody UrlAlias urlAlias) throws ControllerException {

    UrlAlias result;
    try {
      result = urlAliasService.save(urlAlias);
    } catch (CudamiServiceException e) {
      throw new ControllerException(e);
    }

    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @Operation(summary = "Find limited amount of UrlAliases")
  @GetMapping(
      value = {"/v5/urlaliases"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<SearchPageResponse<UrlAlias>> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy)
      throws ControllerException {
    SearchPageRequest pageRequest = new SearchPageRequest(null, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }

    SearchPageResponse<UrlAlias> result;
    try {
      result = urlAliasService.find(pageRequest);
    } catch (CudamiServiceException e) {
      throw new ControllerException(e);
    }

    return new ResponseEntity<>(result, HttpStatus.OK);
  }
}
