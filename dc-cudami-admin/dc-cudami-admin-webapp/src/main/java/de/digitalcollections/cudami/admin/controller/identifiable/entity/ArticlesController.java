package de.digitalcollections.cudami.admin.controller.identifiable.entity;

import de.digitalcollections.commons.springdata.domain.PageConverter;
import de.digitalcollections.commons.springdata.domain.PageWrapper;
import de.digitalcollections.commons.springdata.domain.PageableConverter;
import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.business.api.service.LocaleService;
import de.digitalcollections.cudami.admin.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.entity.ArticleService;
import de.digitalcollections.model.api.identifiable.entity.Article;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for content trees management pages.
 */
@Controller
@SessionAttributes(value = {"article"})
public class ArticlesController extends AbstractController implements MessageSourceAware {

  private static final Logger LOGGER = LoggerFactory.getLogger(ArticlesController.class);

  private MessageSource messageSource;

  @Autowired
  LocaleService localeService;

  @Autowired
  ArticleService<Article> service;

  @Override
  public void setMessageSource(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  @ModelAttribute("menu")
  protected String module() {
    return "articles";
  }

  @RequestMapping(value = "/articles/new", method = RequestMethod.GET)
  public String create(Model model, @RequestParam("parentUuid") String parentUuid) {
    model.addAttribute("article", service.create());
    model.addAttribute("isNew", true);
    model.addAttribute("locales", localeService.findAll());
    model.addAttribute("defaultLocale", localeService.getDefault());
    model.addAttribute("parentUuid", parentUuid);
    return "articles/edit";
  }

  @RequestMapping(value = "/articles/new", method = RequestMethod.POST)
  public String create(@ModelAttribute @Valid Article article, BindingResult results, Model model, SessionStatus status, RedirectAttributes redirectAttributes,
          @RequestParam(required = false, name = "parentType") String parentType,
          @RequestParam(required = false, name = "parentUuid") UUID parentUuid) {
    verifyBinding(results);
    if (results.hasErrors()) {
      model.addAttribute("isNew", true);
      return "articles/edit";
    }
    Article articleDb = null;
    try {
      if ("article".equals(parentType) && parentUuid != null) {
        articleDb = service.saveWithParent(article, parentUuid);
        LOGGER.info("Successfully saved subarticle");
      } else {
        articleDb = service.save(article, results);
        LOGGER.info("Successfully saved article");
      }
    } catch (Exception e) {
      LOGGER.error("Cannot save article: ", e);
      String message = messageSource.getMessage("msg.error", null, LocaleContextHolder.getLocale());
      redirectAttributes.addFlashAttribute("error_message", message);
      return "redirect:/articles";
    }
    if (results.hasErrors()) {
      model.addAttribute("isNew", true);
      return "articles/edit";
    }
    status.setComplete();
    String message = messageSource.getMessage("msg.created_successfully", null, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute("success_message", message);
    return "redirect:/articles/" + articleDb.getUuid().toString();
  }

  @RequestMapping(value = "/articles/{uuid}/edit", method = RequestMethod.GET)
  public String edit(@PathVariable UUID uuid, Model model, RedirectAttributes redirectAttributes) {
    Article article = service.get(uuid);

    model.addAttribute("availableLocales", article.getLabel().getLocales());
    model.addAttribute("defaultLocale", localeService.getDefault());
    model.addAttribute("article", article);

    model.addAttribute("isNew", false);
    model.addAttribute("locales", localeService.findAll());

    return "articles/edit";
  }

  @RequestMapping(value = "/articles/{pathUuid}/edit", method = RequestMethod.POST)
  public String edit(@PathVariable UUID pathUuid, @ModelAttribute @Valid Article article, BindingResult results, Model model, SessionStatus status, RedirectAttributes redirectAttributes) {
    verifyBinding(results);
    if (results.hasErrors()) {
      model.addAttribute("isNew", false);
      return "articles/edit";
    }

    try {
      // get content tree from db
      Article articleDb = service.get(pathUuid);
      // just update the fields, that were editable
      articleDb.setLabel(article.getLabel());
      articleDb.setDescription(article.getDescription());
      articleDb.setText(article.getText());

      service.update(articleDb, results);
    } catch (IdentifiableServiceException e) {
      String message = "Cannot save article with uuid=" + pathUuid + ": " + e;
      LOGGER.error(message, e);
      redirectAttributes.addFlashAttribute("error_message", message);
      return "redirect:/articles/" + pathUuid + "/edit";
    }

    if (results.hasErrors()) {
      model.addAttribute("isNew", false);
      return "articles/edit";
    }
    status.setComplete();
    String message = messageSource.getMessage("msg.changes_saved_successfully", null, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute("success_message", message);
    return "redirect:/articles/" + pathUuid;
  }

  @RequestMapping(value = "/articles", method = RequestMethod.GET)
  public String list(Model model, @PageableDefault(sort = {"lastModified"}, size = 25) Pageable pageable) {
    final PageRequest pageRequest = PageableConverter.convert(pageable);
    final PageResponse pageResponse = service.find(pageRequest);
    Page page = PageConverter.convert(pageResponse, pageRequest);
    model.addAttribute("page", new PageWrapper(page, "/articles"));
    return "articles/list";
  }

  @RequestMapping(value = "/articles/{uuid}", method = RequestMethod.GET)
  public String view(@PathVariable UUID uuid, Model model) {
    Article article = (Article) service.get(uuid);
    model.addAttribute("availableLocales", article.getLabel().getLocales());
    model.addAttribute("defaultLocale", localeService.getDefault());
    model.addAttribute("article", article);
    return "articles/view";
  }

  public void setService(ArticleService service) {
    this.service = service;
  }
}
