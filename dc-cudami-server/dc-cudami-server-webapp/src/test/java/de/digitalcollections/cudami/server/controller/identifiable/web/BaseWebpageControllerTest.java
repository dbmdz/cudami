package de.digitalcollections.cudami.server.controller.identifiable.web;

import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.identifiable.IdentifiableType;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.text.LocalizedStructuredContent;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.text.StructuredContent;
import de.digitalcollections.model.text.contentblock.ParagraphBuilder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

public class BaseWebpageControllerTest extends BaseControllerTest {

  public Webpage createPrefilledWebpage(String path) {
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
    webpage.setCreated(LocalDateTime.parse("2019-01-16T10:06:31.747"));
    webpage.setLastModified(LocalDateTime.parse("2019-01-18T10:26:17.527"));

    return webpage;
  }
}
