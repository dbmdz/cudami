package de.digitalcollections.cudami.client.controller;

import de.digitalcollections.commons.springdata.domain.PageConverter;
import de.digitalcollections.commons.springdata.domain.PageWrapper;
import de.digitalcollections.commons.springdata.domain.PageableConverter;
import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.core.model.api.paging.PageRequest;
import de.digitalcollections.core.model.api.paging.PageResponse;
import de.digitalcollections.cudami.client.business.api.service.LocaleService;
import de.digitalcollections.cudami.client.business.api.service.WebsiteService;
import de.digitalcollections.cudami.client.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.client.propertyeditor.RoleEditor;
import de.digitalcollections.cudami.model.api.entity.Website;
import java.util.UUID;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
  LocaleService localeService;

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
    model.addAttribute("locales", localeService.findAll());
    model.addAttribute("defaultLocale", localeService.getDefault());
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
      websiteService.save(website, results);
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

  @RequestMapping(value = "/{uuid}/edit", method = RequestMethod.GET)
  public String edit(@PathVariable UUID uuid, Model model, RedirectAttributes redirectAttributes) {
//      model.addAttribute("contentNodeTypes", websiteViewService.getContentNodeTypes());
//      model.addAttribute("navigationNodeTypes", websiteViewService.getNavigationNodeTypes());
    Website website = (Website) websiteService.get(uuid);
    model.addAttribute("website", website);
//      LOGGER.error("Cannot retrieve website with id=" + id + ": ", e);
//      String message = messageSource.getMessage("msg.error", null, LocaleContextHolder.getLocale());
//      redirectAttributes.addFlashAttribute("error_message", message);
//      return "redirect:/websites";
    model.addAttribute("isNew", false);
    model.addAttribute("availableLanguages", website.getLabel().getLanguages());
    model.addAttribute("locales", localeService.findAll());
    model.addAttribute("defaultLocale", localeService.getDefault());

    return "websites/edit";
  }

  @RequestMapping(value = "/{pathUuid}/edit", method = RequestMethod.POST)
  public String edit(@PathVariable UUID pathUuid, @ModelAttribute @Valid Website website, BindingResult results, Model model, SessionStatus status, RedirectAttributes redirectAttributes) {
    verifyBinding(results);
    if (results.hasErrors()) {
      model.addAttribute("isNew", false);
      return "websites/edit";
    }

    try {
      // get website from db
      Website websiteDb = (Website) websiteService.get(pathUuid);
      // just update the fields, that were editable
      websiteDb.setUrl(website.getUrl());
      websiteDb.setLabel(website.getLabel());
//      websiteDb.setDescription(website.getDescription());

      website = (Website) websiteService.update(websiteDb, results);
    } catch (IdentifiableServiceException e) {
      String message = "Cannot save website with uuid=" + pathUuid + ": " + e;
      LOGGER.error(message, e);
      redirectAttributes.addFlashAttribute("error_message", message);
      return "redirect:/websites/" + pathUuid;
    }

    if (results.hasErrors()) {
      model.addAttribute("isNew", false);
      return "websites/edit";
    }
    status.setComplete();
    String message = messageSource.getMessage("msg.changes_saved_successfully", null, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute("success_message", message);
    return "redirect:/websites/" + pathUuid + "/edit";
  }

  @RequestMapping(method = RequestMethod.GET)
  public String list(Model model, @PageableDefault(sort = {"email"}, size = 25) Pageable pageable) {
    final PageRequest pageRequest = PageableConverter.convert(pageable);
    final PageResponse pageResponse = websiteService.find(pageRequest);
    Page page = PageConverter.convert(pageResponse, pageRequest);
    model.addAttribute("page", new PageWrapper(page, "/websites"));
    return "websites/list";
  }

  @RequestMapping(value = "/{uuid}", method = RequestMethod.GET)
  public String view(@PathVariable UUID uuid, Model model) {
    Website website = (Website) websiteService.get(uuid);
    model.addAttribute("availableLanguages", website.getLabel().getLanguages());
    model.addAttribute("website", website);
    return "websites/view";
  }

  // ----------------------------------------------------------------------------
  public void setWebsiteService(WebsiteService websiteService) {
    this.websiteService = websiteService;
  }
}
