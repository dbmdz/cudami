package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.ContentTreeRepository;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.ContentTreeService;
import de.digitalcollections.model.api.identifiable.entity.ContentTree;
import de.digitalcollections.model.api.identifiable.entity.parts.ContentNode;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Service for ContentTree handling. */
@Service
// @Transactional(readOnly = true)
public class ContentTreeServiceImpl extends EntityServiceImpl<ContentTree>
    implements ContentTreeService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ContentTreeServiceImpl.class);

  @Autowired
  public ContentTreeServiceImpl(ContentTreeRepository repository) {
    super(repository);
  }

  @Override
  public List<ContentNode> getRootNodes(ContentTree contentTree) {
    return ((ContentTreeRepository) repository).getRootNodes(contentTree);
  }

  @Override
  public List<ContentNode> getRootNodes(UUID uuid) {
    return ((ContentTreeRepository) repository).getRootNodes(uuid);
  }
}
