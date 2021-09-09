package de.digitalcollections.cudami.admin.controller;

import de.digitalcollections.commons.springmvc.controller.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Controller for system setup tasks. */
@Controller
public class SetupController extends AbstractController {
  @GetMapping("/setup/admin")
  public String adminUser(Model model) {
    model.addAttribute("setupAdmin", true);
    return "users/create";
  }
}
