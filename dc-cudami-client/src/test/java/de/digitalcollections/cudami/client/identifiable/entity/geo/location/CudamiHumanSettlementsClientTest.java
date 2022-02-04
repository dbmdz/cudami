package de.digitalcollections.cudami.client.identifiable.entity.geo.location;

import de.digitalcollections.cudami.client.identifiable.entity.BaseCudamiEntitiesClientTest;
import de.digitalcollections.model.identifiable.entity.geo.location.HumanSettlement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The client for HumanSettlements")
class CudamiHumanSettlementsClientTest
    extends BaseCudamiEntitiesClientTest<HumanSettlement, CudamiHumanSettlementsClient> {

  @Test
  @DisplayName("can find by language, initial string and dedicated paging attributes")
  public void findByLanguageInitialAndPagingAttributes() throws Exception {
    client.findByLanguageAndInitial(1, 2, "sortable", "asc", "NATIVE", "de", "a");
    verifyHttpRequestByMethodAndRelativeURL(
        "get", "?language=de&initial=a&pageNumber=1&pageSize=2&sortBy=sortable.asc");
  }

  @Test
  @DisplayName("can find by pageRequest")
  public void findByPageRequest() throws Exception {
    client.find(buildExamplePageRequest());
    verifyHttpRequestByMethodAndRelativeURL(
        "get",
        "?pageNumber=1&pageSize=2&sortBy=sortable.desc.nullsfirst&foo=eq:bar&gnarf=eq:krchch");
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
  @DisplayName("can get by identifier")
  @Override
  public void getByIdentifier() throws Exception {
    String identifierNamespace = "gnd";
    String identifierValue = "1234567-8";

    client.getByIdentifier(identifierNamespace, identifierValue);

    verifyHttpRequestByMethodAndRelativeURL(
        "get", "/identifier/" + identifierNamespace + ":" + identifierValue + ".json");
  }
}
