package de.digitalcollections.cudami.server.controller.identifiable.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.WebsiteService;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import de.digitalcollections.model.paging.Sorting;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import org.springframework.http.HttpStatus;
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
@Tag(name = "Website controller")
public class WebsiteController {

  private final WebsiteService websiteService;

  public WebsiteController(WebsiteService websiteService) {
    this.websiteService = websiteService;
  }

  @Operation(
      summary = "Get count of content trees",
      description = "Get count of content trees",
      responses = {
        @ApiResponse(
            responseCode = "200",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Integer.class),
                    examples = {@ExampleObject(name = "example", value = "42")}))
      })
  @GetMapping(
      value = {"/v5/websites/count", "/v2/websites/count", "/latest/websites/count"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public long count() {
    return websiteService.count();
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
  public SearchPageResponse<Website> find(
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
          String searchTerm) {
    SearchPageRequest searchPageRequest = new SearchPageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      searchPageRequest.setSorting(sorting);
    }
    return websiteService.find(searchPageRequest);
  }

  @Operation(
      summary = "Get a website",
      description = "Get a website by its uuid",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Website",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Website.class)))
      })
  @GetMapping(
      value = {"/v5/websites/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Website> getByUuid(
      @Parameter(
              name = "uuid",
              description = "the UUID of the website",
              example = "7a2f1935-c5b8-40fb-8622-c675de0a6242",
              schema = @Schema(implementation = UUID.class))
          @PathVariable
          UUID uuid)
      throws JsonProcessingException {
    Website website = websiteService.getByUuid(uuid);

    if (website == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    return new ResponseEntity<>(website, HttpStatus.OK);
  }

  @Operation(
      summary = "Get languages of all websites",
      description = "Get languages of all websites",
      responses = {@ApiResponse(responseCode = "200", description = "List&lt;Locale&gt;")})
  @GetMapping(
      value = {"/v5/websites/languages", "/v2/websites/languages", "/latest/websites/languages"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Locale> getLanguages() {
    return websiteService.getLanguages();
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
                    schema = @Schema(implementation = SearchPageResponseWebpage.class)))
      })
  @GetMapping(
      value = {"/v5/websites/{uuid}/rootpages"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public SearchPageResponse<Webpage> findRootPages(
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
          String searchTerm) {
    SearchPageRequest searchPageRequest = new SearchPageRequest(searchTerm, pageNumber, pageSize);
    return websiteService.findRootPages(uuid, searchPageRequest);
  }

  @Operation(
      summary = "Create a website",
      description = "Create and return a newly created website",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Website",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Website.class)))
      })
  @PostMapping(
      value = {"/v5/websites", "/v2/websites", "/latest/websites"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Website save(@RequestBody Website website, BindingResult errors)
      throws IdentifiableServiceException, ValidationException {
    return websiteService.save(website);
  }

  @Operation(
      summary = "Update an existing website",
      description = "Modify and return an existing website",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Website",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Website.class)))
      })
  @PutMapping(
      value = {"/v5/websites/{uuid}", "/v2/websites/{uuid}", "/latest/websites/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Website update(
      @Parameter(
              name = "uuid",
              description = "the UUID of the parent webpage",
              example = "599a120c-2dd5-11e8-b467-0ed5f89f718b",
              schema = @Schema(implementation = UUID.class))
          @PathVariable("uuid")
          UUID uuid,
      @RequestBody Website website,
      BindingResult errors)
      throws IdentifiableServiceException, ValidationException {
    assert Objects.equals(uuid, website.getUuid());
    return websiteService.update(website);
  }

  @Operation(
      summary = "Update rootpage order of a website",
      description = "Update the order of a website's rootpages",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "flag for success",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Boolean.class))),
        @ApiResponse(
            responseCode = "404",
            description = "update failed because the website did not exist")
      })
  @PutMapping(
      value = {
        "/v5/websites/{uuid}/rootpages",
        "/v3/websites/{uuid}/rootpages",
        "/latest/websites/{uuid}/rootpages"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity updateRootPagesOrder(
      @Parameter(
              name = "uuid",
              description = "the UUID of the parent webpage",
              example = "599a120c-2dd5-11e8-b467-0ed5f89f718b",
              schema = @Schema(implementation = UUID.class))
          @PathVariable("uuid")
          UUID uuid,
      @Parameter(required = true, description = "rootpages as a list of webpages") @RequestBody
          List<Webpage> rootPages) {
    Website website = new Website();
    website.setUuid(uuid);

    boolean successful = websiteService.updateRootPagesOrder(website, rootPages);

    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }

  // ----------------- Helper classes for Swagger Annotations only, since Swagger Annotations
  // ----------------- cannot yet handle generics
  @Hidden
  private static class PageResponseWebsite extends PageResponse<Website> {}

  @Hidden
  private static class SearchPageResponseWebpage extends SearchPageResponse<Webpage> {}
}
