package de.digitalcollections.cudami.server.controller.identifiable.entity;

import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.WebsiteService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(WebsiteController.class)
@DisplayName("The WebsiteController")
class WebsiteControllerTest extends BaseControllerTest {

  @MockBean private WebsiteService websiteService;

  @DisplayName("returns all languages")
  @ParameterizedTest
  @ValueSource(strings = {"/v2/websites/languages", "/latest/websites/languages"})
  public void getAllLanguages(String path) throws Exception {
    List<Locale> expected = List.of(Locale.GERMAN);
    when(websiteService.getLanguages()).thenReturn(expected);

    testJson(path);
  }
}
