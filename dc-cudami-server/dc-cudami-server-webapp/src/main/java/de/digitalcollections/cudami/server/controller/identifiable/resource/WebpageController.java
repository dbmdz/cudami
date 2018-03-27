package de.digitalcollections.cudami.server.controller.identifiable.resource;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import java.util.UUID;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiResponseObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(description = "The webpage controller", name = "Webpage controller")
public class WebpageController {

  @ApiMethod(description = "get a webpage")
  @RequestMapping(value = "/v1/webpages/{uuid}", produces = "application/json", method = RequestMethod.GET)
  @ApiResponseObject
  public ResponseEntity<String> getWebpage(@PathVariable UUID uuid) throws IdentifiableServiceException {
    return new ResponseEntity<>("{\"type\":\"doc\",\"content\":[{\"type\":\"heading\",\"attrs\":{\"level\":3},\"content\":[{\"type\":\"text\",\"text\":\"Impressum\"}]},{\"type\":\"heading\",\"attrs\":{\"level\":4},\"content\":[{\"type\":\"text\",\"text\":\"Bayerische Staatsbibliothek\"}]},{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"Ludwigstraße 16\"},{\"type\":\"hard_break\"},{\"type\":\"text\",\"text\":\"80539 München\"}]},{\"type\":\"heading\",\"attrs\":{\"level\":4},\"content\":[{\"type\":\"text\",\"text\":\"Gesetzlicher Vertreter:\"}]},{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"Generaldirektor Dr. Klaus Ceynowa             \"}]},{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"marks\":[{\"type\":\"strong\"}],\"text\":\"Telefon:\"},{\"type\":\"text\",\"text\":\" +49 89 28638-0\"},{\"type\":\"hard_break\"},{\"type\":\"text\",\"marks\":[{\"type\":\"strong\"}],\"text\":\"Fax:\"},{\"type\":\"text\",\"text\":\" +49 89 28638-2200\"},{\"type\":\"hard_break\"},{\"type\":\"text\",\"marks\":[{\"type\":\"strong\"}],\"text\":\"E-Mail:\"},{\"type\":\"text\",\"text\":\" direktion [AT] bsb-muenchen.de\"},{\"type\":\"hard_break\"},{\"type\":\"text\",\"marks\":[{\"type\":\"strong\"}],\"text\":\"Internet:\"},{\"type\":\"text\",\"text\":\" \"},{\"type\":\"text\",\"marks\":[{\"type\":\"link\",\"attrs\":{\"href\":\"https://www.bsb-muenchen.de\",\"title\":null}}],\"text\":\"https://www.bsb-muenchen.de\"},{\"type\":\"hard_break\"},{\"type\":\"text\",\"marks\":[{\"type\":\"strong\"}],\"text\":\"Umsatzsteueridentifikationsnummer:\"},{\"type\":\"text\",\"text\":\" DE 811335517\"}]},{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"Die Bayerische Staatsbibliothek ist eine dem Bayerischen Staatsministerium für Bildung und Kultus, Wissenschaft und Kunst nachgeordnete Behörde der Mittelstufe mit dem Sitz in München.\"},{\"type\":\"hard_break\"},{\"type\":\"text\",\"marks\":[{\"type\":\"link\",\"attrs\":{\"href\":\"https://www.km.bayern.de/\",\"title\":null}}],\"text\":\"Bayerischen Staatsministerium für Bildung und Kultus, Wissenschaft und Kunst\"}]},{\"type\":\"bullet_list\",\"content\":[{\"type\":\"list_item\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"test 1\"}]}]},{\"type\":\"list_item\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"test 2\"}]}]},{\"type\":\"list_item\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"test 3\"}]}]}]},{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"Mehr Text.\"}]},{\"type\":\"ordered_list\",\"attrs\":{\"order\":1},\"content\":[{\"type\":\"list_item\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"test 1\"}]}]},{\"type\":\"list_item\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"test 2\"}]}]},{\"type\":\"list_item\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"test 3\"}]}]}]}]}", HttpStatus.OK);
  }
}
