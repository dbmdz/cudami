package de.digitalcollections.cudami.server.controller.identifiable.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.text.LocalizedText;
import java.util.Locale;
import java.util.UUID;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

@WebMvcTest(WebpageController.class)
@DisplayName("The WebpageController")
class WebpageControllerTest extends BaseControllerTest {

  @DisplayName("returns the website metadata")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v3/webpages/6d52141c-5c5d-48b4-aee8-7df5404d245e/website",
        "/latest/webpages/6d52141c-5c5d-48b4-aee8-7df5404d245e/website"
      })
  public void pagedRootpages(String path) throws Exception {

    Website expected = new Website();
    expected.setLabel(new LocalizedText(Locale.GERMAN, "MDZ Homepage Relaunch"));
    expected.setUuid(UUID.fromString("7a2f1935-c5b8-40fb-8622-c675de0a6242"));
    expected.setRefId(29);

    when(webpageService.getWebsite(any(UUID.class))).thenReturn(expected);

    mockMvc
        .perform(get(path))
        .andExpect(status().isOk())
        .andExpect(content().contentType(ContentType.APPLICATION_JSON.getMimeType()))
        .andExpect(content().json(getJsonFromFileResource(path)));
  }
}
