package de.digitalcollections.cudami.admin.controller.identifiable.entity.parts;

import de.digitalcollections.commons.springdata.domain.PageConverter;
import de.digitalcollections.commons.springdata.domain.PageWrapper;
import de.digitalcollections.commons.springdata.domain.PageableConverter;
import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.business.api.service.LocaleService;
import de.digitalcollections.cudami.admin.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.entity.parts.ContentNodeService;
import de.digitalcollections.model.api.identifiable.entity.parts.ContentNode;
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
 * Controller for content node management pages.
 */
@Controller
@SessionAttributes(value = {"contentNode"})
public class ContentNodesController extends AbstractController implements MessageSourceAware {

  private static final Logger LOGGER = LoggerFactory.getLogger(ContentNodesController.class);

  private MessageSource messageSource;

  @Autowired
  LocaleService localeService;

  @Autowired
  ContentNodeService contentNodeService;

  @Override
  public void setMessageSource(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  @ModelAttribute("menu")
  protected String module() {
    return "contentnodes";
  }

  @RequestMapping(value = "/contentnodes/new", method = RequestMethod.GET)
  public String create(Model model, @RequestParam("parentType") String parentType, @RequestParam("parentUuid") String parentUuid) {
    Locale defaultLocale = localeService.getDefault();
    List<Locale> locales = localeService.findAll().stream()
            .filter(locale -> !(defaultLocale.equals(locale) || locale.getDisplayName().isEmpty()))
            .sorted(Comparator.comparing(locale -> locale.getDisplayName(LocaleContextHolder.getLocale())))
            .collect(Collectors.toList());

    model.addAttribute("contentNode", contentNodeService.create());
    model.addAttribute("defaultLocale", defaultLocale);
    model.addAttribute("locales", locales);
    model.addAttribute("parentType", parentType);
    model.addAttribute("parentUuid", parentUuid);
    return "contentnodes/create";
  }

  @RequestMapping(value = "/contentnodes/new", method = RequestMethod.POST)
  public String create(@ModelAttribute @Valid ContentNode contentNode, BindingResult results, Model model, SessionStatus status, RedirectAttributes redirectAttributes,
          @RequestParam("parentType") String parentType,
          @RequestParam("parentUuid") UUID parentUuid) {
    verifyBinding(results);
    if (results.hasErrors()) {
      return "contentnodes/create";
    }
    ContentNode contentNodeDb = null;
    try {
      if (Objects.equals(parentType, "contentTree")) {
        contentNodeDb = contentNodeService.saveWithParentContentTree(contentNode, parentUuid, results);
        LOGGER.info("Successfully saved top-level content node");
      } else if (Objects.equals(parentType, "contentNode")) {
        contentNodeDb = contentNodeService.saveWithParentContentNode(contentNode, parentUuid, results);
        LOGGER.info("Successfully saved content node");
      }
    } catch (Exception e) {
      if (Objects.equals(parentType, "contentTree")) {
        LOGGER.info("Cannot save top-level content node: ", e);
      } else if (Objects.equals(parentType, "contentNode")) {
        LOGGER.error("Cannot save content node: ", e);
      }
      String message = messageSource.getMessage("msg.error", null, LocaleContextHolder.getLocale());
      redirectAttributes.addFlashAttribute("error_message", message);
      return "redirect:/contenttrees";
    }
    if (results.hasErrors()) {
      return "contentNodes/create";
    }
    status.setComplete();
    String message = messageSource.getMessage("msg.created_successfully", null, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute("success_message", message);
    return "redirect:/contentnodes/" + contentNodeDb.getUuid().toString();
  }

  @RequestMapping(value = "/contentnodes/{uuid}/edit", method = RequestMethod.GET)
  public String edit(@PathVariable UUID uuid, Model model, RedirectAttributes redirectAttributes) {
    ContentNode contentNode = (ContentNode) contentNodeService.get(uuid);

    HashSet<Locale> availableLocales = (HashSet<Locale>) contentNode.getLabel().getLocales();
    Set<String> availableLocaleTags = availableLocales.stream().map(Locale::toLanguageTag).collect(Collectors.toSet());
    List<Locale> locales = localeService.findAll().stream()
            .filter(locale -> !(availableLocaleTags.contains(locale.toLanguageTag()) || locale.getDisplayName().isEmpty()))
            .sorted(Comparator.comparing(locale -> locale.getDisplayName(LocaleContextHolder.getLocale())))
            .collect(Collectors.toList());

    model.addAttribute("contentNode", contentNode);
    model.addAttribute("availableLocales", availableLocales);
    model.addAttribute("locales", locales);

    return "contentnodes/edit";
  }

  @RequestMapping(value = "/contentnodes/{pathUuid}/edit", method = RequestMethod.POST)
  public String edit(@PathVariable UUID pathUuid, @ModelAttribute @Valid ContentNode contentNode, BindingResult results, Model model, SessionStatus status, RedirectAttributes redirectAttributes) {
    verifyBinding(results);
    if (results.hasErrors()) {
      return "contentnodes/edit";
    }

    try {
      // get content node from db
      ContentNode contentNodeDb = (ContentNode) contentNodeService.get(pathUuid);
      // just update the fields, that were editable
      contentNodeDb.setLabel(contentNode.getLabel());
      contentNodeDb.setDescription(contentNode.getDescription());

      contentNodeService.update(contentNodeDb, results);
    } catch (IdentifiableServiceException e) {
      String message = "Cannot save content node with uuid=" + pathUuid + ": " + e;
      LOGGER.error(message, e);
      redirectAttributes.addFlashAttribute("error_message", message);
      return "redirect:/contentnodes/" + pathUuid + "/edit";
    }

    if (results.hasErrors()) {
      return "contentnodes/edit";
    }
    status.setComplete();
    String message = messageSource.getMessage("msg.changes_saved_successfully", null, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute("success_message", message);
    return "redirect:/contentnodes/" + pathUuid;
  }

  @RequestMapping(value = "/contentnodes", method = RequestMethod.GET)
  public String list(Model model, @PageableDefault(sort = {"uuid"}) Pageable pageable) {
    final PageRequest pageRequest = PageableConverter.convert(pageable);
    final PageResponse pageResponse = contentNodeService.find(pageRequest);
    Page page = PageConverter.convert(pageResponse, pageRequest);
    model.addAttribute("page", new PageWrapper(page, "/contentnodes"));
    return "contentnodes/list";
  }

  @RequestMapping(value = "/contentnodes/{uuid}", method = RequestMethod.GET)
  public String view(@PathVariable UUID uuid, Model model) {
    ContentNode contentNode = (ContentNode) contentNodeService.get(uuid);
    model.addAttribute("availableLocales", contentNode.getLabel().getLocales());
    model.addAttribute("defaultLocale", localeService.getDefault());
    model.addAttribute("contentNode", contentNode);
    return "contentnodes/view";
  }

  public void setContentNodeService(ContentNodeService contentNodeService) {
    this.contentNodeService = contentNodeService;
  }
}
