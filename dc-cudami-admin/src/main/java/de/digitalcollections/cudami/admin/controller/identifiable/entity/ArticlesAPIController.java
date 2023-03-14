package de.digitalcollections.cudami.admin.controller.identifiable.entity;

import de.digitalcollections.cudami.admin.business.i18n.LanguageService;
import de.digitalcollections.cudami.admin.controller.ParameterHelper;
import de.digitalcollections.cudami.admin.model.bootstraptable.BTResponse;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiArticlesClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.Article;
import de.digitalcollections.model.list.paging.PageResponse;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/** Controller for all public "Articles" endpoints (API). */
@RestController
public class ArticlesAPIController
    extends AbstractEntitiesController<Article, CudamiArticlesClient> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ArticlesAPIController.class);

  public ArticlesAPIController(CudamiClient client, LanguageService languageService) {
    super(client.forArticles(), languageService, client.forLocales());
  }

  @GetMapping("/api/articles/new")
  @ResponseBody
  public Article create() throws TechnicalException {
    return service.create();
  }

  @SuppressFBWarnings
  @GetMapping("/api/articles")
  @ResponseBody
  public BTResponse<Article> find(
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(name = "limit", required = false, defaultValue = "1") int limit,
      @RequestParam(name = "search", required = false) String searchTerm,
      @RequestParam(name = "sort", required = false, defaultValue = "label") String sort,
      @RequestParam(name = "order", required = false, defaultValue = "asc") String order,
      @RequestParam(name = "dataLanguage", required = false) String dataLanguage)
      throws TechnicalException {

    PageResponse<Article> pageResponse =
        super.find(localeService, service, offset, limit, searchTerm, sort, order, dataLanguage);
    return new BTResponse<>(pageResponse);
  }

  @GetMapping("/api/articles/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  @ResponseBody
  public Article getByUuid(@PathVariable UUID uuid) throws TechnicalException {
    return service.getByUuid(uuid);
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

  @PutMapping("/api/articles/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  public ResponseEntity update(@PathVariable UUID uuid, @RequestBody Article article) {
    try {
      Article articleDb = service.update(uuid, article);
      return ResponseEntity.ok(articleDb);
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save article with uuid={}", uuid, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }
}
