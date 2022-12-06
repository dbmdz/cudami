package de.digitalcollections.cudami.admin.controller.security;

import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.admin.business.api.service.security.UserService;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.security.Role;
import de.digitalcollections.model.security.User;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import javax.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Controller for all "Users" pages. */
@Controller
@SessionAttributes(value = {"user"})
public class UserController extends AbstractController {

  private final MessageSource messageSource;
  private final UserService<User> service;

  @SuppressFBWarnings(value = "EI_EXPOSE_REP2")
  public UserController(MessageSource messageSource, UserService service) {
    this.messageSource = messageSource;
    this.service = service;
  }

  @GetMapping("/users/{uuid}/activate")
  public String activate(
      @PathVariable UUID uuid, Model model, RedirectAttributes redirectAttributes)
      throws TechnicalException {
    boolean successful = service.setStatus(uuid, true);
    if (successful) {
      String message =
          messageSource.getMessage("msg.user_activated", null, LocaleContextHolder.getLocale());
      redirectAttributes.addFlashAttribute("success_message", message);
    } else {
      String message =
          messageSource.getMessage("error.technical_error", null, LocaleContextHolder.getLocale());
      redirectAttributes.addFlashAttribute("error_message", message);
    }
    return "redirect:/users";
  }

  @GetMapping("/users/new")
  public String create() {
    return "users/create";
  }

  @GetMapping("/users/{uuid}/deactivate")
  public String deactivate(
      @PathVariable UUID uuid, Model model, RedirectAttributes redirectAttributes)
      throws TechnicalException {
    boolean successful = service.setStatus(uuid, false);
    if (successful) {
      String message =
          messageSource.getMessage("msg.user_deactivated", null, LocaleContextHolder.getLocale());
      redirectAttributes.addFlashAttribute("success_message", message);
    } else {
      String message =
          messageSource.getMessage("error.technical_error", null, LocaleContextHolder.getLocale());
      redirectAttributes.addFlashAttribute("error_message", message);
    }
    return "redirect:/users";
  }

  @GetMapping("/users/{uuid}/edit")
  public String edit(@PathVariable UUID uuid, Model model) throws ServiceException {
    model.addAttribute("user", service.getByUuid(uuid));
    return "users/edit";
  }

  @GetMapping("/users")
  public String list() {
    return "users/list";
  }

  @ModelAttribute("menu")
  protected String module() {
    return "users";
  }

  @ModelAttribute("allRoles")
  protected List<Role> populateAllRoles() {
    return Arrays.asList(Role.values());
  }

  @GetMapping("/users/updatePassword")
  public String updatePassword(Model model) throws ServiceException {
    User currentUser =
        service.getByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
    model.addAttribute("user", currentUser);
    return "users/edit-password";
  }

  // TODO: Simplify user management
  @PostMapping("/users/updatePassword")
  public String updatePassword(
      @RequestParam("pwd1") String password1,
      @RequestParam("pwd2") String password2,
      @ModelAttribute(name = "user") @Valid User user,
      BindingResult results,
      Model model,
      SessionStatus status,
      RedirectAttributes redirectAttributes)
      throws ServiceException {
    verifyBinding(results);
    String errorMessage =
        messageSource.getMessage(
            "error.password_change_failed", null, LocaleContextHolder.getLocale());
    if (results.hasErrors()) {
      model.addAttribute("error_message", errorMessage);
      return "users/edit-password";
    }
    service.update(user, password1, password2, results);
    if (results.hasErrors()) {
      model.addAttribute("error_message", errorMessage);
      return "users/edit-password";
    }
    status.setComplete();
    String message =
        messageSource.getMessage(
            "msg.changed_password_successfully", null, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute("success_message", message);
    return "redirect:/";
  }

  @GetMapping("/users/{uuid}")
  public String view(@PathVariable UUID uuid, Model model)
      throws ResourceNotFoundException, ServiceException {
    User user = service.getByUuid(uuid);
    if (user == null) {
      throw new ResourceNotFoundException();
    }
    model.addAttribute("user", user);
    return "users/view";
  }
}
