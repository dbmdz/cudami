package de.digitalcollections.cudami.server.business.api.service.identifiable.entity;

import de.digitalcollections.model.api.identifiable.entity.Topic;
import de.digitalcollections.model.api.identifiable.entity.parts.Subtopic;
import java.util.List;
import java.util.UUID;

/** Service for Topic. */
public interface TopicService extends EntityService<Topic> {

  List<Subtopic> getSubtopics(Topic topic);

  List<Subtopic> getSubtopics(UUID uuid);
}
