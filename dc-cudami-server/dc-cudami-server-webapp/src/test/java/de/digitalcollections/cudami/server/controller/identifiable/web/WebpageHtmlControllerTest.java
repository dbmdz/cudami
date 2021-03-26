package de.digitalcollections.cudami.server.controller.identifiable.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.cudami.server.model.HeadingBuilder;
import de.digitalcollections.cudami.server.model.ParagraphBuilder;
import de.digitalcollections.model.identifiable.IdentifiableType;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.text.LocalizedStructuredContent;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.text.StructuredContent;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

@WebMvcTest(WebpageHtmlController.class)
public class WebpageHtmlControllerTest extends BaseControllerTest {

  @DisplayName("returns a webpage in v3 html format for UUID")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v3/webpages/fae19e02-5fa8-4b5e-8bd8-ff5456371e53.html",
        "/v3/webpages/fae19e02-5fa8-4b5e-8bd8-ff5456371e53.html?pLocale=de_DE"
      })
  public void returnWebpageV3Html(String path) throws Exception {
    LocalizedStructuredContent content = new LocalizedStructuredContent();
    StructuredContent structuredContentDe = new StructuredContent();
    structuredContentDe.addContentBlock(
        new HeadingBuilder().setLevel(4).setText("Bayerische Staatsbibliothek ", "strong").build());
    structuredContentDe.addContentBlock(new ParagraphBuilder().addText("Ludwigstraße 16 ").build());
    structuredContentDe.addContentBlock(new ParagraphBuilder().addText("80539 München ").build());
    structuredContentDe.addContentBlock(
        new HeadingBuilder().setLevel(4).setText("Gesetzlicher Vertreter:", "strong").build());
    structuredContentDe.addContentBlock(
        new ParagraphBuilder().addText("Generaldirektor Dr. Klaus Ceynowa ").build());
    structuredContentDe.addContentBlock(
        new ParagraphBuilder().addText("Telefon:", "strong").addText(" +49 89 28638-0 ").build());
    structuredContentDe.addContentBlock(
        new ParagraphBuilder().addText("Fax:", "strong").addText(" +49 89 28638-2200").build());
    structuredContentDe.addContentBlock(
        new ParagraphBuilder()
            .addText("E-Mail:", "strong")
            .addText("  direktion[at]bsb-muenchen.de")
            .build());
    structuredContentDe.addContentBlock(
        new ParagraphBuilder()
            .addText("Internet:", "strong")
            .addText("  www.bsb-muenchen.de")
            .build());
    structuredContentDe.addContentBlock(
        new ParagraphBuilder()
            .addText("Umsatzsteueridentifikationsnummer:", "strong")
            .addText(" DE-811259539")
            .build());
    structuredContentDe.addContentBlock(
        new ParagraphBuilder()
            .addHardBreak()
            .addText(
                "Die Bayerische Staatsbibliothek ist eine dem Bayerischen Staatsministerium für Wissenschaft und Kunst nachgeordnete Behörde der Mittelstufe mit dem Sitz in München. ")
            .build());
    content.put(Locale.GERMAN, structuredContentDe);
    content.put(Locale.ENGLISH, new StructuredContent());

    Webpage webpage = new Webpage();
    webpage.setText(content);
    webpage.setLabel(new LocalizedText(Locale.forLanguageTag("de"), "Impressum"));
    webpage.setUuid(extractFirstUuidFromPath(path));
    webpage.setPublicationStart(LocalDate.parse("2019-01-18"));
    webpage.setType(IdentifiableType.ENTITY);
    webpage.setChildren(List.of());

    if (path.contains("?pLocale")) {
      Locale requestedLocale = LocaleUtils.toLocale(path.split("=")[1]);
      when(webpageService.get(any(UUID.class), eq(requestedLocale))).thenReturn(webpage);
    } else {
      when(webpageService.get(any(UUID.class))).thenReturn(webpage);
    }

    mockMvc
        .perform(get(path))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(ContentType.TEXT_HTML.getMimeType() + ";charset=UTF-8"))
        .andExpect(content().string(getHtmlFromFileResource(path)));
  }
}
