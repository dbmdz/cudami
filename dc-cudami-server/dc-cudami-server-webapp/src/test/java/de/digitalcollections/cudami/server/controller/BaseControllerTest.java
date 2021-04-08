package de.digitalcollections.cudami.server.controller;

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
import org.springframework.test.web.servlet.MockMvc;

public abstract class BaseControllerTest {

  @Autowired protected MockMvc mockMvc;

  protected <T> PageResponse<T> buildStandardPageResponse(Class<T> type, List<T> content) {
    PageResponse<T> pageResponse = new PageResponse<>();
    pageResponse.setContent(content);
    pageResponse.setTotalElements(content.size());
    PageRequest pageRequest = new PageRequestBuilder().pageSize(25).pageNumber(0).build();
    pageResponse.setPageRequest(pageRequest);
    return pageResponse;
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

  protected String getHtmlFromFileResource(String sourcePath)
      throws URISyntaxException, IOException {
    String path = sourcePath.replaceAll("[?&].*", "");
    String fullPath = "html" + path;
    Path pathToRessource = getPath(fullPath);
    return Files.readAllLines(pathToRessource).stream()
        .map(l -> l.replaceAll("^\\s+", ""))
        .collect(Collectors.joining());
  }

  protected String getJsonFromFileResource(String sourcePath) throws IOException {
    String path = sourcePath.replaceAll("[?&].*", "");
    UUID uuid = extractFirstUuidFromPath(sourcePath);
    if (uuid != null) {
      // replace slash behind the first UUID with an underscore
      path = path.replaceAll(uuid.toString() + "/", uuid.toString() + "_");
    }
    String suffix = (path.endsWith(".json") ? "" : ".json");
    String fullPath = "json" + path + suffix;
    Path pathToResource = getPath(fullPath);
    return Files.readString(pathToResource);
  }

  private Path getPath(String fullPath) throws RuntimeException {
    ClassLoader classLoader = getClass().getClassLoader();
    URL resource = classLoader.getResource(fullPath);
    if (resource == null) {
      throw new RuntimeException("Cannot read " + fullPath + " (null)");
    }
    final Path pathToResource;
    try {
      pathToResource = Path.of(resource.toURI());
    } catch (URISyntaxException ex) {
      throw new RuntimeException("Cannot read " + fullPath + " (URI syntax wrong)");
    }
    return pathToResource;
  }

  protected String getXmlFromFileResource(String sourcePath) throws IOException {
    String path = sourcePath.replaceAll("[?&].*", "");
    String suffix = (path.endsWith(".xml") ? "" : ".xml");
    String fullPath = "xml" + path + suffix;
    Path pathToResource = getPath(fullPath);
    return Files.readString(pathToResource);
  }
}
