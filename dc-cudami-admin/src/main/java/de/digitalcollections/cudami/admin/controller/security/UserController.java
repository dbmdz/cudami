package de.digitalcollections.cudami.admin.controller.security;

import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.admin.business.api.service.security.UserService;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import de.digitalcollections.model.security.Role;
import de.digitalcollections.model.security.User;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import javax.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
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

  @GetMapping("/users/new")
  public String create() {
    return "users/create";
  }

  @GetMapping("/api/users/new")
  @ResponseBody
  public User create(
      @RequestParam(name = "admin", required = false, defaultValue = "false") boolean admin)
      throws ServiceException {
    if (admin) {
      return service.createAdminUser();
    }
    return service.create();
  }

  @GetMapping("/users/{uuid}/edit")
  public String edit(@PathVariable UUID uuid, Model model) throws ServiceException {
    model.addAttribute("user", service.getByUuid(uuid));
    return "users/edit";
  }

  @GetMapping("/api/users")
  @ResponseBody
  public PageResponse<User> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "searchTerm", required = false) String searchTerm,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy)
      throws ServiceException {
    PageRequest pageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    return service.find(pageRequest);
  }

  @GetMapping("/api/users/{uuid}")
  @ResponseBody
  public User getByUuid(@PathVariable UUID uuid) throws ServiceException {
    return this.service.getByUuid(uuid);
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

  @PostMapping("/api/users")
  public ResponseEntity save(
      @RequestParam(value = "pwd1", required = false) String password1,
      @RequestParam(value = "pwd2", required = false) String password2,
      @RequestBody @Valid User user,
      BindingResult results)
      throws ServiceException {
    this.verifyBinding(results);
    if (results.hasErrors()) {
      return new ResponseEntity<>(results.getGlobalError(), HttpStatus.BAD_REQUEST);
    }
    User userDb = service.create(user, password1, password2, results);
    if (results.hasErrors()) {
      return new ResponseEntity<>(results.getGlobalError(), HttpStatus.BAD_REQUEST);
    }
    return ResponseEntity.ok(userDb);
  }

  @PatchMapping("/api/users/{uuid}")
  public ResponseEntity setStatus(@PathVariable("uuid") UUID uuid, @RequestBody User user) {
    boolean successful = service.setStatus(uuid, user.isEnabled());
    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @PutMapping("/api/users/{uuid}")
  public ResponseEntity update(
      @PathVariable UUID uuid,
      @RequestParam(name = "pwd1", required = false) String password1,
      @RequestParam(name = "pwd2", required = false) String password2,
      @RequestBody User user,
      BindingResult results)
      throws ServiceException {
    this.verifyBinding(results);
    if (results.hasErrors()) {
      return new ResponseEntity<>(results.getGlobalError(), HttpStatus.BAD_REQUEST);
    }
    User updatedUser = this.service.update(user, password1, password2, results);
    if (results.hasErrors()) {
      return new ResponseEntity<>(results.getGlobalError(), HttpStatus.BAD_REQUEST);
    }
    return ResponseEntity.ok(updatedUser);
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
