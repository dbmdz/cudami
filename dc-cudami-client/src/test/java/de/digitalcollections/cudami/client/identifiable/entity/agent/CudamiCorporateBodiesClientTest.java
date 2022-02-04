package de.digitalcollections.cudami.client.identifiable.entity.agent;

import de.digitalcollections.cudami.client.identifiable.entity.BaseCudamiEntitiesClientTest;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.paging.PageRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The client for corporateBodies")
class CudamiCorporateBodiesClientTest
    extends BaseCudamiEntitiesClientTest<CorporateBody, CudamiCorporateBodiesClient> {

  @Test
  @DisplayName("can access the endpoint for fetch and save by Gnd Id")
  public void fetchAndSaveByGndId() throws Exception {
    String gndId = "123456-7";

    client.fetchAndSaveByGndId(gndId);

    verifyHttpRequestByMethodAndRelativeURL("post", "/gnd/" + gndId);
  }

  @Test
  @DisplayName("can access the find endpoint with a PageRequest (legacy)")
  public void legacyFindWithPageRequest() throws Exception {
    PageRequest pageRequest = new PageRequest();
    client.find(pageRequest);

    verifyHttpRequestByMethodAndRelativeURL("get", "?pageNumber=0&pageSize=0");
  }

  @Test
  @DisplayName("can access the endpoint for getLanguages")
  public void getLanguages() throws Exception {
    client.getLanguages();

    verifyHttpRequestByMethodAndRelativeURL("get", "/languages");
  }
}
