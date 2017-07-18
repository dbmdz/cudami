package de.digitalcollections.cudami.server.controller;

import de.digitalcollections.core.model.impl.SortingImpl;
import de.digitalcollections.cudami.model.api.security.User;
import de.digitalcollections.cudami.server.business.api.service.UserService;
import java.util.List;
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
@RequestMapping("/user")
@Api(description = "The user controller", name = "User controller")
public class UserController {

  @Autowired
  private UserService service;

  @ApiMethod(description = "get a newly created user")
  @RequestMapping(value = "/v1/create", produces = "application/json", method = RequestMethod.GET)
  @ApiResponseObject
  public User create() {
    return service.create();
  }

  @ApiMethod(description = "get all active admin users")
  @RequestMapping(value = "/v1/findActiveAdminUsers", produces = "application/json", method = RequestMethod.GET)
  @ApiResponseObject
  public List<User> findActiveAdminUsers() {
    return service.findActiveAdminUsers();
  }

  @ApiMethod(description = "get all users")
  @RequestMapping(value = "/v1/findAll", produces = "application/json", method = {RequestMethod.GET, RequestMethod.POST})
  @ApiResponseObject
  public List<User> findAll(@RequestParam(name = "sortOrder", required = false) String sortOrder, @RequestParam(name = "sortField", required = false) String sortField, @RequestParam(name = "sortType",
          required = false) String sortType) {
    return service.getAll(new SortingImpl(sortField, sortOrder, sortType));
  }

  @ApiMethod(description = "get user by id")
  @RequestMapping(value = "/v1/{id}", produces = "application/json", method = RequestMethod.GET)
  @ApiResponseObject
  public User findById(@PathVariable Long id) {
    return service.get(id);
  }

  @ApiMethod(description = "get user by email address")
  @RequestMapping(value = "/v1/findByEmail/{email}", produces = "application/json", method = RequestMethod.GET)
  @ApiResponseObject
  public User findByName(@PathVariable String email) {
    return service.loadUserByUsername(email);
  }

  @ApiMethod(description = "save a newly created user")
  @RequestMapping(value = "/v1/save", produces = "application/json", method = RequestMethod.POST)
  @ApiResponseObject
  public User save(@RequestBody User user, BindingResult errors) {
    return service.save(user, errors);
  }

  @ApiMethod(description = "save a newly created user")
  @RequestMapping(value = "/v1/save", produces = "application/json", method = RequestMethod.PUT)
  @ApiResponseObject
  public User update(@RequestBody User user, BindingResult errors) {
    return service.update(user, errors);
  }
}
