package de.digitalcollections.cudami.client.relation;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiRestClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.exception.http.client.ResourceNotFoundException;
import de.digitalcollections.model.relation.Predicate;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

public class CudamiPredicatesClient extends CudamiRestClient<Predicate> {

  public CudamiPredicatesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Predicate.class, mapper, API_VERSION_PREFIX + "/predicates");
  }

  public Predicate getByValue(String value) throws TechnicalException {
    try {
      return doGetRequestForObject(
          String.format("%s/%s", baseEndpoint, URLEncoder.encode(value, StandardCharsets.UTF_8)));
    } catch (ResourceNotFoundException e) {
      return null;
    }
  }

  public List<Locale> getLanguages() throws TechnicalException {
    return this.doGetRequestForObjectList(baseEndpoint + "/languages", Locale.class);
  }

  public Predicate update(Predicate predicate) throws TechnicalException {
    if (predicate.getUuid() == null) {
      // Old consumers don't set the UUID, we must provide the value in the request path
      return doPutRequestForObject(
          String.format(
              "%s/%s",
              baseEndpoint, URLEncoder.encode(predicate.getValue(), StandardCharsets.UTF_8)),
          predicate);
    }

    return super.update(predicate.getUuid(), predicate);
  }
}
