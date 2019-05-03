package de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity;

import de.digitalcollections.model.api.identifiable.entity.ContentTree;
import de.digitalcollections.model.api.identifiable.entity.parts.ContentNode;
import java.util.List;
import java.util.UUID;

/**
 * Repository for ContentTree persistence handling.
 */
public interface ContentTreeRepository extends EntityRepository<ContentTree> {

  List<ContentNode> getRootNodes(ContentTree contentTree);

  List<ContentNode> getRootNodes(UUID uuid);
}
