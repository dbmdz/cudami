package de.digitalcollections.cudami.client.backend.impl.repository;

import de.digitalcollections.core.model.api.Sorting;
import de.digitalcollections.cudami.client.backend.api.repository.UserRepository;
import de.digitalcollections.cudami.client.backend.impl.repository.exceptionhandling.EndpointErrorDecoder;
import de.digitalcollections.cudami.client.backend.impl.repository.exceptionhandling.client.ResourceNotFoundException;
import de.digitalcollections.cudami.model.impl.security.UserImpl;
import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryImpl implements UserRepository<UserImpl, Long> {

  private final UserRepositoryEndpoint endpoint = Feign.builder()
          .decoder(new GsonDecoder())
          .encoder(new GsonEncoder())
          .errorDecoder(new EndpointErrorDecoder())
          .target(UserRepositoryEndpoint.class, "http://localhost:8080");

  @Override
  public UserImpl create() {
    return endpoint.create();
  }

  @Override
  public List<UserImpl> findAll(Sorting sort) {
    return endpoint.findAll(sort.getSortOrder().name(), sort.getSortField(), sort.getSortType().name());
  }

  @Override
  public UserImpl findByEmail(String email) {
    UserImpl user;
    try {
      user = endpoint.findByEmail(email);
    } catch (ResourceNotFoundException e) {
      // TODO may be throw a business exception instead returning null to make more clear what happened?
      user = null;
    }
    return user;
  }

  @Override
  public List<UserImpl> findActiveAdminUsers() {
    return endpoint.findActiveAdminUsers();
  }

//  @Override
//  public Page<UserImpl> findAll(Pageable pgbl) {
//    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//  }
  @Override
  public UserImpl save(UserImpl user) {
    return (UserImpl) endpoint.save(user);
  }

//  @Override
//  public <S extends UserImpl> Iterable<S> save(Iterable<S> itrbl) {
//    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//  }
  @Override
  public UserImpl findOne(Long id) {
    return endpoint.findOne(id);
  }

//  @Override
//  public boolean exists(Long id) {
//    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//  }
//  @Override
//  public Iterable<UserImpl> findAll() {
//    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//  }
//  @Override
//  public Iterable<UserImpl> findAll(Iterable<Long> itrbl) {
//    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//  }
//  @Override
//  public long count() {
//    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//  }
//  @Override
//  public void delete(Long id) {
//    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//  }
//  @Override
//  public void delete(UserImpl t) {
//    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//  }
//  @Override
//  public void delete(Iterable<? extends UserImpl> itrbl) {
//    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//  }
//  @Override
//  public void deleteAll() {
//    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//  }
}
