package de.digitalcollections.cudami.client.identifiable;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiRestClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.exception.http.client.ResourceNotFoundException;
import de.digitalcollections.model.identifiable.IdentifierType;
import java.net.http.HttpClient;
import java.util.UUID;

public class CudamiIdentifierTypesClient extends CudamiRestClient<IdentifierType> {

  public CudamiIdentifierTypesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, IdentifierType.class, mapper, API_VERSION_PREFIX + "/identifiertypes");
  }

  public IdentifierType getByNamespace(String namespace) throws TechnicalException {
    try {
      return doGetRequestForObject(String.format(baseEndpoint + "/namespace/%s", namespace));
    } catch (ResourceNotFoundException e) {
      return null;
    }
  }

  public IdentifierType getByUuidAndLocale(UUID uuid, String locale) throws TechnicalException {
    try {
      return doGetRequestForObject(String.format(baseEndpoint + "/%s?locale=%s", uuid, locale));
    } catch (ResourceNotFoundException e) {
      return null;
    }
  }
}
