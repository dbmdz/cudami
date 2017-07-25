package de.digitalcollections.cudami.client.webapp.controller;

import de.digitalcollections.commons.springdata.domain.PageConverter;
import de.digitalcollections.commons.springdata.domain.PageableConverter;
import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.core.model.api.paging.PageRequest;
import de.digitalcollections.core.model.api.paging.PageResponse;
import de.digitalcollections.cudami.client.business.api.service.UserService;
import de.digitalcollections.cudami.client.webapp.propertyeditor.RoleEditor;
import de.digitalcollections.cudami.model.api.security.User;
import de.digitalcollections.cudami.model.api.security.enums.Role;
import java.util.Arrays;
import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for all "Users" pages.
 */
@Controller
@RequestMapping(value = {"/users"})
@SessionAttributes(value = {"user"})
public class UserController extends AbstractController implements MessageSourceAware {

  private MessageSource messageSource;

  @Autowired
  UserService userService;

  @Autowired
  RoleEditor roleEditor;

  @Override
  public void setMessageSource(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  @ModelAttribute("menu")
  protected String module() {
    return "users";
  }

  @ModelAttribute("allRoles")
  protected List<Role> populateAllRoles() {
    return Arrays.asList(Role.values());
  }

  @InitBinder("user")
  protected void initBinder(WebDataBinder binder) {
    binder.registerCustomEditor(Role.class, roleEditor);
    binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
//        binder.setDisallowedFields("password");
//        binder.addValidators(mySpecialUserValidator);
  }

  @RequestMapping(value = "/{id}/activate", method = RequestMethod.GET)
  public String activate(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
    User user = userService.activate(id);
    String message = messageSource.getMessage("msg.user_activated", new Object[]{user.getEmail()}, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute("success_message", message);
    return "redirect:/users";
  }

  @RequestMapping(value = "/{id}/deactivate", method = RequestMethod.GET)
  public String deactivate(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
    User user = userService.deactivate(id);
    String message = messageSource.getMessage("msg.user_deactivated", new Object[]{user.getEmail()}, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute("warning_message", message);
    return "redirect:/users";
  }

  @RequestMapping(value = "new", method = RequestMethod.GET)
  public String create(Model model) {
    model.addAttribute("user", userService.create());
    model.addAttribute("isNew", true);
    return "users/edit";
  }

  @RequestMapping(value = "new", method = RequestMethod.POST)
  public String create(@RequestParam("pwd1") String password1, @RequestParam("pwd2") String password2, @ModelAttribute @Valid User user, BindingResult results, Model model, SessionStatus status, RedirectAttributes redirectAttributes) {
    verifyBinding(results);
    if (results.hasErrors()) {
      model.addAttribute("isNew", true);
      return "users/edit";
    }
    user = userService.create(user, password1, password2, (Errors) results);
    if (results.hasErrors()) {
      model.addAttribute("isNew", true);
      return "users/edit";
    }
    status.setComplete();
    String message = messageSource.getMessage("msg.created_successfully", null, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute("success_message", message);
    return "redirect:/users/" + user.getId();
  }

  @RequestMapping(value = "/{id}/edit", method = RequestMethod.GET)
  public String edit(@PathVariable long id, Model model) {
    model.addAttribute("user", userService.get(id));
    model.addAttribute("isNew", false);
    return "users/edit";
  }

  @RequestMapping(value = "/{id}/edit", method = RequestMethod.POST)
  public String edit(@PathVariable long id, @RequestParam("pwd1") String password1, @RequestParam("pwd2") String password2, @ModelAttribute @Valid User user, BindingResult results, Model model, SessionStatus status, RedirectAttributes redirectAttributes) {
    verifyBinding(results);
    if (results.hasErrors()) {
      model.addAttribute("isNew", false);
      return "users/edit";
    }
    userService.update(user, password1, password2, (Errors) results);
    if (results.hasErrors()) {
      model.addAttribute("isNew", false);
      return "users/edit";
    }
    status.setComplete();
    String message = messageSource.getMessage("msg.changes_saved_successfully", null, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute("success_message", message);
    return "redirect:/users/" + id;
  }

  @RequestMapping(method = RequestMethod.GET)
  public String list(Model model, @SortDefault("email") Pageable pageable) {
//    List<User> users = userService.getAll();
//    model.addAttribute("users", users);
    final PageRequest pageRequest = PageableConverter.convert(pageable);
    final PageResponse pageResponse = userService.find(pageRequest);
    Page page = PageConverter.convert(pageResponse, pageRequest);
    model.addAttribute("page", page);
    return "users/list";
  }

  @RequestMapping(value = "/{id}", method = RequestMethod.GET)
  public String view(@PathVariable long id, Model model) {
    model.addAttribute("user", userService.get(id));
    return "users/view";
  }
}
