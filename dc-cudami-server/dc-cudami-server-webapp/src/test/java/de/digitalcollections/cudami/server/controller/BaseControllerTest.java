package de.digitalcollections.cudami.server.controller;

import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.CollectionService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.DigitalObjectService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.ProjectService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.WebsiteService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.web.WebpageService;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageRequestBuilder;
import de.digitalcollections.model.paging.PageResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

public abstract class BaseControllerTest {

  @Autowired protected MockMvc mockMvc;

  @MockBean protected CollectionService collectionService;
  @MockBean protected DigitalObjectService digitalObjectService;
  @MockBean protected LocaleService localeService;
  @MockBean protected ProjectService projectService;
  @MockBean protected WebpageService webpageService;
  @MockBean protected WebsiteService websiteService;

  protected String getJsonFromFileResource(String sourcePath) throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    String path = sourcePath.replaceAll("[?&].*", "");
    UUID uuid = extractFirstUuidFromPath(sourcePath);
    if (uuid != null) {
      // replace slash behind the first UUID with an underscore
      path = path.replaceAll(uuid.toString() + "/", uuid.toString() + "_");
    }

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

  protected <T> PageResponse<T> buildStandardPageResponse(Class<T> type, List content) {
    PageResponse pageResponse = new PageResponse();
    pageResponse.setContent(content);
    pageResponse.setTotalElements(content.size());
    PageRequest pageRequest = new PageRequestBuilder().pageSize(25).pageNumber(0).build();
    pageResponse.setPageRequest(pageRequest);
    return pageResponse;
  }
}
