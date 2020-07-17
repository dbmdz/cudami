package de.digitalcollections.cudami.client;

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
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CudamiSubtopicsClient extends CudamiBaseClient<SubtopicImpl> {

  public CudamiSubtopicsClient(String serverUrl) {
    super(serverUrl, SubtopicImpl.class);
  }

  public Subtopic create() {
    return new SubtopicImpl();
  }

  public long count() throws Exception {
    return Long.parseLong(doGetRequestForString("/latest/subtopics/count"));
  }

  public PageResponse<SubtopicImpl> find(PageRequest pageRequest) throws Exception {
    return doGetRequestForPagedObjectList("/latest/subtopics", pageRequest);
  }

  public Subtopic findOne(UUID uuid) throws Exception {
    return doGetRequestForObject(String.format("/latest/subtopics/%s", uuid));
  }

  public Subtopic findOne(UUID uuid, Locale locale) throws Exception {
    return findOne(uuid, locale.toString());
  }

  public Subtopic findOne(UUID uuid, String locale) throws Exception {
    return doGetRequestForObject(String.format("/latest/subtopics/%s?locale=%s", uuid, locale));
  }

  public Subtopic findOneByIdentifier(String namespace, String id) throws Exception {
    return doGetRequestForObject(
        String.format("/latest/subtopics/identifier/%s:%s.json", namespace, id));
  }

  public BreadcrumbNavigation getBreadcrumbNavigation(UUID uuid) throws Exception {
    return (BreadcrumbNavigation)
        doGetRequestForObject(
            String.format("/latest/subtopics/%s/breadcrumb", uuid), BreadcrumbNavigationImpl.class);
  }

  public List<SubtopicImpl> getChildren(UUID uuid) throws Exception {
    return doGetRequestForObjectList(String.format("/latest/subtopics/%s/children", uuid));
  }

  public PageResponse<SubtopicImpl> getChildren(UUID uuid, PageRequest pageRequest)
      throws Exception {
    return doGetRequestForPagedObjectList(
        String.format("/latest/subtopics/%s/children", uuid), pageRequest);
  }

  public List getEntities(UUID uuid) throws Exception {
    return doGetRequestForObjectList(
        String.format("/latest/subtopics/%s/entities", uuid), EntityImpl.class);
  }

  public List getFileResources(UUID uuid) throws Exception {
    return doGetRequestForObjectList(
        String.format("/latest/subtopics/%s/fileresources", uuid), FileResourceImpl.class);
  }

  public Subtopic getParent(UUID uuid) throws Exception {
    return doGetRequestForObject(String.format("/latest/subtopics/%s/parent", uuid));
  }

  public List getRelatedFileResources(UUID uuid) throws Exception {
    return doGetRequestForObjectList(
        String.format("/latest/entities/%s/related/fileresources", uuid), FileResourceImpl.class);
  }

  public List<SubtopicImpl> getSubtopicsOfEntity(UUID uuid) throws Exception {
    return doGetRequestForObjectList(String.format("/latest/subtopics/entity/%s", uuid));
  }

  public List<SubtopicImpl> getSubtopicsOfFileResource(UUID uuid) throws Exception {
    return doGetRequestForObjectList(String.format("/latest/subtopics/fileresource/%s", uuid));
  }

  public Topic getTopic(UUID rootWebpageUuid) throws Exception {
    return (Topic)
        doGetRequestForObject(
            String.format("/latest/subtopics/%s/topic", rootWebpageUuid), TopicImpl.class);
  }

  public Subtopic save(Subtopic subtopic) throws Exception {
    return doPostRequestForObject("/latest/subtopics", (SubtopicImpl) subtopic);
  }

  public List<Entity> saveEntities(UUID uuid, List entities) throws Exception {
    return doPostRequestForObjectList(
        String.format("/latest/subtopics/%s/entities", uuid), entities, EntityImpl.class);
  }

  public List<FileResource> saveFileResources(UUID uuid, List fileResources) throws Exception {
    return doPostRequestForObjectList(
        String.format("/latest/subtopics/%s/fileresources", uuid),
        fileResources,
        FileResourceImpl.class);
  }

  public Subtopic saveWithParentTopic(Subtopic subtopic, UUID parentTopicUuid) throws Exception {
    return doPostRequestForObject(
        String.format("/latest/topics/%s/subtopic", parentTopicUuid), (SubtopicImpl) subtopic);
  }

  public Subtopic saveWithParentSubtopic(Subtopic subtopic, UUID parentSubtopicUuid)
      throws Exception {
    return doPostRequestForObject(
        String.format("/latest/subtopics/%s/subtopic", parentSubtopicUuid),
        (SubtopicImpl) subtopic);
  }

  public Subtopic update(UUID uuid, Subtopic subtopic) throws Exception {
    return doPutRequestForObject(
        String.format("/latest/subtopics/%s", uuid), (SubtopicImpl) subtopic);
  }
}
