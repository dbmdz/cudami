package de.digitalcollections.cudami.server.controller.identifiable.entity.parts;

import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.parts.WebpageService;
import de.digitalcollections.model.api.filter.FilterCriterion;
import de.digitalcollections.model.api.filter.Filtering;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.Website;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.Sorting;
import de.digitalcollections.model.api.paging.enums.Direction;
import de.digitalcollections.model.api.paging.enums.NullHandling;
import de.digitalcollections.model.api.view.BreadcrumbNavigation;
import de.digitalcollections.model.impl.paging.OrderImpl;
import de.digitalcollections.model.impl.paging.PageRequestImpl;
import de.digitalcollections.model.impl.paging.SortingImpl;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiPathParam;
import org.jsondoc.core.annotation.ApiQueryParam;
import org.jsondoc.core.annotation.ApiResponseObject;
import org.springframework.beans.factory.annotation.Autowired;
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
@Api(description = "The webpage controller", name = "Webpage controller")
public class WebpageController {

  @Autowired private WebpageService<Entity> webpageService;

  @Autowired private LocaleService localeService;

  @ApiMethod(description = "Get all webpages")
  @GetMapping(
      value = {"/latest/webpages", "/v2/webpages"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public PageResponse<Webpage> findAll(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortField", required = false, defaultValue = "lastModified")
          String sortField,
      @RequestParam(name = "sortDirection", required = false, defaultValue = "DESC")
          Direction sortDirection,
      @RequestParam(name = "nullHandling", required = false, defaultValue = "NATIVE")
          NullHandling nullHandling,
      @RequestParam(name = "publicationStart", required = false)
          FilterCriterion<LocalDate> publicationStart,
      @RequestParam(name = "publicationEnd", required = false)
          FilterCriterion<LocalDate> publicationEnd) {
    OrderImpl order = new OrderImpl(sortDirection, sortField, nullHandling);
    Sorting sorting = new SortingImpl(order);
    Filtering filtering =
        Filtering.defaultBuilder()
            .add("publicationStart", publicationStart)
            .add("publicationEnd", publicationEnd)
            .build();
    PageRequest pageRequest = new PageRequestImpl(pageNumber, pageSize, sorting, filtering);
    return webpageService.find(pageRequest);
  }

  // Test-URL: http://localhost:9000/latest/webpages/599a120c-2dd5-11e8-b467-0ed5f89f718b
  @ApiMethod(
      description =
          "Get a webpage as JSON or XML, depending on extension or <tt>format</tt> request parameter or accept header")
  @GetMapping(
      value = {"/latest/webpages/{uuid}", "/v3/webpages/{uuid}"},
      produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ApiResponseObject
  public ResponseEntity<Webpage> getWebpage(
      @ApiPathParam(
              description =
                  "UUID of the webpage, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @ApiQueryParam(
              name = "pLocale",
              description =
                  "Desired locale, e.g. <tt>de_DE</tt>. If unset, contents in all languages will be returned")
          @RequestParam(name = "pLocale", required = false)
          Locale pLocale)
      throws IdentifiableServiceException {
    Webpage webpage;
    if (pLocale == null) {
      webpage = webpageService.get(uuid);
    } else {
      webpage = webpageService.get(uuid, pLocale);
    }
    return new ResponseEntity<>(webpage, HttpStatus.OK);
  }

  @ApiMethod(description = "Get children of a webpage as JSON")
  @GetMapping(
      value = {"/latest/webpages/{uuid}/children", "/v3/webpages/{uuid}/children"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public PageResponse<Webpage> getWebpageChildren(
      @ApiPathParam(
              description =
                  "UUID of the parent webpage, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortField", required = false) String sortField,
      @RequestParam(name = "sortDirection", required = false, defaultValue = "DESC")
          Direction sortDirection,
      @RequestParam(name = "nullHandling", required = false, defaultValue = "NATIVE")
          NullHandling nullHandling,
      @RequestParam(name = "publicationStart", required = false)
          FilterCriterion<LocalDate> publicationStart,
      @RequestParam(name = "publicationEnd", required = false)
          FilterCriterion<LocalDate> publicationEnd)
      throws IdentifiableServiceException {
    Sorting sorting = null;
    if (sortField != null) {
      OrderImpl order = new OrderImpl(sortDirection, sortField, nullHandling);
      sorting = new SortingImpl(order);
    }
    Filtering filtering =
        Filtering.defaultBuilder()
            .add("publicationStart", publicationStart)
            .add("publicationEnd", publicationEnd)
            .build();
    PageRequest pageRequest = new PageRequestImpl(pageNumber, pageSize, sorting, filtering);
    return webpageService.getChildren(uuid, pageRequest);
  }

  @ApiMethod(description = "Get parent of a webpage as JSON")
  @GetMapping(
      value = {"/latest/webpages/{uuid}/parent", "/v3/webpages/{uuid}/parent"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public Webpage getWebpageParent(
      @ApiPathParam(
              description =
                  "UUID of the webpage, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid)
      throws IdentifiableServiceException {
    return webpageService.getParent(uuid);
  }

  @ApiMethod(description = "Get website of a webpage as JSON")
  @GetMapping(
      value = {"/latest/webpages/{uuid}/website", "/v3/webpages/{uuid}/website"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public Website getWebsite(
      @ApiPathParam(
              description =
                  "UUID of the webpage, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid)
      throws IdentifiableServiceException {
    return webpageService.getWebsite(uuid);
  }

  @ApiMethod(description = "Save a newly created top-level webpage")
  @PostMapping(
      value = {
        "/latest/websites/{parentWebsiteUuid}/webpage",
        "/v2/websites/{parentWebsiteUuid}/webpage"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public Webpage saveWithParentWebsite(
      @PathVariable UUID parentWebsiteUuid, @RequestBody Webpage webpage, BindingResult errors)
      throws IdentifiableServiceException {
    return webpageService.saveWithParentWebsite(webpage, parentWebsiteUuid);
  }

  @ApiMethod(description = "Save a newly created webpage")
  @PostMapping(
      value = {
        "/latest/webpages/{parentWebpageUuid}/webpage",
        "/v2/webpages/{parentWebpageUuid}/webpage"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public Webpage saveWithParentWebpage(
      @PathVariable UUID parentWebpageUuid, @RequestBody Webpage webpage, BindingResult errors)
      throws IdentifiableServiceException {
    return webpageService.saveWithParentWebpage(webpage, parentWebpageUuid);
  }

  @ApiMethod(description = "Update a webpage")
  @PutMapping(
      value = {"/latest/webpages/{uuid}", "/v2/webpages/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public Webpage update(@PathVariable UUID uuid, @RequestBody Webpage webpage, BindingResult errors)
      throws IdentifiableServiceException {
    assert Objects.equals(uuid, webpage.getUuid());
    return webpageService.update(webpage);
  }

  @ApiMethod(description = "Get file resources related to webpage")
  @GetMapping(
      value = {
        "/latest/webpages/{uuid}/related/fileresources",
        "/v2/webpages/{uuid}/related/fileresources"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public List<FileResource> getRelatedFileResources(@PathVariable UUID uuid) {
    return webpageService.getRelatedFileResources(uuid);
  }

  @ApiMethod(description = "Add file resource related to webpage")
  @PostMapping(
      value = {
        "/latest/webpages/{uuid}/related/fileresources/{fileResourceUuid}",
        "/v2/webpages/{uuid}/related/fileresources/{fileResourceUuid}"
      })
  @ResponseStatus(value = HttpStatus.OK)
  @ApiResponseObject
  public void addRelatedFileResource(@PathVariable UUID uuid, @PathVariable UUID fileResourceUuid) {
    webpageService.addRelatedFileresource(uuid, fileResourceUuid);
  }

  @ApiMethod(description = "Get the breadcrumb for a webpage")
  @GetMapping(
      value = {"/latest/webpages/{uuid}/breadcrumb", "/v3/webpages/{uuid}/breadcrumb"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public ResponseEntity<BreadcrumbNavigation> getBreadcrumb(
      @ApiPathParam(
              description =
                  "UUID of the webpage, e.g. <tt>6119d8e9-9c92-4091-8dcb-bc4053385406</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @ApiQueryParam(
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
}
