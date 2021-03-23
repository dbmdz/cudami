package de.digitalcollections.cudami.server.controller.v2.identifiable.web;

import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.digitalcollections.cudami.server.business.api.service.identifiable.web.WebpageService;
import de.digitalcollections.cudami.server.config.SpringConfigWeb;
import de.digitalcollections.model.identifiable.web.Webpage;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

@DisplayName("The WebpageController v2")
@WebMvcTest(controllers = V2WebpageController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ComponentScan(basePackageClasses = V2WebpageController.class)
@ContextConfiguration(classes = SpringConfigWeb.class)
class V2WebpageControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  WebpageService webpageService;

  @DisplayName("Returns a webpage in v2 json format for UUID only, when json is demanded explicitly")
  @Test
  public void returnWebpageV2Json() throws Exception {
    Webpage webpage = new Webpage();
    when(webpageService.get(any(UUID.class))).thenReturn(webpage);

    mockMvc.perform(get("/v2/webpages/123e4567-e89b-12d3-a456-426614174000.json")).andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("{\"identifiers\":[],\"type\":\"RESOURCE\"}")));
  }
}