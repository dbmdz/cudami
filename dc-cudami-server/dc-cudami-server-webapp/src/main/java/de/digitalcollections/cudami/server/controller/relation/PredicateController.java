package de.digitalcollections.cudami.server.controller.relation;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.PredicatesServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.relation.PredicateService;
import de.digitalcollections.cudami.server.controller.ParameterHelper;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import de.digitalcollections.model.relation.Predicate;
import de.digitalcollections.model.validation.ValidationError;
import de.digitalcollections.model.validation.ValidationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Predicate controller")
public class PredicateController {

  private final PredicateService predicateService;

  public PredicateController(PredicateService predicateService) {
    this.predicateService = predicateService;
  }

  @Operation(summary = "Get all predicates as (sorted, paged) list")
  @GetMapping(
      value = {"/v6/predicates"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Predicate> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "searchTerm", required = false) String searchTerm,
      @RequestParam(name = "label", required = false) String labelTerm,
      @RequestParam(name = "labelLanguage", required = false) Locale labelLanguage) {
    //    return predicateService.find(pageNumber, pageSize, sortBy, searchTerm, labelTerm,
    // labelLanguage);

    PageRequest pageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }

    //    if (filterCriteria.length > 0 && filterCriteria.length % 2 == 0) {
    //      // Since Map.of doesn't support null values we try it this varargs-way.
    //      // There must be two entries per criterion so the array's length must be even.
    //      for (int i = 0; i < filterCriteria.length; i += 2) {
    //        if (filterCriteria[i + 1] == null) {
    //          continue;
    //        }
    //        if (!(filterCriteria[i] instanceof String)
    //            || !(filterCriteria[i + 1] instanceof FilterCriterion)) {
    //          throw new IllegalArgumentException(
    //              "`filterCriteria` must be pairs of a `String`, the expression, and the
    // corresponding `FilterCriterion`");
    //        }
    //        String expression = (String) filterCriteria[i];
    //        FilterCriterion<?> criterion = (FilterCriterion<?>) filterCriteria[i + 1];
    //        criterion.setExpression(expression);
    //        pageRequest.add(new Filtering(List.of(criterion)));
    //      }
    //    }
    //
    //    addLabelFilter(pageRequest, labelTerm, labelLanguage);
    return predicateService.find(pageRequest);
  }

  @GetMapping(value = {"/v6/predicates/all"})
  public List<Predicate> getAll() {
    return predicateService.getAll();
  }

  @Operation(summary = "Get a predicate by uuid")
  @GetMapping(
      value = {"/v6/predicates/{uuid:" + ParameterHelper.UUID_PATTERN + "}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Predicate getByUuid(@PathVariable UUID uuid) {
    return predicateService.getByUuid(uuid);
  }

  @Operation(summary = "Get a predicate by its value")
  @GetMapping(
      value = {"/v6/predicates/{value}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Predicate getByValue(@PathVariable("value") String value) {
    return predicateService.getByValue(value);
  }

  @Operation(summary = "Get languages of all predicates")
  @GetMapping(
      value = {"/v6/predicates/languages"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Locale> getLanguages() {
    return predicateService.getLanguages();
  }

  @Operation(summary = "Save a newly created predicate")
  @PostMapping(
      value = {"/v6/predicates", "/v5/predicates", "/v3/predicates", "/latest/predicates"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Predicate save(@Valid @RequestBody Predicate predicate, BindingResult bindingResult)
      throws IdentifiableServiceException, ServiceException, ValidationException {
    if (bindingResult.hasErrors()) {
      ValidationException validationException = new ValidationException("validation error");
      bindingResult
          .getAllErrors()
          .forEach(
              (error) -> {
                String fieldName = ((FieldError) error).getField();
                String errorMessage = error.getDefaultMessage();
                validationException.addError(new ValidationError(fieldName, errorMessage));
              });
      throw validationException;
    }
    return predicateService.save(predicate);
  }

  /*
  Since we cannot use .* als "fallback" mapping (Spring reports "ambigious handler methods"), we
  must evaluate the parameter manually
   */
  @Operation(summary = "create or update a predicate, identified either by its value or by uuid")
  @PutMapping(
      value = {
        "/v6/predicates/{valueOrUuid:.+}",
        "/v5/predicates/{valueOrUuid:.+}",
        "/v3/predicates/{valueOrUuid:.+}",
        "/latest/predicates/{valueOrUuid:.+}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Predicate update(
      @PathVariable("valueOrUuid") String valueOrUuid, @NotNull @RequestBody Predicate predicate)
      throws PredicatesServiceException {

    if (valueOrUuid.matches(ParameterHelper.UUID_PATTERN)) {
      UUID uuid = UUID.fromString(valueOrUuid);
      if (!predicate.getUuid().equals(uuid)) {
        throw new IllegalArgumentException(
            "path value of uuid="
                + uuid
                + " does not match uuid of predicate="
                + predicate.getUuid());
      }
      return predicateService.save(predicate);
    }

    String value = valueOrUuid;
    if (!value.matches(predicate.getValue())) {
      throw new IllegalArgumentException(
          "value of path=" + value + " does not match value of predicate=" + predicate.getValue());
    }

    return predicateService.save(predicate);
  }
}
