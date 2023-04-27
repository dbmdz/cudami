package de.digitalcollections.cudami.client.identifiable.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.client.identifiable.BaseCudamiIdentifiablesClientTest;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The client for FileResourcesMetadata")
class CudamiFileResourcesMetadataClientTest
    extends BaseCudamiIdentifiablesClientTest<FileResource, CudamiFileResourcesMetadataClient> {

  @Test
  @DisplayName("can find FileResources by their type")
  public void testFindFileResourcesByType() throws Exception {
    client.findByType(new PageRequest(), "jpg");
    verifyHttpRequestByMethodAndRelativeURL("get", "/type/jpg?pageNumber=0&pageSize=0");
  }

  @Test
  @DisplayName("can execute the find method with a PageRequest")
  @Override
  public void testFindWithPageRequest() throws Exception {
    String bodyJson =
        "{"
            + "\"listResponseType\":\"PAGE_RESPONSE\","
            + "\"content\":["
            + "{"
            + "\"objectType\":\"FILE_RESOURCE\","
            + "\"fileResource\":{\"identifiableObjectType\":\"FILE_RESOURCE\"}"
            + "}"
            + "]"
            + "}";
    when(httpResponse.body()).thenReturn(bodyJson.getBytes(StandardCharsets.UTF_8));

    PageRequest pageRequest = new PageRequest();
    PageResponse<FileResource> response = client.find(pageRequest);
    assertThat(response).isNotNull();
    assertThat(response.getContent().get(0)).isExactlyInstanceOf(FileResource.class);

    verifyHttpRequestByMethodAndRelativeURL("get", "?pageNumber=0&pageSize=0");
  }

  @Test
  @DisplayName("can return the languages for all FileResources")
  public void testGetLanguages() throws Exception {
    client.getLanguages();
    verifyHttpRequestByMethodAndRelativeURL("get", "/languages");
  }
}
