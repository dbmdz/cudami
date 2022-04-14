package de.digitalcollections.cudami.admin.controller.identifiable.entity;

import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.CudamiLocalesClient;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiArticlesClient;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.Article;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.Sorting;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/** Controller for articles management pages. */
@Controller
public class ArticlesController extends AbstractController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ArticlesController.class);

  private final LanguageSortingHelper languageSortingHelper;
  private final CudamiLocalesClient localeService;
  private final CudamiArticlesClient service;

  public ArticlesController(LanguageSortingHelper languageSortingHelper, CudamiClient client) {
    this.languageSortingHelper = languageSortingHelper;
    this.localeService = client.forLocales();
    this.service = client.forArticles();
  }

  @GetMapping("/articles/new")
  public String create(Model model) throws TechnicalException {
    model.addAttribute("activeLanguage", localeService.getDefaultLanguage());
    return "articles/create";
  }

  @GetMapping("/api/articles/new")
  @ResponseBody
  public Article create() {
    return service.create();
  }

  @GetMapping("/articles/{uuid}/edit")
  public String edit(
      @PathVariable UUID uuid,
      @RequestParam(name = "activeLanguage", required = false) Locale activeLanguage,
      Model model)
      throws TechnicalException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    Article article = service.getByUuid(uuid);
    List<Locale> existingLanguages =
        languageSortingHelper.sortLanguages(displayLocale, article.getLabel().getLocales());

    if (activeLanguage != null && existingLanguages.contains(activeLanguage)) {
      model.addAttribute("activeLanguage", activeLanguage);
    } else {
      model.addAttribute("activeLanguage", existingLanguages.get(0));
    }
    model.addAttribute("existingLanguages", existingLanguages);
    model.addAttribute("uuid", article.getUuid());

    return "articles/edit";
  }

  @GetMapping("/api/articles")
  @ResponseBody
  public PageResponse<Article> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "searchTerm", required = false) String searchTerm,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy)
      throws TechnicalException {
    SearchPageRequest searchPageRequest = new SearchPageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      searchPageRequest.setSorting(sorting);
    }
    return this.service.find(searchPageRequest);
  }

  @GetMapping("/api/articles/{uuid}")
  @ResponseBody
  public Article getByUuid(@PathVariable UUID uuid) throws TechnicalException {
    return service.getByUuid(uuid);
  }

  @GetMapping("/articles")
  public String list(Model model) throws TechnicalException {
    final Locale locale = LocaleContextHolder.getLocale();
    model.addAttribute(
        "existingLanguages",
        this.languageSortingHelper.sortLanguages(locale, this.service.findLanguages()));
    return "articles/list";
  }

  @ModelAttribute("menu")
  protected String module() {
    return "articles";
  }

  @PostMapping("/api/articles")
  public ResponseEntity save(@RequestBody Article article) {
    try {
      Article articleDb = service.save(article);
      return ResponseEntity.status(HttpStatus.CREATED).body(articleDb);
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save article: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @PutMapping("/api/articles/{uuid}")
  public ResponseEntity update(@PathVariable UUID uuid, @RequestBody Article article) {
    try {
      Article articleDb = service.update(uuid, article);
      return ResponseEntity.ok(articleDb);
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save article with uuid={}", uuid, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @GetMapping("/articles/{uuid}")
  public String view(@PathVariable UUID uuid, Model model)
      throws TechnicalException, ResourceNotFoundException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    Article article = service.getByUuid(uuid);
    if (article == null) {
      throw new ResourceNotFoundException();
    }
    List<Locale> existingLanguages =
        languageSortingHelper.sortLanguages(displayLocale, article.getLabel().getLocales());
    List<FileResource> relatedFileResources = service.findRelatedFileResources(article.getUuid());
    model
        .addAttribute("article", article)
        .addAttribute("existingLanguages", existingLanguages)
        .addAttribute("relatedFileResources", relatedFileResources);
    return "articles/view";
  }
}
