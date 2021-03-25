package de.digitalcollections.cudami.server.controller.v2.identifiable.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.text.LocalizedStructuredContent;
import de.digitalcollections.model.text.StructuredContent;
import de.digitalcollections.model.text.contentblock.Text;
import java.util.Locale;
import java.util.UUID;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

@WebMvcTest(V2WebpageController.class)
public class AnotherWebpageV2ControllerTest extends BaseControllerTest {

  @DisplayName(
      "Returns a webpage in v2 json format for UUID only, when json is demanded explicitly")
  @ParameterizedTest
  @CsvSource({"/v2/webpages/123e4567-e89b-12d3-a456-426614174000.json"})
  public void returnWebpageV2Json(String path) throws Exception {
    LocalizedStructuredContent content = new LocalizedStructuredContent();
    StructuredContent structuredContentDe = new StructuredContent();
    structuredContentDe.addContentBlock(new Text("Hallo"));
    content.put(Locale.GERMAN, structuredContentDe);
    Webpage webpage = new Webpage();
    webpage.setText(content);
    when(webpageService.get(any(UUID.class))).thenReturn(webpage);

    mockMvc.perform(get(path))
        .andExpect(status().isOk())
        .andExpect(content().contentType(ContentType.APPLICATION_JSON.getMimeType()))
        .andExpect(content().json(getJsonFromFileResource(path)));
  }
}