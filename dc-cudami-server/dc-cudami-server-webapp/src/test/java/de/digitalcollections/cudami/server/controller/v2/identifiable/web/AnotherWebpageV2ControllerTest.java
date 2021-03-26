package de.digitalcollections.cudami.server.controller.v2.identifiable.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.identifiable.IdentifiableType;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.text.LocalizedStructuredContent;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.text.StructuredContent;
import de.digitalcollections.model.text.contentblock.HardBreak;
import de.digitalcollections.model.text.contentblock.Mark;
import de.digitalcollections.model.text.contentblock.Paragraph;
import de.digitalcollections.model.text.contentblock.Text;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

@WebMvcTest(V2WebpageController.class)
public class AnotherWebpageV2ControllerTest extends BaseControllerTest {

  @DisplayName(
      "returns a webpage in v2 json format for UUID, with or without json suffix in the url")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v2/webpages/8f95bd0a-7095-44e7-9ab3-061f288741aa.json",
        "/v2/webpages/8f95bd0a-7095-44e7-9ab3-061f288741aa"
      })
  public void returnWebpageV2Json(String path) throws Exception {
    LocalizedStructuredContent content = new LocalizedStructuredContent();
    StructuredContent structuredContentDe = new StructuredContent();
    structuredContentDe.addContentBlock(
        new ParagraphBuilder()
            .addText("Bayerische Staatsbibliothek", "strong")
            .addText(
                "                                                                                              ")
            .addText(
                "                                                                           ",
                "strong")
            .addText(
                "                                                                                                                       Ludwigstraße 16")
            .addHardBreak()
            .addText("80539 München")
            .build());
    structuredContentDe.addContentBlock(
        new ParagraphBuilder().addText("Gesetzlicher Vertreter:", "strong").build());
    structuredContentDe.addContentBlock(
        new ParagraphBuilder().addText("Generaldirektor Dr. Klaus Ceynowa").build());
    structuredContentDe.addContentBlock(
        new ParagraphBuilder()
            .addText("Telefon: ", "strong")
            .addText("+49 89 28638-0")
            .addHardBreak()
            .addText("Fax: ", "strong")
            .addText("+49 89 28638-2200")
            .build());
    structuredContentDe.addContentBlock(
        new ParagraphBuilder()
            .addText("E-Mail: ", "strong")
            .addText("direktion[at]bsb-muenchen.de")
            .addHardBreak()
            .addText(
                "                                                                                                                                                                                                                                                              ")
            .addText(
                "                       Internet:                                                   ",
                "strong")
            .addText(
                "                                                                                                                                                                                                                                                       ")
            .addLink(
                "                                             www.bsb-muenchen.de",
                "https://www.bsb-muenchen.de/")
            .addText(
                "                                                                                        ")
            .addHardBreak()
            .addText(
                "                                                                                                                                                                                                                                                              ")
            .addText("                       Umsatzsteueridentifikationsnummer: ", "strong")
            .addText("DE-811259539                              ")
            .build());
    structuredContentDe.addContentBlock(
        new ParagraphBuilder()
            .addText("Die Bayerische Staatsbibliothek ist eine dem ")
            .addLinkWithTitle(
                "Bayerischen Staatsministerium für Wissenschaft und Kunst",
                "https://www.stmwk.bayern.de/",
                "")
            .addText(
                "  nachgeordnete Behörde der Mittelstufe mit dem Sitz in München.                               ")
            .build());
    content.put(Locale.GERMAN, structuredContentDe);
    content.put(Locale.ENGLISH, new StructuredContent());

    Webpage webpage = new Webpage();
    webpage.setText(content);
    webpage.setLabel(new LocalizedText(Locale.forLanguageTag("de"), "Impressum (DEV)"));
    webpage.setUuid(extractFirstUuidFromPath(path));
    webpage.setPublicationStart(LocalDate.parse("2019-01-18"));
    webpage.setType(IdentifiableType.ENTITY);
    webpage.setChildren(List.of());

    when(webpageService.get(any(UUID.class))).thenReturn(webpage);

    mockMvc
        .perform(get(path))
        .andExpect(status().isOk())
        .andExpect(content().contentType(ContentType.APPLICATION_JSON.getMimeType()))
        .andExpect(content().json(getJsonFromFileResource(path)));
  }

  // ---------------------------------------------------------------
  private static class ParagraphBuilder {

    Paragraph paragraph = new Paragraph();

    public ParagraphBuilder addText(String text) {
      paragraph.addContentBlock(new Text(text));
      return this;
    }

    public ParagraphBuilder addText(String text, String... marks) {
      paragraph.addContentBlock(new Text(text, marks));
      return this;
    }

    public ParagraphBuilder addHardBreak() {
      paragraph.addContentBlock(new HardBreak());
      return this;
    }

    public ParagraphBuilder addLink(String text, String href) {
      Text linkText = new Text(text);
      Mark mark = new Mark("link");
      mark.addAttribute("href", href);
      linkText.addMark(mark);
      paragraph.addContentBlock(linkText);
      return this;
    }

    public ParagraphBuilder addLinkWithTitle(String text, String href, String title) {
      Text linkText = new Text(text);
      Mark mark = new Mark("link");
      mark.addAttribute("href", href);
      mark.addAttribute("title", title);
      linkText.addMark(mark);
      paragraph.addContentBlock(linkText);
      return this;
    }

    public Paragraph build() {
      return paragraph;
    }
  }
}
