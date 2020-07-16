package de.digitalcollections.cudami.admin.controller.identifiable.entity;

import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.backend.api.repository.LocaleRepository;
import de.digitalcollections.cudami.admin.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.cudami.client.CudamiArticlesClient;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.model.api.identifiable.entity.Article;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

/** Controller for articles management pages. */
@Controller
public class ArticlesController extends AbstractController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ArticlesController.class);

  private final LanguageSortingHelper languageSortingHelper;
  private final LocaleRepository localeRepository;
  private final CudamiArticlesClient client;

  @Autowired
  public ArticlesController(
      LanguageSortingHelper languageSortingHelper,
      LocaleRepository localeRepository,
      CudamiClient client) {
    this.languageSortingHelper = languageSortingHelper;
    this.localeRepository = localeRepository;
    this.client = client.forArticles();
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
    return client.create();
  }

  @GetMapping("/articles/{uuid}/edit")
  public String edit(@PathVariable UUID uuid, Model model) {
    //    final Locale displayLocale = LocaleContextHolder.getLocale();
    // FIXME
    //    Article article = client.findOne(uuid);
    //    List<Locale> existingLanguages =
    //        languageSortingHelper.sortLanguages(displayLocale, article.getLabel().getLocales());
    //
    //    model.addAttribute("activeLanguage", existingLanguages.get(0));
    //    model.addAttribute("existingLanguages", existingLanguages);
    //    model.addAttribute("uuid", article.getUuid());

    return "articles/edit";
  }

  @GetMapping("/api/articles/{uuid}")
  @ResponseBody
  public Article get(@PathVariable UUID uuid) {
    // FIXME
    return null;
    //    return client.findOne(uuid);
  }

  @GetMapping("/articles")
  public String list(
      Model model,
      @PageableDefault(
              sort = {"lastModified"},
              size = 25)
          Pageable pageable) {
    //    final PageRequest pageRequest = PageableConverter.convert(pageable);
    // FIXME
    //    final PageResponse pageResponse = client.find(pageRequest);
    //    Page page = PageConverter.convert(pageResponse, pageRequest);
    //    model.addAttribute("page", new PageWrapper(page, "/articles"));
    return "articles/list";
  }

  @PostMapping("/api/articles/new")
  public ResponseEntity save(@RequestBody Article article) throws IdentifiableServiceException {
    try {
      // FIXME
      Article articleDb = null;
      //      Article articleDb = client.save(article);
      return ResponseEntity.status(HttpStatus.CREATED).body(articleDb);
    } catch (Exception e) {
      LOGGER.error("Cannot save article: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @PutMapping("/api/articles/{uuid}")
  public ResponseEntity update(@PathVariable UUID uuid, @RequestBody Article article)
      throws IdentifiableServiceException {
    // FIXME
    return null;
    //    try {
    //      Article articleDb = client.update(uuid, article);
    //      return ResponseEntity.ok(articleDb);
    //    } catch (Exception e) {
    //      LOGGER.error("Cannot save article with uuid={}", uuid, e);
    //      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    //    }
  }

  @GetMapping("/articles/{uuid}")
  public String view(@PathVariable UUID uuid, Model model) {
    //    final Locale displayLocale = LocaleContextHolder.getLocale();
    // FIXME
    //    Article article = (Article) client.findOne(uuid);
    //    List<Locale> existingLanguages =
    //        languageSortingHelper.sortLanguages(displayLocale, article.getLabel().getLocales());
    //
    //    model.addAttribute("article", article);
    //    model.addAttribute("existingLanguages", existingLanguages);
    //
    //    List<FileResource> relatedFileResources =
    // client.getRelatedFileResources(article.getUuid());
    //    model.addAttribute("relatedFileResources", relatedFileResources);

    return "articles/view";
  }
}
