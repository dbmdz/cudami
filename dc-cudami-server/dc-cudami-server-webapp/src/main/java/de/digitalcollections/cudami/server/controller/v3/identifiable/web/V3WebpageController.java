package de.digitalcollections.cudami.server.controller.v3.identifiable.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.web.WebpageService;
import de.digitalcollections.cudami.server.controller.AbstractLegacyController;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.Sorting;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiPathParam;
import org.jsondoc.core.annotation.ApiQueryParam;
import org.jsondoc.core.annotation.ApiResponseObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(description = "The V3 webpage controller", name = "V3 Webpage controller")
public class V3WebpageController extends AbstractLegacyController {

  private final LocaleService localeService;
  private final WebpageService webpageService;

  // private final XmlMapper xmlMapper = new XmlMapper();

  public V3WebpageController(LocaleService localeService, WebpageService webpageService) {
    this.localeService = localeService;
    this.webpageService = webpageService;
  }

  @ApiMethod(
      description =
          "Get a webpage as JSON, depending on extension or <tt>format</tt> request parameter or accept header")
  @GetMapping(
      value = {"/v3/webpages/{uuid}"},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @ApiResponseObject
  public ResponseEntity<String> getWebpageJson(
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
          Locale pLocale,
      @ApiQueryParam(
              name = "active",
              description = "If set, object will only be returned if active")
          @RequestParam(name = "active", required = false)
          String active)
      throws IdentifiableServiceException, JsonProcessingException {
    Webpage webpage;
    if (active != null) {
      if (pLocale == null) {
        webpage = webpageService.getActive(uuid);
      } else {
        webpage = webpageService.getActive(uuid, pLocale);
      }
    } else {
      if (pLocale == null) {
        webpage = webpageService.get(uuid);
      } else {
        webpage = webpageService.get(uuid, pLocale);
      }
    }

    if (webpage == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    return new ResponseEntity<>(fixSimpleObject(webpage, "ENTITY_PART", "WEBPAGE"), HttpStatus.OK);

    // tring result = fixSimpleObject(webpage, "ENTITY_PART", "WEBPAGE");
    // return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @ApiMethod(
      description =
          "Get a webpage as XML, depending on extension or <tt>format</tt> request parameter or accept header")
  @GetMapping(
      value = {"/v3/webpages/{uuid}"},
      produces = {MediaType.APPLICATION_XML_VALUE})
  @ApiResponseObject
  public ResponseEntity<String> getWebpageXml(
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
          Locale pLocale,
      @ApiQueryParam(
              name = "active",
              description = "If set, object will only be returned if active")
          @RequestParam(name = "active", required = false)
          String active)
      throws IdentifiableServiceException, IOException {

    return new ResponseEntity<>(HttpStatus.UNSUPPORTED_MEDIA_TYPE);

    /*
    ResponseEntity<String> jsonResponse = getWebpageJson(uuid, pLocale, active);
    if (jsonResponse.getStatusCode() != HttpStatus.OK) {
      return jsonResponse;
    }

    //FIXME Unfortunately, this is not enough. The XML serialization is so much different due to
    // the new model, just fixing the two fields (done by the json method) is not enough
    JsonNode tree = objectMapper.readTree(jsonResponse.getBody());
    String xml = xmlMapper.writer().withRootName("webpage").writeValueAsString(tree);
    return new ResponseEntity<>(xml, HttpStatus.OK);
     */
  }

  @ApiMethod(description = "Get (active or all) paged children of a webpage as JSON")
  @GetMapping(
      value = {"/v3/webpages/{uuid}/children"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public ResponseEntity<String> getWebpageChildren(
      @ApiPathParam(
              description =
                  "UUID of the parent webpage, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "active", required = false) String active)
      throws IdentifiableServiceException, JsonProcessingException {
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    PageResponse<Webpage> pageResponse;

    if (active != null) {
      pageResponse = webpageService.getActiveChildren(uuid, pageRequest);
    } else {
      pageResponse = webpageService.getChildren(uuid, pageRequest);
    }

    // String result = fixPageResponseChildren(pageResponse, "webpage",
    //    "de.digitalcollections.model.impl.identifiable.entity.parts.WebpageImpl",
    //    "ENTITY_PART", "WEBPAGE");

    // return new ResponseEntity<>(result, HttpStatus.OK);

    return new ResponseEntity<>(fixPageResponse(pageResponse), HttpStatus.OK);
  }

  @ApiMethod(description = "Get (active or all) children of a webpage recursivly as JSON")
  @GetMapping(
      value = {"/v3/webpages/{uuid}/childrentree"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public ResponseEntity<String> getWebpageChildrenTree(
      @ApiPathParam(
              description =
                  "UUID of the root webpage, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @ApiQueryParam(name = "active", description = "If set, only active children will be returned")
          @RequestParam(name = "active", required = false)
          String active)
      throws JsonProcessingException {
    List<Webpage> childrenList;
    if (active != null) {
      childrenList = webpageService.getActiveChildrenTree(uuid);
    } else {
      childrenList = webpageService.getChildrenTree(uuid);
    }

    JSONArray resultList = new JSONArray(objectMapper.writeValueAsString(childrenList));
    resultList = fixWebpageChildren(resultList);
    return new ResponseEntity<>(resultList.toString(), HttpStatus.OK);
  }

  private JSONArray fixWebpageChildren(JSONArray webpages) {
    for (int i = 0; i < webpages.length(); i++) {
      JSONObject webpage = webpages.getJSONObject(i);
      webpage.put("type", "ENTITY_PART");
      webpage.put("entityPartType", "WEBPAGE");

      if (webpage.has("children")) {
        JSONArray children = webpage.getJSONArray("children");
        if (children != null && children.length() > 0) {
          children = fixWebpageChildren(children);
          webpage.put("children", children);
        }
      } else {
        webpage.put("children", List.of());
      }
    }
    return webpages;
  }
}
