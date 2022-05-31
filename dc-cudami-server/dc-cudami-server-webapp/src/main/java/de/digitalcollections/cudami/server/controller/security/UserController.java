package de.digitalcollections.cudami.server.controller.security;

import de.digitalcollections.cudami.server.business.api.service.security.UserService;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import de.digitalcollections.model.security.Role;
import de.digitalcollections.model.security.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "User controller")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @Operation(summary = "Get all users")
  @GetMapping(
      value = {"/v6/users"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<User> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "searchTerm", required = false) String searchTerm) {
    PageRequest pageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    return userService.find(pageRequest);
  }

  @Operation(summary = "Get all users with given role and enabled status")
  @GetMapping(
      value = {"/v6/users", "/v5/users", "/v2/users", "/latest/users"},
      params = {"role", "enabled"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<User> getByRoleAndStatus(
      @RequestParam(name = "role") Role role, @RequestParam(name = "enabled") boolean enabled) {
    // FIXME: ignores role, just returns admins? what if we want other role users?
    return userService.getActiveAdminUsers();
  }

  @Operation(summary = "Get user by email address")
  @GetMapping(
      value = {"/v6/users", "/v5/users", "/v2/users", "/latest/users"},
      params = {"email"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public User getByUsername(@RequestParam(name = "email") String email) {
    return userService.getByUsername(email);
  }

  @Operation(summary = "Get user by uuid")
  @GetMapping(
      value = {"/v6/users/{uuid}", "/v5/users/{uuid}", "/v2/users/{uuid}", "/latest/users/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public User getByUuid(@PathVariable UUID uuid) {
    return userService.getByUuid(uuid);
  }

  @Operation(summary = "Save a newly created user")
  @PostMapping(
      value = {"/v6/users", "/v5/users", "/v2/users", "/latest/users"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public User save(@RequestBody User user, BindingResult errors) {
    return userService.save(user, errors);
  }

  @Operation(summary = "Update a user")
  @PutMapping(
      value = {"/v6/users/{uuid}", "/v5/users/{uuid}", "/v2/users/{uuid}", "/latest/users/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public User update(@PathVariable UUID uuid, @RequestBody User user, BindingResult errors) {
    assert Objects.equals(uuid, user.getUuid());
    return userService.update(user, errors);
  }
}
