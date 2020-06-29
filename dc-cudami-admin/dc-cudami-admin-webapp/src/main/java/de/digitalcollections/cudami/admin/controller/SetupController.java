package de.digitalcollections.cudami.admin.controller;

import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.business.api.service.security.UserService;
import de.digitalcollections.model.impl.security.UserImpl;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Controller for system setup tasks. */
@Controller
@SessionAttributes(value = {"user"})
public class SetupController extends AbstractController {

  private final MessageSource messageSource;

  UserService userService;

  @Autowired
  public SetupController(MessageSource messageSource, UserService userService) {
    this.messageSource = messageSource;
    this.userService = userService;
  }

  @ModelAttribute("createAdmin")
  public boolean adminFlag() {
    return true;
  }

  @GetMapping(value = "/setup/adminUser")
  public String adminUser(Model model) {
    model.addAttribute("user", userService.createAdminUser());
    return "users/create";
  }

  @PostMapping(value = "/setup/adminUser")
  public String adminUser(
      @RequestParam("pwd1") String password1,
      @RequestParam("pwd2") String password2,
      @ModelAttribute(name = "user") @Valid UserImpl user,
      BindingResult results,
      Model model,
      SessionStatus status,
      RedirectAttributes redirectAttributes) {
    verifyBinding(results);
    if (results.hasErrors()) {
      return "users/create";
    }
    userService.create(user, password1, password2, (Errors) results);
    if (results.hasErrors()) {
      return "users/create";
    }
    status.setComplete();
    String message =
        messageSource.getMessage("msg.created_successfully", null, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute("success_message", message);
    return "redirect:/";
  }
}
