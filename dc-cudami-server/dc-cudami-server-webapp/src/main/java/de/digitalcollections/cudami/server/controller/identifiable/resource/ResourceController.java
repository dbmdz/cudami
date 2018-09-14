package de.digitalcollections.cudami.server.controller.identifiable.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.ResourceService;
import de.digitalcollections.model.api.identifiable.resource.Resource;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.Sorting;
import de.digitalcollections.model.api.paging.enums.Direction;
import de.digitalcollections.model.api.paging.enums.NullHandling;
import de.digitalcollections.model.api.paging.impl.OrderImpl;
import de.digitalcollections.model.api.paging.impl.PageRequestImpl;
import de.digitalcollections.model.api.paging.impl.SortingImpl;
import java.util.Locale;
import java.util.UUID;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiPathParam;
import org.jsondoc.core.annotation.ApiQueryParam;
import org.jsondoc.core.annotation.ApiResponseObject;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(description = "The resource controller", name = "Resource controller")
public class ResourceController {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ResourceController.class);

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private ResourceService<Resource> service;

  @ApiMethod(description = "get all resources")
  @RequestMapping(value = "/v1/resources",
          produces = "application/json", method = RequestMethod.GET)
  @ApiResponseObject
  public PageResponse<Resource> findAll(
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
    return service.find(pageRequest);
  }

  @ApiMethod(description = "get a resource as JSON or XML, depending on extension or <tt>format</tt> request parameter or accept header")
  @RequestMapping(value = {"/v1/resources/{uuid}"}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, method = RequestMethod.GET)
  @ApiResponseObject
  public ResponseEntity<Resource> get(
          @ApiPathParam(description = "UUID of the webpage, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>") @PathVariable("uuid") UUID uuid,
          @ApiQueryParam(name = "pLocale", description = "Desired locale, e.g. <tt>de_DE</tt>. If unset, contents in all languages will be returned")
          @RequestParam(name = "pLocale", required = false) Locale pLocale
  ) throws IdentifiableServiceException {

    Resource resource;
    if (pLocale == null) {
      resource = service.get(uuid);
    } else {
      resource = service.get(uuid, pLocale);
    }
    return new ResponseEntity<>(resource, HttpStatus.OK);
  }
}
