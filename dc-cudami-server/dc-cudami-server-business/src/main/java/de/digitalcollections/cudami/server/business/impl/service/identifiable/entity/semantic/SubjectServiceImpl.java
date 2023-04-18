package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.semantic;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.semantic.SubjectRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.semantic.SubjectService;
import de.digitalcollections.cudami.server.business.impl.service.UniqueObjectServiceImpl;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.semantic.Subject;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class SubjectServiceImpl extends UniqueObjectServiceImpl<Subject, SubjectRepository>
    implements SubjectService {

  public SubjectServiceImpl(SubjectRepository repository) {
    super(repository);
  }

  @Override
  public Subject getByTypeAndIdentifier(String type, Identifier identifier)
      throws ServiceException {
    try {
      return repository.getByTypeAndIdentifier(type, identifier);
    } catch (RepositoryException e) {
      throw new ServiceException(
          "cannot get by type=" + type + ", identifier=" + identifier + ": " + e, e);
    }
  }

  @Override
  public List<Locale> getLanguages() throws ServiceException {
    try {
      return repository.getLanguages();
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }
}
