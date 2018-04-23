package de.digitalcollections.cudami.model.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.model.api.identifiable.parts.MultilanguageDocument;
import de.digitalcollections.cudami.model.api.identifiable.resource.Webpage;
import de.digitalcollections.cudami.model.api.security.User;
import de.digitalcollections.cudami.model.impl.identifiable.entity.WebsiteImpl;
import de.digitalcollections.cudami.model.impl.identifiable.parts.MultilanguageDocumentImpl;
import de.digitalcollections.cudami.model.impl.identifiable.parts.TextImpl;
import de.digitalcollections.cudami.model.impl.identifiable.resource.WebpageImpl;
import de.digitalcollections.cudami.model.impl.security.UserImpl;
import de.digitalcollections.prosemirror.model.api.ContentBlock;
import de.digitalcollections.prosemirror.model.api.Document;
import de.digitalcollections.prosemirror.model.api.Mark;
import de.digitalcollections.prosemirror.model.api.contentblocks.BulletList;
import de.digitalcollections.prosemirror.model.api.contentblocks.OrderedList;
import de.digitalcollections.prosemirror.model.api.contentblocks.Paragraph;
import de.digitalcollections.prosemirror.model.impl.DocumentImpl;
import de.digitalcollections.prosemirror.model.impl.MarkImpl;
import de.digitalcollections.prosemirror.model.impl.contentblocks.BulletListImpl;
import de.digitalcollections.prosemirror.model.impl.contentblocks.EmbeddedCodeImpl;
import de.digitalcollections.prosemirror.model.impl.contentblocks.HardBreakImpl;
import de.digitalcollections.prosemirror.model.impl.contentblocks.HeadingImpl;
import de.digitalcollections.prosemirror.model.impl.contentblocks.ListItemImpl;
import de.digitalcollections.prosemirror.model.impl.contentblocks.OrderedListImpl;
import de.digitalcollections.prosemirror.model.impl.contentblocks.ParagraphImpl;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
public class CudamiModuleTest extends BaseSerializationTest {

  ObjectMapper mapper;

  @BeforeEach
  public void setUp() {
    mapper = new ObjectMapper();
    mapper.registerModule(new CudamiModule());
  }

  @Override
  protected ObjectMapper getMapper() {
    return mapper;
  }

  @Test
  public void testSerializeDeserializeUser() throws Exception {
    User user = new UserImpl();

    checkSerializeDeserialize(user);

  }

  @Test
  public void testWebsite() throws Exception {
    WebsiteImpl website = new WebsiteImpl(new URL("http://www.example.org/"));
    String serializedObject = mapper.writeValueAsString(website);
    checkSerializeDeserialize(website);
  }

  @Test
  public void testWebpage() throws Exception {
    List<ContentBlock> contents = new ArrayList<>();

    contents.add(new HeadingImpl(3, "Impressum"));
    contents.add(new HeadingImpl(4, "Bayerische Staatsbibliothek"));
    contents.add(new ParagraphImpl("Ludwigstraße 16"));
    contents.add(new HardBreakImpl());
    contents.add(new HeadingImpl(4, "Gesetzlicher Vertreter:"));
    contents.add(new ParagraphImpl("Generaldirektor Dr. Klaus Ceynowa"));

    Paragraph paragraph1 = new ParagraphImpl();
    paragraph1.addContentBlock(new de.digitalcollections.prosemirror.model.impl.contentblocks.TextImpl("Telefon:", "strong"));
    paragraph1.addContentBlock(new de.digitalcollections.prosemirror.model.impl.contentblocks.TextImpl(" +49 89 28638-0"));
    paragraph1.addContentBlock(new HardBreakImpl());
    paragraph1.addContentBlock(new de.digitalcollections.prosemirror.model.impl.contentblocks.TextImpl("Fax:", "strong", "em"));
    paragraph1.addContentBlock(new de.digitalcollections.prosemirror.model.impl.contentblocks.TextImpl(" +49 89 28638-2200"));
    paragraph1.addContentBlock(new HardBreakImpl());
    paragraph1.addContentBlock(new de.digitalcollections.prosemirror.model.impl.contentblocks.TextImpl("E-Mail:", "strong"));
    paragraph1.addContentBlock(new de.digitalcollections.prosemirror.model.impl.contentblocks.TextImpl(" direktion [AT] bsb-muenchen.de"));
    paragraph1.addContentBlock(new HardBreakImpl());
    paragraph1.addContentBlock(new de.digitalcollections.prosemirror.model.impl.contentblocks.TextImpl("Internet:", "strong"));
    paragraph1.addContentBlock(new de.digitalcollections.prosemirror.model.impl.contentblocks.TextImpl(" "));

    de.digitalcollections.prosemirror.model.api.contentblocks.Text internet = new de.digitalcollections.prosemirror.model.impl.contentblocks.TextImpl("https://www.bsb-muenchen.de");
    Mark link = new MarkImpl("link");
    link.addAttribute("href", "https://www.bsb-muenchen.de");
    link.addAttribute("title", null);
    internet.addMark(link);
    paragraph1.addContentBlock(internet);
    paragraph1.addContentBlock(new HardBreakImpl());
    paragraph1.addContentBlock(new de.digitalcollections.prosemirror.model.impl.contentblocks.TextImpl("Umsatzsteueridentifikationsnummer:", "strong"));
    paragraph1.addContentBlock(new de.digitalcollections.prosemirror.model.impl.contentblocks.TextImpl(" DE 811335517"));
    contents.add(paragraph1);

    contents
            .add(new ParagraphImpl("Die Bayerische Staatsbibliothek ist eine dem Bayerischen Staatsministerium für Bildung und Kultus, Wissenschaft und Kunst nachgeordnete Behörde der Mittelstufe mit dem Sitz in München"));
    contents.add(new HardBreakImpl());

    de.digitalcollections.prosemirror.model.api.contentblocks.Text text2 = new de.digitalcollections.prosemirror.model.impl.contentblocks.TextImpl("Bayerischen Staatsministerium für Bildung und Kultus, Wissenschaft und Kunst");
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

    contents
            .add(new EmbeddedCodeImpl("<iframe style=\"border: 1px solid lightgrey\" frameborder=\"no\" width=\"98%\" height=\"auto\" src=\"https://statistiken.digitale-sammlungen.de/index.php?module=CoreAdminHome&amp;action=optOut&amp;language=de\"></iframe>"));

    Document document = new DocumentImpl();
    document.setContentBlocks(contents);

    Webpage webpage = new WebpageImpl();
    webpage.setLabel(new TextImpl(Locale.GERMANY, ""));

    MultilanguageDocument mld = new MultilanguageDocumentImpl();
    mld.addDocument(Locale.GERMAN, document);
    webpage.setText(mld);

    checkSerializeDeserialize(webpage);

  }

//  @Test
//  public void testSerializeDeserializeContentInDefaultLocale() throws Exception {
//    Text content = new TextImpl("de", "test");
//
//    checkSerializeDeserialize(content);
//  }
//
//  @Test
//  public void testSerializeDeserializeEmptyContent() throws Exception {
//    Text content = new TextImpl();
//
//    checkSerializeDeserialize(content);
//  }
//
//  @Test
//  public void testSerializeDeserializeContentInSeveralLanguages() throws Exception {
//    Text content = new TextImpl("de", "test");
//    content.setText("it", "testo");
//
//    checkSerializeDeserialize(content);
//  }
  // -------------------------------------------------------------------------------------------------------
}
