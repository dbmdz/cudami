package de.digitalcollections.cudami.server.backend.impl.jpa.repository;

import java.io.Serializable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * @param <T> jpa entity
 * @param <ID> unique entity id
 * @param <R> repository instance
 */
public abstract class AbstractPagingAndSortingRepositoryImplJpa<T, ID extends Serializable, R extends JpaRepository>
        implements PagingAndSortingRepository<T, ID> {

  protected R jpaRepository;

  abstract void setJpaRepository(R jpaRepository);

  @Override
  public long count() {
    return jpaRepository.count();
  }

  @Override
  public void delete(ID id) {
    jpaRepository.delete(id);
  }

  @Override
  public void delete(T entity) {
    jpaRepository.delete(entity);
  }

  @Override
  public void delete(Iterable<? extends T> entities) {
    jpaRepository.delete(entities);
  }

  @Override
  public void deleteAll() {
    jpaRepository.deleteAll();
  }

  @Override
  public boolean exists(ID id) {
    return jpaRepository.exists(id);
  }

  @Override
  public Iterable<T> findAll(Sort sort) {
    return jpaRepository.findAll(sort);
  }

  @Override
  public Page<T> findAll(Pageable pageable) {
    return jpaRepository.findAll(pageable);
  }

  @Override
  public Iterable<T> findAll() {
    return jpaRepository.findAll();
  }

  @Override
  public Iterable<T> findAll(Iterable<ID> ids) {
    return jpaRepository.findAll(ids);
  }

  @Override
  public T findOne(ID id) {
    return (T) jpaRepository.findOne(id);
  }

  @Override
  public <S extends T> S save(S entity) {
    return (S) jpaRepository.save(entity);
  }

  @Override
  public <S extends T> Iterable<S> save(Iterable<S> entities) {
    return jpaRepository.save(entities);
  }
}
