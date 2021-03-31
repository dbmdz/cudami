package de.digitalcollections.cudami.server.controller;

import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.WebsiteService;
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

  @MockBean protected WebsiteService websiteService;

  @MockBean protected LocaleService localeService;

  protected String getJsonFromFileResource(String sourcePath) throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    String path = sourcePath.replaceAll("[?&].*", "");

    String suffix = (path.endsWith(".json") ? "" : ".json");
    String fullPath = "json" + path + suffix;
    URL resource = classLoader.getResource(fullPath);
    try {
      return Files.readString(Path.of(resource.toURI()));
    } catch (Exception e) {
      throw new IOException(
          "Cannot read expected json for sourcePath="
              + sourcePath
              + " from resource path="
              + fullPath
              + ": "
              + e,
          e);
    }
  }

  protected String getHtmlFromFileResource(String sourcePath)
      throws URISyntaxException, IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    String path = sourcePath.replaceAll("[?&].*", "");

    String filename = "html" + path;
    URL resource = classLoader.getResource(filename);
    if (resource == null) {
      throw new RuntimeException("Cannot read " + filename);
    }
    return Files.readAllLines(Path.of(resource.toURI())).stream()
        .map(l -> l.replaceAll("^\\s+", ""))
        .collect(Collectors.joining());
  }

  protected String getXmlFromFileResource(String sourcePath) throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    String path = sourcePath.replaceAll("[?&].*", "");

    String suffix = (path.endsWith(".xml") ? "" : ".xml");
    String fullPath = "xml" + path + suffix;
    URL resource = classLoader.getResource(fullPath);
    try {
      return Files.readString(Path.of(resource.toURI()));
    } catch (Exception e) {
      throw new IOException(
          "Cannot read expected xml for sourcePath="
              + sourcePath
              + " from resource path="
              + fullPath
              + ": "
              + e,
          e);
    }
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
