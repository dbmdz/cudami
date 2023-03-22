package de.digitalcollections.cudami.client.identifiable.entity.geo.location;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.client.identifiable.entity.BaseCudamiEntitiesClientTest;
import de.digitalcollections.model.identifiable.entity.geo.location.GeoLocation;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The client for GeoLocations")
class CudamiGeoLocationsClientTest
    extends BaseCudamiEntitiesClientTest<GeoLocation, CudamiGeoLocationsClient> {

  @Test
  @DisplayName("can execute the find method with a PageRequest")
  @Override
  public void testFindWithPageRequest() throws Exception {
    String bodyJson =
        "{"
            + "\"listResponseType\":\"PAGE_RESPONSE\","
            + "\"content\":[{\"objectType\":\"GEO_LOCATION\", \"geoLocation\":{\"identifiableObjectType\":\"GEO_LOCATION\"}}]}";
    when(httpResponse.body()).thenReturn(bodyJson.getBytes(StandardCharsets.UTF_8));

    PageRequest pageRequest = new PageRequest();
    PageResponse<GeoLocation> response = client.find(pageRequest);
    assertThat(response).isNotNull();
    assertThat(response.getContent().get(0)).isExactlyInstanceOf(GeoLocation.class);

    verifyHttpRequestByMethodAndRelativeURL("get", "?pageNumber=0&pageSize=0");
  }

  @Test
  @DisplayName("can return the languages for all geo locations")
  public void testGetLanguages() throws Exception {
    client.getLanguages();
    verifyHttpRequestByMethodAndRelativeURL("get", "/languages");
  }
}
