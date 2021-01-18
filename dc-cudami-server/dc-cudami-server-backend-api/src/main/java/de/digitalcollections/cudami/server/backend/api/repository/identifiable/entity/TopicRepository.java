package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity;

import de.digitalcollections.model.api.identifiable.entity.Topic;
import de.digitalcollections.model.api.identifiable.entity.parts.Subtopic;
import java.util.List;
import java.util.UUID;

/** Repository for Topic persistence handling.
 * @param <T> instance of topic implementation */
public interface TopicRepository<T extends Topic> extends EntityRepository<T> {

  List<Subtopic> getSubtopics(T topic);

  List<Subtopic> getSubtopics(UUID uuid);
}
