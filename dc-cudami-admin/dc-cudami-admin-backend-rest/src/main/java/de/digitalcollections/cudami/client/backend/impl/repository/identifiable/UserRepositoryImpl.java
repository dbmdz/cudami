package de.digitalcollections.cudami.client.backend.impl.repository.identifiable;

import de.digitalcollections.core.model.api.http.exceptions.client.ResourceNotFoundException;
import de.digitalcollections.core.model.api.paging.Order;
import de.digitalcollections.core.model.api.paging.PageRequest;
import de.digitalcollections.core.model.api.paging.PageResponse;
import de.digitalcollections.core.model.api.paging.Sorting;
import de.digitalcollections.cudami.client.backend.api.repository.identifiable.UserRepository;
import de.digitalcollections.cudami.model.impl.security.UserImpl;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryImpl implements UserRepository<UserImpl> {

  @Autowired
  private UserRepositoryEndpoint endpoint;

  @Override
  public long count() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public UserImpl create() {
    return new UserImpl();
  }

  @Override
  public PageResponse<UserImpl> find(PageRequest pageRequest) {

    int pageNumber = pageRequest.getPageNumber();
    int pageSize = pageRequest.getPageSize();

    Sorting sorting = pageRequest.getSorting();
    Iterator<Order> iterator = sorting.iterator();

    // FIXME add support for multiple sort fields
    String sortField = "";
    String sortDirection = "";
    String nullHandling = "";
//    while (iterator.hasNext()) {
    if (iterator.hasNext()) {
      Order order = iterator.next();
      sortField = order.getProperty() == null ? "" : order.getProperty();
      sortDirection = order.getDirection() == null ? "" : order.getDirection().name();
      nullHandling = order.getNullHandling() == null ? "" : order.getNullHandling().name();
    }

    return endpoint.find(pageNumber, pageSize, sortField, sortDirection, nullHandling);
  }

  @Override
  public List<UserImpl> findAll() {
    PageResponse<UserImpl> response = endpoint.find(-1, -1, "", "", "");
    return response.getContent();
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
//  public Page<UserImpl> find(Pageable pgbl) {
//    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//  }
  @Override
  public UserImpl save(UserImpl user) {
    return (UserImpl) endpoint.save(user);
  }

  @Override
  public UserImpl update(UserImpl user) {
    return (UserImpl) endpoint.update(user.getUuid(), user);
  }

//  @Override
//  public <S extends UserImpl> Iterable<S> save(Iterable<S> itrbl) {
//    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//  }
  @Override
  public UserImpl findOne(UUID uuid) {
    return endpoint.findOne(uuid);
  }

//  @Override
//  public boolean exists(Long id) {
//    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//  }
//  @Override
//  public Iterable<UserImpl> find() {
//    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//  }
//  @Override
//  public Iterable<UserImpl> find(Iterable<Long> itrbl) {
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
