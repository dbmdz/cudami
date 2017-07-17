package de.digitalcollections.cudami.server.backend.impl.mock;

import de.digitalcollections.core.model.api.Sorting;
import de.digitalcollections.cudami.model.api.security.User;
import de.digitalcollections.cudami.model.api.security.enums.Role;
import de.digitalcollections.cudami.model.impl.security.UserImpl;
import de.digitalcollections.cudami.server.backend.api.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryImpl implements UserRepository<User, Long> {

  @Override
  public User create() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public List<User> findAll(Sort sort) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public List<User> findAll(Sorting sorting) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public User findByEmail(String email) {
    final UserImpl user = new UserImpl();
    user.setEmail(email);
    user.setPasswordHash("password");
    return user;
  }

  @Override
  public List<User> findActiveAdminUsers() {
    List<User> result = new ArrayList<>();
    final UserImpl user = new UserImpl();
    user.getRoles().add(Role.ADMIN);
    result.add(user);
    return result;
  }

  @Override
  public Page<User> findAll(Pageable pgbl) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public <S extends User> S save(S s) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public <S extends User> Iterable<S> save(Iterable<S> itrbl) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public User findOne(Long id) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public boolean exists(Long id) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public Iterable<User> findAll() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public Iterable<User> findAll(Iterable<Long> itrbl) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public long count() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void delete(Long id) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void delete(User t) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void delete(Iterable<? extends User> itrbl) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void deleteAll() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

}
