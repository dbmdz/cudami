package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.semantic;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.semantic.SubjectRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.semantic.SubjectService;
import de.digitalcollections.cudami.server.business.impl.service.UniqueObjectServiceImpl;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.semantic.Subject;
import de.digitalcollections.model.validation.ValidationException;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
// override the default from super class to increase isolation level
@Transactional(rollbackFor = Exception.class, isolation = Isolation.SERIALIZABLE)
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

  /* KEEP THESE "SUPERFLUOUS" OVERRIDES!
   *
   * The isolation level must be increased to prevent duplicates in the DB.
   * Although the annotation `@Transactional` overrides the one of the super class (and btw is inherited by further sub classes)
   * it does not extend to methods inherited from the super class.
   * To change the isolation level of inherited methods too they must be overridden here.
   */
  @Override
  public void save(Subject uniqueObject) throws ValidationException, ServiceException {
    super.save(uniqueObject);
  }

  @Override
  public void update(Subject uniqueObject) throws ValidationException, ServiceException {
    super.update(uniqueObject);
  }

  @Override
  public int delete(Set<Subject> uniqueObjects) throws ConflictException, ServiceException {
    return super.delete(uniqueObjects);
  }

  @Override
  public boolean delete(Subject uniqueObject) throws ConflictException, ServiceException {
    return super.delete(uniqueObject);
  }
  /* KEEP THESE "SUPERFLUOUS" OVERRIDES! */
}
