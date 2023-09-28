package de.digitalcollections.cudami.server.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

public abstract class BaseControllerTest {

  @Autowired private MockMvc mockMvc;

  @Deprecated
  protected <T> PageResponse<T> buildStandardPageResponse(Class<T> type, List<T> content) {
    PageResponse<T> pageResponse = new PageResponse<>();
    pageResponse.setContent(content);
    pageResponse.setTotalElements(content.size());
    PageRequest pageRequest = PageRequest.builder().pageSize(25).pageNumber(0).build();
    pageResponse.setRequest(pageRequest);
    return pageResponse;
  }

  protected UUID extractNthUuidFromPath(String path, int n) {
    Pattern uuidPattern =
        Pattern.compile("(\\b[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12})");
    Matcher matcher = uuidPattern.matcher(path);
    if (matcher.find()) {
      for (int i = 0; i < n; i++) {
        matcher.find();
      }
      return UUID.fromString(matcher.group(0));
    }
    return null;
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
      path = path.replaceAll(uuid + "/", uuid + "_");
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

  protected void testDeleteJsonSuccessful(String path, String jsonBody) throws Exception {
    mockMvc
        .perform(delete(path).contentType(MediaType.APPLICATION_JSON).content(jsonBody))
        .andExpect(status().is(HttpStatus.NO_CONTENT.value()));
  }

  /**
   * Check, if the JSON result from a GET request to the given path matches the contents of a JSON
   * file, which lies in src/test/resources under the identically constructed path ( and a .json
   * suffix of the file name)
   *
   * @param path the path of the HTTP GET request and of the JSON file, against which the validation
   *     is made (the file gets a .json suffix)
   * @throws Exception in case of an error
   */
  protected void testJson(String path) throws Exception {
    mockMvc
        .perform(get(path))
        .andExpect(status().isOk())
        .andExpect(content().contentType(ContentType.APPLICATION_JSON.getMimeType()))
        .andExpect(content().json(getJsonFromFileResource(path)));
  }

  /**
   * Check, if the JSON result for a GET request to the given path matches the contents of a JSON
   * file, which resides in the provided expectedJsonPath.
   *
   * @param path the path of the HTTP GET request
   * @param expectedJsonPath the location of the JSON file, against which the validation is made
   * @throws Exception in case of an error
   */
  protected void testJson(String path, String expectedJsonPath) throws Exception {
    mockMvc
        .perform(get(path))
        .andExpect(status().isOk())
        .andExpect(content().contentType(ContentType.APPLICATION_JSON.getMimeType()))
        .andExpect(content().json(getJsonFromFileResource(expectedJsonPath)));
  }

  /**
   * Checks, if the JSON result of a POST request to the given path with the jsonBody as payload is
   * equal to the contents of a JSON file, which resides in the expectedJsonPath
   *
   * @param path the path of the HTTP POST request
   * @param jsonBody the JSON body of the HTTP POST request
   * @param expectedJsonPath the location of the JSON file, against which the validation is made
   * @throws Exception in case of an error
   */
  protected void testPostJson(String path, String jsonBody, String expectedJsonPath)
      throws Exception {
    mockMvc
        .perform(post(path).contentType(MediaType.APPLICATION_JSON).content(jsonBody))
        .andExpect(status().isOk())
        .andExpect(content().contentType(ContentType.APPLICATION_JSON.getMimeType()))
        .andExpect(content().json(getJsonFromFileResource(expectedJsonPath)));
  }

  /**
   * Checks, if a POST request with the provided jsonBody as payload to the provided path returns
   * the expectedState as result
   *
   * @param path the path of the HTTP POST request
   * @param jsonBody the JSON body of the HTTP POST request
   * @param expectedState the expected HTTP state (result code)
   * @throws Exception in case of an error
   */
  protected void testPostJsonWithState(String path, String jsonBody, int expectedState)
      throws Exception {
    mockMvc
        .perform(post(path).contentType(MediaType.APPLICATION_JSON).content(jsonBody))
        .andExpect(status().is(expectedState));
  }

  /**
   * Checks, if the JSON result of a PUT request to the given path with the jsonBody as payload is
   * equal to the contents of a JSON file, which resides in the expectedJsonPath
   *
   * @param path the path of the HTTP PUT request
   * @param jsonBody the JSON body of the HTTP PUT request
   * @param expectedJsonPath the location of the JSON file, against which the validation is made
   * @throws Exception in case of an error
   */
  protected void testPutJson(String path, String jsonBody, String expectedJsonPath)
      throws Exception {
    mockMvc
        .perform(put(path).contentType(MediaType.APPLICATION_JSON).content(jsonBody))
        .andExpect(status().isOk())
        .andExpect(content().contentType(ContentType.APPLICATION_JSON.getMimeType()))
        .andExpect(content().json(getJsonFromFileResource(expectedJsonPath)));
  }

  /**
   * Checks, if a PUT request with the provided jsonBody as payload to the provided path returns the
   * expectedState as result
   *
   * @param path the path of the HTTP PUT request
   * @param jsonBody the JSON body of the HTTP PUT request
   * @param expectedState the expected HTTP state (result code)
   * @throws Exception in case of an error
   */
  protected void testPutJsonWithState(String path, String jsonBody, int expectedState)
      throws Exception {
    if (jsonBody != null) {
      mockMvc
          .perform(put(path).contentType(MediaType.APPLICATION_JSON).content(jsonBody))
          .andExpect(status().is(expectedState));
    } else {
      mockMvc
          .perform(put(path).contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().is(expectedState));
    }
  }

  /**
   * Checks, if an HTTP GET request to a path is successful and returns status 200
   *
   * @param path the path of the HTTP GET request
   * @throws Exception in case of an error
   */
  protected void testHttpGet(String path) throws Exception {
    mockMvc.perform(get(path)).andExpect(status().isOk());
  }

  /**
   * Checks, if an HTTP GET request to a path returns the expected status code
   *
   * @param path the path of the HTTP GET request
   * @param expectedStatus the expected Status
   * @throws Exception in case of an error
   */
  protected void testHttpGetWithExpectedStatus(String path, int expectedStatus) throws Exception {
    mockMvc.perform(get(path)).andExpect(status().is(expectedStatus));
  }

  /**
   * Check, if the HTML result from a GET request to the given path matches the contents of a HTML
   * file, which lies in src/test/resources under the identically constructed path ( and a .html
   * suffix of the file name).
   *
   * <p>Empty title attributes on a link are ignored!
   *
   * @param path the path of the HTTP GET request and of the HTML file, against which the validation
   *     is made (the file gets a .html suffix)
   * @throws Exception in case of an error
   */
  protected void testHtml(String path) throws Exception {
    String actual = mockMvc.perform(get(path)).andReturn().getResponse().getContentAsString();
    String expected = getHtmlFromFileResource(path);

    Document actualDocument = Jsoup.parse(actual).normalise();
    removeEmptyTitleAttributes(actualDocument);
    Document expectedDocument = Jsoup.parse(expected).normalise();
    removeEmptyTitleAttributes(expectedDocument);

    assertThat(actualDocument.html()).isEqualTo(expectedDocument.html());
  }

  private void removeEmptyTitleAttributes(Document document) {
    document
        .select("a")
        .forEach(
            l -> {
              String titleAttribute = l.attr("title");
              if (titleAttribute.isEmpty()) {
                l.removeAttr("title");
              }
            });
  }

  /**
   * Check, if the XML result from a GET request to the given path matches the contents of an XML
   * file, which lies in src/test/resources under the identically constructed path ( and a .xml
   * suffix of the file name)
   *
   * @param path the path of the HTTP GET request and of the XML file, against which the validation
   *     is made (the file gets a .xml suffix)
   * @throws Exception in case of an error
   */
  protected void testXml(String path) throws Exception {
    mockMvc
        .perform(get(path))
        .andExpect(status().isOk())
        .andExpect(
            content().contentType(ContentType.APPLICATION_XML.getMimeType() + ";charset=UTF-8"))
        .andExpect(content().xml(getXmlFromFileResource(path)));
  }

  /**
   * Check, if the JSON result from a GET request to the given path matches the contents of the
   * expected JSON string
   *
   * @param path the path of the HTTP GET request
   * @param expected the expected JSON string
   * @throws Exception in case of an error
   */
  protected void testGetJsonString(String path, String expected) throws Exception {
    mockMvc
        .perform(get(path))
        .andExpect(status().isOk())
        .andExpect(content().contentType(ContentType.APPLICATION_JSON.getMimeType()))
        .andExpect(content().string(expected));
  }

  /**
   * Verifies, that an HTTP GET request to the given path results in an HTTP not found state
   *
   * @param path the path of the HTTP GET request
   * @throws Exception in case of an error
   */
  protected void testNotFound(String path) throws Exception {
    mockMvc.perform(get(path)).andExpect(status().isNotFound());
  }

  /**
   * Verifies, that an HTTP GET request to the given path results in an internal server error
   *
   * @param path the path of the HTTP GET request
   * @throws Exception in case of an error
   */
  protected void testInternalError(String path) throws Exception {
    mockMvc.perform(get(path)).andExpect(status().is(500));
  }

  /**
   * Verifies, that an HTTP DELETE request results in a resource not found Exception (HTTP status
   * 404)
   *
   * @param path the path of the HTTP DELETE request
   * @throws Exception in case of an error
   */
  protected void testDeleteNotFound(String path) throws Exception {
    mockMvc.perform(delete(path)).andExpect(status().isNotFound());
  }

  /**
   * Verifies, that an HTTP DELETE request was successful and returns the NO CONTENT state (code
   * 204)
   *
   * @param path the path of the HTTP DELETE request
   * @thries Exception in case of an error
   */
  protected void testDeleteSuccessful(String path) throws Exception {
    mockMvc.perform(delete(path)).andExpect(status().is(HttpStatus.NO_CONTENT.value())); // 204
  }
}
