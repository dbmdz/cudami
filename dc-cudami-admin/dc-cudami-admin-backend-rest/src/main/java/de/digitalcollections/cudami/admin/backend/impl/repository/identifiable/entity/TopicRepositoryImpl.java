package de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.entity;

import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity.TopicRepository;
import de.digitalcollections.model.api.http.exceptions.client.ResourceNotFoundException;
import de.digitalcollections.model.api.identifiable.entity.Topic;
import de.digitalcollections.model.api.identifiable.entity.parts.Subtopic;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.TopicImpl;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class TopicRepositoryImpl extends EntityRepositoryImpl<Topic> implements TopicRepository {

  @Autowired private TopicRepositoryEndpoint endpoint;

  @Override
  public long count() {
    return endpoint.count();
  }

  @Override
  public Topic create() {
    return new TopicImpl();
  }

  @Override
  public PageResponse<Topic> find(PageRequest pageRequest) {
    FindParams f = getFindParams(pageRequest);
    PageResponse<Topic> pageResponse =
        endpoint.find(
            f.getPageNumber(),
            f.getPageSize(),
            f.getSortField(),
            f.getSortDirection(),
            f.getNullHandling());
    return getGenericPageResponse(pageResponse);
  }

  @Override
  public Topic findOneByIdentifier(String namespace, String id) {
    try {
      return endpoint.findOneByIdentifier(namespace, id);
    } catch (ResourceNotFoundException e) {
      return null;
    }
  }

  @Override
  public Topic findOne(UUID uuid) {
    return endpoint.findOne(uuid);
  }

  @Override
  public Topic findOne(UUID uuid, Locale locale) {
    return endpoint.findOne(uuid, locale.toString());
  }

  @Override
  public List<Subtopic> getSubtopics(Topic topic) {
    return getSubtopics(topic.getUuid());
  }

  @Override
  public List<Subtopic> getSubtopics(UUID uuid) {
    return endpoint.getSubtopics(uuid);
  }

  @Override
  public Topic save(Topic topic) {
    return endpoint.save(topic);
  }

  @Override
  public Topic update(Topic topic) {
    return endpoint.update(topic.getUuid(), topic);
  }
}
