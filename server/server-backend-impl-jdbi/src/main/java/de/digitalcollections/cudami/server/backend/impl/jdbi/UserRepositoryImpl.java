package de.digitalcollections.cudami.server.backend.impl.jdbi;

import de.digitalcollections.core.model.api.Sorting;
import de.digitalcollections.cudami.model.api.security.enums.Role;
import de.digitalcollections.cudami.model.impl.security.UserImpl;
import de.digitalcollections.cudami.server.backend.api.repository.UserRepository;
import java.util.Iterator;
import java.util.List;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryImpl implements UserRepository<UserImpl, Long> {

  @Autowired
  private Jdbi dbi;

  @Override
  public long count() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public UserImpl create() {
    return new UserImpl();
  }

  @Override
  public void delete(Long id) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void delete(UserImpl t) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void delete(Iterable<? extends UserImpl> itrbl) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void deleteAll() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public boolean exists(Long id) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public List<UserImpl> findActiveAdminUsers() {
    return dbi.withHandle(h -> h.createQuery(
            "SELECT * FROM users WHERE '" + Role.ADMIN.name() + "' = any(roles)")
            .mapToBean(UserImpl.class)
            .list());
  }

  @Override
  public List<UserImpl> findAll(Sort sort) {
    StringBuilder query = new StringBuilder("SELECT * FROM users ORDER BY :order_field");

    String sortField = null;
    if (sort != null) {
      Iterator<Sort.Order> iterator = sort.iterator();
      if (iterator.hasNext()) { // just supporting one field sorting until now
        Sort.Order order = iterator.next();
        sortField = order.getProperty();
        if (sortField != null) {
          Direction sortDirection = order.getDirection();
          if (sortDirection != null && sortDirection.isDescending()) {
            query.append(" DESC");
          }
        }
      }
    }
    if (sortField == null) {
      sortField = "id";
    }
    final String finalSortField = sortField;

    return dbi.withHandle(h -> h.createQuery(query.toString())
            .bind("order_field", finalSortField)
            .mapToBean(UserImpl.class)
            .list());
  }

  @Override
  public Page<UserImpl> findAll(Pageable pgbl) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public Iterable<UserImpl> findAll() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public Iterable<UserImpl> findAll(Iterable<Long> itrbl) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public List<UserImpl> findAll(Sorting sorting) {
    Sort sort = createSort(sorting);
    return findAll(sort);
  }

  private static Sort createSort(Sorting sorting) {
    final String sortField = sorting.getSortField();
    if (sortField == null) {
      return null;
    }
    Direction direction = Direction.ASC;
    if (sorting.getSortOrder() != null) {
      direction = Direction.fromStringOrNull(sorting.getSortOrder().name());
    }
    return new Sort(direction, sortField);
  }

  @Override
  public UserImpl findByEmail(String email) {
    List<UserImpl> users = dbi.withHandle(h -> h.createQuery(
            "SELECT * FROM users WHERE email = :email")
            .bind("email", email)
            .mapToBean(UserImpl.class)
            .list());
    if (users.isEmpty()) {
      return null;
    }
    return users.get(0);
  }

  @Override
  public UserImpl findOne(Long id) {
    List<UserImpl> users = dbi.withHandle(h -> h.createQuery(
            "SELECT * FROM users WHERE id = :id")
            .bind("id", id)
            .mapToBean(UserImpl.class)
            .list());
    if (users.isEmpty()) {
      return null;
    }
    return users.get(0);
  }

  @Override
  public <S extends UserImpl> S save(S user) {
//    UserImpl result = dbi.withHandle(h -> h.createQuery(
//            "INSERT INTO users(email, enabled, firstname, lastname, passwordHash, roles) VALUES (:email, :enabled, :firstname, :lastname, :passwordHash, :roles) RETURNING *")
//            .bindBean(user)
//            .bind("roles", user.getRoles().stream().map(Role::name).toArray(String[]::new))
//            .mapToBean(UserImpl.class)
//            .findOnly());
//    return (S) result;

    UserImpl result = dbi.withHandle(h -> h
            .registerArrayType(Role.class, "varchar")
            .createQuery("INSERT INTO users(email, enabled, firstname, lastname, passwordHash, roles) VALUES (:email, :enabled, :firstname, :lastname, :passwordHash, :roles) RETURNING *")
            .bindBean(user)
            .mapToBean(UserImpl.class)
            .findOnly());
    return (S) result;
  }

  @Override
  public <S extends UserImpl> Iterable<S> save(Iterable<S> itrbl) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

}
