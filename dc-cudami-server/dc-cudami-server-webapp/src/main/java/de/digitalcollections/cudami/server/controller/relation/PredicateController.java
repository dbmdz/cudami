package de.digitalcollections.cudami.server.controller.relation;

import de.digitalcollections.cudami.server.business.api.service.UniqueObjectService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.relation.PredicateService;
import de.digitalcollections.cudami.server.controller.AbstractUniqueObjectController;
import de.digitalcollections.cudami.server.controller.ParameterHelper;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.relation.Predicate;
import de.digitalcollections.model.validation.ValidationError;
import de.digitalcollections.model.validation.ValidationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Predicate controller")
public class PredicateController extends AbstractUniqueObjectController<Predicate> {

  private final PredicateService service;

  public PredicateController(PredicateService predicateService) {
    this.service = predicateService;
  }

  @Override
  @DeleteMapping(value = {"/v6/predicates/{uuid:" + ParameterHelper.UUID_PATTERN + "}"})
  public ResponseEntity delete(
      @Parameter(
              example = "",
              description =
                  "UUID of the predicate, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid)
      throws ConflictException, ServiceException {
    return super.delete(uuid);
  }

  @Override
  @Operation(summary = "Get all predicates as (paged, sorted, filtered) list")
  @GetMapping(
      value = {"/v6/predicates"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Predicate> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "filter", required = false) List<FilterCriterion> filterCriteria,
      @RequestParam(name = "filtering", required = false) Filtering filtering)
      throws ServiceException {
    return super.find(pageNumber, pageSize, sortBy, filterCriteria, filtering);
  }

  @Operation(summary = "Get a predicate by its value or UUID")
  @GetMapping(
      value = {"/v6/predicates/{valueOrUuid:.+}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Predicate> getByValueOrUUID(@PathVariable("valueOrUuid") String valueOrUuid)
      throws ServiceException {
    Predicate result;
    if (valueOrUuid.matches(ParameterHelper.UUID_PATTERN)) {
      UUID uuid = UUID.fromString(valueOrUuid);
      result = service.getByExample(buildExampleWithUuid(uuid));
    } else {
      result = service.getByValue(valueOrUuid);
    }
    return new ResponseEntity<>(result, result != null ? HttpStatus.OK : HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Get languages of all predicates")
  @GetMapping(
      value = {"/v6/predicates/languages"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Locale> getLanguages() throws ServiceException {
    return service.getLanguages();
  }

  @Override
  protected UniqueObjectService<Predicate> getService() {
    return service;
  }

  @Operation(summary = "Save a newly created predicate")
  @PostMapping(
      value = {"/v6/predicates", "/v5/predicates", "/v3/predicates", "/latest/predicates"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Predicate saveWithValidation(
      @Valid @RequestBody Predicate predicate, BindingResult bindingResult)
      throws ServiceException, ValidationException {
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
    return super.save(predicate, bindingResult);
  }

  /*
   * Since we cannot use .* als "fallback" mapping (Spring reports
   * "ambigious handler methods"), we must evaluate the parameter manually
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
      throws ServiceException, ValidationException {

    if (valueOrUuid.matches(ParameterHelper.UUID_PATTERN)) {
      UUID uuid = UUID.fromString(valueOrUuid);
      if (!predicate.getUuid().equals(uuid)) {
        throw new IllegalArgumentException(
            "path value of uuid="
                + uuid
                + " does not match uuid of predicate="
                + predicate.getUuid());
      }
      service.update(predicate);
      return predicate;
    }

    String value = valueOrUuid;
    if (!value.matches(predicate.getValue())) {
      throw new IllegalArgumentException(
          "value of path=" + value + " does not match value of predicate=" + predicate.getValue());
    }

    service.saveOrUpdate(predicate);
    return predicate;
  }
}
