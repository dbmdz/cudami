package de.digitalcollections.cudami.client.controller;

import de.digitalcollections.cudami.client.business.api.service.identifiable.UserService;
import de.digitalcollections.cudami.model.api.security.User;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * External REST-Interfaces
 */
@Controller
public class ApiController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApiController.class);

  @Autowired
  private UserService userService;

  @ResponseBody
  @RequestMapping(value = "/api/users", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
  public List<User> getUsers() {
    return userService.getAll();
  }
}
