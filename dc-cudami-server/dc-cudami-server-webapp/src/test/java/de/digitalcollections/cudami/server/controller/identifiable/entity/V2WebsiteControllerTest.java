package de.digitalcollections.cudami.server.controller.identifiable.entity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.cudami.server.model.DescriptionBuilder;
import de.digitalcollections.cudami.server.model.WebpageBuilder;
import de.digitalcollections.model.identifiable.IdentifiableType;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.text.LocalizedText;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

@WebMvcTest(V2WebsiteController.class)
class V2WebsiteControllerTest extends BaseControllerTest {

  @DisplayName(
      "returns a website in v2 json format for UUID, with or without json suffix in the url")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v2/websites/7ebaf4b1-cf5a-491b-991c-4fd082677ff9.json",
        "/v2/websites/7ebaf4b1-cf5a-491b-991c-4fd082677ff9"
      })
  public void returnWebsiteV2Json(String path) throws Exception {
    Website website = new Website();
    website.setCreated(LocalDateTime.parse("2018-05-04T09:05:47.493"));
    website.setLastModified(LocalDateTime.parse("2018-05-04T09:05:47.493"));
    website.setIdentifiers(Set.of());
    website.setLabel(new LocalizedText(Locale.GERMAN, "Altsinica"));
    website.setDescription(new DescriptionBuilder().setLanguage("de").setType("paragraph").build());
    website.setType(IdentifiableType.ENTITY);
    website.setUuid(extractFirstUuidFromPath(path));
    website.setRefId(84);
    website.setUrl(new URL("https://ostasien.digitale-sammlungen.de/"));
    website.setRootPages(
        List.of(
            new WebpageBuilder()
                .setUUID("6d9adace-187a-4f14-9a5a-e768558028a3")
                .setCreated("2018-05-04T09:06:05.333")
                .setLastModified("2020-09-30T16:23:44.393791")
                .setLabel(Map.of("de", "Impressum"))
                .setPublicationStart("2020-09-30")
                .setIdentifiers(Set.of())
                .build(),
            new WebpageBuilder()
                .setUUID("b0739393-2fdc-4703-8af1-c3b440292872")
                .setCreated("2020-03-12T12:28:57.082438")
                .setLastModified("2020-09-30T16:24:43.844093")
                .setLabel(Map.of("de", "Barrierefreiheit"))
                .setPublicationStart("2020-09-30")
                .setIdentifiers(Set.of())
                .build(),
            new WebpageBuilder()
                .setUUID("cbb85056-5e30-49cf-bd87-fd09486b9aa9")
                .setCreated("2018-05-04T09:06:19.201")
                .setLastModified("2020-09-30T16:24:23.379512")
                .setLabel(Map.of("de", "Datenschutzerklärung"))
                .setPublicationStart("2020-09-30")
                .setIdentifiers(Set.of())
                .build()));

    when(websiteService.get(any(UUID.class))).thenReturn(website);

    mockMvc
        .perform(get(path))
        .andExpect(status().isOk())
        .andExpect(content().contentType(ContentType.APPLICATION_JSON.getMimeType()))
        .andExpect(content().json(getJsonFromFileResource(path)));
  }
}
