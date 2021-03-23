package de.digitalcollections.cudami.server.controller.relation;

import de.digitalcollections.cudami.server.business.api.service.exceptions.PredicatesServiceException;
import de.digitalcollections.cudami.server.business.api.service.relation.PredicateService;
import de.digitalcollections.model.relation.Predicate;
import java.util.List;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiResponseObject;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(description = "The predicates controller", name = "Predicates controller")
public class PredicateController {

  private final PredicateService predicateService;

  public PredicateController(PredicateService predicateService) {
    this.predicateService = predicateService;
  }

  @ApiMethod(description = "Get all predicates")
  @GetMapping(value = {"/latest/predicates", "/v3/predicates"})
  @ApiResponseObject
  public List<Predicate> getPredicates() {
    return predicateService.findAll();
  }

  @ApiMethod(description = "create or update a predicate, identified by its value")
  @PutMapping(
      value = {"/latest/predicates/{value}", "/v3/predicates/{value}"},
      produces = "application/json")
  @ApiResponseObject
  public Predicate update(
      @PathVariable("value") String value, @RequestBody Predicate predicate, BindingResult errors)
      throws PredicatesServiceException {
    if (value == null || predicate == null || !value.equals(predicate.getValue())) {
      throw new IllegalArgumentException("value of path does not match value of predicate");
    }

    return predicateService.save(predicate);
  }
}
