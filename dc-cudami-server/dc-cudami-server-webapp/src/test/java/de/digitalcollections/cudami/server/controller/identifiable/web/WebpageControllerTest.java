package de.digitalcollections.cudami.server.controller.identifiable.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.web.WebpageService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.identifiable.entity.Website;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(WebpageController.class)
@DisplayName("The WebpageController")
class WebpageControllerTest extends BaseControllerTest {

  @MockBean private LocaleService localeService;
  @MockBean private WebpageService webpageService;

  @DisplayName("returns the website metadata")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v3/webpages/6d52141c-5c5d-48b4-aee8-7df5404d245e/website",
        "/latest/webpages/6d52141c-5c5d-48b4-aee8-7df5404d245e/website"
      })
  public void pagedRootpages(String path) throws Exception {
    Website expected =
        Website.builder()
            .withLabel(Locale.GERMAN, "MDZ Homepage Relaunch")
            .withUuid("7a2f1935-c5b8-40fb-8622-c675de0a6242")
            .withRefId(29)
            .build();

    when(webpageService.getWebsite(any(UUID.class))).thenReturn(expected);

    testJson(path);
  }
}
