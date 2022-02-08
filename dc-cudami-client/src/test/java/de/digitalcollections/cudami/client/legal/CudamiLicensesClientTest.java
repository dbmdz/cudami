package de.digitalcollections.cudami.client.legal;

import de.digitalcollections.cudami.client.BaseCudamiRestClientTest;
import de.digitalcollections.model.legal.License;
import java.net.URI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CudamiLicensesClientTest
    extends BaseCudamiRestClientTest<License, CudamiLicensesClient> {

  @Test
  @DisplayName("can delete by Url")
  public void testDeleteByUrl_String() throws Exception {
    client.deleteByUrl("http://rightsstatements.org/vocab/InC-NC/1.0/");
    verifyHttpRequestByMethodAndRelativeURL(
        "delete", "?url=http%3A%2F%2Frightsstatements.org%2Fvocab%2FInC-NC%2F1.0%2F");
  }

  @Test
  @DisplayName("can delete by Url")
  public void testDeleteByUrl_URL() throws Exception {
    client.deleteByUrl(URI.create("http://rightsstatements.org/vocab/InC-NC/1.0/").toURL());
    verifyHttpRequestByMethodAndRelativeURL(
        "delete", "?url=http%3A%2F%2Frightsstatements.org%2Fvocab%2FInC-NC%2F1.0%2F");
  }

  @Test
  @DisplayName("can get by Url")
  public void testGetByUrl_String() throws Exception {
    client.getByUrl("http://rightsstatements.org/vocab/InC-NC/1.0/");
    verifyHttpRequestByMethodAndRelativeURL(
        "get", "?url=http%3A%2F%2Frightsstatements.org%2Fvocab%2FInC-NC%2F1.0%2F");
  }

  @Test
  @DisplayName("can get by Url")
  public void testGetByUrl_URL() throws Exception {
    client.getByUrl(URI.create("http://rightsstatements.org/vocab/InC-NC/1.0/").toURL());
    verifyHttpRequestByMethodAndRelativeURL(
        "get", "?url=http%3A%2F%2Frightsstatements.org%2Fvocab%2FInC-NC%2F1.0%2F");
  }
}
