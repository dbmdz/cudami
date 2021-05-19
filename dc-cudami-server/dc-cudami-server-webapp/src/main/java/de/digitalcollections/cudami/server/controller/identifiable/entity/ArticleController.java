package de.digitalcollections.cudami.server.controller.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.ArticleService;
import de.digitalcollections.model.identifiable.entity.Article;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.Sorting;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiPathParam;
import org.jsondoc.core.annotation.ApiQueryParam;
import org.jsondoc.core.annotation.ApiResponseObject;
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
@Api(description = "The article controller", name = "Article controller")
public class ArticleController {

  private final ArticleService articleService;

  public ArticleController(ArticleService articleService) {
    this.articleService = articleService;
  }

  @ApiMethod(description = "Get count of articles")
  @GetMapping(
      value = {"/latest/articles/count", "/v2/articles/count"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public long count() {
    return articleService.count();
  }

  @ApiMethod(description = "Get all articles")
  @GetMapping(
      value = {"/latest/articles", "/v2/articles"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public PageResponse<Article> findAll(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "searchTerm", required = false) String searchTerm) {
    SearchPageRequest pageRequest = new SearchPageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    return articleService.find(pageRequest);
  }

  // Test-URL: http://localhost:9000/latest/articles/599a120c-2dd5-11e8-b467-0ed5f89f718b
  @ApiMethod(
      description =
          "Get an article as JSON or XML, depending on extension or <tt>format</tt> request parameter or accept header")
  @GetMapping(
      value = {"/latest/articles/{uuid}", "/v2/articles/{uuid}"},
      produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ApiResponseObject
  public ResponseEntity<Article> getArticle(
      @ApiPathParam(
              description =
                  "UUID of the article, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @ApiQueryParam(
              name = "pLocale",
              description =
                  "Desired locale, e.g. <tt>de_DE</tt>. If unset, contents in all languages will be returned")
          @RequestParam(name = "pLocale", required = false)
          Locale pLocale)
      throws IdentifiableServiceException {

    Article article;
    if (pLocale == null) {
      article = articleService.get(uuid);
    } else {
      article = articleService.get(uuid, pLocale);
    }
    return new ResponseEntity<>(article, HttpStatus.OK);
  }

  @ApiMethod(description = "Get languages of all articles")
  @GetMapping(
      value = {"/latest/articles/languages", "/v2/articles/languages"},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @ApiResponseObject
  public List<Locale> getLanguages() {
    return this.articleService.getLanguages();
  }

  @ApiMethod(description = "Save a newly created article")
  @PostMapping(
      value = {"/latest/articles", "/v2/articles"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public Article save(@RequestBody Article article, BindingResult errors)
      throws IdentifiableServiceException {
    return articleService.save(article);
  }

  @ApiMethod(description = "Update an article")
  @PutMapping(
      value = {"/latest/articles/{uuid}", "/v2/articles/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public Article update(@PathVariable UUID uuid, @RequestBody Article article, BindingResult errors)
      throws IdentifiableServiceException {
    assert Objects.equals(uuid, article.getUuid());
    return articleService.update(article);
  }
}
