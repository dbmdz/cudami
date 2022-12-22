package de.digitalcollections.cudami.server.controller.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.ArticleService;
import de.digitalcollections.model.identifiable.entity.Article;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Article controller")
public class ArticleController {

  private final ArticleService articleService;

  public ArticleController(ArticleService articleService) {
    this.articleService = articleService;
  }

  @Operation(summary = "Get count of articles")
  @GetMapping(
      value = {
        "/v6/articles/count",
        "/v5/articles/count",
        "/v2/articles/count",
        "/latest/articles/count"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public long count() {
    return articleService.count();
  }

  @Operation(summary = "Get all articles")
  @GetMapping(
      value = {"/v6/articles"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Article> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "searchTerm", required = false) String searchTerm) {
    PageRequest pageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    return articleService.find(pageRequest);
  }

  @Operation(summary = "Get an article")
  @GetMapping(
      value = {
        "/v6/articles/{uuid}",
        "/v5/articles/{uuid}",
        "/v2/articles/{uuid}",
        "/latest/articles/{uuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Article> getByUuid(
      @Parameter(
              example = "",
              description =
                  "UUID of the article, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @Parameter(
              name = "pLocale",
              description =
                  "Desired locale, e.g. <tt>de_DE</tt>. If unset, contents in all languages will be returned")
          @RequestParam(name = "pLocale", required = false)
          Locale pLocale)
      throws ServiceException {

    Article article;
    if (pLocale == null) {
      article = articleService.getByUuid(uuid);
    } else {
      article = articleService.getByUuidAndLocale(uuid, pLocale);
    }
    return new ResponseEntity<>(article, article != null ? HttpStatus.OK : HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Get languages of all articles")
  @GetMapping(
      value = {
        "/v6/articles/languages",
        "/v5/articles/languages",
        "/v2/articles/languages",
        "/latest/articles/languages"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Locale> getLanguages() {
    return this.articleService.getLanguages();
  }

  @Operation(summary = "Save a newly created article")
  @PostMapping(
      value = {"/v6/articles", "/v5/articles", "/v2/articles", "/latest/articles"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Article save(@RequestBody Article article, BindingResult errors)
      throws ServiceException, ValidationException {
    articleService.save(article);
    return article;
  }

  @Operation(summary = "Update an article")
  @PutMapping(
      value = {
        "/v6/articles/{uuid}",
        "/v5/articles/{uuid}",
        "/v2/articles/{uuid}",
        "/latest/articles/{uuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Article update(@PathVariable UUID uuid, @RequestBody Article article, BindingResult errors)
      throws ServiceException, ValidationException {
    assert Objects.equals(uuid, article.getUuid());
    articleService.update(article);
    return article;
  }
}
