package de.digitalcollections.cudami.client.relation;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.client.BaseRestClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.relation.Predicate;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class CudamiPredicatesClient extends BaseRestClient<Predicate> {

  public CudamiPredicatesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Predicate.class, mapper, "/v5/predicates");
  }

  public List<Predicate> getAll() throws TechnicalException {
    return doGetRequestForObjectList(baseEndpoint, Predicate.class);
  }

  public Predicate save(Predicate predicate) throws TechnicalException {
    return doPutRequestForObject(
        String.format(
            "%s/%s", baseEndpoint, URLEncoder.encode(predicate.getValue(), StandardCharsets.UTF_8)),
        predicate);
  }
}
