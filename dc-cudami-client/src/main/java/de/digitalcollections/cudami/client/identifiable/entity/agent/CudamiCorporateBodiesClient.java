package de.digitalcollections.cudami.client.identifiable.entity.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiBaseClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CudamiCorporateBodiesClient extends CudamiBaseClient<CorporateBody> {

  public CudamiCorporateBodiesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, CorporateBody.class, mapper);
  }

  public long count() throws HttpException {
    return Long.parseLong(doGetRequestForString("/latest/corporatebodies/count"));
  }

  public CorporateBody create() {
    return new CorporateBody();
  }

  public CorporateBody fetchAndSaveByGndId(String gndId) throws HttpException {
    return doPostRequestForObject(String.format("/latest/corporatebodies/gnd/%s", gndId));
  }

  public SearchPageResponse<CorporateBody> find(SearchPageRequest searchPageRequest)
      throws HttpException {
    return doGetSearchRequestForPagedObjectList("/latest/corporatebodies", searchPageRequest);
  }

  public CorporateBody findOne(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/latest/corporatebodies/%s", uuid));
  }

  public CorporateBody findOne(UUID uuid, String locale) throws HttpException {
    return doGetRequestForObject(
        String.format("/latest/corporatebodies/%s?pLocale=%s", uuid, locale));
  }

  public CorporateBody findOneByIdentifier(String namespace, String id) throws HttpException {
    return doGetRequestForObject(
        String.format("/latest/corporatebodies/identifier/%s:%s.json", namespace, id));
  }

  public List<Locale> getLanguages() throws HttpException {
    return doGetRequestForObjectList("/latest/corporatebodies/languages", Locale.class);
  }

  public CorporateBody save(CorporateBody corporateBody) throws HttpException {
    return doPostRequestForObject("/latest/corporatebodies", corporateBody);
  }

  public CorporateBody update(UUID uuid, CorporateBody corporateBody) throws HttpException {
    return doPutRequestForObject(String.format("/latest/corporatebodies/%s", uuid), corporateBody);
  }
}
