package de.digitalcollections.cudami.server.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

public abstract class BaseControllerTest {

  @Autowired private MockMvc mockMvc;

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

  private String getHtmlFromFileResource(String sourcePath) throws IOException {
    String path = sourcePath.replaceAll("[?&].*", "");
    String fullPath = "html" + path;
    Path pathToRessource = getPath(fullPath);
    return Files.readAllLines(pathToRessource).stream()
        .map(l -> l.replaceAll("^\\s+", ""))
        .collect(Collectors.joining());
  }

  private String getJsonFromFileResource(String sourcePath) throws IOException {
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

  private String getXmlFromFileResource(String sourcePath) throws IOException {
    String path = sourcePath.replaceAll("[?&].*", "");
    String suffix = (path.endsWith(".xml") ? "" : ".xml");
    String fullPath = "xml" + path + suffix;
    Path pathToResource = getPath(fullPath);
    return Files.readString(pathToResource);
  }

  protected void testJson(String path) throws Exception {
    mockMvc
        .perform(get(path))
        .andExpect(status().isOk())
        .andExpect(content().contentType(ContentType.APPLICATION_JSON.getMimeType()))
        .andExpect(content().json(getJsonFromFileResource(path)));
  }

  protected void testHtml(String path) throws Exception {
    mockMvc
        .perform(get(path))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(ContentType.TEXT_HTML.getMimeType() + ";charset=UTF-8"))
        .andExpect(content().string(getHtmlFromFileResource(path)));
  }

  protected void testXml(String path) throws Exception {
    mockMvc
        .perform(get(path))
        .andExpect(status().isOk())
        .andExpect(
            content().contentType(ContentType.APPLICATION_XML.getMimeType() + ";charset=UTF-8"))
        .andExpect(content().xml(getXmlFromFileResource(path)));
  }

  protected void testNotFound(String path) throws Exception {
    mockMvc.perform(get(path)).andExpect(status().isNotFound());
  }
}
