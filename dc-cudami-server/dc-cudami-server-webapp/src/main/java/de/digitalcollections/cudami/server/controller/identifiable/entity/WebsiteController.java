package de.digitalcollections.cudami.server.controller.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.WebsiteService;
import de.digitalcollections.model.api.identifiable.entity.Website;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.Sorting;
import de.digitalcollections.model.api.paging.enums.Direction;
import de.digitalcollections.model.api.paging.enums.NullHandling;
import de.digitalcollections.model.impl.paging.OrderImpl;
import de.digitalcollections.model.impl.paging.PageRequestImpl;
import de.digitalcollections.model.impl.paging.SortingImpl;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiResponseObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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

  @ApiMethod(description = "Get all websites")
  @GetMapping(
      value = {"/latest/websites", "/v2/websites"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public PageResponse<Website> findAll(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortField", required = false, defaultValue = "uuid") String sortField,
      @RequestParam(name = "sortDirection", required = false, defaultValue = "ASC")
          Direction sortDirection,
      @RequestParam(name = "nullHandling", required = false, defaultValue = "NATIVE")
          NullHandling nullHandling) {
    OrderImpl order = new OrderImpl(sortDirection, sortField, nullHandling);
    Sorting sorting = new SortingImpl(order);
    PageRequest pageRequest = new PageRequestImpl(pageNumber, pageSize, sorting);
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

  @ApiMethod(description = "Get count of content trees")
  @GetMapping(
      value = {"/latest/websites/count", "/v2/websites/count"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public long count() {
    return service.count();
  }

  @ApiMethod(description = "Get root pages of website")
  @GetMapping(
      value = {"/latest/websites/{uuid}/rootPages", "/v2/websites/{uuid}/rootPages"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  List<Webpage> getRootPages(@PathVariable UUID uuid) {
    return service.getRootPages(uuid);
  }
}
