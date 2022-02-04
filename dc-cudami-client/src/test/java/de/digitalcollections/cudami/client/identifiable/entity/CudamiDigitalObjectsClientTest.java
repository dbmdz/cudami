package de.digitalcollections.cudami.client.identifiable.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.resource.FileResource;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The client for DigitalObjects")
class CudamiDigitalObjectsClientTest
    extends BaseCudamiEntitiesClientTest<DigitalObject, CudamiDigitalObjectsClient> {

  @Test
  @DisplayName("can retrieve the list of reduced DigitalObjects")
  public void testFindAllReduced() throws Exception {
    String bodyJson = "[{\"entityType\":\"DIGITAL_OBJECT\"}]";
    when(httpResponse.body()).thenReturn(bodyJson.getBytes(StandardCharsets.UTF_8));

    List<DigitalObject> actual = client.findAllReduced();
    assertThat(actual).isNotNull();
    assertThat(actual.get(0)).isExactlyInstanceOf(DigitalObject.class);

    verifyHttpRequestByMethodAndRelativeURL("get", "/reduced");
  }

  @Test
  @DisplayName("can find a number of random DigitalObjects")
  public void testFindRandomDigitalObjects() throws Exception {
    client.findRandomDigitalObjects(42);
    verifyHttpRequestByMethodAndRelativeURL("get", "/random?pageNumber=0&pageSize=42");
  }

  @Test
  @DisplayName("can retrieve active collections of a DigitalObject by a SearchPageRequest")
  public void testGetActiveCollectionsBySearchPageRequest() throws Exception {
    UUID uuid = UUID.randomUUID();
    client.getActiveCollections(uuid, buildExampleSearchPageRequest());
    verifyHttpRequestByMethodAndRelativeURL(
        "get",
        "/"
            + uuid
            + "/collections?active=true&pageNumber=1&pageSize=2&sortBy=sortable.desc.nullsfirst&searchTerm=foo");
  }

  @Test
  @DisplayName("can retrieve collections of a DigitalObject by a SearchPageRequest")
  public void testGetCollectionsBySearchPageRequest() throws Exception {
    UUID uuid = UUID.randomUUID();
    client.getCollections(uuid, buildExampleSearchPageRequest());
    verifyHttpRequestByMethodAndRelativeURL(
        "get",
        "/"
            + uuid
            + "/collections?pageNumber=1&pageSize=2&sortBy=sortable.desc.nullsfirst&searchTerm=foo");
  }

  @Test
  @DisplayName("can retrieve FileResources for a DigitalObject")
  public void testGetFileResources() throws Exception {
    UUID uuid = UUID.randomUUID();
    client.getFileResources(uuid);
    verifyHttpRequestByMethodAndRelativeURL("get", "/" + uuid + "/fileresources");
  }

  @Test
  @DisplayName("can retrieve ImageFileResources for a DigitalObject")
  public void testGetImageFileResources() throws Exception {
    UUID uuid = UUID.randomUUID();
    client.getImageFileResources(uuid);
    verifyHttpRequestByMethodAndRelativeURL("get", "/" + uuid + "/fileresources/images");
  }

  @Test
  @DisplayName("can retrieve an item for a DigitalObject")
  public void testGetItem() throws Exception {
    UUID uuid = UUID.randomUUID();
    client.getItem(uuid);
    verifyHttpRequestByMethodAndRelativeURL("get", "/" + uuid + "/item");
  }

  @Test
  @DisplayName("can return the languages for all DigitalObjects")
  public void testGetLanguages() throws Exception {
    client.getLanguages();
    verifyHttpRequestByMethodAndRelativeURL("get", "/languages");
  }

  @Test
  @DisplayName(
      "can retrieve the languages of all collections, in which the DigitalObject is contained")
  public void testGetLanguagesForCollections() throws Exception {
    UUID uuid = UUID.randomUUID();
    client.getLanguagesOfCollections(uuid);
    verifyHttpRequestByMethodAndRelativeURL("get", "/" + uuid + "/collections/languages");
  }

  @Test
  @DisplayName(
      "can retrieve the languages of all projects, in which the DigitalObject is contained")
  public void testGetLanguagesForProjects() throws Exception {
    UUID uuid = UUID.randomUUID();
    client.getLanguagesOfProjects(uuid);
    verifyHttpRequestByMethodAndRelativeURL("get", "/" + uuid + "/projects/languages");
  }

  @Test
  @DisplayName("can retrieve projects of a DigitalObject by a SearchPageRequest")
  public void testGetProjectsBySearchPageRequest() throws Exception {
    UUID uuid = UUID.randomUUID();
    client.getProjects(uuid, buildExampleSearchPageRequest());
    verifyHttpRequestByMethodAndRelativeURL(
        "get",
        "/"
            + uuid
            + "/projects?pageNumber=1&pageSize=2&sortBy=sortable.desc.nullsfirst&searchTerm=foo");
  }

  @Test
  @DisplayName("can save a list of FileResources for a DigitalObject")
  public void testSaveFileResources() throws Exception {
    UUID uuid = UUID.randomUUID();
    FileResource fileResource = new FileResource();
    List<FileResource> fileResources = List.of(fileResource);

    client.saveFileResources(uuid, fileResources);

    verifyHttpRequestByMethodRelativeUrlAndRequestBody(
        "post", "/" + uuid + "/fileresources", fileResources);
  }
}
