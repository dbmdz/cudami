package de.digitalcollections.cudami.server.controller.relation;

import de.digitalcollections.cudami.server.business.api.service.exceptions.PredicatesServiceException;
import de.digitalcollections.cudami.server.business.api.service.relation.PredicateService;
import de.digitalcollections.model.relation.Predicate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Predicates controller")
public class PredicateController {

  private final PredicateService predicateService;

  public PredicateController(PredicateService predicateService) {
    this.predicateService = predicateService;
  }

  @Operation(summary = "Get all predicates")
  @GetMapping(value = {"/v5/predicates", "/v3/predicates", "/latest/predicates"})
  public List<Predicate> getPredicates() {
    return predicateService.findAll();
  }

  @Operation(summary = "create or update a predicate, identified by its value")
  @PutMapping(
      value = {"/v5/predicates/{value}", "/v3/predicates/{value}", "/latest/predicates/{value}"},
      produces = "application/json")
  public Predicate update(
      @PathVariable("value") String value, @RequestBody Predicate predicate, BindingResult errors)
      throws PredicatesServiceException {
    if (value == null || predicate == null || !value.equals(predicate.getValue())) {
      throw new IllegalArgumentException("value of path does not match value of predicate");
    }

    return predicateService.save(predicate);
  }
}
