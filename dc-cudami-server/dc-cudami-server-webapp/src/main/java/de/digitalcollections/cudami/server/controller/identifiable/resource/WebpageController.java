package de.digitalcollections.cudami.server.controller.identifiable.resource;

import de.digitalcollections.core.model.api.paging.PageRequest;
import de.digitalcollections.core.model.api.paging.PageResponse;
import de.digitalcollections.core.model.api.paging.Sorting;
import de.digitalcollections.core.model.api.paging.enums.Direction;
import de.digitalcollections.core.model.api.paging.enums.NullHandling;
import de.digitalcollections.core.model.impl.paging.OrderImpl;
import de.digitalcollections.core.model.impl.paging.PageRequestImpl;
import de.digitalcollections.core.model.impl.paging.SortingImpl;
import de.digitalcollections.cudami.model.api.identifiable.resource.Webpage;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.WebpageService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(description = "The webpage controller", name = "Webpage controller")
public class WebpageController {

  @Autowired
  private WebpageService<Webpage> webpageService;

  @ApiMethod(description = "get all webpages")
  @RequestMapping(value = "/v1/webpages",
          produces = "application/json", method = RequestMethod.GET)
  @ApiResponseObject
  public PageResponse<Webpage> findAll(
          @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
          @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
          @RequestParam(name = "sortField", required = false, defaultValue = "uuid") String sortField,
          @RequestParam(name = "sortDirection", required = false, defaultValue = "ASC") Direction sortDirection,
          @RequestParam(name = "nullHandling", required = false, defaultValue = "NATIVE") NullHandling nullHandling
  ) {
    // FIXME add support for multiple sorting orders
    OrderImpl order = new OrderImpl(sortDirection, sortField, nullHandling);
    Sorting sorting = new SortingImpl(order);
    PageRequest pageRequest = new PageRequestImpl(pageNumber, pageSize, sorting);
    return webpageService.find(pageRequest);
  }

  // Test-URL: http://localhost:9000/v1/webpages/599a120c-2dd5-11e8-b467-0ed5f89f718b
  @ApiMethod(description = "get a webpage as JSON or XML, depending on extension or <tt>format</tt> request parameter or accept header")
  @RequestMapping(value = {"/v1/webpages/{uuid}"}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, method = RequestMethod.GET)
  @ApiResponseObject
  public ResponseEntity<Webpage> getWebpage(
          @ApiPathParam(description = "UUID of the webpage, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>") @PathVariable("uuid") UUID uuid,
          @ApiQueryParam(name = "pLocale", description = "Desired locale, e.g. <tt>de_DE</tt>. If unset, contents in all languages will be returned")
          @RequestParam(name = "pLocale", required = false) Locale pLocale
  ) throws IdentifiableServiceException {

    Webpage webpage;
    if (pLocale == null) {
      webpage = webpageService.get(uuid);
    } else {
      webpage = webpageService.get(uuid, pLocale);
    }
    return new ResponseEntity<>(webpage, HttpStatus.OK);
  }

  @ApiMethod(description = "save a newly created top-level webpage")
  @RequestMapping(value = "/v1/websites/{parentWebsiteUuid}/webpage", produces = "application/json", method = RequestMethod.POST)
  @ApiResponseObject
  public Webpage saveWithParentWebsite(@PathVariable UUID parentWebsiteUuid, @RequestBody Webpage webpage, BindingResult errors) throws IdentifiableServiceException {
    return webpageService.saveWithParentWebsite(webpage, parentWebsiteUuid);
  }

  @ApiMethod(description = "save a newly created webpage")
  @RequestMapping(value = "/v1/webpages/{parentWebpageUuid}/webpage", produces = "application/json", method = RequestMethod.POST)
  @ApiResponseObject
  public Webpage saveWithParentWebpage(@PathVariable UUID parentWebpageUuid, @RequestBody Webpage webpage, BindingResult errors) throws IdentifiableServiceException {
    return webpageService.saveWithParentWebpage(webpage, parentWebpageUuid);
  }

  @ApiMethod(description = "update a webpage")
  @RequestMapping(value = "/v1/webpages/{uuid}", produces = "application/json", method = RequestMethod.PUT)
  @ApiResponseObject
  public Webpage update(@PathVariable UUID uuid, @RequestBody Webpage webpage, BindingResult errors) throws IdentifiableServiceException {
    assert Objects.equals(uuid, webpage.getUuid());
    return webpageService.update(webpage);
  }

}
