package de.digitalcollections.cudami.client.entity.work;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiBaseClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.api.identifiable.entity.agent.Agent;
import de.digitalcollections.model.api.identifiable.entity.work.Work;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.DigitalObjectImpl;
import de.digitalcollections.model.impl.identifiable.entity.work.ItemImpl;
import de.digitalcollections.model.impl.identifiable.entity.work.WorkImpl;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class CudamiWorksClient extends CudamiBaseClient<WorkImpl> {

  public CudamiWorksClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, WorkImpl.class, mapper);
  }

  public Work create() {
    return new WorkImpl();
  }

  public long count() throws HttpException {
    return Long.parseLong(doGetRequestForString("/v2/works/count"));
  }

  public PageResponse<WorkImpl> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/v2/works", pageRequest);
  }

  public PageResponse findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial) throws HttpException {
    return findByLanguageAndInitial("/v2/works", pageRequest, language, initial);
  }

  public PageResponse<WorkImpl> findByLanguageAndInitial(
      int pageNumber,
      int pageSize,
      String sortField,
      String sortDirection,
      String nullHandling,
      String language,
      String initial)
      throws HttpException {
    return findByLanguageAndInitial(
        "/v2/works",
        pageNumber,
        pageSize,
        sortField,
        sortDirection,
        nullHandling,
        language,
        initial);
  }

  public Work findOne(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/v2/works/%s", uuid));
  }

  public Work findOneByIdentifier(String namespace, String id) throws HttpException {
    return doGetRequestForObject(
        String.format("/v2/works/identifier?namespace=%s&id=%s", namespace, id));
  }

  public Set<Agent> getCreators(UUID uuid) throws HttpException {
    return (Set<Agent>)
        doGetRequestForObjectList(
            String.format("/v2/works/%s/creators", uuid), DigitalObjectImpl.class);
  }

  public List getItems(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(String.format("/v2/works/%s/items", uuid), ItemImpl.class);
  }

  public Work save(Work work) throws HttpException {
    return doPostRequestForObject("/v2/works", (WorkImpl) work);
  }

  public Work update(UUID uuid, Work work) throws HttpException {
    return doPutRequestForObject(String.format("/v2/works/%s", uuid), (WorkImpl) work);
  }
}
