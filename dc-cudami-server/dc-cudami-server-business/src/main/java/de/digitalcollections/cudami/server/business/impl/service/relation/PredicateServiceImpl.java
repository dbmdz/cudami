package de.digitalcollections.cudami.server.business.impl.service.relation;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.relation.PredicateRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.relation.PredicateService;
import de.digitalcollections.cudami.server.business.impl.service.UniqueObjectServiceImpl;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.Sorting;
import de.digitalcollections.model.relation.Predicate;
import de.digitalcollections.model.validation.ValidationException;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

/** Service for managing predicates */
@Service
// @Transactional(rollbackFor = {Exception.class}) //is set on super class
public class PredicateServiceImpl extends UniqueObjectServiceImpl<Predicate, PredicateRepository>
    implements PredicateService {

  public PredicateServiceImpl(PredicateRepository repository) {
    super(repository);
  }

  @Override
  public boolean deleteByValue(String value) throws ServiceException {
    try {
      return repository.deleteByValue(value);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public Predicate getByValue(String value) throws ServiceException {
    try {
      return repository.getByValue(value);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
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

  @Override
  public void saveOrUpdate(Predicate predicate) throws ValidationException, ServiceException {
    try {
      repository.saveOrUpdate(predicate);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  protected void setDefaultSorting(PageRequest pageRequest) {
    if (!pageRequest.hasSorting()) {
      Sorting sorting = new Sorting(Direction.ASC, "value");
      pageRequest.setSorting(sorting);
    }
  }
}
