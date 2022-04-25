package de.digitalcollections.cudami.client.identifiable.entity.agent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.client.identifiable.entity.BaseCudamiEntitiesClientTest;
import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.identifiable.entity.work.Work;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The client for persons")
class CudamiPersonsClientTest extends BaseCudamiEntitiesClientTest<Person, CudamiPersonsClient> {

  @Test
  @DisplayName("can find by the place of birth")
  public void testFindByPlaceOfBirth() throws Exception {
    UUID placeUuid = UUID.randomUUID();
    client.findByPlaceOfBirth(buildExamplePageRequest(), placeUuid);
    verifyHttpRequestByMethodAndRelativeURL(
        "get",
        "/placeofbirth/"
            + placeUuid
            + "?pageNumber=1&pageSize=2&sortBy=sortable.desc.nullsfirst&foo=eq:bar&gnarf=eq:krchch");
  }

  @Test
  @DisplayName("can find by the place of death")
  public void testFindByPlaceOfDeath() throws Exception {
    UUID placeUuid = UUID.randomUUID();
    client.findByPlaceOfDeath(buildExamplePageRequest(), placeUuid);
    verifyHttpRequestByMethodAndRelativeURL(
        "get",
        "/placeofdeath/"
            + placeUuid
            + "?pageNumber=1&pageSize=2&sortBy=sortable.desc.nullsfirst&foo=eq:bar&gnarf=eq:krchch");
  }

  @Test
  @DisplayName("can find DigitalObjects for a person")
  public void testGetDigitalObjects() throws Exception {
    UUID personUuid = UUID.randomUUID();
    client.getDigitalObjects(personUuid);
    verifyHttpRequestByMethodAndRelativeURL("get", "/" + personUuid + "/digitalobjects");
  }

  @Test
  @DisplayName("can return the languages for all persons")
  public void testGetLanguages() throws Exception {
    client.getLanguages();
    verifyHttpRequestByMethodAndRelativeURL("get", "/languages");
  }

  @Test
  @DisplayName("can return all works of a person")
  public void testGetWorks() throws Exception {
    String bodyJson = "[{\"entityType\":\"WORK\",\"identifiableType\":\"ENTITY\"}]";
    when(httpResponse.body()).thenReturn(bodyJson.getBytes(StandardCharsets.UTF_8));

    UUID personUuid = UUID.randomUUID();
    List<Work> works = client.getWorks(personUuid);
    assertThat(works).isNotNull();
    assertThat(works.get(0)).isExactlyInstanceOf(Work.class);
    verifyHttpRequestByMethodAndRelativeURL("get", "/" + personUuid + "/works");
  }

  @Test
  @DisplayName("can execute the find method with a SearchPageRequest")
  @Override
  public void testFindWithSearchPageRequest() throws Exception {
    String bodyJson = "{}";
    when(httpResponse.body()).thenReturn(bodyJson.getBytes(StandardCharsets.UTF_8));

    SearchPageRequest searchPageRequest = new SearchPageRequest();
    SearchPageResponse<Person> response = client.find(searchPageRequest);
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
