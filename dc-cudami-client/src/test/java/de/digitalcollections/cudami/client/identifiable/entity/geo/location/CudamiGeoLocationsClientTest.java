package de.digitalcollections.cudami.client.identifiable.entity.geo.location;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.client.identifiable.entity.agent.BaseCudamiIdentifiablesClientTest;
import de.digitalcollections.model.identifiable.entity.geo.location.GeoLocation;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The client for GeoLocations")
class CudamiGeoLocationsClientTest
    extends BaseCudamiIdentifiablesClientTest<GeoLocation, CudamiGeoLocationsClient> {

  @Test
  @DisplayName("can find by identifier")
  @Override
  public void findByIdentifier() throws Exception {
    String identifierNamespace = "gnd";
    String identifierValue = "1234567-8";

    client.findOneByIdentifier(identifierNamespace, identifierValue);

    verifyHttpRequestByMethodAndRelativeURL(
        "get", "/identifier?namespace=" + identifierNamespace + "&id=" + identifierValue);
  }

  @Test
  @DisplayName("can find by pageRequest, language and initial string")
  public void findByPageRequestLanguageInitial() throws Exception {
    client.findByLanguageAndInitial(buildExamplePageRequest(), "de", "a");
    verifyHttpRequestByMethodAndRelativeURL(
        "get",
        "?language=de&initial=a&pageNumber=1&pageSize=2&sortBy=sortable.desc.nullsfirst&foo=eq:bar&gnarf=eq:krchch");
  }

  @Test
  @DisplayName("can find by language, initial string and dedicated paging attributes")
  public void findByLanguageInitialAndPagingAttributes() throws Exception {
    client.findByLanguageAndInitial(1, 2, "sortable", "asc", "NATIVE", "de", "a");
    verifyHttpRequestByMethodAndRelativeURL(
        "get", "?language=de&initial=a&pageNumber=1&pageSize=2&sortBy=sortable.asc");
  }

  @Test
  @DisplayName("can return the languages for all geo locations")
  public void getLanguages() throws Exception {
    client.getLanguages();
    verifyHttpRequestByMethodAndRelativeURL("get", "/languages");
  }

  @Test
  @DisplayName("can execute the find method with a search term and max results")
  public void findWithSearchTermAndMaxResults() throws Exception {
    String bodyJson = "{\"content\":[]}";
    when(httpResponse.body()).thenReturn(bodyJson.getBytes(StandardCharsets.UTF_8));

    assertThat(client.find("foo", 100)).isNotNull();

    verifyHttpRequestByMethodAndRelativeURL("get", "?pageNumber=0&pageSize=100&searchTerm=foo");
  }

  @Test
  @DisplayName("can execute the find method with a SearchPageRequest")
  public void findWithSearchPageRequest() throws Exception {
    String bodyJson =
        "{\"content\":[{\"objectType\":\"GEO_LOCATION\", \"geoLocation\":{\"entityType\":\"GEO_LOCATION\",\"identifiableType\":\"ENTITY\"}}]}";
    when(httpResponse.body()).thenReturn(bodyJson.getBytes(StandardCharsets.UTF_8));

    SearchPageRequest searchPageRequest = new SearchPageRequest();
    SearchPageResponse<GeoLocation> response = client.find(searchPageRequest);
    assertThat(response).isNotNull();
    assertThat(response.getContent().get(0)).isExactlyInstanceOf(GeoLocation.class);

    verifyHttpRequestByMethodAndRelativeURL("get", "?pageNumber=0&pageSize=0");
  }
}
