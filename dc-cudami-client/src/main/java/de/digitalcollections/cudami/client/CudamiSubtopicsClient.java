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
import de.digitalcollections.model.impl.identifiable.entity.Entity;
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
    return Long.parseLong(doGetRequestForString("/latest/subtopics/count"));
  }

  public PageResponse<SubtopicImpl> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/latest/subtopics", pageRequest);
  }

  public Subtopic findOne(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/latest/subtopics/%s", uuid));
  }

  public Subtopic findOne(UUID uuid, Locale locale) throws HttpException {
    return findOne(uuid, locale.toString());
  }

  public Subtopic findOne(UUID uuid, String locale) throws HttpException {
    return doGetRequestForObject(String.format("/latest/subtopics/%s?locale=%s", uuid, locale));
  }

  public Subtopic findOneByIdentifier(String namespace, String id) throws HttpException {
    return doGetRequestForObject(
        String.format("/latest/subtopics/identifier/%s:%s.json", namespace, id));
  }

  public BreadcrumbNavigation getBreadcrumbNavigation(UUID uuid) throws HttpException {
    return (BreadcrumbNavigation)
        doGetRequestForObject(
            String.format("/latest/subtopics/%s/breadcrumb", uuid), BreadcrumbNavigationImpl.class);
  }

  public List<SubtopicImpl> getChildren(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(String.format("/latest/subtopics/%s/children", uuid));
  }

  public PageResponse<SubtopicImpl> getChildren(UUID uuid, PageRequest pageRequest)
      throws HttpException {
    return doGetRequestForPagedObjectList(
        String.format("/latest/subtopics/%s/children", uuid), pageRequest);
  }

  public List getEntities(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(String.format("/latest/subtopics/%s/entities", uuid), Entity.class);
  }

  public List getFileResources(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(
        String.format("/latest/subtopics/%s/fileresources", uuid), FileResourceImpl.class);
  }

  public Subtopic getParent(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/latest/subtopics/%s/parent", uuid));
  }

  public List getRelatedFileResources(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(
        String.format("/latest/entities/%s/related/fileresources", uuid), FileResourceImpl.class);
  }

  public List<SubtopicImpl> getSubtopicsOfEntity(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(String.format("/latest/subtopics/entity/%s", uuid));
  }

  public List<SubtopicImpl> getSubtopicsOfFileResource(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(String.format("/latest/subtopics/fileresource/%s", uuid));
  }

  public Topic getTopic(UUID rootWebpageUuid) throws HttpException {
    return (Topic)
        doGetRequestForObject(
            String.format("/latest/subtopics/%s/topic", rootWebpageUuid), TopicImpl.class);
  }

  public Subtopic save(Subtopic subtopic) throws HttpException {
    return doPostRequestForObject("/latest/subtopics", (SubtopicImpl) subtopic);
  }

  public List<Entity> saveEntities(UUID uuid, List entities) throws HttpException {
    return doPostRequestForObjectList(String.format("/latest/subtopics/%s/entities", uuid), entities, Entity.class);
  }

  public List<FileResource> saveFileResources(UUID uuid, List fileResources) throws HttpException {
    return doPostRequestForObjectList(
        String.format("/latest/subtopics/%s/fileresources", uuid),
        fileResources,
        FileResourceImpl.class);
  }

  public Subtopic saveWithParentTopic(Subtopic subtopic, UUID parentTopicUuid)
      throws HttpException {
    return doPostRequestForObject(
        String.format("/latest/topics/%s/subtopic", parentTopicUuid), (SubtopicImpl) subtopic);
  }

  public Subtopic saveWithParentSubtopic(Subtopic subtopic, UUID parentSubtopicUuid)
      throws HttpException {
    return doPostRequestForObject(
        String.format("/latest/subtopics/%s/subtopic", parentSubtopicUuid),
        (SubtopicImpl) subtopic);
  }

  public Subtopic update(UUID uuid, Subtopic subtopic) throws HttpException {
    return doPutRequestForObject(
        String.format("/latest/subtopics/%s", uuid), (SubtopicImpl) subtopic);
  }
}
