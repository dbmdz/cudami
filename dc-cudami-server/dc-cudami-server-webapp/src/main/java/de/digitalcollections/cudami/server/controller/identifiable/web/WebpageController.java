package de.digitalcollections.cudami.server.controller.identifiable.web;

import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.web.WebpageService;
import de.digitalcollections.model.filter.FilterCriterion;
import de.digitalcollections.model.filter.Filtering;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.Sorting;
import de.digitalcollections.model.view.BreadcrumbNavigation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Webpage controller")
public class WebpageController {

  private final LocaleService localeService;
  private final WebpageService webpageService;

  public WebpageController(LocaleService localeService, WebpageService webpageService) {
    this.localeService = localeService;
    this.webpageService = webpageService;
  }

  @Operation(summary = "Add file resource related to webpage")
  @PostMapping(
      value = {
        "/v5/webpages/{uuid}/related/fileresources/{fileResourceUuid}",
        "/v2/webpages/{uuid}/related/fileresources/{fileResourceUuid}",
        "/latest/webpages/{uuid}/related/fileresources/{fileResourceUuid}"
      })
  @ResponseStatus(value = HttpStatus.OK)
  public void addRelatedFileResource(@PathVariable UUID uuid, @PathVariable UUID fileResourceUuid) {
    webpageService.addRelatedFileresource(uuid, fileResourceUuid);
  }

