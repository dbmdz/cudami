package de.digitalcollections.cudami.server.controller;

import de.digitalcollections.cudami.model.api.security.Role;
import de.digitalcollections.cudami.server.business.api.service.RoleService;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiResponseObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/role")
@Api(description = "The role controller", name = "Role controller")
public class RoleController {

  @Autowired
  private RoleService service;

  @ApiMethod(description = "get role by name")
  @RequestMapping(value = "/v1/findByName/{name}", produces = "application/json", method = RequestMethod.GET)
  @ApiResponseObject
  public Role findByName(@PathVariable String name) {
    return service.findByName(name);
  }

}
