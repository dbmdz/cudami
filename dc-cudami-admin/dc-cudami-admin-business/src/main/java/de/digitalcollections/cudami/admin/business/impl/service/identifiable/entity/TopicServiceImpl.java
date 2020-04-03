package de.digitalcollections.cudami.admin.business.impl.service.identifiable.entity;

import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity.TopicRepository;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.entity.TopicService;
import de.digitalcollections.model.api.identifiable.entity.Topic;
import de.digitalcollections.model.api.identifiable.entity.parts.Subtopic;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Service for Topic handling. */
@Service
public class TopicServiceImpl extends EntityServiceImpl<Topic> implements TopicService {

  @Autowired
  public TopicServiceImpl(TopicRepository repository) {
    super(repository);
  }

  @Override
  public List<Subtopic> getSubtopics(Topic topic) {
    return ((TopicRepository) repository).getSubtopics(topic);
  }
}
