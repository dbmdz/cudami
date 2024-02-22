package io.github.dbmdz.cudami.admin.controller.identifiable;

import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.IdentifierType;
import io.github.dbmdz.cudami.admin.business.i18n.LanguageService;
import io.github.dbmdz.cudami.admin.controller.AbstractUniqueObjectController;
import io.github.dbmdz.cudami.admin.controller.ParameterHelper;
import java.util.UUID;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Controller for identifier type management pages. */
@Controller
@SessionAttributes(value = {"identifierType"})
public class IdentifierTypeController extends AbstractUniqueObjectController<IdentifierType> {

  private static final Logger LOGGER = LoggerFactory.getLogger(IdentifierTypeController.class);

  private final MessageSource messageSource;

  public IdentifierTypeController(
      CudamiClient client, LanguageService languageService, MessageSource messageSource) {
    super(client.forIdentifierTypes(), languageService);
    this.messageSource = messageSource;
  }

  @GetMapping("/identifiertypes/new")
  public String create(Model model) throws TechnicalException {
    IdentifierType identifierType = service.create();
    model.addAttribute("identifierType", identifierType);
    model.addAttribute("mode", "create");
    return "identifiertypes/create-or-edit";
  }

  @GetMapping("/identifiertypes/{uuid:" + ParameterHelper.UUID_PATTERN + "}/edit")
  public String edit(@PathVariable UUID uuid, Model model)
      throws TechnicalException, ResourceNotFoundException {
    IdentifierType identifierType = service.getByUuid(uuid);
    if (identifierType == null) {
      throw new ResourceNotFoundException();
    }
    model.addAttribute("identifierType", identifierType);
    model.addAttribute("mode", "edit");
    return "identifiertypes/create-or-edit";
  }

  @GetMapping("/identifiertypes")
  public String list() {
    return "identifiertypes/list";
  }

  @ModelAttribute("menu")
  protected String module() {
    return "identifiertypes";
  }

  @PostMapping("/identifiertypes/new")
  public String save(
      @ModelAttribute @Valid IdentifierType identifierType,
      BindingResult results,
      Model model,
      SessionStatus status,
      RedirectAttributes redirectAttributes) {
    model.addAttribute("mode", "create");

    verifyBinding(results);
    if (results.hasErrors()) {
      return "identifiertypes/create-or-edit";
    }
    try {
      service.save(identifierType);
      LOGGER.info("Successfully saved identifier type");
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save identifier type: ", e);
      String message =
          messageSource.getMessage("error.technical_error", null, LocaleContextHolder.getLocale());
      redirectAttributes.addFlashAttribute("error_message", message);
      return "redirect:/identifiertypes";
    }
    if (results.hasErrors()) {
      return "identifiertypes/create-or-edit";
    }
    status.setComplete();
    String message =
        messageSource.getMessage("msg.created_successfully", null, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute("success_message", message);
    return "redirect:/identifiertypes";
  }

  @PostMapping(value = "/identifiertypes/{uuid:" + ParameterHelper.UUID_PATTERN + "}/edit")
  public String update(
      @PathVariable UUID uuid,
      @ModelAttribute @Valid IdentifierType identifierType,
      BindingResult results,
      Model model,
      SessionStatus status,
      RedirectAttributes redirectAttributes)
      throws TechnicalException {
    model.addAttribute("mode", "edit");
    verifyBinding(results);
    if (results.hasErrors()) {
      return "identifiertypes/create-or-edit";
    }

    try {
      service.update(uuid, identifierType);
    } catch (TechnicalException e) {
      String message = "Cannot update identifierType with uuid=" + uuid + ": " + e;
      LOGGER.error(message, e);
      redirectAttributes.addFlashAttribute("error_message", message);
      return "redirect:/identifiertypes/" + uuid + "/edit";
    }

    status.setComplete();
    String message =
        messageSource.getMessage(
            "msg.changes_saved_successfully", null, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute("success_message", message);
    return "redirect:/identifiertypes/" + uuid;
  }

  @GetMapping("/identifiertypes/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  public String view(@PathVariable UUID uuid, Model model)
      throws TechnicalException, ResourceNotFoundException {
    IdentifierType identifierType = service.getByUuid(uuid);
    if (identifierType == null) {
      throw new ResourceNotFoundException();
    }

    model.addAttribute("identifierType", identifierType);
    return "identifiertypes/view";
  }
}
