package de.digitalcollections.cudami.server.controller.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.ArticleService;
import de.digitalcollections.cudami.server.controller.ParameterHelper;
import de.digitalcollections.cudami.server.controller.identifiable.AbstractIdentifiableController;
import de.digitalcollections.model.identifiable.entity.Article;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.validation.ValidationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Article controller")
public class ArticleController extends AbstractIdentifiableController<Article> {

  private final ArticleService service;

  public ArticleController(ArticleService articleService) {
    this.service = articleService;
  }

  @Operation(summary = "Add existing agents as creators to an existing article")
  @PostMapping(
      value = {"/v6/articles/{uuid:" + ParameterHelper.UUID_PATTERN + "}/creators"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity addCreators(
      @Parameter(example = "", description = "UUID of the article") @PathVariable("uuid")
          UUID articleUuid,
      @Parameter(example = "", description = "List of the creators to add") @RequestBody
          List<Agent> creators)
      throws ServiceException {
    Article article = new Article();
    article.setUuid(articleUuid);

    boolean successful = service.addCreators(article, creators);
    return successful
        ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
        : new ResponseEntity<>(HttpStatus.NOT_FOUND);
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
  public long count() throws ServiceException {
    return super.count();
  }

  @Operation(summary = "Get all articles as (paged, sorted, filtered) list")
  @GetMapping(
      value = {"/v6/articles"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Article> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "filter", required = false) List<FilterCriterion> filterCriteria,
      @RequestParam(name = "filtering", required = false) Filtering filtering)
      throws ServiceException {
    return super.find(pageNumber, pageSize, sortBy, filterCriteria, filtering);
  }

  @Operation(summary = "Get an article")
  @GetMapping(
      value = {
        "/v6/articles/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/articles/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v2/articles/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/latest/articles/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
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
    if (pLocale == null) {
      return super.getByUuid(uuid);
    } else {
      return super.getByUuidAndLocale(uuid, pLocale);
    }
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
  public List<Locale> getLanguages() throws ServiceException {
    return super.getLanguages();
  }

  @Override
  protected IdentifiableService<Article> getService() {
    return service;
  }

  @Operation(summary = "Remove an existing creator from an existing article")
  @DeleteMapping(
      value = {"/v6/articles/{uuid:" + ParameterHelper.UUID_PATTERN + "}/creators/{agentUuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity removeCreator(
      @Parameter(example = "", description = "UUID of the article") @PathVariable("uuid")
          UUID articleUuid,
      @Parameter(example = "", description = "UUID of the creator/agent") @PathVariable("agentUuid")
          UUID agentUuid)
      throws ServiceException {
    Article article = new Article();
    article.setUuid(articleUuid);

    Agent agent = new Agent();
    agent.setUuid(agentUuid);

    boolean successful = service.removeCreator(article, agent);
    return successful
        ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
        : new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Save a newly created article")
  @PostMapping(
      value = {"/v6/articles", "/v5/articles", "/v2/articles", "/latest/articles"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Article save(@RequestBody Article article, BindingResult errors)
      throws ServiceException, ValidationException {
    return super.save(article, errors);
  }

  @Operation(summary = "Update an article")
  @PutMapping(
      value = {
        "/v6/articles/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/articles/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v2/articles/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/latest/articles/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Article update(@PathVariable UUID uuid, @RequestBody Article article, BindingResult errors)
      throws ServiceException, ValidationException {
    return super.update(uuid, article, errors);
  }
}
