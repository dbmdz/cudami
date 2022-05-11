package de.digitalcollections.cudami.server.controller.identifiable.versioning;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.versioning.VersionService;
import de.digitalcollections.model.identifiable.versioning.Version;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Version controller")
public class VersionController {

  private final VersionService versionService;

  public VersionController(VersionService versionService) {
    this.versionService = versionService;
  }

  @Operation(summary = "Get version by uuid")
  @GetMapping(
      value = {
        "/v6/versions/{uuid}",
        "/v5/versions/{uuid}",
        "/v2/versions/{uuid}",
        "/latest/versions/{uuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Version getByUuid(@PathVariable UUID uuid) {
    return versionService.getByUuid(uuid);
  }

  @Operation(summary = "Update the version status")
  @PutMapping(
      value = {
        "/v6/versions/{uuid}",
        "/v5/versions/{uuid}",
        "/v2/versions/{uuid}",
        "/latest/versions/{uuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Version update(@PathVariable UUID uuid, @RequestBody Version version, BindingResult errors)
      throws ValidationException {
    return versionService.update(version);
  }
}
