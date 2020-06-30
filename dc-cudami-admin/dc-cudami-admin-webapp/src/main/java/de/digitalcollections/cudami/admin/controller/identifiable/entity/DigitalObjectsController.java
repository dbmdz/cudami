package de.digitalcollections.cudami.admin.controller.identifiable.entity;

import de.digitalcollections.commons.springdata.domain.PageConverter;
import de.digitalcollections.commons.springdata.domain.PageWrapper;
import de.digitalcollections.commons.springdata.domain.PageableConverter;
import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.entity.DigitalObjectService;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.DigitalObjectImpl;
import java.util.UUID;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

/** Controller for digital objects management pages. */
@Controller
@SessionAttributes(value = {"digitalobject"})
public class DigitalObjectsController extends AbstractController {

  private static final Logger LOGGER = LoggerFactory.getLogger(DigitalObjectsController.class);

  DigitalObjectService service;

  @Autowired
  public DigitalObjectsController(DigitalObjectService service) {
    this.service = service;
  }

  @ModelAttribute("menu")
  protected String module() {
    return "digitalobjects";
  }

  @GetMapping(value = "/digitalobjects/new")
  public String create(Model model) {
    throw new UnsupportedOperationException();
  }

  @PostMapping(value = "/digitalobjects/new")
  public String create(
      @ModelAttribute @Valid DigitalObjectImpl digitalObject,
      BindingResult results,
      Model model,
      SessionStatus status,
      RedirectAttributes redirectAttributes) {
    throw new UnsupportedOperationException();
  }

  @GetMapping("/digitalobjects/{uuid}/edit")
  public String edit(@PathVariable UUID uuid, Model model, RedirectAttributes redirectAttributes) {
    throw new UnsupportedOperationException();
  }

  @PostMapping("/digitalobjects/{pathUuid}/edit")
  public String edit(
      @PathVariable UUID pathUuid,
      @ModelAttribute @Valid DigitalObjectImpl digitalObject,
      BindingResult results,
      Model model,
      SessionStatus status,
      RedirectAttributes redirectAttributes) {
    throw new UnsupportedOperationException();
  }

  @GetMapping("/digitalobjects")
  public String list(
      Model model,
      @PageableDefault(
              sort = {"lastModified"},
              size = 25)
          Pageable pageable) {
    final PageRequest pageRequest = PageableConverter.convert(pageable);
    final PageResponse pageResponse = service.find(pageRequest);
    Page page = PageConverter.convert(pageResponse, pageRequest);
    model.addAttribute("page", new PageWrapper(page, "/digitalobjects"));
    return "digitalobjects/list";
  }

  @GetMapping("/digitalobjects/{uuid}")
  public String view(@PathVariable UUID uuid, Model model) {
    DigitalObject digitalObject = (DigitalObject) service.get(uuid);
    model.addAttribute("availableLocales", digitalObject.getLabel().getLocales());
    model.addAttribute("digitalObject", digitalObject);
    return "digitalobjects/view";
  }
}
