package de.digitalcollections.cudami.client.identifiable.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.identifiable.CudamiIdentifiablesClient;
import de.digitalcollections.model.identifiable.resource.LinkedDataFileResource;
import java.net.http.HttpClient;

public class CudamiLinkedDataFileResourcesClient
    extends CudamiIdentifiablesClient<LinkedDataFileResource> {

  public CudamiLinkedDataFileResourcesClient(
      HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(
        http,
        serverUrl,
        LinkedDataFileResource.class,
        mapper,
        API_VERSION_PREFIX + "/linkeddatafileresources");
  }
}
