package de.digitalcollections.cudami.client.identifiable.entity.agent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.identifiable.entity.work.Work;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The client for persons")
class CudamiPersonsClientTest
    extends BaseCudamiIdentifiablesClientTest<Person, CudamiPersonsClient> {

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
  @DisplayName("can find by the place of birth")
  public void findByPlaceOfBirth() throws Exception {
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
  public void findByPlaceOfDeath() throws Exception {
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
  public void findDigitalObjectsForPersons() throws Exception {
    UUID personUuid = UUID.randomUUID();
    client.getDigitalObjects(personUuid);
    verifyHttpRequestByMethodAndRelativeURL("get", "/" + personUuid + "/digitalobjects");
  }

  @Test
  @DisplayName("can return the languages for all persons")
  public void getLanguages() throws Exception {
    client.getLanguages();
    verifyHttpRequestByMethodAndRelativeURL("get", "/languages");
  }

  @Test
  @DisplayName("can return all works of a person")
  public void getWorks() throws Exception {
    String bodyJson = "[{\"entityType\":\"WORK\",\"identifiableType\":\"ENTITY\"}]";
    when(httpResponse.body()).thenReturn(bodyJson.getBytes(StandardCharsets.UTF_8));

    UUID personUuid = UUID.randomUUID();
    List<Work> works = client.getWorks(personUuid);
    assertThat(works).isNotNull();
    assertThat(works.get(0)).isExactlyInstanceOf(Work.class);
    verifyHttpRequestByMethodAndRelativeURL("get", "/" + personUuid + "/works");
  }

  @Test
  @DisplayName("can find by identifier")
  @Override
  public void findByIdentifier() throws Exception {
    String identifierNamespace = "mdz-obj";
    String identifierValue = "bsb12345678";

    client.findOneByIdentifier(identifierNamespace, identifierValue);

    verifyHttpRequestByMethodAndRelativeURL(
        "get", "/identifier?namespace=" + identifierNamespace + "&id=" + identifierValue);
  }
}
