package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.relation;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.relation.PredicateRepository;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.relation.PredicateService;
import de.digitalcollections.model.api.relations.Predicate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Service for managing predicates */
@Service
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