  @Operation(summary = "Get all webpages")
  @GetMapping(
      value = {"/v5/webpages", "/v2/webpages", "/latest/webpages"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Webpage> findAll(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "publicationStart", required = false)
          FilterCriterion<LocalDate> publicationStart,
      @RequestParam(name = "publicationEnd", required = false)
          FilterCriterion<LocalDate> publicationEnd) {
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    Filtering filtering =
        Filtering.defaultBuilder()
            .add("publicationStart", publicationStart)
            .add("publicationEnd", publicationEnd)
            .build();
    pageRequest.setFiltering(filtering);
    return webpageService.find(pageRequest);
  }

  @Operation(summary = "Get the breadcrumb for a webpage")
  @GetMapping(
      value = {
        "/v5/webpages/{uuid}/breadcrumb",
        "/v3/webpages/{uuid}/breadcrumb",
        "/latest/webpages/{uuid}/breadcrumb"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<BreadcrumbNavigation> getBreadcrumb(
      @Parameter(
              example = "",
              description =
                  "UUID of the webpage, e.g. <tt>6119d8e9-9c92-4091-8dcb-bc4053385406</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @Parameter(
              name = "pLocale",
              description =
                  "Desired locale, e.g. <tt>de_DE</tt>. If unset, contents in all languages will be returned")
          @RequestParam(name = "pLocale", required = false)
          Locale pLocale) {

    BreadcrumbNavigation breadcrumbNavigation;

    if (pLocale == null) {
      breadcrumbNavigation = webpageService.getBreadcrumbNavigation(uuid);
    } else {
      breadcrumbNavigation =
          webpageService.getBreadcrumbNavigation(uuid, pLocale, localeService.getDefaultLocale());
    }

    if (breadcrumbNavigation == null || breadcrumbNavigation.getNavigationItems().isEmpty()) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    return new ResponseEntity<>(breadcrumbNavigation, HttpStatus.OK);
  }

  @Operation(summary = "Get file resources related to webpage")
  @GetMapping(
      value = {
        "/v5/webpages/{uuid}/related/fileresources",
        "/v2/webpages/{uuid}/related/fileresources",
        "/latest/webpages/{uuid}/related/fileresources"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<FileResource> getRelatedFileResources(@PathVariable UUID uuid) {
    return webpageService.getRelatedFileResources(uuid);
  }

  @Operation(summary = "Get a webpage by uuid")
  @GetMapping(
      value = {"/v5/webpages/{uuid}", "/latest/webpages/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Webpage> getWebpage(
      @Parameter(
              example = "",
              description =
                  "UUID of the webpage, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @Parameter(
              name = "pLocale",
              description =
                  "Desired locale, e.g. <tt>de_DE</tt>. If unset, contents in all languages will be returned")
          @RequestParam(name = "pLocale", required = false)
          Locale pLocale,
      @Parameter(name = "active", description = "If set, object will only be returned if active")
          @RequestParam(name = "active", required = false)
          String active)
      throws IdentifiableServiceException {
    Webpage webpage;
    if (active != null) {
      if (pLocale == null) {
        webpage = webpageService.getActive(uuid);
      } else {
        webpage = webpageService.getActive(uuid, pLocale);
      }
    } else {
      if (pLocale == null) {
        webpage = webpageService.getByUuid(uuid);
      } else {
        webpage = webpageService.getByUuidAndLocale(uuid, pLocale);
      }
    }
    return new ResponseEntity<>(webpage, HttpStatus.OK);
  }

  @Operation(summary = "Get (active or all) paged children of a webpage as JSON")
  @GetMapping(
      value = {"/v5/webpages/{uuid}/children"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Webpage> getSubpages(
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
      throws IdentifiableServiceException {
    SearchPageRequest searchPageRequest = new SearchPageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      searchPageRequest.setSorting(sorting);
    }
    if (active != null) {
      return webpageService.findActiveChildren(uuid, searchPageRequest);
    }
    return webpageService.findChildren(uuid, searchPageRequest);
  }

  @Operation(summary = "Get (active or all) children of a webpage recursivly as JSON")
  @GetMapping(
      value = {"/v5/webpages/{uuid}/childrentree", "/latest/webpages/{uuid}/childrentree"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Webpage> getWebpageChildrenTree(
      @Parameter(
              example = "",
              description =
                  "UUID of the root webpage, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @Parameter(name = "active", description = "If set, only active children will be returned")
          @RequestParam(name = "active", required = false)
          String active) {

    return (active != null)
        ? webpageService.getActiveChildrenTree(uuid)
        : webpageService.getChildrenTree(uuid);
  }

  @Operation(summary = "Get parent of a webpage as JSON")
  @GetMapping(
      value = {
        "/v5/webpages/{uuid}/parent",
        "/v3/webpages/{uuid}/parent",
        "/latest/webpages/{uuid}/parent"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Webpage getWebpageParent(
      @Parameter(
              example = "",
              description =
                  "UUID of the webpage, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid)
      throws IdentifiableServiceException {
    return webpageService.getParent(uuid);
  }

  @Operation(summary = "Get website of a webpage as JSON")
  @GetMapping(
      value = {
        "/v5/webpages/{uuid}/website",
        "/v3/webpages/{uuid}/website",
        "/latest/webpages/{uuid}/website"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Website getWebsite(
      @Parameter(
              example = "",
              description =
                  "UUID of the webpage, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid)
      throws IdentifiableServiceException {
    return webpageService.getWebsite(uuid);
  }

  @Operation(summary = "Save a newly created webpage")
  @PostMapping(
      value = {
        "/v5/webpages/{parentWebpageUuid}/webpage",
        "/v2/webpages/{parentWebpageUuid}/webpage",
        "/latest/webpages/{parentWebpageUuid}/webpage"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Webpage saveWithParentWebpage(
      @PathVariable UUID parentWebpageUuid, @RequestBody Webpage webpage, BindingResult errors)
      throws IdentifiableServiceException, ValidationException {
    return webpageService.saveWithParent(webpage, parentWebpageUuid);
  }

  @Operation(summary = "Save a newly created top-level webpage")
  @PostMapping(
      value = {
        "/v5/websites/{parentWebsiteUuid}/webpage",
        "/v2/websites/{parentWebsiteUuid}/webpage",
        "/latest/websites/{parentWebsiteUuid}/webpage",
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Webpage saveWithParentWebsite(
      @PathVariable UUID parentWebsiteUuid, @RequestBody Webpage webpage, BindingResult errors)
      throws IdentifiableServiceException {
    return webpageService.saveWithParentWebsite(webpage, parentWebsiteUuid);
  }

  @Operation(summary = "Update a webpage")
  @PutMapping(
      value = {"/v5/webpages/{uuid}", "/v2/webpages/{uuid}", "/latest/webpages/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Webpage update(@PathVariable UUID uuid, @RequestBody Webpage webpage, BindingResult errors)
      throws IdentifiableServiceException, ValidationException {
    assert Objects.equals(uuid, webpage.getUuid());
    return webpageService.update(webpage);
  }

  @Operation(summary = "Update the order of a webpage's children")
  @PutMapping(
      value = {
        "/v5/webpages/{uuid}/children",
        "/v3/webpages/{uuid}/children",
        "/latest/webpages/{uuid}/children"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity updateChildrenOrder(
      @Parameter(example = "", description = "UUID of the webpage") @PathVariable("uuid") UUID uuid,
      @Parameter(example = "", description = "List of the children") @RequestBody
          List<Webpage> rootPages) {
    boolean successful = webpageService.updateChildrenOrder(uuid, rootPages);

    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }
}
