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
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for digital objects management pages.
 */
@Controller
@SessionAttributes(value = {"digitalobject"})
public class DigitalObjectsController extends AbstractController implements MessageSourceAware {

  private static final Logger LOGGER = LoggerFactory.getLogger(DigitalObjectsController.class);

  private MessageSource messageSource;

  @Autowired
  DigitalObjectService service;

  @Override
  public void setMessageSource(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  @ModelAttribute("menu")
  protected String module() {
    return "digitalobjects";
  }

  @RequestMapping(value = "/digitalobjects/new", method = RequestMethod.GET)
  public String create(Model model) {
    throw new UnsupportedOperationException();
  }

  @RequestMapping(value = "/digitalobjects/new", method = RequestMethod.POST)
  public String create(@ModelAttribute @Valid DigitalObjectImpl digitalObject, BindingResult results, Model model, SessionStatus status, RedirectAttributes redirectAttributes) {
    throw new UnsupportedOperationException();
  }

  @RequestMapping(value = "/digitalobjects/{uuid}/edit", method = RequestMethod.GET)
  public String edit(@PathVariable UUID uuid, Model model, RedirectAttributes redirectAttributes) {
    throw new UnsupportedOperationException();
  }

  @RequestMapping(value = "/digitalobjects/{pathUuid}/edit", method = RequestMethod.POST)
  public String edit(@PathVariable UUID pathUuid, @ModelAttribute @Valid DigitalObjectImpl digitalObject, BindingResult results, Model model, SessionStatus status, RedirectAttributes redirectAttributes) {
    throw new UnsupportedOperationException();
  }

  @RequestMapping(value = "/digitalobjects", method = RequestMethod.GET)
  public String list(Model model, @PageableDefault(sort = {"lastModified"}, size = 25) Pageable pageable) {
    final PageRequest pageRequest = PageableConverter.convert(pageable);
    final PageResponse pageResponse = service.find(pageRequest);
    Page page = PageConverter.convert(pageResponse, pageRequest);
    model.addAttribute("page", new PageWrapper(page, "/digitalobjects"));
    return "digitalobjects/list";
  }

  @RequestMapping(value = "/digitalobjects/{uuid}", method = RequestMethod.GET)
  public String view(@PathVariable UUID uuid, Model model) {
    DigitalObject digitalObject = (DigitalObject) service.get(uuid);
    model.addAttribute("availableLocales", digitalObject.getLabel().getLocales());
    model.addAttribute("digitalObject", digitalObject);
    return "digitalobjects/view";
  }

  // ----------------------------------------------------------------------------
  public void setWebsiteService(DigitalObjectService digitalObjectService) {
    this.service = digitalObjectService;
  }
}
