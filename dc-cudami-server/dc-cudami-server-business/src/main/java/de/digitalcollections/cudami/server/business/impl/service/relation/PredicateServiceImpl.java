package de.digitalcollections.cudami.server.business.impl.service.relation;

import de.digitalcollections.cudami.server.backend.api.repository.relation.PredicateRepository;
import de.digitalcollections.cudami.server.business.api.service.relation.PredicateService;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.Sorting;
import de.digitalcollections.model.relation.Predicate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
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
  public boolean delete(UUID uuid) {
    return repository.deleteByUuid(uuid);
  }

  @Override
  public boolean delete(String value) {
    return repository.deleteByValue(value);
  }

  @Override
  public PageResponse<Predicate> find(PageRequest pageRequest) {
    setDefaultSorting(pageRequest);
    return repository.find(pageRequest);
  }

  @Override
  public List<Predicate> getAll() {
    return repository.getAll();
  }

  @Override
  public Predicate getByUuid(UUID uuid) {
    return repository.getByUuid(uuid);
  }

  @Override
  public Predicate getByValue(String value) {
    return repository.getByValue(value);
  }

  @Override
  public List<Locale> getLanguages() {
    return repository.getLanguages();
  }

  @Override
  public Predicate save(Predicate predicate) {
    return repository.save(predicate);
  }

  private void setDefaultSorting(PageRequest pageRequest) {
    if (!pageRequest.hasSorting()) {
      Sorting sorting = new Sorting(Direction.ASC, "value");
      pageRequest.setSorting(sorting);
    }
  }

  @Override
  public Predicate update(Predicate predicate) {
    return repository.update(predicate);
  }
}
