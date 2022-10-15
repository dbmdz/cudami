package de.digitalcollections.cudami.admin.controller.identifiable.entity;

import static de.digitalcollections.model.list.sorting.Order.builder;

import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.model.bootstraptable.BTResponse;
import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.CudamiLocalesClient;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiArticlesClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.Article;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
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

/** Controller for articles management pages. */
@RestController
public class ArticlesAPIController extends AbstractController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ArticlesAPIController.class);

  private final LanguageSortingHelper languageSortingHelper;
  private final CudamiLocalesClient localeService;
  private final CudamiArticlesClient service;

  public ArticlesAPIController(LanguageSortingHelper languageSortingHelper, CudamiClient client) {
    this.languageSortingHelper = languageSortingHelper;
    this.localeService = client.forLocales();
    this.service = client.forArticles();
  }

  @GetMapping("/api/articles/new")
  @ResponseBody
  public Article create() throws TechnicalException {
    return service.create();
  }

  //  @GetMapping("/api/articles")
  //  @ResponseBody
  //  public PageResponse<Article> find(
  //          @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int
  // pageNumber,
  //          @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
  //          @RequestParam(name = "searchTerm", required = false) String searchTerm,
  //          @RequestParam(name = "sortBy", required = false) List<Order> sortBy)
  //          throws TechnicalException {
  //    PageRequest pageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
  //    if (sortBy != null) {
  //      Sorting sorting = new Sorting(sortBy);
  //      pageRequest.setSorting(sorting);
  //    }
  //    return this.service.find(pageRequest);
  //  }

  @SuppressFBWarnings
  @GetMapping("/api/articles")
  @ResponseBody
  public BTResponse<Article> find(
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(name = "limit", required = false, defaultValue = "1") int limit,
      @RequestParam(name = "search", required = false) String searchTerm,
      @RequestParam(name = "sort", required = false, defaultValue = "value") String sort,
      @RequestParam(name = "order", required = false, defaultValue = "asc") String order,
      HttpServletRequest request)
      throws TechnicalException {
    Map<String, String[]> parameterMap =
        request.getParameterMap(); // just for introspection of incoming request....

    Sorting sorting = null;
    if (sort != null && order != null) {
      Order sortingOrder = builder().property(sort).direction(Direction.fromString(order)).build();
      sorting = Sorting.builder().order(sortingOrder).build();
    }

    PageRequest pageRequest =
        PageRequest.builder()
            .pageNumber((int) Math.ceil(offset / limit))
            .pageSize(limit)
            .searchTerm(searchTerm)
            .sorting(sorting)
            .build();

    PageResponse<Article> pageResponse = service.find(pageRequest);
    return new BTResponse<>(pageResponse);
  }

  @GetMapping("/api/articles/{uuid}")
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
}
