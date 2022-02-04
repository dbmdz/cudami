package de.digitalcollections.cudami.client.identifiable.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.client.identifiable.BaseCudamiIdentifiablesClientTest;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The client for FileResourcesMetadata")
class CudamiFileResourcesMetadataClientTest
    extends BaseCudamiIdentifiablesClientTest<FileResource, CudamiFileResourcesMetadataClient> {

  @Test
  @DisplayName("can find FileResources by their type")
  public void testFindFileResourcesByType() throws Exception {
    client.findFileResourcesByType(new SearchPageRequest(), "jpg");
    verifyHttpRequestByMethodAndRelativeURL("get", "/type/jpg?pageNumber=0&pageSize=0");
  }

  @Test
  @DisplayName("can execute the find method with a SearchPageRequest")
  @Override
  public void testFindWithSearchPageRequest() throws Exception {
    String bodyJson =
        "{\"content\":[{\"objectType\":\"FILE_RESOURCE\", \"fileResource\":{\"entityType\":\"FILE_RESOURCE\",\"identifiableType\":\"ENTITY\"}}]}";
    when(httpResponse.body()).thenReturn(bodyJson.getBytes(StandardCharsets.UTF_8));

    SearchPageRequest searchPageRequest = new SearchPageRequest();
    SearchPageResponse<FileResource> response = client.find(searchPageRequest);
    assertThat(response).isNotNull();
    assertThat(response.getContent().get(0)).isExactlyInstanceOf(FileResource.class);

    verifyHttpRequestByMethodAndRelativeURL("get", "?pageNumber=0&pageSize=0");
  }

  @Test
  @DisplayName("can execute the find method with a search term and max results")
  @Override
  public void testFindWithSearchTermAndMaxResults() throws Exception {
    String bodyJson = "{\"content\":[]}";
    when(httpResponse.body()).thenReturn(bodyJson.getBytes(StandardCharsets.UTF_8));

    assertThat(client.find("foo", 100)).isNotNull();

    verifyHttpRequestByMethodAndRelativeURL("get", "?pageNumber=0&pageSize=100&searchTerm=foo");
  }

  @Test
  @DisplayName("can return the languages for all FileResources")
  public void testGetLanguages() throws Exception {
    client.getLanguages();
    verifyHttpRequestByMethodAndRelativeURL("get", "/languages");
  }
}
