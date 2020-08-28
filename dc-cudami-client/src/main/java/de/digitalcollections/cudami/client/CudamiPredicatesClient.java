package de.digitalcollections.cudami.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.api.relations.Predicate;
import de.digitalcollections.model.impl.relations.PredicateImpl;
import java.net.http.HttpClient;
import java.util.List;

public class CudamiPredicatesClient extends CudamiBaseClient<PredicateImpl> {

  public CudamiPredicatesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, PredicateImpl.class, mapper);
  }

  public List<Predicate> findAllPredicates() throws HttpException {
    return doGetRequestForObjectList("/latest/predicates", Predicate.class);
  }

  public Predicate save(Predicate predicate) throws HttpException {
    return doPutRequestForObject("/latest/works", (PredicateImpl) predicate);
  }
}
