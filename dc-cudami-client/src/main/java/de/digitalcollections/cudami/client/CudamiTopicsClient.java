package de.digitalcollections.cudami.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.api.identifiable.entity.Topic;
import de.digitalcollections.model.api.identifiable.entity.parts.Subtopic;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.TopicImpl;
import de.digitalcollections.model.impl.identifiable.entity.parts.SubtopicImpl;
import java.net.http.HttpClient;
import java.util.List;
import java.util.UUID;

public class CudamiTopicsClient extends CudamiBaseClient<TopicImpl> {

  public CudamiTopicsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, TopicImpl.class, mapper);
  }

  public Topic create() {
    return new TopicImpl();
  }

  public long count() throws HttpException {
    return Long.parseLong(doGetRequestForString("/v2/topics/count"));
  }

  public PageResponse<TopicImpl> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/v2/topics", pageRequest);
  }

  public Topic findOne(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/v2/topics/%s", uuid));
  }

  public Topic findOne(UUID uuid, String locale) throws HttpException {
    return doGetRequestForObject(String.format("/v2/topics/%s?locale=%s", uuid, locale));
  }

  public Topic findOneByIdentifier(String namespace, String id) throws HttpException {
    // No GET endpoint for /latest/topics/identifier/%s:%s.json available!
    throw new HttpException(
        String.format("/latest/topics/identifier/%s:%s.json", namespace, id), 404);
  }

  public List<Subtopic> getSubtopics(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(
        String.format("/v2/topics/%s/subtopics", uuid), SubtopicImpl.class);
  }

  public Topic save(Topic topic) throws HttpException {
    return doPostRequestForObject("/v2/topics", (TopicImpl) topic);
  }

  public Topic update(UUID uuid, Topic topic) throws HttpException {
    return doPutRequestForObject(String.format("/v2/topics/%s", uuid), (TopicImpl) topic);
  }
}
