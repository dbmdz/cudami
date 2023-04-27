package de.digitalcollections.cudami.server.business.impl.service.semantic;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.semantic.TagRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.semantic.TagService;
import de.digitalcollections.cudami.server.business.impl.service.UniqueObjectServiceImpl;
import de.digitalcollections.model.semantic.Tag;
import org.springframework.stereotype.Service;

@Service
// @Transactional(rollbackFor = Exception.class) //is set on super class
public class TagServiceImpl extends UniqueObjectServiceImpl<Tag, TagRepository>
    implements TagService {

  public TagServiceImpl(TagRepository repository) {
    super(repository);
  }

  @Override
  public Tag getByValue(String value) throws ServiceException {
    try {
      return repository.getByValue(value);
    } catch (RepositoryException e) {
      throw new ServiceException("cannot get by value=" + value + ": " + e, e);
    }
  }
}
