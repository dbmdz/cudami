package de.digitalcollections.cudami.admin.business.api.service.identifiable.entity;

import de.digitalcollections.model.api.identifiable.entity.ContentTree;
import de.digitalcollections.model.api.identifiable.entity.parts.ContentNode;
import java.util.List;

/** Service for ContentTree. */
public interface ContentTreeService extends EntityService<ContentTree> {

  List<ContentNode> getRootNodes(ContentTree contentTree);
}
