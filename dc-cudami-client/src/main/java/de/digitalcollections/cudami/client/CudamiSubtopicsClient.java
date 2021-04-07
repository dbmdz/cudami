package de.digitalcollections.cudami.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.Topic;
import de.digitalcollections.model.api.identifiable.entity.parts.Subtopic;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.view.BreadcrumbNavigation;
import de.digitalcollections.model.impl.identifiable.entity.EntityImpl;
import de.digitalcollections.model.impl.identifiable.entity.TopicImpl;
import de.digitalcollections.model.impl.identifiable.entity.parts.SubtopicImpl;
import de.digitalcollections.model.impl.identifiable.resource.FileResourceImpl;
import de.digitalcollections.model.impl.view.BreadcrumbNavigationImpl;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CudamiSubtopicsClient extends CudamiBaseClient<SubtopicImpl> {

  public CudamiSubtopicsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, SubtopicImpl.class, mapper);
  }

  public Subtopic create() {
    return new SubtopicImpl();
  }

  public long count() throws HttpException {
    return Long.parseLong(doGetRequestForString("/v2/subtopics/count"));
  }

  public PageResponse<SubtopicImpl> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/v2/subtopics", pageRequest);
  }

  public Subtopic findOne(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/v2/subtopics/%s", uuid));
  }

  public Subtopic findOne(UUID uuid, Locale locale) throws HttpException {
    return findOne(uuid, locale.toString());
  }

  public Subtopic findOne(UUID uuid, String locale) throws HttpException {
    return doGetRequestForObject(String.format("/v2/subtopics/%s?locale=%s", uuid, locale));
  }

  public Subtopic findOneByIdentifier(String namespace, String id) throws HttpException {
    // No GET endpoint for /latest/subtopics/identifier/%s:%s.json available!
    throw new HttpException(
        String.format("/latest/subtopics/identifier/%s:%s.json", namespace, id), 404);
  }

  public BreadcrumbNavigation getBreadcrumbNavigation(UUID uuid) throws HttpException {
    return (BreadcrumbNavigation)
        doGetRequestForObject(
            String.format("/v3/subtopics/%s/breadcrumb", uuid), BreadcrumbNavigationImpl.class);
  }

  public List<SubtopicImpl> getChildren(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(String.format("/v2/subtopics/%s/children", uuid));
  }

  public PageResponse<SubtopicImpl> getChildren(UUID uuid, PageRequest pageRequest)
      throws HttpException {
    return doGetRequestForPagedObjectList(
        String.format("/v2/subtopics/%s/children", uuid), pageRequest);
  }

  public List getEntities(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(
        String.format("/v2/subtopics/%s/entities", uuid), EntityImpl.class);
  }

  public List getFileResources(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(
        String.format("/v2/subtopics/%s/fileresources", uuid), FileResourceImpl.class);
  }

  public Subtopic getParent(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/v2/subtopics/%s/parent", uuid));
  }

  public List getRelatedFileResources(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(
        String.format("/v2/entities/%s/related/fileresources", uuid), FileResourceImpl.class);
  }

  public List<SubtopicImpl> getSubtopicsOfEntity(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(String.format("/v2/subtopics/entity/%s", uuid));
  }

  public List<SubtopicImpl> getSubtopicsOfFileResource(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(String.format("/v2/subtopics/fileresource/%s", uuid));
  }

  public Topic getTopic(UUID rootWebpageUuid) throws HttpException {
    return (Topic)
        doGetRequestForObject(
            String.format("/v3/subtopics/%s/topic", rootWebpageUuid), TopicImpl.class);
  }

  public Subtopic save(Subtopic subtopic) throws HttpException {
    // No POST endpoint for /latest/subtopics available!
    throw new HttpException("/latest/subtopics", 404);
  }

  public List<Entity> saveEntities(UUID uuid, List entities) throws HttpException {
    return doPostRequestForObjectList(
        String.format("/v2/subtopics/%s/entities", uuid), entities, EntityImpl.class);
  }

  public List<FileResource> saveFileResources(UUID uuid, List fileResources) throws HttpException {
    return doPostRequestForObjectList(
        String.format("/v2/subtopics/%s/fileresources", uuid),
        fileResources,
        FileResourceImpl.class);
  }

  public Subtopic saveWithParentTopic(Subtopic subtopic, UUID parentTopicUuid)
      throws HttpException {
    return doPostRequestForObject(
        String.format("/v2/topics/%s/subtopic", parentTopicUuid), (SubtopicImpl) subtopic);
  }

  public Subtopic saveWithParentSubtopic(Subtopic subtopic, UUID parentSubtopicUuid)
      throws HttpException {
    return doPostRequestForObject(
        String.format("/v2/subtopics/%s/subtopic", parentSubtopicUuid), (SubtopicImpl) subtopic);
  }

  public Subtopic update(UUID uuid, Subtopic subtopic) throws HttpException {
    return doPutRequestForObject(String.format("/v2/subtopics/%s", uuid), (SubtopicImpl) subtopic);
  }
}
