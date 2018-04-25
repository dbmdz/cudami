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
import de.digitalcollections.cudami.model.api.identifiable.parts.MultilanguageDocument;
import de.digitalcollections.cudami.model.api.identifiable.resource.Webpage;
import de.digitalcollections.cudami.model.impl.identifiable.parts.MultilanguageDocumentImpl;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.WebpageService;
import de.digitalcollections.prosemirror.model.api.ContentBlock;
import de.digitalcollections.prosemirror.model.api.Document;
import de.digitalcollections.prosemirror.model.api.Mark;
import de.digitalcollections.prosemirror.model.api.contentblocks.Blockquote;
import de.digitalcollections.prosemirror.model.api.contentblocks.BulletList;
import de.digitalcollections.prosemirror.model.api.contentblocks.CodeBlock;
import de.digitalcollections.prosemirror.model.api.contentblocks.OrderedList;
import de.digitalcollections.prosemirror.model.api.contentblocks.Paragraph;
import de.digitalcollections.prosemirror.model.api.contentblocks.Text;
import de.digitalcollections.prosemirror.model.impl.DocumentImpl;
import de.digitalcollections.prosemirror.model.impl.MarkImpl;
import de.digitalcollections.prosemirror.model.impl.contentblocks.BlockquoteImpl;
import de.digitalcollections.prosemirror.model.impl.contentblocks.BulletListImpl;
import de.digitalcollections.prosemirror.model.impl.contentblocks.CodeBlockImpl;
import de.digitalcollections.prosemirror.model.impl.contentblocks.EmbeddedCodeImpl;
import de.digitalcollections.prosemirror.model.impl.contentblocks.HardBreakImpl;
import de.digitalcollections.prosemirror.model.impl.contentblocks.HeadingImpl;
import de.digitalcollections.prosemirror.model.impl.contentblocks.ListItemImpl;
import de.digitalcollections.prosemirror.model.impl.contentblocks.OrderedListImpl;
import de.digitalcollections.prosemirror.model.impl.contentblocks.ParagraphImpl;
import de.digitalcollections.prosemirror.model.impl.contentblocks.TextImpl;
import java.util.ArrayList;
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
  private LocaleService localeService;

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
  @ApiMethod(description = "get a webpage as JSON or XML, depending on extension or <tt>format</tt> request parameter or accept header")
  @RequestMapping(value = {"/v1/webpages/{uuid}"}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, method = RequestMethod.GET)
  @ApiResponseObject
  public ResponseEntity<Webpage> getWebpage(
          @ApiPathParam(description = "UUID of the webpage, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>") @PathVariable("uuid") UUID uuid,
          @ApiQueryParam(name = "pLocale", description = "Desired locale, e.g. <tt>de_DE</tt>. If unset, contents in all languages will be returned")
          @RequestParam(name = "pLocale", required = false) Locale pLocale
  ) throws IdentifiableServiceException {

//    if (pLocale == null) {
//      pLocale = localeService.getDefault();
//    }
//    Webpage webpage = createDummyWebpage(pLocale);
    // TODO implement locale specific webpage get
    Webpage webpage = (Webpage) service.get(uuid);
    return new ResponseEntity<>(webpage, HttpStatus.OK);
  }

  private Webpage createDummyWebpage(Locale pLocale) {
    Webpage webpage = (Webpage) service.create();
    webpage.getLabel().setText(Locale.GERMANY, "Dummy Impressum");
    List<ContentBlock> contents = new ArrayList<>();
    contents.add(new HeadingImpl(4, "Bayerische Staatsbibliothek"));
    contents.add(new ParagraphImpl("Ludwigstraße 16"));
    contents.add(new HardBreakImpl());
    contents.add(new HeadingImpl(4, "Gesetzlicher Vertreter:"));
    contents.add(new ParagraphImpl("Generaldirektor Dr. Klaus Ceynowa"));
    Paragraph paragraph1 = new ParagraphImpl();
    paragraph1.addContentBlock(new TextImpl("Telefon:", "strong"));
    paragraph1.addContentBlock(new TextImpl(" +49 89 28638-0"));
    paragraph1.addContentBlock(new HardBreakImpl());
    paragraph1.addContentBlock(new TextImpl("Fax:", "strong", "em"));
    paragraph1.addContentBlock(new TextImpl(" +49 89 28638-2200"));
    paragraph1.addContentBlock(new HardBreakImpl());
    paragraph1.addContentBlock(new TextImpl("E-Mail:", "strong"));
    paragraph1.addContentBlock(new TextImpl(" direktion [AT] bsb-muenchen.de"));
    paragraph1.addContentBlock(new HardBreakImpl());
    paragraph1.addContentBlock(new TextImpl("Internet:", "strong"));
    paragraph1.addContentBlock(new TextImpl(" "));
    Text internet = new TextImpl("https://www.bsb-muenchen.de");
    Mark link = new MarkImpl("link");
    link.addAttribute("href", "https://www.bsb-muenchen.de");
    link.addAttribute("title", null);
    internet.addMark(link);
    paragraph1.addContentBlock(internet);
    paragraph1.addContentBlock(new HardBreakImpl());
    paragraph1.addContentBlock(new TextImpl("Umsatzsteueridentifikationsnummer:", "strong"));
    paragraph1.addContentBlock(new TextImpl(" DE 811335517"));
    contents.add(paragraph1);
    contents
            .add(new ParagraphImpl("Die Bayerische Staatsbibliothek ist eine dem Bayerischen Staatsministerium für Bildung und Kultus, Wissenschaft und Kunst nachgeordnete Behörde der Mittelstufe mit dem Sitz in München"));
    contents.add(new HardBreakImpl());
    Text text2 = new TextImpl("Bayerischen Staatsministerium für Bildung und Kultus, Wissenschaft und Kunst");
    Mark link2 = new MarkImpl("link");
    link2.addAttribute("href", "https://www.km.bayern.de");
    link2.addAttribute("title", null);
    text2.addMark(link2);
    contents.add(text2);
    BulletList bulletList = new BulletListImpl();
    bulletList.addContentBlock(new ListItemImpl("test 1"));
    bulletList.addContentBlock(new ListItemImpl("test 2"));
    bulletList.addContentBlock(new ListItemImpl("test 3"));
    contents.add(bulletList);
    contents.add(new ParagraphImpl("Mehr Text."));
    OrderedList orderedList = new OrderedListImpl(1);
    orderedList.addContentBlock(new ListItemImpl("test 1"));
    orderedList.addContentBlock(new ListItemImpl("test 2"));
    orderedList.addContentBlock(new ListItemImpl("test 3"));
    contents.add(orderedList);
    contents.add(new EmbeddedCodeImpl("<iframe style=\"border: 1px solid lightgrey\" frameborder=\"no\" width=\"98%\" height=\"auto\" src=\"https://statistiken.digitale-sammlungen.de/index.php?module=CoreAdminHome&amp;action=optOut&amp;language=de\"></iframe>"));

    Blockquote blockQuote = new BlockquoteImpl();
    blockQuote.addContentBlock(new TextImpl("Das ist ein Zitat"));
    contents.add(blockQuote);

    CodeBlock codeBlock = new CodeBlockImpl();
    codeBlock.addContentBlock(new TextImpl("10 print \"Hallo Welt\""));
    contents.add(codeBlock);

    Document document = new DocumentImpl();
    document.setContentBlocks(contents);
    MultilanguageDocument multilanguageDocument = new MultilanguageDocumentImpl();
    multilanguageDocument.addDocument(pLocale, document);
    webpage.setText(multilanguageDocument);
    return webpage;
  }

  @ApiMethod(description = "save a newly created webpage")
  @RequestMapping(value = "/v1/websites/{websiteUuid}/webpage", produces = "application/json", method = RequestMethod.POST)
  @ApiResponseObject
  public Webpage save(@PathVariable UUID websiteUuid, @RequestBody Webpage webpage, BindingResult errors) throws IdentifiableServiceException {
    return (Webpage) service.save(webpage, websiteUuid);
  }

  @ApiMethod(description = "update a webpage")
  @RequestMapping(value = "/v1/webpages/{uuid}", produces = "application/json", method = RequestMethod.PUT)
  @ApiResponseObject
  public Webpage update(@PathVariable UUID uuid, @RequestBody Webpage webpage, BindingResult errors) throws IdentifiableServiceException {
    assert Objects.equals(uuid, webpage.getUuid());
    return (Webpage) service.update(webpage);
  }

}
