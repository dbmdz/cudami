package de.digitalcollections.cudami.server.controller.identifiable.entity.parts;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.parts.WebpageService;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import de.digitalcollections.model.impl.identifiable.entity.parts.WebpageImpl;
import de.digitalcollections.model.jackson.DigitalCollectionsObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.UUID;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@ActiveProfiles("TEST")
@AutoConfigureMockMvc
@SpringBootTest
public class WebpageHtmlControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private WebpageService webpageService;

  @Test
  public void shouldReturnValidHtml() throws Exception {
    prepareWebpageMock();
    MvcResult result =
        mockMvc
            .perform(get("/latest/webpages/{uuid}.html", "00000000-0000-0000-0000-000000000000"))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();
    String content = result.getResponse().getContentAsString();
    File contentFile = File.createTempFile("content", ".html");
    contentFile.deleteOnExit();
    Files.write(contentFile.toPath(), content.getBytes());
    SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
    Schema schema = factory.newSchema(new ClassPathResource("xhtml11.xsd").getFile());
    Validator validator = schema.newValidator();
    Source source = new StreamSource(contentFile);
    assertThatCode(() -> validator.validate(source)).doesNotThrowAnyException();
  }

  private void prepareWebpageMock() {
    final ObjectMapper mapper = new DigitalCollectionsObjectMapper();
    Webpage dummyWebpage;
    try {
      final String dummyWebpageJson =
          Resources.toString(Resources.getResource("webpage.json"), Charset.defaultCharset());
      dummyWebpage = mapper.readValue(dummyWebpageJson, Webpage.class);
    } catch (IOException ex) {
      dummyWebpage = new WebpageImpl();
    }
    Mockito.when(webpageService.get(UUID.fromString("00000000-0000-0000-0000-000000000000")))
        .thenReturn(dummyWebpage);
  }
}
