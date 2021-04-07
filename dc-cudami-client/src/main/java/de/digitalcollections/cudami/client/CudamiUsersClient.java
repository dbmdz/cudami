package de.digitalcollections.cudami.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.security.User;
import de.digitalcollections.model.impl.security.UserImpl;
import java.net.http.HttpClient;
import java.util.List;
import java.util.UUID;

public class CudamiUsersClient extends CudamiBaseClient<UserImpl> {

  public CudamiUsersClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, UserImpl.class, mapper);
  }

  public User create() {
    return new UserImpl();
  }

  public PageResponse<UserImpl> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/v2/users", pageRequest);
  }

  public List<UserImpl> findActiveAdminUsers() throws HttpException {
    return doGetRequestForObjectList("/v2/users?role=ADMIN&enabled=true");
  }

  public List<UserImpl> findAll() throws HttpException {
    return doGetRequestForObjectList("/v2/users");
  }

  public User findOne(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/v2/users/%s", uuid));
  }

  public User findOneByEmail(String email) throws HttpException {
    return doGetRequestForObject(String.format("/v2/users?email=%s", email));
  }

  public User save(User user) throws HttpException {
    return doPostRequestForObject("/v2/users", (UserImpl) user);
  }

  public User update(UUID uuid, User user) throws HttpException {
    return doPutRequestForObject(String.format("/v2/users/%s", uuid), (UserImpl) user);
  }
}
