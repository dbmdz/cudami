package de.digitalcollections.cudami.client.identifiable.entity.geo.location;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.client.identifiable.entity.BaseCudamiEntitiesClientTest;
import de.digitalcollections.model.identifiable.entity.geo.location.GeoLocation;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The client for GeoLocations")
class CudamiGeoLocationsClientTest
    extends BaseCudamiEntitiesClientTest<GeoLocation, CudamiGeoLocationsClient> {

  @Test
  @DisplayName("can execute the find method with a SearchPageRequest")
  @Override
  public void testFindWithSearchPageRequest() throws Exception {
    String bodyJson =
        "{\"content\":[{\"objectType\":\"GEO_LOCATION\", \"geoLocation\":{\"entityType\":\"GEO_LOCATION\",\"identifiableType\":\"ENTITY\"}}]}";
    when(httpResponse.body()).thenReturn(bodyJson.getBytes(StandardCharsets.UTF_8));

    SearchPageRequest searchPageRequest = new SearchPageRequest();
    SearchPageResponse<GeoLocation> response = client.find(searchPageRequest);
    assertThat(response).isNotNull();
    assertThat(response.getContent().get(0)).isExactlyInstanceOf(GeoLocation.class);

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
  @DisplayName("can return the languages for all geo locations")
  public void testGetLanguages() throws Exception {
    client.findLanguages();
    verifyHttpRequestByMethodAndRelativeURL("get", "/languages");
  }
}
