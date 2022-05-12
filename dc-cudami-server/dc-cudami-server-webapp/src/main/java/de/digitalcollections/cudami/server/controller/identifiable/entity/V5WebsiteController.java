package de.digitalcollections.cudami.server.controller.identifiable.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.WebsiteService;
import de.digitalcollections.cudami.server.controller.CudamiControllerException;
import de.digitalcollections.cudami.server.controller.legacy.V5MigrationHelper;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Website controller")
public class V5WebsiteController {

  private final WebsiteService websiteService;

  private final ObjectMapper objectMapper;

  public V5WebsiteController(WebsiteService websiteService, ObjectMapper objectMapper) {
    this.websiteService = websiteService;
    this.objectMapper = objectMapper;
  }

  @Operation(
      summary = "Get websites",
      description = "Get a paged, filtered and sorted list of websites",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "SearchPageResponse&lt;Website&gt;",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = PageResponseWebsite.class)))
      })
  @GetMapping(
      value = {"/v5/websites"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> find(
      @Parameter(
              name = "pageNumber",
              description = "the page number (starting with 0); if unset, defaults to 0.",
              example = "0",
              schema = @Schema(type = "integer"))
          @RequestParam(name = "pageNumber", required = false, defaultValue = "0")
          int pageNumber,
      @Parameter(
              name = "pageSize",
              description = "the page size; if unset, defaults to 25",
              example = "25",
              schema = @Schema(type = "integer"))
          @RequestParam(name = "pageSize", required = false, defaultValue = "25")
          int pageSize,
      @Parameter(
              name = "sortBy",
              description =
                  "the sorting specification; if unset, default to alphabetically ascending sorting of the field 'label')",
              example = "label_de.desc.nullsfirst",
              schema = @Schema(type = "string"))
          @RequestParam(name = "sortBy", required = false)
          List<Order> sortBy,
      @Parameter(
              name = "searchTerm",
              description = "the search term, of which the result is filtered (substring match)",
              example = "Test",
              schema = @Schema(type = "string"))
          @RequestParam(name = "searchTerm", required = false)
          String searchTerm)
      throws CudamiControllerException {
    PageRequest pageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    PageResponse<Website> pageResponse = websiteService.find(pageRequest);
    try {
      String result = V5MigrationHelper.migrateToV5(pageResponse, objectMapper);
      return new ResponseEntity<>(result, HttpStatus.OK);
    } catch (JsonProcessingException e) {
      throw new CudamiControllerException(e);
    }
  }

  @Operation(
      summary = "Get root pages of a website",
      description = "Get a paged, filtered and sorted list of root pages of a website",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "SearchPageResponse&lt;Webpage&gt;",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = PageResponseWebpage.class)))
      })
  @GetMapping(
      value = {"/v5/websites/{uuid}/rootpages"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> findRootPages(
      @Parameter(
              name = "uuid",
              description = "the UUID of the parent webpage",
              example = "599a120c-2dd5-11e8-b467-0ed5f89f718b",
              schema = @Schema(implementation = UUID.class))
          @PathVariable("uuid")
          UUID uuid,
      @Parameter(
              name = "pageNumber",
              description = "the page number (starting with 0); if unset, defaults to 0.",
              example = "0",
              schema = @Schema(type = "integer"))
          @RequestParam(name = "pageNumber", required = false, defaultValue = "0")
          int pageNumber,
      @Parameter(
              name = "pageSize",
              description = "the page size; if unset, defaults to 25",
              example = "25",
              schema = @Schema(type = "integer"))
          @RequestParam(name = "pageSize", required = false, defaultValue = "25")
          int pageSize,
      @Parameter(
              name = "searchTerm",
              description = "the search term, of which the result is filtered (substring match)",
              example = "Test",
              schema = @Schema(type = "string"))
          @RequestParam(name = "searchTerm", required = false)
          String searchTerm)
      throws CudamiControllerException {
    PageRequest searchPageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
    PageResponse<Webpage> pageResponse = websiteService.findRootWebpages(uuid, searchPageRequest);
    try {
      String result = V5MigrationHelper.migrateToV5(pageResponse, objectMapper);
      return new ResponseEntity<>(result, HttpStatus.OK);
    } catch (JsonProcessingException e) {
      throw new CudamiControllerException(e);
    }
  }

  // ----------------- Helper classes for Swagger Annotations only, since Swagger Annotations
  // ----------------- cannot yet handle generics
  @Hidden
  private static class PageResponseWebpage extends PageResponse<Webpage> {}

  @Hidden
  private static class PageResponseWebsite extends PageResponse<Website> {}
}
