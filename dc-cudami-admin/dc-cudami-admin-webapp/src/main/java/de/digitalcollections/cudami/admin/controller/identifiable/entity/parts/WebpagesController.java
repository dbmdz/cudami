package de.digitalcollections.cudami.admin.controller.identifiable.entity.parts;

import de.digitalcollections.commons.springdata.domain.PageConverter;
import de.digitalcollections.commons.springdata.domain.PageWrapper;
import de.digitalcollections.commons.springdata.domain.PageableConverter;
import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.business.api.service.LocaleService;
import de.digitalcollections.cudami.admin.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.entity.parts.WebpageService;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
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

  @RequestMapping(value = "/webpages/new", method = RequestMethod.GET)
  public String create(Model model, @RequestParam("parentType") String parentType, @RequestParam("parentUuid") String parentUuid) {
    Locale defaultLocale = localeService.getDefault();
    List<Locale> locales = localeService.findAll().stream()
            .filter(locale -> !(defaultLocale.equals(locale) || locale.getDisplayName().isEmpty()))
            .sorted(Comparator.comparing(locale -> locale.getDisplayName(LocaleContextHolder.getLocale())))
            .collect(Collectors.toList());

    model.addAttribute("defaultLocale", defaultLocale);
    model.addAttribute("locales", locales);
    model.addAttribute("parentType", parentType);
    model.addAttribute("parentUuid", parentUuid);
    model.addAttribute("webpage", webpageService.create());
    return "webpages/create";
  }

  @RequestMapping(value = "/webpages/new", method = RequestMethod.POST)
  public String create(@ModelAttribute @Valid Webpage webpage, BindingResult results, Model model, SessionStatus status, RedirectAttributes redirectAttributes,
          @RequestParam("parentType") String parentType,
          @RequestParam("parentUuid") UUID parentUuid) {
    verifyBinding(results);
    if (results.hasErrors()) {
      return "webpages/create";
    }
    Webpage webpageDb = null;
    try {
      if (Objects.equals(parentType, "website")) {
        webpageDb = webpageService.saveWithParentWebsite(webpage, parentUuid, results);
        LOGGER.info("Successfully saved top-level webpage");
      } else if (Objects.equals(parentType, "webpage")) {
        webpageDb = webpageService.saveWithParentWebpage(webpage, parentUuid, results);
        LOGGER.info("Successfully saved webpage");
      }
    } catch (Exception e) {
      if (Objects.equals(parentType, "website")) {
        LOGGER.info("Cannot save top-level webpage: ", e);
      } else if (Objects.equals(parentType, "webpage")) {
        LOGGER.error("Cannot save webpage: ", e);
      }
      String message = messageSource.getMessage("msg.error", null, LocaleContextHolder.getLocale());
      redirectAttributes.addFlashAttribute("error_message", message);
      return "redirect:/websites";
    }
    if (results.hasErrors()) {
      return "webpages/create";
    }
    status.setComplete();
    String message = messageSource.getMessage("msg.created_successfully", null, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute("success_message", message);
    return "redirect:/webpages/" + webpageDb.getUuid().toString();
  }

  @RequestMapping(value = "/webpages/{uuid}/edit", method = RequestMethod.GET)
  public String edit(@PathVariable UUID uuid, Model model, RedirectAttributes redirectAttributes) {
    Webpage webpage = (Webpage) webpageService.get(uuid);

    HashSet<Locale> availableLocales = (HashSet<Locale>) webpage.getLabel().getLocales();
    Set<String> availableLocaleTags = availableLocales.stream().map(Locale::toLanguageTag).collect(Collectors.toSet());
    List<Locale> locales = localeService.findAll().stream()
            .filter(locale -> !(availableLocaleTags.contains(locale.toLanguageTag()) || locale.getDisplayName().isEmpty()))
            .sorted(Comparator.comparing(locale -> locale.getDisplayName(LocaleContextHolder.getLocale())))
            .collect(Collectors.toList());

    model.addAttribute("webpage", webpage);
    model.addAttribute("availableLocales", availableLocales);
    model.addAttribute("locales", locales);

    return "webpages/edit";
  }

  @RequestMapping(value = "/webpages/{pathUuid}/edit", method = RequestMethod.POST)
  public String edit(@PathVariable UUID pathUuid, @ModelAttribute @Valid Webpage webpage, BindingResult results, Model model, SessionStatus status, RedirectAttributes redirectAttributes) {
    verifyBinding(results);
    if (results.hasErrors()) {
      return "webpages/edit";
    }

    try {
      // get webpage from db
      Webpage webpageDb = (Webpage) webpageService.get(pathUuid);
      // just update the fields, that were editable
      webpageDb.setLabel(webpage.getLabel());
      webpageDb.setDescription(webpage.getDescription());
      webpageDb.setText(webpage.getText());

      webpageService.update(webpageDb, results);
    } catch (IdentifiableServiceException e) {
      String message = "Cannot save webpage with uuid=" + pathUuid + ": " + e;
      LOGGER.error(message, e);
      redirectAttributes.addFlashAttribute("error_message", message);
      return "redirect:/webpages/" + pathUuid + "/edit";
    }

    if (results.hasErrors()) {
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
    model.addAttribute("availableLocales", webpage.getLabel().getLocales());
    model.addAttribute("defaultLocale", localeService.getDefault());
    model.addAttribute("webpage", webpage);
    return "webpages/view";
  }

  public void setWebpageService(WebpageService webpageService) {
    this.webpageService = webpageService;
  }
}
