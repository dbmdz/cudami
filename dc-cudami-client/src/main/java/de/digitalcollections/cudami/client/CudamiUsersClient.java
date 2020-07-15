package de.digitalcollections.cudami.client;

import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.security.User;
import de.digitalcollections.model.impl.security.UserImpl;
import java.util.List;
import java.util.UUID;

public class CudamiUsersClient extends CudamiBaseClient<UserImpl> {

  public CudamiUsersClient(String serverUrl) {
    super(serverUrl, UserImpl.class);
  }

  public User create() {
    return new UserImpl();
  }

  public PageResponse<UserImpl> find(PageRequest pageRequest) throws Exception {
    return doGetRequestForPagedObjectList("/latest/users", pageRequest);
  }

  public List<UserImpl> findActiveAdminUsers() throws Exception {
    return doGetRequestForObjectList("/latest/users?role=ADMIN&enabled=true");
  }

  public List<UserImpl> findAll() throws Exception {
    return doGetRequestForObjectList("/latest/users");
  }

  public User findOne(UUID uuid) throws Exception {
    return doGetRequestForObject(String.format("/latest/users/%s", uuid));
  }

  public User findOneByEmail(String email) throws Exception {
    return doGetRequestForObject(String.format("/latest/users?email=%s", email));
  }

  public User save(User user) throws Exception {
    return doPostRequestForObject("/latest/users", (UserImpl) user);
  }

  public User update(UUID uuid, User user) throws Exception {
    return doPutRequestForObject(String.format("/latest/users/%s", uuid), (UserImpl) user);
  }
}
