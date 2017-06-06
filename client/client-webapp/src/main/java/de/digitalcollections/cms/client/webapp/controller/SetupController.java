package de.digitalcollections.cms.client.webapp.controller;

import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cms.client.business.api.service.RoleService;
import de.digitalcollections.cms.client.business.api.service.UserService;
import de.digitalcollections.cms.model.api.security.User;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for system setup tasks.
 */
@Controller
@RequestMapping(value = {"/setup"})
@SessionAttributes(value = {"user"})
public class SetupController extends AbstractController implements MessageSourceAware {

  private MessageSource messageSource;

  @Autowired
  RoleService roleService;

  @Autowired
  UserService userService;

  @Override
  public void setMessageSource(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  @ModelAttribute("createAdmin")
  public boolean adminFlag() {
    return true;
  }

  @ModelAttribute("isNew")
  public boolean newFlag() {
    return true;
  }

  @RequestMapping(value = "adminUser", method = RequestMethod.GET)
  public String adminUser(Model model) {
    model.addAttribute("user", userService.createAdminUser());
    return "users/edit";
  }

  @RequestMapping(value = "adminUser", method = RequestMethod.POST)
  public String adminUser(@RequestParam("pwd1") String password1, @RequestParam("pwd2") String password2, @ModelAttribute @Valid User user, BindingResult results, Model model, SessionStatus status, RedirectAttributes redirectAttributes) {
    verifyBinding(results);
    if (results.hasErrors()) {
      return "users/edit";
    }
    userService.create(user, password1, password2, (Errors) results);
    if (results.hasErrors()) {
      return "users/edit";
    }
    status.setComplete();
    String message = messageSource.getMessage("msg.created_successfully", null, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute("success_message", message);
    return "redirect:/";
  }
}
