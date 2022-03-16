package de.digitalcollections.cudami.client.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiRestClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.security.User;
import java.net.http.HttpClient;
import java.util.List;

public class CudamiUsersClient extends CudamiRestClient<User> {

  public CudamiUsersClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, User.class, mapper, "/v5/users");
  }

  public List<User> findActiveAdminUsers() throws TechnicalException {
    return doGetRequestForObjectList(String.format("%s?role=ADMIN&enabled=true", baseEndpoint));
  }

  @Override
  public List<User> findAll() throws TechnicalException {
    return doGetRequestForObjectList("/v5/users");
  }

  public User getByEmail(String email) throws TechnicalException {
    return doGetRequestForObject(String.format("%s?email=%s", baseEndpoint, email));
  }
}
