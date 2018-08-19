package de.digitalcollections.cudami.admin.business.impl.service.identifiable.entity;

import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity.ContentTreeRepository;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.entity.ContentTreeService;
import de.digitalcollections.model.api.identifiable.entity.ContentTree;
import de.digitalcollections.model.api.identifiable.resource.ContentNode;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for ContentTree handling.
 */
@Service
//@Transactional(readOnly = true)
public class ContentTreeServiceImpl extends EntityServiceImpl<ContentTree> implements ContentTreeService<ContentTree> {

  @Autowired
  public ContentTreeServiceImpl(ContentTreeRepository<ContentTree> repository) {
    super(repository);
  }

  @Override
  public List<ContentNode> getRootNodes(ContentTree contentTree) {
    return ((ContentTreeRepository) repository).getRootNodes(contentTree);
  }
}
