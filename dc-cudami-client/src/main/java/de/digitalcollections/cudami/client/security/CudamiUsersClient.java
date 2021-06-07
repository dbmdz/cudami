package de.digitalcollections.cudami.client.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiBaseClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.security.User;
import java.net.http.HttpClient;
import java.util.List;
import java.util.UUID;

public class CudamiUsersClient extends CudamiBaseClient<User> {

  public CudamiUsersClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, User.class, mapper);
  }

  public User create() {
    return new User();
  }

  public PageResponse<User> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/v5/users", pageRequest);
  }

  public List<User> findActiveAdminUsers() throws HttpException {
    return doGetRequestForObjectList("/v5/users?role=ADMIN&enabled=true");
  }

  public List<User> findAll() throws HttpException {
    return doGetRequestForObjectList("/v5/users");
  }

  public User findOne(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/v5/users/%s", uuid));
  }

  public User findOneByEmail(String email) throws HttpException {
    return doGetRequestForObject(String.format("/v5/users?email=%s", email));
  }

  public User save(User user) throws HttpException {
    return doPostRequestForObject("/v5/users", user);
  }

  public User update(UUID uuid, User user) throws HttpException {
    return doPutRequestForObject(String.format("/v5/users/%s", uuid), user);
  }
}
