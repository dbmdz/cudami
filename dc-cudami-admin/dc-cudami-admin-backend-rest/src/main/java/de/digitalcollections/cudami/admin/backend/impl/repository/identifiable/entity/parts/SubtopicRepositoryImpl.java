package de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.entity.parts;

import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity.parts.SubtopicRepository;
import de.digitalcollections.model.api.http.exceptions.client.ResourceNotFoundException;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.parts.Subtopic;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.parts.SubtopicImpl;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class SubtopicRepositoryImpl extends EntityPartRepositoryImpl<Subtopic, Entity>
    implements SubtopicRepository {

  @Autowired private SubtopicRepositoryEndpoint endpoint;

  @Override
  public long count() {
    return endpoint.count();
  }

  @Override
  public Subtopic create() {
    return new SubtopicImpl();
  }

  @Override
  public PageResponse<Subtopic> find(PageRequest pageRequest) {
    FindParams f = getFindParams(pageRequest);
    PageResponse<Subtopic> pageResponse =
        endpoint.find(
            f.getPageNumber(),
            f.getPageSize(),
            f.getSortField(),
            f.getSortDirection(),
            f.getNullHandling());
    return getGenericPageResponse(pageResponse);
  }

  @Override
  public Subtopic findOneByIdentifier(String namespace, String id) {
    try {
      return endpoint.findOneByIdentifier(namespace, id);
    } catch (ResourceNotFoundException e) {
      return null;
    }
  }

  @Override
  public Subtopic findOne(UUID uuid) {
    return endpoint.findOne(uuid);
  }

  @Override
  public Subtopic findOne(UUID uuid, Locale locale) {
    return endpoint.findOne(uuid, locale.toString());
  }

  @Override
  public List<Entity> getEntities(Subtopic subtopic) {
    return getEntities(subtopic.getUuid());
  }

  @Override
  public List<Entity> getEntities(UUID subtopicUuid) {
    List<Entity> entities = endpoint.getEntities(subtopicUuid);
    return entities.stream().map(e -> (Entity) e).collect(Collectors.toList());
  }

  @Override
  public List<FileResource> getFileResources(Subtopic subtopic) {
    return getFileResources(subtopic.getUuid());
  }

  @Override
  public List<FileResource> getFileResources(UUID subtopicUuid) {
    return endpoint.getFileResources(subtopicUuid);
  }

  @Override
  public Subtopic getParent(UUID nodeUuid) {
    return endpoint.getParent(nodeUuid);
  }

  @Override
  public List<Subtopic> getSubtopicsOfEntity(Entity entity) {
    return getSubtopicsOfEntity(entity.getUuid());
  }

  @Override
  public List<Subtopic> getSubtopicsOfEntity(UUID entityUuid) {
    List<Subtopic> subtopics = endpoint.getSubtopicsOfEntity(entityUuid);
    return subtopics.stream().map(s -> (Subtopic) s).collect(Collectors.toList());
  }

  @Override
  public List<Subtopic> getSubtopicsOfFileResource(FileResource fileResource) {
    return getSubtopicsOfFileResource(fileResource.getUuid());
  }

  @Override
  public List<Subtopic> getSubtopicsOfFileResource(UUID fileResourceUuid) {
    List<Subtopic> subtopics = endpoint.getSubtopicsOfFileResource(fileResourceUuid);
    return subtopics.stream().map(s -> (Subtopic) s).collect(Collectors.toList());
  }

  @Override
  public Subtopic save(Subtopic subtopic) {
    return endpoint.save(subtopic);
  }

  @Override
  public List<Entity> saveEntities(Subtopic subtopic, List<Entity> entities) {
    return saveEntities(subtopic.getUuid(), entities);
  }

  @Override
  public List<Entity> saveEntities(UUID subtopicUuid, List<Entity> entities) {
    List<Entity> savedEntities =
        endpoint.saveEntities(
            subtopicUuid, entities.stream().map(Entity.class::cast).collect(Collectors.toList()));
    return savedEntities.stream().map(e -> (Entity) e).collect(Collectors.toList());
  }

  @Override
  public List<FileResource> saveFileResources(Subtopic subtopic, List<FileResource> fileResources) {
    return saveFileResources(subtopic.getUuid(), fileResources);
  }

  @Override
  public List<FileResource> saveFileResources(UUID subtopicUuid, List<FileResource> fileResources) {
    return endpoint.saveFileResources(subtopicUuid, fileResources);
  }

  @Override
  public Subtopic update(Subtopic subtopic) {
    return endpoint.update(subtopic.getUuid(), subtopic);
  }

  @Override
  public List<Subtopic> getChildren(UUID uuid) {
    return endpoint.getChildren(uuid);
  }

  @Override
  public List<Subtopic> getChildren(Subtopic subtopic) {
    return getChildren(subtopic.getUuid());
  }

  @Override
  public Subtopic saveWithParentTopic(Subtopic subtopic, UUID parentTopicUUID) {
    return endpoint.saveWithParentTopic(subtopic, parentTopicUUID);
  }

  @Override
  public Subtopic saveWithParentSubtopic(Subtopic subtopic, UUID parentSubtopicUUID) {
    return endpoint.saveWithParentSubtopic(subtopic, parentSubtopicUUID);
  }
}
