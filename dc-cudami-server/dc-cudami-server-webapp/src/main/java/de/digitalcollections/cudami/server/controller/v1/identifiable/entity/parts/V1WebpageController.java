package de.digitalcollections.cudami.server.controller.v1.identifiable.entity.parts;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.parts.WebpageService;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import de.digitalcollections.model.jackson.DigitalCollectionsObjectMapper;
import de.digitalcollections.model.xml.xstream.v1.V1DigitalCollectionsXStreamMarshaller;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Locale;
import java.util.UUID;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.json.JSONObject;
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

  private final DigitalCollectionsObjectMapper objectMapper = new DigitalCollectionsObjectMapper();
  private final V1DigitalCollectionsXStreamMarshaller v1XStreamMarshaller = new V1DigitalCollectionsXStreamMarshaller();

  @Autowired
  private WebpageService webpageService;

  @ApiMethod(description = "get a webpage as JSON (Version 1), depending on extension or <tt>format</tt> request parameter or accept header")
  @RequestMapping(value = {"/v1/webpages/{uuid}.json", "/v1/webpages/{uuid}"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
  @ApiResponseObject
  public ResponseEntity<String> getWebpageV1Json(
      @ApiPathParam(description = "UUID of the webpage, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>") @PathVariable("uuid") UUID uuid,
      @ApiQueryParam(name = "pLocale", description = "Desired locale, e.g. <tt>de_DE</tt>. If unset, contents in all languages will be returned")
      @RequestParam(name = "pLocale", required = false) Locale pLocale
  ) throws IdentifiableServiceException, JsonProcessingException {
    Webpage webpage = loadWebpage(pLocale, uuid);
    //String result = objectMapper.writeValueAsString(webpage);
    JSONObject result = new JSONObject(objectMapper.writeValueAsString(webpage));
    JSONObject description = new JSONObject();
    description.put("documents", result.getJSONObject("description"));
    result.put("description", description);
    JSONObject text = new JSONObject();
    text.put("documents", result.getJSONObject("text"));
    result.put("text", text);
    result.put("type", "RESOURCE");
    return new ResponseEntity<>(result.toString(), HttpStatus.OK);
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
    return new ResponseEntity<>(addDocumentsLayer(result), HttpStatus.OK);
  }

  private String addDocumentsLayer(String xml) {
    try {
      SAXBuilder saxBuilder = new SAXBuilder();
      Document doc = saxBuilder.build(new StringReader(xml));

      Element description = doc.getRootElement().getChild("description");
      Element descriptionContent = description.getChild("entry");
      description.removeChildren("entry");
      description.setContent(createDocumentsElement(descriptionContent));

      Element text = doc.getRootElement().getChild("text");
      Element textContent = text.getChild("entry");
      text.removeChild("entry");
      text.setContent(createDocumentsElement(textContent));

      return new XMLOutputter().outputString(doc);
    } catch (IOException | JDOMException ex) {
      return xml;
    }
  }

  private Element createDocumentsElement(Element content) {
    Element documents = new Element("documents");
    documents.setContent(content);
    return documents;
  }

  private Webpage loadWebpage(Locale pLocale, UUID uuid) throws IdentifiableServiceException {
    Webpage webpage;
    if (pLocale == null) {
      webpage = (Webpage) webpageService.get(uuid);
    } else {
      webpage = webpageService.get(uuid, pLocale);
    }
    return webpage;
  }
}
