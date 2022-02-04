package de.digitalcollections.cudami.client.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiRestClient;
import de.digitalcollections.model.exception.http.HttpException;
import de.digitalcollections.model.security.User;
import java.net.http.HttpClient;
import java.util.List;

public class CudamiUsersClient extends CudamiRestClient<User> {

  public CudamiUsersClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, User.class, mapper, "/v5/users");
  }

  public List<User> findActiveAdminUsers() throws HttpException {
    return doGetRequestForObjectList(String.format("%s?role=ADMIN&enabled=true", baseEndpoint));
  }

  @Override
  public List<User> findAll() throws HttpException {
    return doGetRequestForObjectList("/v5/users");
  }

  /**
   * @deprecated This method is subject to be removed.
   *     <p>Use {@link #getByEmail(java.lang.String)} instead.
   * @param email email of user
   * @return user with given email
   */
  @Deprecated(forRemoval = true)
  public User findOneByEmail(String email) throws HttpException {
    return getByEmail(email);
  }

  public User getByEmail(String email) throws HttpException {
    return doGetRequestForObject(String.format("%s?email=%s", baseEndpoint, email));
  }
}
