package io.github.dbmdz.cudami.controller;

import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.CudamiIdentifierTypesClient;
import de.digitalcollections.model.identifiable.IdentifierType;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.security.User;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.dbmdz.cudami.business.api.service.exceptions.ServiceException;
import io.github.dbmdz.cudami.business.api.service.security.UserService;
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
  private final CudamiIdentifierTypesClient identifierService;

  @SuppressFBWarnings(value = "EI_EXPOSE_REP2")
  public MainController(CudamiClient cudamiClient, UserService<User> userService) {
    this.identifierService = cudamiClient.forIdentifierTypes();
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

  @GetMapping("/search")
  public String search(@RequestParam(value = "q", defaultValue = "") String q, Model model)
      throws Exception {
    if (q == null || q.isEmpty()) {
      return "main";
    }

    // Check, if the query string starts with an identifier
    if (q.contains(":")) {
      String identifierNamespace = q.split(":")[0];
      // Check, if the namespace is valid. if yes, we can directly forward to the destination
      PageResponse<IdentifierType> identifierTypePageResponse =
          identifierService.find(PageRequest.builder().pageSize(9999).pageNumber(0).build());
      for (IdentifierType identifierType : identifierTypePageResponse.getContent()) {
        String namespace = identifierType.getNamespace();
        if (namespace.equalsIgnoreCase(identifierNamespace)) {
          return "forward:/identifiables/" + q.replaceFirst("^.*?:", namespace + ":");
        }
      }
    }

    // There was no identifiable, so we make a search over all labels as IdentifiableObjectType
    return "redirect:/identifiables/search?term=" + q;
  }
}
