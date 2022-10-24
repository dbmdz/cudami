package de.digitalcollections.cudami.server.controller.relation;

import de.digitalcollections.cudami.server.business.api.service.exceptions.PredicatesServiceException;
import de.digitalcollections.cudami.server.business.api.service.relation.PredicateService;
import de.digitalcollections.model.relation.Predicate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Predicate controller")
public class PredicateController {

  private final PredicateService predicateService;
  private static final String REGEX_UUID =
      "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

  public PredicateController(PredicateService predicateService) {
    this.predicateService = predicateService;
  }

  @Operation(summary = "Get all predicates")
  @GetMapping(value = {"/v6/predicates", "/v5/predicates", "/v3/predicates", "/latest/predicates"})
  public List<Predicate> getAll() {
    return predicateService.getAll();
  }

  @Operation(summary = "Get a predicate by its value")
  @GetMapping(
      value = {"/v6/predicates/{value}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Predicate getByValue(@PathVariable("value") String value) {
    return predicateService.getByValue(value);
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

    if (valueOrUuid.matches(REGEX_UUID)) {
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

  @Operation(summary = "saves a predicate")
  @PostMapping(
      value = {"/v6/predicates", "/v5/predicates", "/v3/predicates", "/latest/predicates"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Predicate save(@RequestBody Predicate predicate) throws PredicatesServiceException {
    if (predicate == null || predicate.getValue() == null) {
      throw new IllegalArgumentException("Invalid predicate: " + predicate);
    }

    return predicateService.save(predicate);
  }
}
