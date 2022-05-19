package de.digitalcollections.cudami.server.controller;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Configuration controller")
@ContextConfiguration(classes = {CudamiConfig.class})
public class ConfigController {

  private CudamiConfig cudamiConfig;

  public ConfigController(CudamiConfig cudamiConfig) {
    this.cudamiConfig = cudamiConfig;
  }

  @Operation(summary = "Get cudami configuration")
  @GetMapping(
      value = {"/v6/config", "/v5/config"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public CudamiConfig getCudamiConfig() {
    return cudamiConfig;
  }
}
