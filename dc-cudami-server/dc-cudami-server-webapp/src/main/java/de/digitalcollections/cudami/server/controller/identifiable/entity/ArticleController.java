package de.digitalcollections.cudami.server.controller.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.ArticleService;
import de.digitalcollections.model.api.identifiable.Identifiable;
import de.digitalcollections.model.api.identifiable.entity.Article;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.Sorting;
import de.digitalcollections.model.api.paging.enums.Direction;
import de.digitalcollections.model.api.paging.enums.NullHandling;
import de.digitalcollections.model.impl.paging.OrderImpl;
import de.digitalcollections.model.impl.paging.PageRequestImpl;
import de.digitalcollections.model.impl.paging.SortingImpl;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiPathParam;
import org.jsondoc.core.annotation.ApiQueryParam;
import org.jsondoc.core.annotation.ApiResponseObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(description = "The article controller", name = "article controller")
public class ArticleController {

  @Autowired
  private ArticleService<Article, Identifiable> articleService;

  @ApiMethod(description = "get all articles")
  @RequestMapping(value = "/v1/articles", produces = "application/json", method = RequestMethod.GET)
  @ApiResponseObject
  public PageResponse<Article> findAll(
          @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
          @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
          @RequestParam(name = "sortField", required = false, defaultValue = "uuid") String sortField,
          @RequestParam(name = "sortDirection", required = false, defaultValue = "ASC") Direction sortDirection,
          @RequestParam(name = "nullHandling", required = false, defaultValue = "NATIVE") NullHandling nullHandling
  ) {
    // FIXME add support for multiple sorting orders
    OrderImpl order = new OrderImpl(sortDirection, sortField, nullHandling);
    Sorting sorting = new SortingImpl(order);
    PageRequest pageRequest = new PageRequestImpl(pageNumber, pageSize, sorting);
    return articleService.find(pageRequest);
  }

  // Test-URL: http://localhost:9000/v1/articles/599a120c-2dd5-11e8-b467-0ed5f89f718b
  @ApiMethod(description = "get an article as JSON or XML, depending on extension or <tt>format</tt> request parameter or accept header")
  @RequestMapping(value = {"/v1/articles/{uuid}"}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, method = RequestMethod.GET)
  @ApiResponseObject
  public ResponseEntity<Article> getWebpage(
          @ApiPathParam(description = "UUID of the article, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>") @PathVariable("uuid") UUID uuid,
          @ApiQueryParam(name = "pLocale", description = "Desired locale, e.g. <tt>de_DE</tt>. If unset, contents in all languages will be returned")
          @RequestParam(name = "pLocale", required = false) Locale pLocale
  ) throws IdentifiableServiceException {

    Article article;
    if (pLocale == null) {
      article = articleService.get(uuid);
    } else {
      article = articleService.get(uuid, pLocale);
    }
    return new ResponseEntity<>(article, HttpStatus.OK);
  }

  @ApiMethod(description = "save a newly created article")
  @RequestMapping(value = "/v1/articles", produces = "application/json", method = RequestMethod.POST)
  @ApiResponseObject
  public Article save(@RequestBody Article article, BindingResult errors) throws IdentifiableServiceException {
    return articleService.save(article);
  }

  @ApiMethod(description = "update an article")
  @RequestMapping(value = "/v1/articles/{uuid}", produces = "application/json", method = RequestMethod.PUT)
  @ApiResponseObject
  public Article update(@PathVariable UUID uuid, @RequestBody Article article, BindingResult errors) throws IdentifiableServiceException {
    assert Objects.equals(uuid, article.getUuid());
    return articleService.update(article);
  }

  @RequestMapping(value = "/v1/articles/{uuid}/identifiables", produces = "application/json", method = RequestMethod.GET)
  public List<Identifiable> getIdentifiables(@PathVariable UUID uuid) {
    return articleService.getIdentifiables(uuid);
  }
  
  // TODO: saveIdentifiables
}
