package de.digitalcollections.cudami.admin.controller.identifiable.resource;

import de.digitalcollections.commons.springdata.domain.PageConverter;
import de.digitalcollections.commons.springdata.domain.PageWrapper;
import de.digitalcollections.commons.springdata.domain.PageableConverter;
import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.core.model.api.paging.PageRequest;
import de.digitalcollections.core.model.api.paging.PageResponse;
import de.digitalcollections.cudami.admin.business.api.service.LocaleService;
import de.digitalcollections.cudami.admin.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.resource.WebpageService;
import de.digitalcollections.cudami.model.api.identifiable.resource.Webpage;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for webpage management pages.
 */
@Controller
@SessionAttributes(value = {"webpage"})
public class WebpagesController extends AbstractController implements MessageSourceAware {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebpagesController.class);

  private MessageSource messageSource;

  @Autowired
  LocaleService localeService;

  @Autowired
  WebpageService webpageService;

  @Override
  public void setMessageSource(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  @ModelAttribute("menu")
  protected String module() {
    return "webpages";
  }

  @InitBinder("webpage")
  protected void initBinder(WebDataBinder binder) {
    binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
  }

  @RequestMapping(value = "/webpages/new", method = RequestMethod.GET)
  public String create(Model model, @RequestParam("websiteUuid") String websiteUuid) {
    model.addAttribute("webpage", webpageService.create());
    model.addAttribute("isNew", true);
    model.addAttribute("locales", localeService.findAll());
    model.addAttribute("defaultLocale", localeService.getDefault());
    model.addAttribute("websiteUuid", websiteUuid);
    return "webpages/edit";
  }

  @RequestMapping(value = "/webpages/new", method = RequestMethod.POST)
  public String create(@ModelAttribute @Valid Webpage webpage, BindingResult results, Model model, SessionStatus status, RedirectAttributes redirectAttributes, @RequestParam("websiteUuid") UUID websiteUuid) {
    verifyBinding(results);
    if (results.hasErrors()) {
      model.addAttribute("isNew", true);
      return "webpages/edit";
    }
    Webpage webpageDb = null;
    try {
      webpageDb = webpageService.save(webpage, websiteUuid, results);
      LOGGER.info("Successfully saved webpage");
    } catch (Exception e) {
      LOGGER.error("Cannot save webpage: ", e);
      String message = messageSource.getMessage("msg.error", null, LocaleContextHolder.getLocale());
      redirectAttributes.addFlashAttribute("error_message", message);
      return "redirect:/websites";
    }
    if (results.hasErrors()) {
      model.addAttribute("isNew", true);
      return "webpages/edit";
    }
    status.setComplete();
    String message = messageSource.getMessage("msg.created_successfully", null, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute("success_message", message);
    return "redirect:/webpages/" + webpageDb.getUuid().toString();
  }

  @RequestMapping(value = "/webpages/{uuid}/edit", method = RequestMethod.GET)
  public String edit(@PathVariable UUID uuid, Model model, RedirectAttributes redirectAttributes) {
    Webpage webpage = (Webpage) webpageService.get(uuid);
    model.addAttribute("webpage", webpage);
    model.addAttribute("isNew", false);
    model.addAttribute("availableLanguages", webpage.getLabel().getLanguages());
    model.addAttribute("locales", localeService.findAll());
    model.addAttribute("defaultLocale", localeService.getDefault());

    return "webpages/edit";
  }

  @RequestMapping(value = "/webpages/{pathUuid}/edit", method = RequestMethod.POST)
  public String edit(@PathVariable UUID pathUuid, @ModelAttribute @Valid Webpage webpage, BindingResult results, Model model, SessionStatus status, RedirectAttributes redirectAttributes) {
    verifyBinding(results);
    if (results.hasErrors()) {
      model.addAttribute("isNew", false);
      return "webpages/edit";
    }

    try {
      // get webpage from db
      Webpage webpageDb = (Webpage) webpageService.get(pathUuid);
      // just update the fields, that were editable
      webpageDb.setLabel(webpage.getLabel());
      webpageDb.setDescription(webpage.getDescription());

      webpage = (Webpage) webpageService.update(webpageDb, results);
    } catch (IdentifiableServiceException e) {
      String message = "Cannot save webpage with uuid=" + pathUuid + ": " + e;
      LOGGER.error(message, e);
      redirectAttributes.addFlashAttribute("error_message", message);
      return "redirect:/webpages/" + pathUuid + "/edit";
    }

    if (results.hasErrors()) {
      model.addAttribute("isNew", false);
      return "webpages/edit";
    }
    status.setComplete();
    String message = messageSource.getMessage("msg.changes_saved_successfully", null, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute("success_message", message);
    return "redirect:/webpages/" + pathUuid;
  }

  @RequestMapping(value = "/webpages", method = RequestMethod.GET)
  public String list(Model model, @PageableDefault(sort = {"email"}, size = 25) Pageable pageable) {
    final PageRequest pageRequest = PageableConverter.convert(pageable);
    final PageResponse pageResponse = webpageService.find(pageRequest);
    Page page = PageConverter.convert(pageResponse, pageRequest);
    model.addAttribute("page", new PageWrapper(page, "/webpages"));
    return "webpages/list";
  }

  @RequestMapping(value = "/webpages/{uuid}", method = RequestMethod.GET)
  public String view(@PathVariable UUID uuid, Model model) {
    Webpage webpage = (Webpage) webpageService.get(uuid);
    model.addAttribute("availableLanguages", webpage.getLabel().getLanguages());
    model.addAttribute("defaultLocale", localeService.getDefault());
    model.addAttribute("webpage", webpage);
    return "webpages/view";
  }

  public void setWebpageService(WebpageService webpageService) {
    this.webpageService = webpageService;
  }
}
