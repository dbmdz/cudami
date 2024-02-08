package de.digitalcollections.cudami.admin.controller;

import de.digitalcollections.cudami.admin.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.admin.business.api.service.security.UserService;
import de.digitalcollections.model.security.User;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

@Controller
@SessionAttributes("loggedInUser")
public class MainController {

  private final UserService<User> userService;

  @SuppressFBWarnings(value = "EI_EXPOSE_REP2")
  public MainController(UserService<User> userService) {
    this.userService = userService;
  }

  @GetMapping("/login")
  public String login(
      @RequestParam(value = "error", defaultValue = "false") boolean error, Model model) {
    model.addAttribute("error", error);
    model.addAttribute("login", true);
    return "login";
  }

  @GetMapping(value = {"", "/"})
  public String printWelcome(Model model) {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (principal instanceof UserDetails) {
      try {
        String username = ((UserDetails) principal).getUsername();
        User user = userService.getByEmail(username);
        model.addAttribute("loggedInUser", user);
      } catch (ServiceException ex) {
      }
    }
    return "main";
  }
}
