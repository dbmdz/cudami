package de.digitalcollections.cudami.server.controller;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.relation.PredicateService;
import de.digitalcollections.model.relation.Predicate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.stream.Collectors;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Configuration controller")
public class ConfigController {

  private CudamiConfig cudamiConfig;

  private PredicateService predicateService;

  public ConfigController(CudamiConfig cudamiConfig, PredicateService predicateService) {
    this.cudamiConfig = cudamiConfig;
    this.predicateService = predicateService;
  }

  @Operation(summary = "Get cudami configuration")
  @GetMapping(
      value = {"/v6/config", "/v5/config"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public CudamiConfig getCudamiConfig() throws ServiceException {
    cudamiConfig
        .getTypeDeclarations()
        .setRelationPredicates(
            predicateService.getAll().stream()
                .map(Predicate::getValue)
                .collect(Collectors.toList()));
    return cudamiConfig;
  }
}
