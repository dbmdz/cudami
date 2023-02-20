package de.digitalcollections.cudami.server.controller.identifiable.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.web.WebpageService;
import de.digitalcollections.cudami.server.controller.CudamiControllerException;
import de.digitalcollections.cudami.server.controller.ParameterHelper;
import de.digitalcollections.cudami.server.controller.legacy.V5MigrationHelper;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Webpage controller")
public class V5WebpageController {

  private final LocaleService localeService;
  private final WebpageService webpageService;

  private final ObjectMapper objectMapper;

  public V5WebpageController(
      LocaleService localeService, WebpageService webpageService, ObjectMapper objectMapper) {
    this.localeService = localeService;
    this.webpageService = webpageService;
    this.objectMapper = objectMapper;
  }

  @Operation(summary = "Get all webpages")
  @GetMapping(
      value = {"/v5/webpages", "/v2/webpages", "/latest/webpages"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "publicationStart", required = false)
          FilterCriterion<LocalDate> publicationStart,
      @RequestParam(name = "publicationEnd", required = false)
          FilterCriterion<LocalDate> publicationEnd)
      throws CudamiControllerException {
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    Filtering filtering =
        Filtering.builder()
            .add("publicationStart", publicationStart)
            .add("publicationEnd", publicationEnd)
            .build();
    pageRequest.setFiltering(filtering);
    PageResponse<Webpage> pageResponse = webpageService.find(pageRequest);
    try {
      String result = V5MigrationHelper.migrate(pageResponse, objectMapper);
      return new ResponseEntity<>(result, HttpStatus.OK);
    } catch (JsonProcessingException e) {
      throw new CudamiControllerException(e);
    }
  }

  @Operation(summary = "Get (active or all) paged children of a webpage as JSON")
  @GetMapping(
      value = {"/v5/webpages/{uuid:" + ParameterHelper.UUID_PATTERN + "}/children"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> findSubpages(
      @Parameter(
              example = "",
              description =
                  "UUID of the parent webpage, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "active", required = false) String active,
      @RequestParam(name = "searchTerm", required = false) String searchTerm)
      throws ServiceException, CudamiControllerException {
    PageRequest searchPageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(V5MigrationHelper.migrate(sortBy));
      searchPageRequest.setSorting(sorting);
    }

    PageResponse<Webpage> pageResponse;
    if (active != null) {
      pageResponse = webpageService.findActiveChildren(uuid, searchPageRequest);
    } else {
      pageResponse = webpageService.findChildren(uuid, searchPageRequest);
    }

    try {
      String result = V5MigrationHelper.migrate(pageResponse, objectMapper);
      return new ResponseEntity<>(result, HttpStatus.OK);
    } catch (JsonProcessingException e) {
      throw new CudamiControllerException(e);
    }
  }
}
