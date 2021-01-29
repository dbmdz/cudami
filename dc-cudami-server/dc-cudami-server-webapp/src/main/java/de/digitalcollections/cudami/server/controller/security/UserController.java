package de.digitalcollections.cudami.server.controller.security;

import de.digitalcollections.cudami.server.business.api.service.security.UserService;
import de.digitalcollections.model.api.paging.Order;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.Sorting;
import de.digitalcollections.model.api.security.User;
import de.digitalcollections.model.api.security.enums.Role;
import de.digitalcollections.model.impl.paging.PageRequestImpl;
import de.digitalcollections.model.impl.paging.SortingImpl;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiResponseObject;
import org.springframework.beans.factory.annotation.Autowired;
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
@Api(description = "The user controller", name = "User controller")
public class UserController {

  @Autowired private UserService service;

  @ApiMethod(description = "Get all users with given role and enabled status")
  @GetMapping(
      value = {"/latest/users", "/v2/users"},
      params = {"role", "enabled"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public List<User> getByRoleAndStatus(
      @RequestParam(name = "role") Role role, @RequestParam(name = "enabled") boolean enabled) {
    return service.findActiveAdminUsers();
  }

  @ApiMethod(description = "Get all users")
  @GetMapping(
      value = {"/latest/users", "/v2/users"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public PageResponse<User> findAll(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy) {
    PageRequest pageRequest = new PageRequestImpl(pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting =
          new SortingImpl(sortBy.stream().filter(Objects::nonNull).collect(Collectors.toList()));
      pageRequest.setSorting(sorting);
    }
    return service.find(pageRequest);
  }

  @ApiMethod(description = "Get user by uuid")
  @GetMapping(
      value = {"/latest/users/{uuid}", "/v2/users/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public User findById(@PathVariable UUID uuid) {
    return service.get(uuid);
  }

  @ApiMethod(description = "Get user by email address")
  @GetMapping(
      value = {"/latest/users", "/v2/users"},
      params = {"email"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public User findByName(@RequestParam(name = "email") String email) {
    return service.loadUserByUsername(email);
  }

  @ApiMethod(description = "Save a newly created user")
  @PostMapping(
      value = {"/latest/users", "/v2/users"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public User save(@RequestBody User user, BindingResult errors) {
    return service.save(user, errors);
  }

  @ApiMethod(description = "Update a user")
  @PutMapping(
      value = {"/latest/users/{uuid}", "/v2/users/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public User update(@PathVariable UUID uuid, @RequestBody User user, BindingResult errors) {
    assert Objects.equals(uuid, user.getUuid());
    return service.update(user, errors);
  }
}
