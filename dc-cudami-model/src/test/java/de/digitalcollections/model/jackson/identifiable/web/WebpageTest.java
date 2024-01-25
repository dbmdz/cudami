package de.digitalcollections.model.jackson.identifiable.web;

import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import de.digitalcollections.model.text.LocalizedStructuredContent;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.text.StructuredContent;
import de.digitalcollections.model.text.contentblock.Heading;
import de.digitalcollections.model.text.contentblock.IFrame;
import de.digitalcollections.model.text.contentblock.Image;
import de.digitalcollections.model.text.contentblock.Mark;
import de.digitalcollections.model.text.contentblock.Paragraph;
import de.digitalcollections.model.text.contentblock.Text;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import org.junit.jupiter.api.Test;

public class WebpageTest extends BaseJsonSerializationTest {

  public Webpage createObject() {
    Webpage webpage = new Webpage();
    webpage.setLabel(new LocalizedText(Locale.GERMANY, "Meine Homepage"));
    webpage.setCreated(LocalDateTime.of(2000, 1, 1, 10, 15));
    webpage.setLastModified(LocalDateTime.of(2000, 1, 3, 23, 15));

    LocalizedStructuredContent description = new LocalizedStructuredContent();
    StructuredContent structuredContent = new StructuredContent();
    Paragraph p = new Paragraph("");
    structuredContent.addContentBlock(p);
    description.put(Locale.GERMANY, structuredContent);
    webpage.setDescription(description);

    structuredContent = new StructuredContent();

    Heading h = new Heading();
    h.addAttribute("level", 4);
    Text t = new Text("Bayerische Staatsbibliothek");
    Mark m = new Mark("strong");
    t.addMark(m);
    h.addContentBlock(t);
    structuredContent.addContentBlock(h);

    Paragraph p2 = new Paragraph();
    p2.addContentBlock(new Text("Internet:", "strong"));
    p2.addContentBlock(new Text("   "));
    Text link = new Text("www.bsb-muenchen.de");
    Mark linkMark = new Mark("link");
    linkMark.addAttribute("href", "https://www.bsb-muenchen.de/");
    link.addMark(linkMark);
    p2.addContentBlock(link);
    structuredContent.addContentBlock(p2);

    Image image =
        new Image(
            "left",
            null,
            null,
            true,
            null,
            "135ec10b-ac65-4217-83fc-db5e9ff62cac",
            null,
            "https://www.bsb-muenchen.de/logo.png",
            "33%");
    structuredContent.addContentBlock(image);

    IFrame iFrame =
        new IFrame(
            "https://external.content.org/index.php?language=de&action=test",
            "90%",
            "150px",
            "Iframe title");
    structuredContent.addContentBlock(iFrame);

    LocalizedStructuredContent text = new LocalizedStructuredContent();
    text.put(Locale.GERMANY, structuredContent);
    webpage.setText(text);

    webpage.setPublicationStart(LocalDate.MIN);
    webpage.setPublicationEnd(LocalDate.MAX);
    return webpage;
  }

  @Test
  public void testSerializeDeserialize() throws Exception {
    Webpage webpage = createObject();
    checkSerializeDeserialize(webpage, "serializedTestObjects/identifiable/web/Webpage.json");
  }
}
