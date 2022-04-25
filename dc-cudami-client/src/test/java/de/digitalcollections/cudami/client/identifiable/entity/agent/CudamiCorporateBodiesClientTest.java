package de.digitalcollections.cudami.client.identifiable.entity.agent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.client.identifiable.entity.BaseCudamiEntitiesClientTest;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The client for corporateBodies")
class CudamiCorporateBodiesClientTest
    extends BaseCudamiEntitiesClientTest<CorporateBody, CudamiCorporateBodiesClient> {

  @Test
  @DisplayName("can access the endpoint for fetch and save by Gnd Id")
  public void testFetchAndSaveByGndId() throws Exception {
    String gndId = "123456-7";

    client.fetchAndSaveByGndId(gndId);

    verifyHttpRequestByMethodAndRelativeURL("post", "/gnd/" + gndId);
  }

  @Test
  @DisplayName("can access the endpoint for getLanguages")
  public void testGetLanguages() throws Exception {
    client.getLanguages();

    verifyHttpRequestByMethodAndRelativeURL("get", "/languages");
  }

  @Test
  @DisplayName("can execute the find method with a SearchPageRequest")
  @Override
  public void testFindWithSearchPageRequest() throws Exception {
    String bodyJson = "{}";
    when(httpResponse.body()).thenReturn(bodyJson.getBytes(StandardCharsets.UTF_8));

    SearchPageRequest searchPageRequest = new SearchPageRequest();
    SearchPageResponse<CorporateBody> response = client.find(searchPageRequest);
    assertThat(response).isNotNull();

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
}
