package de.digitalcollections.cudami.client.identifiable;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.identifiable.CudamiIdentifiablesClientTest.CudamiIdentifiablesClientForIdentifiables;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import java.net.http.HttpClient;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The Identifiables Client for Identifiables")
public class CudamiIdentifiablesClientTest
    extends BaseCudamiIdentifiablesClientTest<
        Identifiable, CudamiIdentifiablesClientForIdentifiables> {

  // ------------------------------------------
  // We must create a generics-free client, since we cannot use generics with the extended
  // BaseCudamiIdentifiablesClientTest as second type argument
  public static class CudamiIdentifiablesClientForIdentifiables
      extends CudamiIdentifiablesClient<Identifiable> {

    public CudamiIdentifiablesClientForIdentifiables(
        HttpClient httpClient, String serverUrl, ObjectMapper objectMapper) {
      super(
          httpClient,
          serverUrl,
          Identifiable.class,
          objectMapper,
          API_VERSION_PREFIX + "/identifiable");
    }
  }

  @Test
  @DisplayName("params for label filtering are treated different")
  public void testLabelParams() {
    var fcLabel =
        FilterCriterion.builder().withExpression("label").contains("something special").build();
    var expLabel = "label=something+special";

    var fcLabelWithLanguage =
        FilterCriterion.builder().withExpression("label.en").contains("something").build();
    var expLabelWithLanguage = "label=something&labelLanguage=en";

    var fcLabelEquals =
        FilterCriterion.builder().withExpression("label").isEquals("something special").build();
    var expLabelEquals = "label=%22something+special%22";

    assertThat(client.filterCriterionToUrlParam(fcLabel)).isEqualTo(expLabel);
    assertThat(client.filterCriterionToUrlParam(fcLabelWithLanguage))
        .isEqualTo(expLabelWithLanguage);
    assertThat(client.filterCriterionToUrlParam(fcLabelEquals)).isEqualTo(expLabelEquals);

    // normal case
    var date = LocalDate.now();
    var fcDate = FilterCriterion.builder().withExpression("lastModified").isEquals(date).build();
    var expDate = String.format("lastModified=eq:%s", date.toString());

    assertThat(client.filterCriterionToUrlParam(fcDate)).isEqualTo(expDate);
  }

  @Test
  @DisplayName("params for name filtering are treated different")
  public void testNameParams() {
    var fcLabel =
        FilterCriterion.builder().withExpression("name").contains("something special").build();
    var expLabel = "name=something+special";

    var fcLabelWithLanguage =
        FilterCriterion.builder().withExpression("name.en").contains("something").build();
    var expLabelWithLanguage = "name=something&nameLanguage=en";

    var fcLabelEquals =
        FilterCriterion.builder().withExpression("name").isEquals("something special").build();
    var expLabelEquals = "name=%22something+special%22";

    assertThat(client.filterCriterionToUrlParam(fcLabel)).isEqualTo(expLabel);
    assertThat(client.filterCriterionToUrlParam(fcLabelWithLanguage))
        .isEqualTo(expLabelWithLanguage);
    assertThat(client.filterCriterionToUrlParam(fcLabelEquals)).isEqualTo(expLabelEquals);
  }
}
