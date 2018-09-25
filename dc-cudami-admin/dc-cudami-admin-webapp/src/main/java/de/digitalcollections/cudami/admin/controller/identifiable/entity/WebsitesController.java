package de.digitalcollections.cudami.admin.controller.identifiable.entity;

import de.digitalcollections.commons.springdata.domain.PageConverter;
import de.digitalcollections.commons.springdata.domain.PageWrapper;
import de.digitalcollections.commons.springdata.domain.PageableConverter;
import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.business.api.service.LocaleService;
import de.digitalcollections.cudami.admin.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.entity.WebsiteService;
import de.digitalcollections.model.api.identifiable.entity.Website;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
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
 * Controller for website management pages.
 */
@Controller
@SessionAttributes(value = {"website"})
public class WebsitesController extends AbstractController implements MessageSourceAware {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebsitesController.class);

  private MessageSource messageSource;

  @Autowired
  LocaleService localeService;

  @Autowired
  WebsiteService websiteService;

  @Override
  public void setMessageSource(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  @ModelAttribute("menu")
  protected String module() {
    return "websites";
  }

  @RequestMapping(value = "/websites/new", method = RequestMethod.GET)
  public String create(Model model) {
    Locale defaultLocale = localeService.getDefault();
    List<Locale> locales = localeService.findAll().stream()
            .filter(locale -> !(defaultLocale.equals(locale) || locale.getDisplayName().isEmpty()))
            .sorted(Comparator.comparing(locale -> locale.getDisplayName(LocaleContextHolder.getLocale())))
            .collect(Collectors.toList());

    model.addAttribute("defaultLocale", defaultLocale);
    model.addAttribute("locales", locales);
    model.addAttribute("website", websiteService.create());
    return "websites/create";
  }

  @RequestMapping(value = "/websites/new", method = RequestMethod.POST)
  public String create(@ModelAttribute @Valid Website website, BindingResult results, Model model, SessionStatus status, RedirectAttributes redirectAttributes) {
    verifyBinding(results);
    if (results.hasErrors()) {
      return "websites/create";
    }
    Website websiteDb = null;
    try {
      websiteDb = (Website) websiteService.save(website, results);
      LOGGER.info("Successfully saved website");
    } catch (Exception e) {
      LOGGER.error("Cannot save website: ", e);
      String message = messageSource.getMessage("msg.error", null, LocaleContextHolder.getLocale());
      redirectAttributes.addFlashAttribute("error_message", message);
      return "redirect:/websites";
    }
    if (results.hasErrors()) {
      return "websites/create";
    }
    status.setComplete();
    String message = messageSource.getMessage("msg.created_successfully", null, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute("success_message", message);
    return "redirect:/websites/" + websiteDb.getUuid().toString();
  }

  @RequestMapping(value = "/websites/{uuid}/edit", method = RequestMethod.GET)
  public String edit(@PathVariable UUID uuid, Model model, RedirectAttributes redirectAttributes) {
//      model.addAttribute("contentNodeTypes", websiteViewService.getContentNodeTypes());
//      model.addAttribute("navigationNodeTypes", websiteViewService.getNavigationNodeTypes());
    Website website = (Website) websiteService.get(uuid);
    model.addAttribute("website", website);

    HashSet<Locale> availableLocales = (HashSet<Locale>) website.getLabel().getLocales();
    Set<String> availableLocaleTags = availableLocales.stream().map(Locale::toLanguageTag).collect(Collectors.toSet());
    List<Locale> locales = localeService.findAll().stream()
            .filter(locale -> !(availableLocaleTags.contains(locale.toLanguageTag()) || locale.getDisplayName().isEmpty()))
            .sorted(Comparator.comparing(locale -> locale.getDisplayName(LocaleContextHolder.getLocale())))
            .collect(Collectors.toList());
//      LOGGER.error("Cannot retrieve website with id=" + id + ": ", e);
//      String message = messageSource.getMessage("msg.error", null, LocaleContextHolder.getLocale());
//      redirectAttributes.addFlashAttribute("error_message", message);
//      return "redirect:/websites";
    model.addAttribute("availableLocales", availableLocales);
    model.addAttribute("locales", locales);

    return "websites/edit";
  }

  @RequestMapping(value = "/websites/{pathUuid}/edit", method = RequestMethod.POST)
  public String edit(@PathVariable UUID pathUuid, @ModelAttribute @Valid Website website, BindingResult results, Model model, SessionStatus status, RedirectAttributes redirectAttributes) {
    verifyBinding(results);
    if (results.hasErrors()) {
      return "websites/edit";
    }

    try {
      // get website from db
      Website websiteDb = (Website) websiteService.get(pathUuid);
      // just update the fields, that were editable
      websiteDb.setUrl(website.getUrl());
      websiteDb.setLabel(website.getLabel());
      websiteDb.setDescription(website.getDescription());

      websiteService.update(websiteDb, results);
    } catch (IdentifiableServiceException e) {
      String message = "Cannot save website with uuid=" + pathUuid + ": " + e;
      LOGGER.error(message, e);
      redirectAttributes.addFlashAttribute("error_message", message);
      return "redirect:/websites/" + pathUuid + "/edit";
    }

    if (results.hasErrors()) {
      return "websites/edit";
    }
    status.setComplete();
    String message = messageSource.getMessage("msg.changes_saved_successfully", null, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute("success_message", message);
    return "redirect:/websites/" + pathUuid;
  }

  @RequestMapping(value = "/websites", method = RequestMethod.GET)
  public String list(Model model, @PageableDefault(sort = {"email"}, size = 25) Pageable pageable) {
    final PageRequest pageRequest = PageableConverter.convert(pageable);
    final PageResponse pageResponse = websiteService.find(pageRequest);
    Page page = PageConverter.convert(pageResponse, pageRequest);
    model.addAttribute("defaultLocale", localeService.getDefault());
    model.addAttribute("page", new PageWrapper(page, "/websites"));
    return "websites/list";
  }

  @RequestMapping(value = "/websites/{uuid}", method = RequestMethod.GET)
  public String view(@PathVariable UUID uuid, Model model) {
    Website website = (Website) websiteService.get(uuid);
    model.addAttribute("availableLocales", website.getLabel().getLocales());
    model.addAttribute("defaultLocale", localeService.getDefault());
    model.addAttribute("website", website);
    return "websites/view";
  }

  // ----------------------------------------------------------------------------
  public void setWebsiteService(WebsiteService websiteService) {
    this.websiteService = websiteService;
  }
}
