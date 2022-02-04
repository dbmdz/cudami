package de.digitalcollections.cudami.client.legal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.client.BaseCudamiRestClientTest;
import de.digitalcollections.model.legal.License;
import org.junit.jupiter.api.Test;

public class CudamiLicensesClientTest
    extends BaseCudamiRestClientTest<License, CudamiLicensesClient> {

  @Test
  public void testCount() throws Exception {
    when(httpResponse.body()).thenReturn("42");

    assertThat(client.count()).isEqualTo(42);

    verifyHttpRequestByMethodAndRelativeURL("get", "/count");
  }

  @Test
  public void testCreate() {}

  @Test
  public void testDeleteByUrl_String() throws Exception {}

  @Test
  public void testDeleteByUrl_URL() throws Exception {}

  @Test
  public void testDeleteByUuid() throws Exception {}

  @Test
  public void testDeleteByUuids() {}

  @Test
  public void testFind() throws Exception {}

  @Test
  public void testFindAll() throws Exception {}

  @Test
  public void testGetByUrl_String() throws Exception {}

  @Test
  public void testGetByUrl_URL() throws Exception {}

  @Test
  public void testGetByUuid() throws Exception {}

  @Test
  public void testSave() throws Exception {}

  @Test
  public void testUpdate() throws Exception {}
}
