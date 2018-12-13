package de.digitalcollections.cudami.server.controller.v1.identifiable.entity.parts;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.parts.WebpageService;
import de.digitalcollections.model.api.identifiable.Identifiable;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import de.digitalcollections.model.jackson.v1.V1DigitalCollectionsObjectMapper;
import de.digitalcollections.model.xml.xstream.v1.V1DigitalCollectionsXStreamMarshaller;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;
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
import org.springframework.oxm.XmlMappingException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(description = "The V1 webpage controller", name = "V1 Webpage controller")
public class V1WebpageController {

  private final V1DigitalCollectionsObjectMapper v1ObjectMapper = new V1DigitalCollectionsObjectMapper();
  private final V1DigitalCollectionsXStreamMarshaller v1XStreamMarshaller = new V1DigitalCollectionsXStreamMarshaller();

  @Autowired
  private WebpageService<Webpage, Identifiable> webpageService;

  @ApiMethod(description = "get a webpage as JSON (Version 1), depending on extension or <tt>format</tt> request parameter or accept header")
  @RequestMapping(value = {"/v1/webpages/{uuid}.json"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
  @ApiResponseObject
  public ResponseEntity<String> getWebpageV1Json(
          @ApiPathParam(description = "UUID of the webpage, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>") @PathVariable("uuid") UUID uuid,
          @ApiQueryParam(name = "pLocale", description = "Desired locale, e.g. <tt>de_DE</tt>. If unset, contents in all languages will be returned")
          @RequestParam(name = "pLocale", required = false) Locale pLocale
  ) throws IdentifiableServiceException, JsonProcessingException {

    Webpage webpage = loadWebpage(pLocale, uuid);
    String result = v1ObjectMapper.writeValueAsString(webpage);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @ApiMethod(description = "get a webpage as JSON or XML (Version 1), depending on extension or <tt>format</tt> request parameter or accept header")
  @RequestMapping(value = {"/v1/webpages/{uuid}.xml"}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, method = RequestMethod.GET)
  @ApiResponseObject
  public ResponseEntity<String> getWebpageV1Xml(
          @ApiPathParam(description = "UUID of the webpage, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>") @PathVariable("uuid") UUID uuid,
          @ApiQueryParam(name = "pLocale", description = "Desired locale, e.g. <tt>de_DE</tt>. If unset, contents in all languages will be returned")
          @RequestParam(name = "pLocale", required = false) Locale pLocale
  ) throws IdentifiableServiceException, JsonProcessingException, XmlMappingException, IOException {

    Webpage webpage = loadWebpage(pLocale, uuid);
    StringWriter sw = new StringWriter();
    v1XStreamMarshaller.marshalWriter(webpage, sw);
    String result = sw.toString();
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  private Webpage loadWebpage(Locale pLocale, UUID uuid) throws IdentifiableServiceException {
    Webpage webpage;
    if (pLocale == null) {
      webpage = webpageService.get(uuid);
    } else {
      webpage = webpageService.get(uuid, pLocale);
    }
    return webpage;
  }
}
