package de.digitalcollections.cudami.admin.controller.relation;

import static de.digitalcollections.model.list.sorting.Order.builder;

import de.digitalcollections.cudami.admin.model.bootstraptable.BTResponse;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.relation.CudamiPredicatesClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import de.digitalcollections.model.relation.Predicate;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/** Controller for predicate management pages. */
@Controller
public class PredicatesAPIController {

  private static final Logger LOGGER = LoggerFactory.getLogger(PredicatesAPIController.class);

  private final CudamiPredicatesClient service;

  public PredicatesAPIController(CudamiClient client) {
    this.service = client.forPredicates();
  }

  @GetMapping("/api/predicates/new")
  @ResponseBody
  public Predicate createModel() throws TechnicalException {
    return service.create();
  }

  @SuppressFBWarnings
  @GetMapping("/api/predicates")
  @ResponseBody
  public BTResponse<Predicate> find(
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(name = "limit", required = false, defaultValue = "1") int limit,
      @RequestParam(name = "search", required = false) String searchTerm,
      @RequestParam(name = "sort", required = false, defaultValue = "value") String sort,
      @RequestParam(name = "order", required = false, defaultValue = "asc") String order,
      HttpServletRequest request)
      throws TechnicalException {
    Map<String, String[]> parameterMap = request.getParameterMap();

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

    PageResponse<Predicate> pageResponse = service.find(pageRequest);
    return new BTResponse<>(pageResponse);
  }

  @GetMapping("/api/predicates/{uuid}")
  @ResponseBody
  public Predicate getByUuid(@PathVariable UUID uuid) throws TechnicalException {
    return service.getByUuid(uuid);
  }

  @PostMapping("/api/predicates")
  public ResponseEntity save(@RequestBody Predicate predicate) {
    try {
      Predicate predicateDB = service.save(predicate);
      return ResponseEntity.status(HttpStatus.CREATED).body(predicateDB);
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save predicate: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @PutMapping("/api/predicates/{uuid}")
  public ResponseEntity update(@PathVariable UUID uuid, @RequestBody Predicate predicate) {
    try {
      Predicate predicateDB = service.update(uuid, predicate);
      return ResponseEntity.ok(predicateDB);
    } catch (TechnicalException e) {
      LOGGER.error("Cannot update predicate with uuid={}", uuid, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }
}
