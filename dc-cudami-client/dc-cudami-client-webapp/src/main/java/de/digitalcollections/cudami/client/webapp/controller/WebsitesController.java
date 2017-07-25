package de.digitalcollections.cudami.client.webapp.controller;

import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.client.business.api.service.WebsiteService;
import de.digitalcollections.cudami.client.business.api.service.exceptions.WebsiteServiceException;
import de.digitalcollections.cudami.client.webapp.propertyeditor.RoleEditor;
import de.digitalcollections.cudami.model.api.entity.Website;
import java.util.List;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for website management pages.
 */
@Controller
@RequestMapping(value = {"/websites"})
@SessionAttributes(value = {"website"})
public class WebsitesController extends AbstractController implements MessageSourceAware {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebsitesController.class);

  private MessageSource messageSource;

  @Autowired
  WebsiteService websiteService;

  @Autowired
  RoleEditor roleEditor;

  @Override
  public void setMessageSource(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  @ModelAttribute("menu")
  protected String module() {
    return "websites";
  }

  @InitBinder("website")
  protected void initBinder(WebDataBinder binder) {
    binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
  }

  @RequestMapping(value = "new", method = RequestMethod.GET)
  public String create(Model model) {
    model.addAttribute("website", websiteService.create());
    model.addAttribute("isNew", true);
    return "websites/edit";
  }

  @RequestMapping(value = "new", method = RequestMethod.POST)
  public String create(@ModelAttribute @Valid Website website, BindingResult results, Model model, SessionStatus status, RedirectAttributes redirectAttributes) {
    verifyBinding(results);
    if (results.hasErrors()) {
      model.addAttribute("isNew", true);
      return "websites/edit";
    }
    try {
      websiteService.save(website);
      LOGGER.info("Successfully saved website");
    } catch (Exception e) {
      LOGGER.error("Cannot save website: ", e);
      String message = messageSource.getMessage("msg.error", null, LocaleContextHolder.getLocale());
      redirectAttributes.addFlashAttribute("error_message", message);
      return "redirect:/websites";
    }
    if (results.hasErrors()) {
      model.addAttribute("isNew", true);
      return "websites/edit";
    }
    status.setComplete();
    String message = messageSource.getMessage("msg.created_successfully", null, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute("success_message", message);
    return "redirect:/websites";
  }

  @RequestMapping(value = "/{id}/edit", method = RequestMethod.GET)
  public String edit(@PathVariable long id, Model model, RedirectAttributes redirectAttributes) {
    try {
//      model.addAttribute("contentNodeTypes", websiteViewService.getContentNodeTypes());
//      model.addAttribute("navigationNodeTypes", websiteViewService.getNavigationNodeTypes());
      model.addAttribute("website", websiteService.get(id));
    } catch (WebsiteServiceException e) {
      LOGGER.error("Cannot retrieve website with id=" + id + ": ", e);
      String message = messageSource.getMessage("msg.error", null, LocaleContextHolder.getLocale());
      redirectAttributes.addFlashAttribute("error_message", message);
      return "redirect:/websites";
    }
    model.addAttribute("isNew", false);
    return "websites/edit";
  }

  @RequestMapping(value = "/{id}/edit", method = RequestMethod.POST)
  public String edit(@PathVariable long id, @ModelAttribute @Valid Website website, BindingResult results, Model model, SessionStatus status, RedirectAttributes redirectAttributes) {
    verifyBinding(results);
    if (results.hasErrors()) {
      model.addAttribute("isNew", false);
      return "websites/edit";
    }

    try {
      LOGGER.info("URL before=" + website.getUrl() + " id=" + website.getId());
      website = websiteService.update(website);
      LOGGER.info("URL after=" + website.getUrl() + ", id=" + website.getId());
    } catch (WebsiteServiceException e) {
      String message = "Cannot save website with id=" + id + ": " + e;
      LOGGER.error(message, e);
      redirectAttributes.addFlashAttribute("error_message", message);
      return "redirect:/websites/" + id;
    }

    if (results.hasErrors()) {
      model.addAttribute("isNew", false);
      return "websites/edit";
    }
    status.setComplete();
    String message = messageSource.getMessage("msg.changes_saved_successfully", null, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute("success_message", message);
    return "redirect:/websites/" + id + "/edit";
  }

  @RequestMapping(method = RequestMethod.GET)
  public String list(Model model) {
    List<Website> websites = websiteService.getAll();
    model.addAttribute("websites", websites);
    return "websites/list";
  }

  @RequestMapping(value = "/{id}", method = RequestMethod.GET)
  public String view(@PathVariable long id, Model model) {
    try {
      Website website = websiteService.get(id);
      model.addAttribute("website", website);
    } catch (WebsiteServiceException e) {
      LOGGER.error("Cannot retrieve website with id=" + id + ": ", e);
    }
    return "websites/view";
  }

  // ----------------------------------------------------------------------------
  public void setWebsiteService(WebsiteService websiteService) {
    this.websiteService = websiteService;
  }
}
