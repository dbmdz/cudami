package de.digitalcollections.cudami.client.legal;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiBaseClient;
import de.digitalcollections.model.legal.License;
import java.net.http.HttpClient;

public class CudamiLicenseClient extends CudamiBaseClient<License> {

  public CudamiLicenseClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, License.class, mapper);
  }
}
