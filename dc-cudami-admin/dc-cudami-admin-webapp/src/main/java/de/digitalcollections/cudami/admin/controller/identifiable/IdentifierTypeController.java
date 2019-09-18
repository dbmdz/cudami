package de.digitalcollections.cudami.admin.controller.identifiable;

import de.digitalcollections.commons.springdata.domain.PageConverter;
import de.digitalcollections.commons.springdata.domain.PageWrapper;
import de.digitalcollections.commons.springdata.domain.PageableConverter;
import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.IdentifierTypeService;
import de.digitalcollections.model.api.identifiable.IdentifierType;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.IdentifierTypeImpl;
import java.util.UUID;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@SessionAttributes(value = {"identifierType"})
public class IdentifierTypeController extends AbstractController implements MessageSourceAware {

  private static final Logger LOGGER = LoggerFactory.getLogger(IdentifierTypeController.class);

  private MessageSource messageSource;

  IdentifierTypeService service;

  @Autowired
  public IdentifierTypeController(IdentifierTypeService service) {
    this.service = service;
  }

  @Override
  public void setMessageSource(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  @ModelAttribute("menu")
  protected String module() {
    return "identifiertypes";
  }

  @RequestMapping(value = "/identifiertypes/new", method = RequestMethod.GET)
  public String create(Model model) {
    model.addAttribute("identifierType", service.create());
    return "identifiertypes/create";
  }

  @RequestMapping(value = "/identifiertypes/new", method = RequestMethod.POST)
  public String create(@ModelAttribute @Valid IdentifierTypeImpl identifierType, BindingResult results, Model model, SessionStatus status, RedirectAttributes redirectAttributes) {
    verifyBinding(results);
    if (results.hasErrors()) {
      return "identifiertypes/create";
    }
    try {
      service.save(identifierType);
      LOGGER.info("Successfully saved identifier type");
    } catch (Exception e) {
      LOGGER.error("Cannot save identifier type: ", e);
      String message = messageSource.getMessage("msg.error", null, LocaleContextHolder.getLocale());
      redirectAttributes.addFlashAttribute("error_message", message);
      return "redirect:/identifiertypes";
    }
    if (results.hasErrors()) {
      return "identifiertypes/create";
    }
    status.setComplete();
    String message = messageSource.getMessage("msg.created_successfully", null, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute("success_message", message);
    return "redirect:/identifiertypes";
  }

  @RequestMapping(value = "/identifiertypes/{uuid}/edit", method = RequestMethod.GET)
  public String edit(@PathVariable UUID uuid, Model model, RedirectAttributes redirectAttributes) {
    model.addAttribute("identifierType", service.get(uuid));
    return "identifiertypes/edit";
  }

  @RequestMapping(value = "/identifiertypes/{pathUuid}/edit", method = RequestMethod.POST)
  public String edit(@PathVariable UUID pathUuid, @ModelAttribute @Valid IdentifierTypeImpl identifierType, BindingResult results, Model model, SessionStatus status, RedirectAttributes redirectAttributes) {
    verifyBinding(results);
    if (results.hasErrors()) {
      return "identifiertypes/" + pathUuid + "/edit";
    }

    try {
      // get identifier type from db
      IdentifierType identifierTypeDb = (IdentifierType) service.get(pathUuid);
      // just update the fields, that were editable
      identifierTypeDb.setLabel(identifierType.getLabel());
      identifierTypeDb.setNamespace(identifierType.getNamespace());
      identifierTypeDb.setPattern(identifierType.getPattern());

      service.update(identifierTypeDb);
    } catch (Exception e) {
      String message = "Cannot save identifier type with uuid=" + pathUuid + ": " + e;
      LOGGER.error(message, e);
      redirectAttributes.addFlashAttribute("error_message", message);
      return "redirect:/identifiertypes/" + pathUuid + "/edit";
    }

    if (results.hasErrors()) {
      return "identifiertypes/" + pathUuid + "/edit";
    }
    status.setComplete();
    String message = messageSource.getMessage("msg.changes_saved_successfully", null, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute("success_message", message);
    return "redirect:/identifiertypes";
  }

  @GetMapping(value = "/identifiertypes")
  public String list(Model model, @PageableDefault(sort = {"lastModified"}, size = 25) Pageable pageable) {
    final PageRequest pageRequest = PageableConverter.convert(pageable);
    final PageResponse pageResponse = service.find(pageRequest);
    Page page = PageConverter.convert(pageResponse, pageRequest);
    model.addAttribute("page", new PageWrapper(page, "/identifiertypes"));
    return "identifiertypes/list";
  }
}
