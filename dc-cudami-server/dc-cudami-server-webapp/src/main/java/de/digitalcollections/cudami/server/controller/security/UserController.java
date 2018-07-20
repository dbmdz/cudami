package de.digitalcollections.cudami.server.controller.security;

import de.digitalcollections.core.model.api.paging.PageRequest;
import de.digitalcollections.core.model.api.paging.PageResponse;
import de.digitalcollections.core.model.api.paging.Sorting;
import de.digitalcollections.core.model.api.paging.enums.Direction;
import de.digitalcollections.core.model.api.paging.enums.NullHandling;
import de.digitalcollections.core.model.impl.paging.OrderImpl;
import de.digitalcollections.core.model.impl.paging.PageRequestImpl;
import de.digitalcollections.core.model.impl.paging.SortingImpl;
import de.digitalcollections.cudami.model.api.security.User;
import de.digitalcollections.cudami.model.api.security.enums.Role;
import de.digitalcollections.cudami.server.business.api.service.security.UserService;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
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
  @RequestMapping(value = "/v1/users",
          produces = "application/json", method = RequestMethod.GET)
  @ApiResponseObject
  public PageResponse<User> findAll(
          @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
          @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
          @RequestParam(name = "sortField", required = false, defaultValue = "uuid") String sortField,
          @RequestParam(name = "sortDirection", required = false, defaultValue = "ASC") Direction sortDirection,
          @RequestParam(name = "nullHandling", required = false, defaultValue = "NATIVE") NullHandling nullHandling
  ) {
    // FIXME add support for multiple sorting orders
    OrderImpl order = new OrderImpl(sortDirection, sortField, nullHandling);
    Sorting sorting = new SortingImpl(order);
    PageRequest pageRequest = new PageRequestImpl(pageNumber, pageSize, sorting);
    return service.find(pageRequest);
  }

  @ApiMethod(description = "get user by uuid")
  @RequestMapping(value = "/v1/users/{uuid}", produces = "application/json", method = RequestMethod.GET)
  @ApiResponseObject
  public User findById(@PathVariable UUID uuid) {
    return service.get(uuid);
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
  @RequestMapping(value = "/v1/users/{uuid}", produces = "application/json", method = RequestMethod.PUT)
  @ApiResponseObject
  public User update(@PathVariable UUID uuid, @RequestBody User user, BindingResult errors) {
    assert Objects.equals(uuid, user.getUuid());
    return service.update(user, errors);
  }
}
