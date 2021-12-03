package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.relation;

import de.digitalcollections.cudami.server.backend.api.repository.relation.PredicateRepository;
import de.digitalcollections.cudami.server.business.api.service.relation.PredicateService;
import de.digitalcollections.model.relation.Predicate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for managing predicates */
@Service
@Transactional(rollbackFor = {Exception.class})
public class PredicateServiceImpl implements PredicateService {

  @Autowired private PredicateRepository repository;

  @Override
  public long count() {
    return repository.count();
  }

  @Override
  public void delete(String value) {
    repository.delete(value);
  }

  @Override
  public List<Predicate> findAll() {
    return repository.findAll();
  }

  @Override
  public Predicate getByValue(String value) {
    return repository.findOneByValue(value);
  }

  @Override
  public Predicate save(Predicate predicate) {
    return repository.save(predicate);
  }
}
