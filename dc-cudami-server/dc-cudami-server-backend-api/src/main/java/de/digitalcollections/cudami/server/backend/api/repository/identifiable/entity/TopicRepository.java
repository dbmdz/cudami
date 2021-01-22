package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity;

import de.digitalcollections.model.api.identifiable.entity.Topic;
import de.digitalcollections.model.api.identifiable.entity.parts.Subtopic;
import java.util.List;
import java.util.UUID;

/** Repository for Topic persistence handling. */
public interface TopicRepository extends EntityRepository<Topic> {

  default List<Subtopic> getSubtopics(Topic topic) {
    if (topic == null) {
      return null;
    }
    return getSubtopics(topic.getUuid());
  }

  List<Subtopic> getSubtopics(UUID uuid);
}
