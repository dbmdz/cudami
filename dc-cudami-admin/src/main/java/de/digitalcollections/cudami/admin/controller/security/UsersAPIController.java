package de.digitalcollections.cudami.admin.controller.security;

import de.digitalcollections.cudami.admin.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.admin.business.api.service.security.UserService;
import de.digitalcollections.cudami.admin.controller.AbstractUniqueObjectController;
import de.digitalcollections.cudami.admin.controller.ParameterHelper;
import de.digitalcollections.cudami.admin.model.bootstraptable.BTRequest;
import de.digitalcollections.cudami.admin.model.bootstraptable.BTResponse;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.security.User;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/** Controller for all public "Users" endpoints (API). */
@RestController
public class UsersAPIController extends AbstractUniqueObjectController<User> {

  private static final Logger LOGGER = LoggerFactory.getLogger(UsersAPIController.class);

  private final UserService<User> service;

  @SuppressFBWarnings(value = "EI_EXPOSE_REP2")
  public UsersAPIController(UserService<User> service) {
    super(null, null);
    this.service = service;
  }

  @SuppressFBWarnings
  @GetMapping("/api/users")
  @ResponseBody
  public BTResponse<User> find(
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(name = "limit", required = false, defaultValue = "10") int limit,
      @RequestParam(name = "search", required = false) String searchTerm,
      @RequestParam(name = "sort", required = false, defaultValue = "lastname") String sortProperty,
      @RequestParam(name = "order", required = false, defaultValue = "asc") String sortOrder)
      throws TechnicalException, ServiceException {
    BTRequest btRequest =
        createBTRequest(
            User.class, offset, limit, sortProperty, sortOrder, "lastname", searchTerm, null);
    PageResponse<User> pageResponse = service.find(btRequest);
    return new BTResponse<>(pageResponse);
  }

  @GetMapping("/api/v1/users")
  public ResponseEntity<Map<String, Object>> findAll(
      @RequestParam(required = false) String email,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size)
      throws ServiceException {
    try {
      PageRequest pageRequest = PageRequest.builder().pageNumber(page).pageSize(size).build();
      PageResponse<User> pageResponse = service.find(pageRequest);

      List<User> users = pageResponse.getContent();

      Map<String, Object> response = new HashMap<>(4);
      response.put("users", users);
      response.put("currentPage", pageResponse.getPageNumber());
      response.put("totalItems", pageResponse.getTotalElements());
      response.put("totalPages", pageResponse.getTotalPages());
      return new ResponseEntity<>(response, HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /* endpoint for addUserStatusChangeHandler in index.js, see users/view.html */
  @PatchMapping("/api/users/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  public ResponseEntity setStatus(@PathVariable("uuid") UUID uuid, @RequestBody User user) {
    boolean successful = service.setStatus(uuid, user.isEnabled());
    return new ResponseEntity<>(
        successful, successful ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
