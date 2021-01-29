package de.digitalcollections.cudami.server.controller.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.WebsiteService;
import de.digitalcollections.model.api.identifiable.entity.Website;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import de.digitalcollections.model.api.paging.Order;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.Sorting;
import de.digitalcollections.model.impl.identifiable.entity.WebsiteImpl;
import de.digitalcollections.model.impl.paging.PageRequestImpl;
import de.digitalcollections.model.impl.paging.SortingImpl;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiPathParam;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(description = "The website controller", name = "Website controller")
public class WebsiteController {

  @Autowired private WebsiteService service;

  @ApiMethod(description = "Get count of content trees")
  @GetMapping(
      value = {"/latest/websites/count", "/v2/websites/count"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public long count() {
    return service.count();
  }

  @ApiMethod(description = "Get all websites")
  @GetMapping(
      value = {"/latest/websites", "/v2/websites"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public PageResponse<Website> findAll(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy) {
    PageRequest pageRequest = new PageRequestImpl(pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting =
          new SortingImpl(sortBy.stream().filter(Objects::nonNull).collect(Collectors.toList()));
      pageRequest.setSorting(sorting);
    }
    return service.find(pageRequest);
  }

  @ApiMethod(description = "Get website by uuid")
  @GetMapping(
      value = {"/latest/websites/{uuid}", "/v2/websites/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public Website findById(@PathVariable UUID uuid) {
    return (Website) service.get(uuid);
  }

  @ApiMethod(description = "Get languages of all websites")
  @GetMapping(
      value = {"/latest/websites/languages", "/v2/websites/languages"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public List<Locale> getLanguages() {
    return service.getLanguages();
  }

  @ApiMethod(description = "Get paged root pages of a website")
  @GetMapping(
      value = {"/latest/websites/{uuid}/rootpages", "/v3/websites/{uuid}/rootpages"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public PageResponse<Webpage> getRootPages(
      @ApiPathParam(
              description =
                  "UUID of the parent webpage, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy)
      throws IdentifiableServiceException {
    PageRequest pageRequest = new PageRequestImpl(pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting =
          new SortingImpl(sortBy.stream().filter(Objects::nonNull).collect(Collectors.toList()));
      pageRequest.setSorting(sorting);
    }
    return service.getRootPages(uuid, pageRequest);
  }

  @ApiMethod(description = "Save a newly created website")
  @PostMapping(
      value = {"/latest/websites", "/v2/websites"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public Website save(@RequestBody Website website, BindingResult errors)
      throws IdentifiableServiceException {
    return (Website) service.save(website);
  }

  @ApiMethod(description = "Update a website")
  @PutMapping(
      value = {"/latest/websites/{uuid}", "/v2/websites/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public Website update(@PathVariable UUID uuid, @RequestBody Website website, BindingResult errors)
      throws IdentifiableServiceException {
    assert Objects.equals(uuid, website.getUuid());
    return (Website) service.update(website);
  }

  @ApiMethod(description = "Update the order of a website's rootpages")
  @PutMapping(
      value = {"/latest/websites/{uuid}/rootpages", "/v3/websites/{uuid}/rootpages"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public ResponseEntity updateRootPagesOrder(
      @ApiPathParam(description = "UUID of the website") @PathVariable("uuid") UUID uuid,
      @ApiPathParam(description = "List of the rootpages") @RequestBody List<Webpage> rootPages) {
    Website website = new WebsiteImpl();
    website.setUuid(uuid);

    boolean successful = service.updateRootPagesOrder(website, rootPages);

    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }
}
