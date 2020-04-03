package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.TopicRepository;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.TopicService;
import de.digitalcollections.model.api.identifiable.entity.Topic;
import de.digitalcollections.model.api.identifiable.entity.parts.Subtopic;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Service for Topic handling. */
@Service
public class TopicServiceImpl extends EntityServiceImpl<Topic> implements TopicService {

  private static final Logger LOGGER = LoggerFactory.getLogger(TopicServiceImpl.class);

  @Autowired
  public TopicServiceImpl(TopicRepository repository) {
    super(repository);
  }

  @Override
  public List<Subtopic> getSubtopics(Topic topic) {
    return ((TopicRepository) repository).getSubtopics(topic);
  }

  @Override
  public List<Subtopic> getSubtopics(UUID uuid) {
    return ((TopicRepository) repository).getSubtopics(uuid);
  }
}
