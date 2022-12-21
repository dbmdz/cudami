package de.digitalcollections.cudami.client;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.jackson.DigitalCollectionsObjectMapper;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import java.net.URI;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The CudamiRestClient")
public class CudamiRestClientTest {

  private CudamiRestClient client;
  String serverUrl = "http://localhost:1234/cudami";

  @BeforeEach
  public void beforeEach() {
    client =
        new CudamiRestClient(
            null, serverUrl, Identifiable.class, new DigitalCollectionsObjectMapper(), "/v999");
  }

  @Test
  public void testCreateFullUriWithPath() {

    String requestUrl = "/foo/bar";
    CudamiRestClient base =
        new CudamiRestClient(
            null, serverUrl, Identifiable.class, new DigitalCollectionsObjectMapper(), "/v999");
    URI result = base.createFullUri(requestUrl);
    URI expectedResult = URI.create(serverUrl + requestUrl);
    assertThat(result).isEqualTo(expectedResult);
  }

  @Test
  public void testCreateFullUriWithoutPath() {
    String serverUrl = "http://localhost:1234";
    String requestUrl = "/foo/bar";
    CudamiRestClient base =
        new CudamiRestClient(
            null, serverUrl, Identifiable.class, new DigitalCollectionsObjectMapper(), "/v999");
    URI result = base.createFullUri(requestUrl);
    URI expectedResult = URI.create(serverUrl + requestUrl);
    assertThat(result).isEqualTo(expectedResult);
  }

  @Test
  @DisplayName("params for label filtering are treated different")
  public void testLabelParams() {
    FilterCriterion fcLabel =
        FilterCriterion.builder().withExpression("label").contains("something special").build();
    String expLabel = "label=something+special";

    FilterCriterion fcLabelWithLanguage =
        FilterCriterion.builder().withExpression("label.en").contains("something").build();
    String expLabelWithLanguage = "label=something&labelLanguage=en";

    FilterCriterion fcLabelEquals =
        FilterCriterion.builder().withExpression("label").isEquals("something special").build();
    String expLabelEquals = "label=%22something+special%22";

    assertThat(client.filterCriterionToUrlParam(fcLabel)).isEqualTo(expLabel);
    assertThat(client.filterCriterionToUrlParam(fcLabelWithLanguage))
        .isEqualTo(expLabelWithLanguage);
    assertThat(client.filterCriterionToUrlParam(fcLabelEquals)).isEqualTo(expLabelEquals);

    // normal case
    LocalDate date = LocalDate.now();
    FilterCriterion fcDate =
        FilterCriterion.builder().withExpression("lastModified").isEquals(date).build();
    String expDate = String.format("lastModified=eq:%s", date.toString());

    assertThat(client.filterCriterionToUrlParam(fcDate)).isEqualTo(expDate);
  }

  @Test
  @DisplayName("params for name filtering are treated different")
  public void testNameParams() {
    FilterCriterion fcLabel =
        FilterCriterion.builder().withExpression("name").contains("something special").build();
    String expLabel = "name=something+special";

    FilterCriterion fcLabelWithLanguage =
        FilterCriterion.builder().withExpression("name.en").contains("something").build();
    String expLabelWithLanguage = "name=something&nameLanguage=en";

    FilterCriterion fcLabelEquals =
        FilterCriterion.builder().withExpression("name").isEquals("something special").build();
    String expLabelEquals = "name=%22something+special%22";

    assertThat(client.filterCriterionToUrlParam(fcLabel)).isEqualTo(expLabel);
    assertThat(client.filterCriterionToUrlParam(fcLabelWithLanguage))
        .isEqualTo(expLabelWithLanguage);
    assertThat(client.filterCriterionToUrlParam(fcLabelEquals)).isEqualTo(expLabelEquals);
  }
}
