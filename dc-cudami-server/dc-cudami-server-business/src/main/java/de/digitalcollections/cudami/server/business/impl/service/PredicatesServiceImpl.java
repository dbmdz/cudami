package de.digitalcollections.cudami.server.business.impl.service;

/** Service for managing predicates */
import de.digitalcollections.cudami.server.backend.api.repository.PredicatesRepository;
import de.digitalcollections.cudami.server.business.api.service.PredicatesService;
import de.digitalcollections.model.api.relations.Predicate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PredicatesServiceImpl implements PredicatesService {

  @Autowired private PredicatesRepository repository;

  @Override
  public List<Predicate> getPredicates() {
    return repository.findAll();
  }

  @Override
  public Predicate save(Predicate predicate) {
    return repository.save(predicate);
  }
}
