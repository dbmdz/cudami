package de.digitalcollections.cudami.server.controller;

import de.digitalcollections.core.model.impl.SortingImpl;
import de.digitalcollections.cudami.model.api.security.User;
import de.digitalcollections.cudami.model.api.security.enums.Role;
import de.digitalcollections.cudami.server.business.api.service.UserService;
import java.util.List;
import java.util.Objects;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiResponseObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
//@RequestMapping("/v1/users") // moved to each method (more readable)
@Api(description = "The user controller", name = "User controller")
public class UserController {

  @Autowired
  private UserService service;

  @ApiMethod(description = "get all users with given role and enabled status")
  @RequestMapping(value = "/v1/users", params = {"role", "enabled"}, produces = "application/json", method = RequestMethod.GET)
  @ApiResponseObject
  public List<User> getByRoleAndStatus(@RequestParam(name = "role") Role role, @RequestParam(name = "enabled") boolean enabled) {
    return service.findActiveAdminUsers();
  }

  @ApiMethod(description = "get all users")
  @RequestMapping(value = "/v1/users", params = {"sortOrder", "sortField", "sortType"}, produces = "application/json", method = {RequestMethod.GET, RequestMethod.POST})
  @ApiResponseObject
  public List<User> findAll(@RequestParam(name = "sortOrder", required = false) String sortOrder, @RequestParam(name = "sortField", required = false) String sortField, @RequestParam(name = "sortType",
          required = false) String sortType) {
    return service.getAll(new SortingImpl(sortField, sortOrder, sortType));
  }

  @ApiMethod(description = "get user by id")
  @RequestMapping(value = "/v1/users/{id}", produces = "application/json", method = RequestMethod.GET)
  @ApiResponseObject
  public User findById(@PathVariable Long id) {
    return service.get(id);
  }

  @ApiMethod(description = "get user by email address")
  @RequestMapping(value = "/v1/users", params = {"email"}, produces = "application/json", method = RequestMethod.GET)
  @ApiResponseObject
  public User findByName(@RequestParam(name = "email") String email) {
    return service.loadUserByUsername(email);
  }

  @ApiMethod(description = "save a newly created user")
  @RequestMapping(value = "/v1/users", produces = "application/json", method = RequestMethod.POST)
  @ApiResponseObject
  public User save(@RequestBody User user, BindingResult errors) {
    return service.save(user, errors);
  }

  @ApiMethod(description = "update a user")
  @RequestMapping(value = "/v1/users/{id}", produces = "application/json", method = RequestMethod.PUT)
  @ApiResponseObject
  public User update(@PathVariable Long id, @RequestBody User user, BindingResult errors) {
    assert Objects.equals(id, user.getId());
    return service.update(user, errors);
  }
}
