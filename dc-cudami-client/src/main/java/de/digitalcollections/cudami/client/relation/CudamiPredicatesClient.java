package de.digitalcollections.cudami.client.relation;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiRestClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.relation.Predicate;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class CudamiPredicatesClient extends CudamiRestClient<Predicate> {

  public CudamiPredicatesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Predicate.class, mapper, API_VERSION_PREFIX + "/predicates");
  }

  public List<Predicate> getAll() throws TechnicalException {
    return doGetRequestForObjectList(baseEndpoint, Predicate.class);
  }

  public Predicate save(Predicate predicate) throws TechnicalException {
    if (predicate.getUuid() == null) {
      // create
      return super.save(predicate);
    }

    // update by uuid
    return doPutRequestForObject(
        String.format("%s/%s", baseEndpoint, predicate.getUuid()), predicate);
  }

  public Predicate create(Predicate predicate) throws TechnicalException {
    return super.save(predicate);
  }

  public Predicate getByValue(String value) throws TechnicalException {
    return doGetRequestForObject(
        String.format("%s/%s", baseEndpoint, URLEncoder.encode(value, StandardCharsets.UTF_8)));
  }
}
