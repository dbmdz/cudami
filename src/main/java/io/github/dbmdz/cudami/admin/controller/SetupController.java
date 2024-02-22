package io.github.dbmdz.cudami.admin.controller;

import de.digitalcollections.model.security.User;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.dbmdz.cudami.admin.business.api.service.exceptions.ServiceException;
import io.github.dbmdz.cudami.admin.business.api.service.security.UserService;
import javax.validation.Valid;
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
@SuppressFBWarnings
@Controller
@SessionAttributes(value = {"user"})
public class SetupController extends AbstractController {

  private final MessageSource messageSource;
  private final UserService userService;

  public SetupController(MessageSource messageSource, UserService userService) {
    this.messageSource = messageSource;
    this.userService = userService;
  }

  @ModelAttribute("createAdmin")
  public boolean adminFlag() {
    return true;
  }

  @GetMapping("/setup/adminUser")
  public String adminUser(Model model) throws ServiceException {
    model.addAttribute("mode", "create");
    model.addAttribute("user", userService.createAdminUser());
    return "users/create-or-edit";
  }

  @PostMapping("/setup/adminUser")
  public String adminUser(
      @RequestParam("pwd1") String password1,
      @RequestParam("pwd2") String password2,
      @ModelAttribute(name = "user") @Valid User user,
      BindingResult results,
      Model model,
      SessionStatus status,
      RedirectAttributes redirectAttributes)
      throws ServiceException {
    model.addAttribute("mode", "create");
    verifyBinding(results);
    if (results.hasErrors()) {
      return "users/create-or-edit";
    }
    userService.create(user, password1, password2, (Errors) results);
    if (results.hasErrors()) {
      return "users/create-or-edit";
    }
    status.setComplete();
    String message =
        messageSource.getMessage("msg.created_successfully", null, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute("success_message", message);
    return "redirect:/";
  }
}
