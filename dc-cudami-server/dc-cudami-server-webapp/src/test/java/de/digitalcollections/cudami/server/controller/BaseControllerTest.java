package de.digitalcollections.cudami.server.controller;

import de.digitalcollections.cudami.server.business.api.service.identifiable.web.WebpageService;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

public abstract class BaseControllerTest {

  @Autowired protected MockMvc mockMvc;

  @MockBean protected WebpageService webpageService;

  protected String getJsonFromFileResource(String path) throws URISyntaxException, IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    String suffix = (path.endsWith(".json") ? "" : ".json");
    URL resource = classLoader.getResource("json" + path + suffix);
    return Files.readString(Path.of(resource.toURI()));
  }

  protected String getHtmlFromFileResource(String path) throws URISyntaxException, IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    String filename = "html" + path;
    if (filename.contains("pLocale=")) {
      // Remove the pLocale part from the filename, since the localization is part of the service
      // and not a domain of the controller. So, our tests will never return different localized
      // contents for the same UUID
      filename = filename.replaceFirst("(\\?|&)pLocale=.*?(&|$)", "");
    }

    URL resource = classLoader.getResource(filename);
    if (resource == null) {
      throw new RuntimeException("Cannot read " + filename);
    }
    return Files.readAllLines(Path.of(resource.toURI())).stream()
        .map(l -> l.replaceAll("^\\s+", ""))
        .collect(Collectors.joining());
  }

  protected UUID extractFirstUuidFromPath(String path) {
    Pattern uuidPattern =
        Pattern.compile("(\\b[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12})");
    Matcher matcher = uuidPattern.matcher(path);
    if (matcher.find()) {
      return UUID.fromString(matcher.group(0));
    }
    return null;
  }
}
