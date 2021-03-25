package de.digitalcollections.cudami.server.controller;

import de.digitalcollections.cudami.server.business.api.service.identifiable.web.WebpageService;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

public abstract class BaseControllerTest {

  @Autowired
  protected MockMvc mockMvc;

  @MockBean
  protected WebpageService webpageService;

  protected String getJsonFromFileResource(String path) throws URISyntaxException, IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    URL resource = classLoader.getResource("json" + path);
    return Files.readString(Path.of(resource.toURI()));
  }

}
