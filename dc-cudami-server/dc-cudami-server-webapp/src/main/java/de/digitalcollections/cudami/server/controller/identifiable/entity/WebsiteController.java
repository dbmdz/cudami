package de.digitalcollections.cudami.server.controller.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.WebsiteService;
import de.digitalcollections.cudami.server.controller.ParameterHelper;
import de.digitalcollections.cudami.server.controller.identifiable.AbstractIdentifiableController;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.validation.ValidationException;
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
public class WebsiteController extends AbstractIdentifiableController<Website> {

  // ----------------- Helper classes for Swagger Annotations only, since Swagger Annotations
  // ----------------- cannot yet handle generics
  @Hidden
  private static class PageResponseWebpage extends PageResponse<Webpage> {}

  @Hidden
  private static class PageResponseWebsite extends PageResponse<Website> {}

  private final WebsiteService service;

  public WebsiteController(WebsiteService websiteService) {
    this.service = websiteService;
  }

  @Override
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
      value = {
        "/v6/websites/count",
        "/v5/websites/count",
        "/v2/websites/count",
        "/latest/websites/count"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public long count() throws ServiceException {
    return super.count();
  }

  @Override
  @Operation(summary = "Get all websites as (paged, sorted, filtered) list")
  @GetMapping(
      value = {"/v6/websites"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Website> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "filter", required = false) List<FilterCriterion> filterCriteria,
      @RequestParam(name = "filtering", required = false) Filtering filtering)
      throws ServiceException {
    return super.find(pageNumber, pageSize, sortBy, filterCriteria, filtering);
  }

  @Operation(
      summary = "Get root pages of a website",
      description = "Get all root webpages of a website as (paged, sorted, filtered) list",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "PageResponse&lt;Webpage&gt;",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = PageResponseWebpage.class)))
      })
  @GetMapping(
      value = {"/v6/websites/{uuid:" + ParameterHelper.UUID_PATTERN + "}/rootpages"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Webpage> findRootPages(
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
              name = "sortBy",
              description = "the sorting specification; if unset, default to sortindex)",
              example = "label_de.desc.nullsfirst",
              schema = @Schema(type = "string"))
          @RequestParam(name = "sortBy", required = false)
          List<Order> sortBy,
      @Parameter(
              name = "filter",
              description = "the filters the result is filtered with",
              example = "label_de:like:Homepage",
              schema = @Schema(type = "List<FilterCriterion>"))
          @RequestParam(name = "filter", required = false)
          List<FilterCriterion> filterCriteria,
      @RequestParam(name = "filtering", required = false) Filtering filtering)
      throws ServiceException {
    PageRequest pageRequest =
        createPageRequest(Webpage.class, pageNumber, pageSize, sortBy, filterCriteria, filtering);
    return service.findRootWebpages(Website.builder().uuid(uuid).build(), pageRequest);
  }

  @Override
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
      value = {
        "/v6/websites/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/websites/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Website> getByUuid(
      @Parameter(
              name = "uuid",
              description = "the UUID of the website",
              example = "7a2f1935-c5b8-40fb-8622-c675de0a6242",
              schema = @Schema(implementation = UUID.class))
          @PathVariable
          UUID uuid)
      throws ServiceException {
    return super.getByUuid(uuid);
  }

  @Override
  @Operation(
      summary = "Get languages of all websites",
      description = "Get languages of all websites",
      responses = {@ApiResponse(responseCode = "200", description = "List&lt;Locale&gt;")})
  @GetMapping(
      value = {
        "/v6/websites/languages",
        "/v5/websites/languages",
        "/v2/websites/languages",
        "/latest/websites/languages"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Locale> getLanguages() throws ServiceException {
    return super.getLanguages();
  }

  @Override
  protected IdentifiableService<Website> getService() {
    return service;
  }

  @Override
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
      value = {"/v6/websites", "/v5/websites", "/v2/websites", "/latest/websites"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Website save(@RequestBody Website website, BindingResult errors)
      throws ServiceException, ValidationException {
    return super.save(website, errors);
  }

  @Override
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
      value = {
        "/v6/websites/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/websites/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v2/websites/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/latest/websites/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
      },
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
      throws ServiceException, ValidationException {
    assert Objects.equals(uuid, website.getUuid());
    service.update(website);
    return website;
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
        "/v6/websites/{uuid:" + ParameterHelper.UUID_PATTERN + "}/rootpages",
        "/v5/websites/{uuid:" + ParameterHelper.UUID_PATTERN + "}/rootpages",
        "/v3/websites/{uuid:" + ParameterHelper.UUID_PATTERN + "}/rootpages",
        "/latest/websites/{uuid:" + ParameterHelper.UUID_PATTERN + "}/rootpages"
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
          List<Webpage> rootPages)
      throws ServiceException {
    boolean successful = service.updateRootWebpagesOrder(buildExampleWithUuid(uuid), rootPages);
    return successful
        ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
        : new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }
}
