package de.digitalcollections.cudami.server.backend.impl.mock;

import de.digitalcollections.core.model.api.paging.PageRequest;
import de.digitalcollections.core.model.api.paging.PageResponse;
import de.digitalcollections.cudami.model.api.security.User;
import de.digitalcollections.cudami.model.api.security.enums.Role;
import de.digitalcollections.cudami.model.impl.security.UserImpl;
import de.digitalcollections.cudami.server.backend.api.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryImpl implements UserRepository<User, Long> {

  @Override
  public User create() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public PageResponse<User> find(PageRequest pageRequest) {
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
  public User findOne(Long id) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public long count() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public User save(User user) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public User update(User user) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

}
