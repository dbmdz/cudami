package de.digitalcollections.cudami.client.identifiable.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.client.identifiable.BaseCudamiIdentifiablesClientTest;
import de.digitalcollections.model.filter.FilterCriterion;
import de.digitalcollections.model.filter.FilterOperation;
import de.digitalcollections.model.filter.Filtering;
import de.digitalcollections.model.identifiable.resource.LinkedDataFileResource;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The client for LinkedDataFileResources")
class CudamiLinkedDataFileResourcesClientTest
    extends BaseCudamiIdentifiablesClientTest<
        LinkedDataFileResource, CudamiLinkedDataFileResourcesClient> {

  @Test
  @DisplayName("can execute the find method with a SearchPageRequest and Filtering")
  public void testFindWithSearchPageRequestAndFiltering() throws Exception {
    String bodyJson = "{}";
    when(httpResponse.body()).thenReturn(bodyJson.getBytes(StandardCharsets.UTF_8));

    SearchPageRequest searchPageRequest = new SearchPageRequest();
    Filtering filtering = new Filtering();
    FilterCriterion filterCriterion =
        new FilterCriterion("uri", FilterOperation.EQUALS, "http://foo.bar/bla.xml");
    filtering.setFilterCriteria(List.of(filterCriterion));
    searchPageRequest.setFiltering(filtering);
    SearchPageResponse<LinkedDataFileResource> response = client.find(searchPageRequest);
    assertThat(response).isNotNull();

    verifyHttpRequestByMethodAndRelativeURL(
        "get", "/search?pageNumber=0&pageSize=0&uri=eq:http%3A%2F%2Ffoo.bar%2Fbla.xml");
  }
}
