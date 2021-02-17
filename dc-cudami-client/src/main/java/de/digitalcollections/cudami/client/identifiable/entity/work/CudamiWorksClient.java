package de.digitalcollections.cudami.client.identifiable.entity.work;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiBaseClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.identifiable.entity.work.Item;
import de.digitalcollections.model.identifiable.entity.work.Work;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class CudamiWorksClient extends CudamiBaseClient<Work> {

  public CudamiWorksClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Work.class, mapper);
  }

  public Work create() {
    return new Work();
  }

  public long count() throws HttpException {
    return Long.parseLong(doGetRequestForString("/latest/works/count"));
  }

  public PageResponse<Work> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/latest/works", pageRequest);
  }

  public PageResponse findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial) throws HttpException {
    return findByLanguageAndInitial("/latest/works", pageRequest, language, initial);
  }

  public PageResponse<Work> findByLanguageAndInitial(
      int pageNumber,
      int pageSize,
      String sortField,
      String sortDirection,
      String nullHandling,
      String language,
      String initial)
      throws HttpException {
    return findByLanguageAndInitial(
        "/latest/works",
        pageNumber,
        pageSize,
        sortField,
        sortDirection,
        nullHandling,
        language,
        initial);
  }

  public Work findOne(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/latest/works/%s", uuid));
  }

  public Work findOneByIdentifier(String namespace, String id) throws HttpException {
    return doGetRequestForObject(
        String.format("/latest/works/identifier?namespace=%s&id=%s", namespace, id));
  }

  public Set<Agent> getCreators(UUID uuid) throws HttpException {
    return (Set<Agent>)
        doGetRequestForObjectList(
            String.format("/latest/works/%s/creators", uuid), DigitalObject.class);
  }

  public List getItems(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(String.format("/latest/works/%s/items", uuid), Item.class);
  }

  public Work save(Work work) throws HttpException {
    return doPostRequestForObject("/latest/works", (Work) work);
  }

  public Work update(UUID uuid, Work work) throws HttpException {
    return doPutRequestForObject(String.format("/latest/works/%s", uuid), (Work) work);
  }
}
