package de.digitalcollections.cudami.model.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.model.api.identifiable.resource.Webpage;
import de.digitalcollections.cudami.model.impl.identifiable.resource.WebpageImpl;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("Between two models...")
public class WebpageTest extends BaseSerializationTest {

  ObjectMapper mapper;

  @BeforeEach
  public void beforeEach() {
    mapper = new ObjectMapper();
    mapper.registerModule(new CudamiModule());
  }

  @Override
  protected ObjectMapper getMapper() {
    return mapper;
  }

  @Test
  public void testSerialisationInBothWays() throws Exception {
    Webpage webpage = constructWebpage();
    checkSerializeDeserialize(webpage);
  }

  // -------------------------------------------------------------------
  private Webpage constructWebpage() {
    Webpage webpage = new WebpageImpl();

    Document document = new DocumentImpl();

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



    document.addContentBlocks(Locale.GERMAN, contents);
    webpage.setContentBlocksContainer(document);

    return webpage;
  }

}
