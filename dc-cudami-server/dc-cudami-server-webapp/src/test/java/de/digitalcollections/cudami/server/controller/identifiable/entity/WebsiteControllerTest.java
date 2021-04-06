package de.digitalcollections.cudami.server.controller.identifiable.entity;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import java.util.List;
import java.util.Locale;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

@WebMvcTest(WebsiteController.class)
@DisplayName("The WebsiteController")
class WebsiteControllerTest extends BaseControllerTest {

  @DisplayName("returns all languages")
  @ParameterizedTest
  @ValueSource(strings = {"/v2/websites/languages", "/latest/websites/languages"})
  public void getAllLanguages(String path) throws Exception {
    List<Locale> expected = List.of(Locale.GERMAN);
    when(websiteService.getLanguages()).thenReturn(expected);

    mockMvc
        .perform(get(path))
        .andExpect(status().isOk())
        .andExpect(content().contentType(ContentType.APPLICATION_JSON.getMimeType()))
        .andExpect(content().json(getJsonFromFileResource(path)));
  }
}
