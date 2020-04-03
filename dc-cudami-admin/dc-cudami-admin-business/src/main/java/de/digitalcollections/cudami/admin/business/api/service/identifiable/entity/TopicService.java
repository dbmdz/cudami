package de.digitalcollections.cudami.admin.business.api.service.identifiable.entity;

import de.digitalcollections.model.api.identifiable.entity.Topic;
import de.digitalcollections.model.api.identifiable.entity.parts.Subtopic;
import java.util.List;

/** Service for Topic. */
public interface TopicService extends EntityService<Topic> {

  List<Subtopic> getSubtopics(Topic topic);
}
