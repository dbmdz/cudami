package de.digitalcollections.cudami.admin.controller.identifiable.entity;

import de.digitalcollections.commons.springdata.domain.PageConverter;
import de.digitalcollections.commons.springdata.domain.PageWrapper;
import de.digitalcollections.commons.springdata.domain.PageableConverter;
import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.core.model.api.paging.PageRequest;
import de.digitalcollections.core.model.api.paging.PageResponse;
import de.digitalcollections.cudami.admin.business.api.service.LocaleService;
import de.digitalcollections.cudami.admin.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.entity.ContentTreeService;
import de.digitalcollections.cudami.model.api.identifiable.entity.ContentTree;
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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for content trees management pages.
 */
@Controller
@SessionAttributes(value = {"contentTree"})
public class ContentTreesController extends AbstractController implements MessageSourceAware {

  private static final Logger LOGGER = LoggerFactory.getLogger(ContentTreesController.class);

  private MessageSource messageSource;

  @Autowired
  LocaleService localeService;

  @Autowired
  ContentTreeService contentTreeService;

  @Override
  public void setMessageSource(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  @ModelAttribute("menu")
  protected String module() {
    return "contenttrees";
  }

  @RequestMapping(value = "/contenttrees/new", method = RequestMethod.GET)
  public String create(Model model) {
    model.addAttribute("contentTree", contentTreeService.create());
    model.addAttribute("isNew", true);
    model.addAttribute("locales", localeService.findAll());
    model.addAttribute("defaultLocale", localeService.getDefault());
    return "contenttrees/edit";
  }

  @RequestMapping(value = "/contenttrees/new", method = RequestMethod.POST)
  public String create(@ModelAttribute @Valid ContentTree contentTree, BindingResult results, Model model, SessionStatus status, RedirectAttributes redirectAttributes) {
    verifyBinding(results);
    if (results.hasErrors()) {
      model.addAttribute("isNew", true);
      return "contenttrees/edit";
    }
    try {
      contentTreeService.save(contentTree, results);
      LOGGER.info("Successfully saved contentTree");
    } catch (Exception e) {
      LOGGER.error("Cannot save contentTree: ", e);
      String message = messageSource.getMessage("msg.error", null, LocaleContextHolder.getLocale());
      redirectAttributes.addFlashAttribute("error_message", message);
      return "redirect:/contenttrees";
    }
    if (results.hasErrors()) {
      model.addAttribute("isNew", true);
      return "contenttrees/edit";
    }
    status.setComplete();
    String message = messageSource.getMessage("msg.created_successfully", null, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute("success_message", message);
    return "redirect:/contenttrees";
  }

  @RequestMapping(value = "/contenttrees/{uuid}/edit", method = RequestMethod.GET)
  public String edit(@PathVariable UUID uuid, Model model, RedirectAttributes redirectAttributes) {
//      model.addAttribute("contentNodeTypes", websiteViewService.getContentNodeTypes());
//      model.addAttribute("navigationNodeTypes", websiteViewService.getNavigationNodeTypes());
    ContentTree contentTree = (ContentTree) contentTreeService.get(uuid);
    model.addAttribute("contentTree", contentTree);
//      LOGGER.error("Cannot retrieve website with id=" + id + ": ", e);
//      String message = messageSource.getMessage("msg.error", null, LocaleContextHolder.getLocale());
//      redirectAttributes.addFlashAttribute("error_message", message);
//      return "redirect:/websites";
    model.addAttribute("isNew", false);
    model.addAttribute("availableLanguages", contentTree.getLabel().getLocales());
    model.addAttribute("locales", localeService.findAll());
    model.addAttribute("defaultLocale", localeService.getDefault());

    return "contenttrees/edit";
  }

  @RequestMapping(value = "/contenttrees/{pathUuid}/edit", method = RequestMethod.POST)
  public String edit(@PathVariable UUID pathUuid, @ModelAttribute @Valid ContentTree contentTree, BindingResult results, Model model, SessionStatus status, RedirectAttributes redirectAttributes) {
    verifyBinding(results);
    if (results.hasErrors()) {
      model.addAttribute("isNew", false);
      return "contenttrees/edit";
    }

    try {
      // get content tree from db
      ContentTree contentTreeDB = (ContentTree) contentTreeService.get(pathUuid);
      // just update the fields, that were editable
      contentTreeDB.setLabel(contentTreeDB.getLabel());
      contentTreeDB.setDescription(contentTreeDB.getDescription());

      contentTreeDB = (ContentTree) contentTreeService.update(contentTreeDB, results);
    } catch (IdentifiableServiceException e) {
      String message = "Cannot save contentTree with uuid=" + pathUuid + ": " + e;
      LOGGER.error(message, e);
      redirectAttributes.addFlashAttribute("error_message", message);
      return "redirect:/contenttrees/" + pathUuid;
    }

    if (results.hasErrors()) {
      model.addAttribute("isNew", false);
      return "contenttrees/edit";
    }
    status.setComplete();
    String message = messageSource.getMessage("msg.changes_saved_successfully", null, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute("success_message", message);
    return "redirect:/contenttrees/" + pathUuid + "/edit";
  }

  @RequestMapping(value = "/contenttrees", method = RequestMethod.GET)
  public String list(Model model, @PageableDefault(sort = {"lastModified"}, size = 25) Pageable pageable) {
    final PageRequest pageRequest = PageableConverter.convert(pageable);
    final PageResponse pageResponse = contentTreeService.find(pageRequest);
    Page page = PageConverter.convert(pageResponse, pageRequest);
    model.addAttribute("page", new PageWrapper(page, "/contenttrees"));
    return "contenttrees/list";
  }

  @RequestMapping(value = "/contenttrees/{uuid}", method = RequestMethod.GET)
  public String view(@PathVariable UUID uuid, Model model) {
    ContentTree contentTree = (ContentTree) contentTreeService.get(uuid);
    model.addAttribute("availableLanguages", contentTree.getLabel().getLocales());
    model.addAttribute("contentTree", contentTree);
    return "contenttrees/view";
  }

  // ----------------------------------------------------------------------------
  public void setService(ContentTreeService contentTreeService) {
    this.contentTreeService = contentTreeService;
  }
}
