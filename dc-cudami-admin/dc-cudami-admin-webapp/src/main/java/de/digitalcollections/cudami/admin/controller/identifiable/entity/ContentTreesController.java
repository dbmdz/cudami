package de.digitalcollections.cudami.admin.controller.identifiable.entity;

import de.digitalcollections.commons.springdata.domain.PageConverter;
import de.digitalcollections.commons.springdata.domain.PageWrapper;
import de.digitalcollections.commons.springdata.domain.PageableConverter;
import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.business.api.service.LocaleService;
import de.digitalcollections.cudami.admin.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.entity.ContentTreeService;
import de.digitalcollections.model.api.identifiable.entity.ContentTree;
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
    Locale defaultLocale = localeService.getDefault();
    List<Locale> locales = localeService.findAll().stream()
            .filter(locale -> !(defaultLocale.equals(locale) || locale.getDisplayName().isEmpty()))
            .sorted(Comparator.comparing(locale -> locale.getDisplayName(LocaleContextHolder.getLocale())))
            .collect(Collectors.toList());

    model.addAttribute("contentTree", contentTreeService.create());
    model.addAttribute("defaultLocale", defaultLocale);
    model.addAttribute("isNew", true);
    model.addAttribute("locales", locales);
    return "contenttrees/edit";
  }

  @RequestMapping(value = "/contenttrees/new", method = RequestMethod.POST)
  public String create(@ModelAttribute @Valid ContentTree contentTree, BindingResult results, Model model, SessionStatus status, RedirectAttributes redirectAttributes) {
    verifyBinding(results);
    if (results.hasErrors()) {
      model.addAttribute("isNew", true);
      return "contenttrees/edit";
    }
    ContentTree contentTreeDb = null;
    try {
      contentTreeDb = (ContentTree) contentTreeService.save(contentTree, results);
      LOGGER.info("Successfully saved content tree");
    } catch (Exception e) {
      LOGGER.error("Cannot save content tree: ", e);
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
    return "redirect:/contenttrees/" + contentTreeDb.getUuid().toString();
  }

  @RequestMapping(value = "/contenttrees/{uuid}/edit", method = RequestMethod.GET)
  public String edit(@PathVariable UUID uuid, Model model, RedirectAttributes redirectAttributes) {
//      model.addAttribute("contentNodeTypes", websiteViewService.getContentNodeTypes());
//      model.addAttribute("navigationNodeTypes", websiteViewService.getNavigationNodeTypes());
    ContentTree contentTree = (ContentTree) contentTreeService.get(uuid);
    model.addAttribute("contentTree", contentTree);

    HashSet<Locale> availableLocales = (HashSet<Locale>) contentTree.getLabel().getLocales();
    Set<String> availableLocaleTags = availableLocales.stream().map(Locale::toLanguageTag).collect(Collectors.toSet());
    List<Locale> locales = localeService.findAll().stream()
            .filter(locale -> !(availableLocaleTags.contains(locale.toLanguageTag()) || locale.getDisplayName().isEmpty()))
            .sorted(Comparator.comparing(locale -> locale.getDisplayName(LocaleContextHolder.getLocale())))
            .collect(Collectors.toList());
//      LOGGER.error("Cannot retrieve website with id=" + id + ": ", e);
//      String message = messageSource.getMessage("msg.error", null, LocaleContextHolder.getLocale());
//      redirectAttributes.addFlashAttribute("error_message", message);
//      return "redirect:/websites";
    model.addAttribute("isNew", false);
    model.addAttribute("availableLocales", availableLocales);
    model.addAttribute("locales", locales);

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
      ContentTree contentTreeDb = (ContentTree) contentTreeService.get(pathUuid);
      // just update the fields, that were editable
      contentTreeDb.setLabel(contentTree.getLabel());
      contentTreeDb.setDescription(contentTree.getDescription());

      contentTreeService.update(contentTreeDb, results);
    } catch (IdentifiableServiceException e) {
      String message = "Cannot save content tree with uuid=" + pathUuid + ": " + e;
      LOGGER.error(message, e);
      redirectAttributes.addFlashAttribute("error_message", message);
      return "redirect:/contenttrees/" + pathUuid + "/edit";
    }

    if (results.hasErrors()) {
      model.addAttribute("isNew", false);
      return "contenttrees/edit";
    }
    status.setComplete();
    String message = messageSource.getMessage("msg.changes_saved_successfully", null, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute("success_message", message);
    return "redirect:/contenttrees/" + pathUuid;
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
    model.addAttribute("availableLocales", contentTree.getLabel().getLocales());
    model.addAttribute("contentTree", contentTree);
    return "contenttrees/view";
  }

  // ----------------------------------------------------------------------------
  public void setService(ContentTreeService contentTreeService) {
    this.contentTreeService = contentTreeService;
  }
}
