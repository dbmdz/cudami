package de.digitalcollections.cudami.admin.controller.security;

import de.digitalcollections.cudami.admin.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.admin.business.api.service.security.UserService;
import de.digitalcollections.cudami.admin.controller.AbstractPagingAndSortingController;
import de.digitalcollections.cudami.admin.model.bootstraptable.BTResponse;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.security.User;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.UUID;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/** Controller for all public "Users" endpoints (API). */
@RestController
public class UserAPIController extends AbstractPagingAndSortingController<User> {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserAPIController.class);

  private final MessageSource messageSource;
  private final UserService<User> service;

  @SuppressFBWarnings(value = "EI_EXPOSE_REP2")
  public UserAPIController(MessageSource messageSource, UserService service) {
    this.messageSource = messageSource;
    this.service = service;
  }

  @GetMapping("/api/users/new")
  @ResponseBody
  public User create(
      @RequestParam(name = "admin", required = false, defaultValue = "false") boolean admin)
      throws ServiceException {
    if (admin) {
      return service.createAdminUser();
    }
    return service.create();
  }

  @SuppressFBWarnings
  @GetMapping("/api/users")
  @ResponseBody
  public BTResponse<User> find(
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(name = "limit", required = false, defaultValue = "1") int limit,
      @RequestParam(name = "search", required = false) String searchTerm,
      @RequestParam(name = "sort", required = false, defaultValue = "lastname") String sort,
      @RequestParam(name = "order", required = false, defaultValue = "asc") String order)
      throws TechnicalException, ServiceException {
    PageRequest pageRequest = createPageRequest(sort, order, null, null, offset, limit, searchTerm);
    PageResponse<User> pageResponse = service.find(pageRequest);
    return new BTResponse<>(pageResponse);
  }

  @GetMapping("/api/users/{uuid}")
  @ResponseBody
  public User getByUuid(@PathVariable UUID uuid) throws ServiceException {
    return this.service.getByUuid(uuid);
  }

  @PostMapping("/api/users")
  public ResponseEntity save(
      @RequestParam(value = "pwd1", required = false) String password1,
      @RequestParam(value = "pwd2", required = false) String password2,
      @RequestBody @Valid User user,
      BindingResult results)
      throws ServiceException {
    this.verifyBinding(results);
    if (results.hasErrors()) {
      return new ResponseEntity<>(results.getGlobalError(), HttpStatus.BAD_REQUEST);
    }
    User userDb = service.create(user, password1, password2, results);
    if (results.hasErrors()) {
      return new ResponseEntity<>(results.getGlobalError(), HttpStatus.BAD_REQUEST);
    }
    return ResponseEntity.ok(userDb);
  }

  /* endpoint for addUserStatusChangeHandler in index.js */
  @PatchMapping("/api/users/{uuid}")
  public ResponseEntity setStatus(@PathVariable("uuid") UUID uuid, @RequestBody User user) {
    boolean successful = service.setStatus(uuid, user.isEnabled());
    return new ResponseEntity<>(
        successful, successful ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @PutMapping("/api/users/{uuid}")
  public ResponseEntity update(
      @PathVariable UUID uuid,
      @RequestParam(name = "pwd1", required = false) String password1,
      @RequestParam(name = "pwd2", required = false) String password2,
      @RequestBody User user,
      BindingResult results)
      throws ServiceException {
    this.verifyBinding(results);
    if (results.hasErrors()) {
      return new ResponseEntity<>(results.getGlobalError(), HttpStatus.BAD_REQUEST);
    }
    User updatedUser = this.service.update(user, password1, password2, results);
    if (results.hasErrors()) {
      return new ResponseEntity<>(results.getGlobalError(), HttpStatus.BAD_REQUEST);
    }
    return ResponseEntity.ok(updatedUser);
  }
}
