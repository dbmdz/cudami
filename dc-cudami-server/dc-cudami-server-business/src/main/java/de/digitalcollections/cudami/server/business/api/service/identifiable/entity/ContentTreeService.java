package de.digitalcollections.cudami.server.business.api.service.identifiable.entity;

import de.digitalcollections.model.api.identifiable.entity.ContentTree;
import de.digitalcollections.model.api.identifiable.entity.parts.ContentNode;
import java.util.List;
import java.util.UUID;

/** Service for ContentTree. */
public interface ContentTreeService extends EntityService<ContentTree> {

  List<ContentNode> getRootNodes(ContentTree contentTree);

  List<ContentNode> getRootNodes(UUID uuid);
}
