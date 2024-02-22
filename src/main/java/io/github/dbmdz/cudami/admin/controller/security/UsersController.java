package io.github.dbmdz.cudami.admin.controller.security;

import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.security.Role;
import de.digitalcollections.model.security.User;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.dbmdz.cudami.admin.business.api.service.exceptions.ServiceException;
import io.github.dbmdz.cudami.admin.business.api.service.security.UserService;
import io.github.dbmdz.cudami.admin.controller.AbstractController;
import io.github.dbmdz.cudami.admin.controller.ParameterHelper;
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
import org.springframework.validation.Errors;
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
public class UsersController extends AbstractController {

  private final MessageSource messageSource;
  private final UserService<User> service;

  @SuppressFBWarnings(value = "EI_EXPOSE_REP2")
  public UsersController(UserService service, MessageSource messageSource) {
    this.messageSource = messageSource;
    this.service = service;
  }

  @GetMapping("/users/{uuid:" + ParameterHelper.UUID_PATTERN + "}/activate")
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
  public String create(Model model) throws ServiceException {
    model.addAttribute("mode", "create");
    model.addAttribute("user", service.create());
    return "users/create-or-edit";
  }

  @PostMapping("/users/new")
  public String create(
      @RequestParam(value = "pwd1", required = false) String password1,
      @RequestParam(value = "pwd2", required = false) String password2,
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
    User userDb = service.create(user, password1, password2, (Errors) results);
    if (results.hasErrors()) {
      return "users/create-or-edit";
    }
    status.setComplete();
    String message =
        messageSource.getMessage("msg.created_successfully", null, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute("success_message", message);
    return "redirect:/users/" + userDb.getUuid().toString();
  }

  @GetMapping("/users/{uuid:" + ParameterHelper.UUID_PATTERN + "}/deactivate")
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

  @GetMapping("/users/{uuid:" + ParameterHelper.UUID_PATTERN + "}/edit")
  public String edit(@PathVariable UUID uuid, Model model) throws ServiceException {
    model.addAttribute("mode", "edit");
    model.addAttribute("user", service.getByUuid(uuid));
    return "users/create-or-edit";
  }

  @PostMapping("/users/{uuid:" + ParameterHelper.UUID_PATTERN + "}/edit")
  public String edit(
      @PathVariable UUID uuid,
      @RequestParam(name = "pwd1", required = false) String password1,
      @RequestParam(name = "pwd2", required = false) String password2,
      @ModelAttribute(name = "user") @Valid User user,
      BindingResult results,
      Model model,
      SessionStatus status,
      RedirectAttributes redirectAttributes)
      throws ServiceException {
    model.addAttribute("mode", "edit");
    verifyBinding(results);
    if (results.hasErrors()) {
      return "users/create-or-edit";
    }
    service.update(user, password1, password2, (Errors) results);
    if (results.hasErrors()) {
      return "users/create-or-edit";
    }
    status.setComplete();
    String message =
        messageSource.getMessage(
            "msg.changes_saved_successfully", null, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute("success_message", message);
    return "redirect:/users/" + uuid;
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

  @GetMapping("/users/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
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
