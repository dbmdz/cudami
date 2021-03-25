package de.digitalcollections.cudami.server.controller.v2.identifiable.web;

import static de.digitalcollections.cudami.server.assertj.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.web.WebpageService;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.text.LocalizedStructuredContent;
import de.digitalcollections.model.text.StructuredContent;
import de.digitalcollections.model.text.contentblock.Text;
import java.util.Locale;
import java.util.UUID;
import javax.sql.DataSource;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@DisplayName("The WebpageController v2")
@ActiveProfiles("TEST")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class V2WebpageControllerTest {

  @MockBean WebpageService webpageService;

  @MockBean DataSource dataSource; // Required, whyever...

  @Autowired private TestRestTemplate testRestTemplate;

  @DisplayName(
      "Returns a webpage in v2 json format for UUID only, when json is demanded explicitly")
  @ParameterizedTest
  @CsvSource({"webpagev2.json"})
  public void returnWebpageV2Json(String expectedJsonSource) throws Exception {
    LocalizedStructuredContent content = new LocalizedStructuredContent();
    StructuredContent structuredContentDe = new StructuredContent();
    structuredContentDe.addContentBlock(new Text("Hallo"));
    content.put(Locale.GERMAN, structuredContentDe);
    Webpage webpage = new Webpage();
    webpage.setText(content);
    when(webpageService.get(any(UUID.class))).thenReturn(webpage);

    ResponseEntity<String> entity =
        this.testRestTemplate.getForEntity(
            "/v2/webpages/123e4567-e89b-12d3-a456-426614174000.json", String.class);
    assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(entity.getHeaders()).hasContentType(ContentType.APPLICATION_JSON);
    assertThat(entity.getBody()).isSemanticallyEqualToJsonFromFile(expectedJsonSource);
  }
}
