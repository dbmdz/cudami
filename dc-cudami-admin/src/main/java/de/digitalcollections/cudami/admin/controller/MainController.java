package de.digitalcollections.cudami.admin.controller;

import java.util.Date;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MainController {

  @GetMapping("/login")
  public String login(
      @RequestParam(value = "error", defaultValue = "false") boolean error, Model model) {
    model.addAttribute("error", error);
    model.addAttribute("login", true);
    return "login";
  }

  @GetMapping(value = {"/lte"})
  public String lteStarter(Model model) {
    model.addAttribute("time", new Date());
    return "starter";
  }

  @GetMapping(value = {"", "/"})
  public String printWelcome(Model model) {
    model.addAttribute("time", new Date());
    return "main";
  }
}
