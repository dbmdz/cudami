package de.digitalcollections.cudami.client.identifiable.entity;

import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.resource.FileResource;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The client for DigitalObjects")
class CudamiDigitalObjectsClientTest
    extends BaseCudamiEntitiesClientTest<DigitalObject, CudamiDigitalObjectsClient> {

  @Test
  @DisplayName("can find a number of random DigitalObjects")
  public void testFindRandomDigitalObjects() throws Exception {
    client.getRandomDigitalObjects(42);
    verifyHttpRequestByMethodAndRelativeURL("get", "/random?count=42");
  }

  @Test
  @DisplayName("can retrieve active collections of a DigitalObject by a PageRequest")
  public void testGetActiveCollectionsByPageRequest() throws Exception {
    UUID uuid = UUID.randomUUID();
    client.findActiveCollections(uuid, buildExamplePageRequest());
    verifyHttpRequestByMethodAndRelativeURL(
        "get",
        "/"
            + uuid
            + "/collections?active=true&pageNumber=1&pageSize=2&sortBy=sortable.desc.nullsfirst.ignorecase&filtering=foo:eq:bar;gnarf:eq:krchch");
  }

  @Test
  @DisplayName("can retrieve collections of a DigitalObject by a PageRequest")
  public void testGetCollectionsByPageRequest() throws Exception {
    UUID uuid = UUID.randomUUID();
    client.findCollections(uuid, buildExamplePageRequest());
    verifyHttpRequestByMethodAndRelativeURL(
        "get",
        "/"
            + uuid
            + "/collections?pageNumber=1&pageSize=2&sortBy=sortable.desc.nullsfirst.ignorecase&filtering=foo:eq:bar;gnarf:eq:krchch");
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
  @DisplayName("can retrieve projects of a DigitalObject by a PageRequest")
  public void testGetProjectsByPageRequest() throws Exception {
    UUID uuid = UUID.randomUUID();
    client.findProjects(uuid, buildExamplePageRequest());
    verifyHttpRequestByMethodAndRelativeURL(
        "get",
        "/"
            + uuid
            + "/projects?pageNumber=1&pageSize=2&sortBy=sortable.desc.nullsfirst.ignorecase&filtering=foo:eq:bar;gnarf:eq:krchch");
  }

  @Test
  @DisplayName("can save a list of FileResources for a DigitalObject")
  public void testSaveFileResources() throws Exception {
    UUID uuid = UUID.randomUUID();
    FileResource fileResource = new FileResource();
    List<FileResource> fileResources = List.of(fileResource);

    client.setFileResources(uuid, fileResources);

    verifyHttpRequestByMethodRelativeUrlAndRequestBody(
        "post", "/" + uuid + "/fileresources", fileResources);
  }

  @Test
  @DisplayName("can retrieve all ADOs for a parent DigitalObject")
  public void retrieveAdosForParent() throws Exception {
    DigitalObject parent =
        DigitalObject.builder().uuid(UUID.randomUUID()).label(Locale.GERMAN, "Parent").build();

    client.getAllForParent(parent);

    verifyHttpRequestByMethodAndRelativeURL(
        "get", "?pageNumber=0&pageSize=10000&filtering=parent.uuid:eq:" + parent.getUuid());
  }
}
