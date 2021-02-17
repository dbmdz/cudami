package de.digitalcollections.cudami.admin.controller.identifiable;

import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.paging.PageConverter;
import de.digitalcollections.cudami.admin.paging.PageWrapper;
import de.digitalcollections.cudami.admin.paging.PageableConverter;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.CudamiIdentifierTypesClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.identifiable.IdentifierType;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import java.util.UUID;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@SessionAttributes(value = {"identifierType"})
public class IdentifierTypeController extends AbstractController {

  private static final Logger LOGGER = LoggerFactory.getLogger(IdentifierTypeController.class);

  private final MessageSource messageSource;

  CudamiIdentifierTypesClient service;

  @Autowired
  public IdentifierTypeController(MessageSource messageSource, CudamiClient cudamiClient) {
    this.messageSource = messageSource;
    this.service = cudamiClient.forIdentifierTypes();
  }

  @ModelAttribute("menu")
  protected String module() {
    return "identifiertypes";
  }

  @GetMapping("/identifiertypes/new")
  public String create(Model model) {
    model.addAttribute("identifierType", service.create());
    return "identifiertypes/create";
  }

  @PostMapping("/identifiertypes/new")
  public String create(
      @ModelAttribute @Valid IdentifierType identifierType,
      BindingResult results,
      Model model,
      SessionStatus status,
      RedirectAttributes redirectAttributes) {
    verifyBinding(results);
    if (results.hasErrors()) {
      return "identifiertypes/create";
    }
    try {
      service.save(identifierType);
      LOGGER.info("Successfully saved identifier type");
    } catch (HttpException e) {
      LOGGER.error("Cannot save identifier type: ", e);
      String message = messageSource.getMessage("msg.error", null, LocaleContextHolder.getLocale());
      redirectAttributes.addFlashAttribute("error_message", message);
      return "redirect:/identifiertypes";
    }
    if (results.hasErrors()) {
      return "identifiertypes/create";
    }
    status.setComplete();
    String message =
        messageSource.getMessage("msg.created_successfully", null, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute("success_message", message);
    return "redirect:/identifiertypes";
  }

  @GetMapping("/identifiertypes/{uuid}/edit")
  public String edit(@PathVariable UUID uuid, Model model, RedirectAttributes redirectAttributes)
      throws HttpException {
    model.addAttribute("identifierType", service.findOne(uuid));
    return "identifiertypes/edit";
  }

  @PostMapping("/identifiertypes/{pathUuid}/edit")
  public String edit(
      @PathVariable UUID pathUuid,
      @ModelAttribute @Valid IdentifierType identifierType,
      BindingResult results,
      Model model,
      SessionStatus status,
      RedirectAttributes redirectAttributes) {
    verifyBinding(results);
    if (results.hasErrors()) {
      return "identifiertypes/" + pathUuid + "/edit";
    }

    try {
      // get identifier type from db
      IdentifierType identifierTypeDb = (IdentifierType) service.findOne(pathUuid);
      // just update the fields, that were editable
      identifierTypeDb.setLabel(identifierType.getLabel());
      identifierTypeDb.setNamespace(identifierType.getNamespace());
      identifierTypeDb.setPattern(identifierType.getPattern());

      service.update(pathUuid, identifierTypeDb);
    } catch (HttpException e) {
      String message = "Cannot save identifier type with uuid=" + pathUuid + ": " + e;
      LOGGER.error(message, e);
      redirectAttributes.addFlashAttribute("error_message", message);
      return "redirect:/identifiertypes/" + pathUuid + "/edit";
    }

    if (results.hasErrors()) {
      return "identifiertypes/" + pathUuid + "/edit";
    }
    status.setComplete();
    String message =
        messageSource.getMessage(
            "msg.changes_saved_successfully", null, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute("success_message", message);
    return "redirect:/identifiertypes";
  }

  @GetMapping("/api/identifiertypes")
  @ResponseBody
  public PageResponse<IdentifierType> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize)
      throws HttpException {
    PageRequest pageRequest = new PageRequest();
    pageRequest.setPageNumber(pageNumber);
    pageRequest.setPageSize(pageSize);
    return service.find(pageRequest);
  }

  @GetMapping(value = "/identifiertypes")
  public String list(
      Model model,
      @PageableDefault(
              sort = {"label"},
              direction = Sort.Direction.ASC,
              size = 25)
          Pageable pageable)
      throws HttpException {
    final PageRequest pageRequest = PageableConverter.convert(pageable);
    final PageResponse pageResponse = service.find(pageRequest);
    Page page = PageConverter.convert(pageResponse, pageRequest);
    model.addAttribute("page", new PageWrapper(page, "/identifiertypes"));
    return "identifiertypes/list";
  }
}
