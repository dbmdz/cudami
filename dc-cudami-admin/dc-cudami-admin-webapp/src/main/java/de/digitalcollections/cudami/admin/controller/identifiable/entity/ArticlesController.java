package de.digitalcollections.cudami.admin.controller.identifiable.entity;

import de.digitalcollections.commons.springdata.domain.PageConverter;
import de.digitalcollections.commons.springdata.domain.PageWrapper;
import de.digitalcollections.commons.springdata.domain.PageableConverter;
import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.backend.api.repository.LocaleRepository;
import de.digitalcollections.cudami.admin.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.entity.ArticleService;
import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.model.api.identifiable.entity.Article;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

/** Controller for content trees management pages. */
@Controller
public class ArticlesController extends AbstractController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ArticlesController.class);

  LanguageSortingHelper languageSortingHelper;
  LocaleRepository localeRepository;
  ArticleService service;

  @Autowired
  public ArticlesController(
      LanguageSortingHelper languageSortingHelper,
      LocaleRepository localeRepository,
      ArticleService service) {
    this.languageSortingHelper = languageSortingHelper;
    this.localeRepository = localeRepository;
    this.service = service;
  }

  @ModelAttribute("menu")
  protected String module() {
    return "articles";
  }

  @GetMapping("/articles/new")
  public String create(Model model) {
    model.addAttribute("activeLanguage", localeRepository.getDefaultLanguage());
    return "articles/create";
  }

  @GetMapping("/api/articles/new")
  @ResponseBody
  public Article create() {
    return service.create();
  }

  @GetMapping("/articles/{uuid}/edit")
  public String edit(@PathVariable UUID uuid, Model model) {
    Article article = service.get(uuid);
    model.addAttribute("activeLanguage", localeRepository.getDefaultLanguage());
    model.addAttribute("uuid", article.getUuid());
    return "articles/edit";
  }

  @GetMapping("/api/articles/{uuid}")
  @ResponseBody
  public Article get(@PathVariable UUID uuid) {
    return service.get(uuid);
  }

  @GetMapping("/articles")
  public String list(
      Model model,
      @PageableDefault(
              sort = {"lastModified"},
              size = 25)
          Pageable pageable) {
    final PageRequest pageRequest = PageableConverter.convert(pageable);
    final PageResponse pageResponse = service.find(pageRequest);
    Page page = PageConverter.convert(pageResponse, pageRequest);
    model.addAttribute("page", new PageWrapper(page, "/articles"));
    return "articles/list";
  }

  @PostMapping("/api/articles/new")
  public ResponseEntity save(@RequestBody Article article) throws IdentifiableServiceException {
    try {
      Article articleDb = service.save(article);
      return ResponseEntity.status(HttpStatus.CREATED).body(articleDb);
    } catch (Exception e) {
      LOGGER.error("Cannot save article: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @PutMapping("/api/articles/{uuid}")
  public ResponseEntity update(@PathVariable UUID uuid, @RequestBody Article article)
      throws IdentifiableServiceException {
    try {
      Article articleDb = service.update(article);
      return ResponseEntity.ok(articleDb);
    } catch (Exception e) {
      LOGGER.error("Cannot save article with uuid={}", uuid, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @GetMapping("/articles/{uuid}")
  public String view(@PathVariable UUID uuid, Model model) {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    Article article = (Article) service.get(uuid);
    List<Locale> availableLanguages =
        languageSortingHelper.sortLanguages(displayLocale, article.getLabel().getLocales());

    model.addAttribute("article", article);
    model.addAttribute("availableLanguages", availableLanguages);

    LinkedHashSet<FileResource> relatedFileResources = service.getRelatedFileResources(article);
    model.addAttribute("relatedFileResources", relatedFileResources);

    return "articles/view";
  }
}
