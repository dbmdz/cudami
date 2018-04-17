package de.digitalcollections.cudami.server.controller.identifiable.resource;

import de.digitalcollections.core.model.api.paging.PageRequest;
import de.digitalcollections.core.model.api.paging.PageResponse;
import de.digitalcollections.core.model.api.paging.Sorting;
import de.digitalcollections.core.model.api.paging.enums.Direction;
import de.digitalcollections.core.model.api.paging.enums.NullHandling;
import de.digitalcollections.core.model.impl.paging.OrderImpl;
import de.digitalcollections.core.model.impl.paging.PageRequestImpl;
import de.digitalcollections.core.model.impl.paging.SortingImpl;
import de.digitalcollections.cudami.model.api.identifiable.entity.Website;
import de.digitalcollections.cudami.model.api.identifiable.resource.Webpage;
import de.digitalcollections.cudami.model.impl.identifiable.resource.WebpageImpl;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.WebpageService;
import de.digitalcollections.prosemirror.model.api.Content;
import de.digitalcollections.prosemirror.model.api.Document;
import de.digitalcollections.prosemirror.model.api.content.BulletList;
import de.digitalcollections.prosemirror.model.api.content.Mark;
import de.digitalcollections.prosemirror.model.api.content.OrderedList;
import de.digitalcollections.prosemirror.model.api.content.Paragraph;
import de.digitalcollections.prosemirror.model.api.content.Text;
import de.digitalcollections.prosemirror.model.impl.DocumentImpl;
import de.digitalcollections.prosemirror.model.impl.content.BulletListImpl;
import de.digitalcollections.prosemirror.model.impl.content.EmbeddedCodeBlockImpl;
import de.digitalcollections.prosemirror.model.impl.content.HardBreakImpl;
import de.digitalcollections.prosemirror.model.impl.content.HeadingImpl;
import de.digitalcollections.prosemirror.model.impl.content.ListItemImpl;
import de.digitalcollections.prosemirror.model.impl.content.MarkImpl;
import de.digitalcollections.prosemirror.model.impl.content.OrderedListImpl;
import de.digitalcollections.prosemirror.model.impl.content.ParagraphImpl;
import de.digitalcollections.prosemirror.model.impl.content.TextImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiPathParam;
import org.jsondoc.core.annotation.ApiResponseObject;
import org.springframework.beans.factory.annotation.Autowired;
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
  private WebpageService service;

  @ApiMethod(description = "get all webpages")
  @RequestMapping(value = "/v1/webpages",
          //params = {"pageNumber", "pageSize", "sortField", "sortDirection", "nullHandling"},
          produces = "application/json", method = RequestMethod.GET)
  @ApiResponseObject
  public PageResponse<Website> findAll(
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

  // Test-URL: http://localhost:9000/v1/webpages/599a120c-2dd5-11e8-b467-0ed5f89f718b
  @ApiMethod(description = "get a webpage as JSON")
  @RequestMapping(value = "/v1/webpages/{uuid}", produces = "application/json", method = RequestMethod.GET)
  @ApiResponseObject
  public Webpage getWebpage(
      @ApiPathParam(description = "UUID of the webpage, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>") @PathVariable("uuid") UUID uuid
  ) throws IdentifiableServiceException {
    List<Content> contents = new ArrayList<>();

    contents.add(new HeadingImpl(3, "Impressum"));
    contents.add(new HeadingImpl(4,"Bayerische Staatsbibliothek"));
    contents.add(new ParagraphImpl("Ludwigstraße 16"));
    contents.add(new HardBreakImpl());
    contents.add(new HeadingImpl(4, "Gesetzlicher Vertreter:"));
    contents.add(new ParagraphImpl("Generaldirektor Dr. Klaus Ceynowa"));

    Paragraph paragraph1 = new ParagraphImpl();
    paragraph1.addContent(new TextImpl("Telefon:", "strong"));
    paragraph1.addContent(new TextImpl(" +49 89 28638-0"));
    paragraph1.addContent(new HardBreakImpl());
    paragraph1.addContent(new TextImpl("Fax:", "strong","em"));
    paragraph1.addContent(new TextImpl(" +49 89 28638-2200"));
    paragraph1.addContent(new HardBreakImpl());
    paragraph1.addContent(new TextImpl("E-Mail:", "strong"));
    paragraph1.addContent(new TextImpl(" direktion [AT] bsb-muenchen.de"));
    paragraph1.addContent(new HardBreakImpl());
    paragraph1.addContent(new TextImpl("Internet:", "strong"));
    paragraph1.addContent(new TextImpl(" "));

    Text internet = new TextImpl("https://www.bsb-muenchen.de");
    Mark link = new MarkImpl("link");
    link.addAttribute("href","https://www.bsb-muenchen.de");
    link.addAttribute("title", null);
    internet.addMark(link);
    paragraph1.addContent(internet);
    paragraph1.addContent(new HardBreakImpl());
    paragraph1.addContent(new TextImpl("Umsatzsteueridentifikationsnummer:", "strong"));
    paragraph1.addContent(new TextImpl(" DE 811335517"));
    contents.add(paragraph1);

    contents.add(new ParagraphImpl("Die Bayerische Staatsbibliothek ist eine dem Bayerischen Staatsministerium für Bildung und Kultus, Wissenschaft und Kunst nachgeordnete Behörde der Mittelstufe mit dem Sitz in München"));
    contents.add(new HardBreakImpl());

    Text text2 = new TextImpl("Bayerischen Staatsministerium für Bildung und Kultus, Wissenschaft und Kunst");
    Mark link2 = new MarkImpl("link");
    link2.addAttribute("href","https://www.km.bayern.de");
    link2.addAttribute("title", null);
    text2.addMark(link);
    contents.add(text2);

    BulletList bulletList = new BulletListImpl();
    bulletList.addContent(new ListItemImpl("test 1"));
    bulletList.addContent(new ListItemImpl("test 2"));
    bulletList.addContent(new ListItemImpl("test 3"));
    contents.add(bulletList);

    contents.add(new ParagraphImpl("Mehr Text."));

    OrderedList orderedList = new OrderedListImpl(1);
    orderedList.addContent(new ListItemImpl("test 1"));
    orderedList.addContent(new ListItemImpl("test 2"));
    orderedList.addContent(new ListItemImpl("test 3"));
    contents.add(orderedList);

    contents.add(new EmbeddedCodeBlockImpl("<iframe style=\"border: 1px solid lightgrey\" frameborder=\"no\" width=\"98%\" height=\"auto\" src=\"https://statistiken.digitale-sammlungen.de/index.php?module=CoreAdminHome&amp;action=optOut&amp;language=de\"></iframe>"));


    Document document = new DocumentImpl();
    document.addContentBlocks(Locale.GERMAN, contents);

    Webpage webpage = new WebpageImpl();
    webpage.setContentBlocksContainer(document);

    return webpage;
  }

  @ApiMethod(description = "save a newly created webpage")
  @RequestMapping(value = "/v1/webpages", produces = "application/json", method = RequestMethod.POST)
  @ApiResponseObject
  public Webpage save(@RequestBody Webpage webpage, BindingResult errors) throws IdentifiableServiceException {
    return (Webpage) service.save(webpage);
  }

  @ApiMethod(description = "update a webpage")
  @RequestMapping(value = "/v1/webpages/{uuid}", produces = "application/json", method = RequestMethod.PUT)
  @ApiResponseObject
  public Webpage update(@PathVariable UUID uuid, @RequestBody Webpage webpage, BindingResult errors) throws IdentifiableServiceException {
    assert Objects.equals(uuid, webpage.getUuid());
    return (Webpage) service.update(webpage);
  }

}
