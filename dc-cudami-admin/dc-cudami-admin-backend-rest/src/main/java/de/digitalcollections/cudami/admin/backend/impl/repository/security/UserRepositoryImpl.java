package de.digitalcollections.cudami.admin.backend.impl.repository.security;

import de.digitalcollections.cudami.admin.backend.api.repository.security.UserRepository;
import de.digitalcollections.model.api.http.exceptions.client.ResourceNotFoundException;
import de.digitalcollections.model.api.paging.Order;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.Sorting;
import de.digitalcollections.model.impl.security.UserImpl;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryImpl implements UserRepository<UserImpl> {

  @Autowired private UserRepositoryEndpoint endpoint;

  @Override
  public long count() {
    throw new UnsupportedOperationException("Not supported yet.");
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
      // TODO may be throw a business exception instead returning null to make more clear what
      // happened?
      user = null;
    }
    return user;
  }

  @Override
  public List<UserImpl> findActiveAdminUsers() {
    return endpoint.findActiveAdminUsers();
  }

  @Override
  public UserImpl save(UserImpl user) {
    return (UserImpl) endpoint.save(user);
  }

  @Override
  public UserImpl update(UserImpl user) {
    return (UserImpl) endpoint.update(user.getUuid(), user);
  }

  @Override
  public UserImpl findOne(UUID uuid) {
    return endpoint.findOne(uuid);
  }
}
