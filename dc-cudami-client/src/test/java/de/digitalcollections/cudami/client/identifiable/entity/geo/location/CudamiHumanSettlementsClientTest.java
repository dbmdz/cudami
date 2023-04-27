package de.digitalcollections.cudami.client.identifiable.entity.geo.location;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.client.identifiable.entity.BaseCudamiEntitiesClientTest;
import de.digitalcollections.model.identifiable.entity.geo.location.HumanSettlement;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The client for HumanSettlements")
class CudamiHumanSettlementsClientTest
    extends BaseCudamiEntitiesClientTest<HumanSettlement, CudamiHumanSettlementsClient> {

  @Test
  @DisplayName("can execute the find method with a PageRequest")
  @Override
  public void testFindWithPageRequest() throws Exception {
    String bodyJson = "{" + "\"listResponseType\":\"PAGE_RESPONSE\"" + "}";
    when(httpResponse.body()).thenReturn(bodyJson.getBytes(StandardCharsets.UTF_8));

    PageRequest pageRequest = new PageRequest();
    PageResponse<HumanSettlement> response = client.find(pageRequest);
    assertThat(response).isNotNull();

    verifyHttpRequestByMethodAndRelativeURL("get", "?pageNumber=0&pageSize=0");
  }
}
