package de.digitalcollections.cudami.client.identifiable.entity.agent;

import de.digitalcollections.cudami.client.identifiable.entity.BaseCudamiEntitiesClientTest;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The client for corporateBodies")
class CudamiCorporateBodiesClientTest
    extends BaseCudamiEntitiesClientTest<CorporateBody, CudamiCorporateBodiesClient> {

  @Test
  @DisplayName("can access the endpoint for fetch and save by Gnd Id")
  public void testFetchAndSaveByGndId() throws Exception {
    String gndId = "123456-7";

    client.fetchAndSaveByGndId(gndId);

    verifyHttpRequestByMethodAndRelativeURL("post", "/gnd/" + gndId);
  }

  @Test
  @DisplayName("can access the endpoint for getLanguages")
  public void testGetLanguages() throws Exception {
    client.getLanguages();

    verifyHttpRequestByMethodAndRelativeURL("get", "/languages");
  }
}
