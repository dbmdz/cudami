package de.digitalcollections.cudami.server.controller;

import de.digitalcollections.cudami.model.api.security.Operation;
import de.digitalcollections.cudami.server.business.api.service.OperationService;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiResponseObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/operation")
@Api(description = "The operation controller", name = "Operation controller")
public class OperationController {

  @Autowired
  private OperationService service;

  @ApiMethod(description = "get operation by name")
  @RequestMapping(value = "/v1/findByName/{name}", produces = "application/json", method = RequestMethod.GET)
  @ApiResponseObject
  public Operation findByName(@PathVariable String name) {
    return service.findByName(name);
  }

}
