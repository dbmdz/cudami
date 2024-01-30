package de.digitalcollections.cudami.server.controller.identifiable.versioning;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.versioning.VersionService;
import de.digitalcollections.cudami.server.controller.ParameterHelper;
import de.digitalcollections.model.identifiable.versioning.Version;
import de.digitalcollections.model.validation.ValidationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Version controller")
public class VersionController {

  private final VersionService service;

  public VersionController(VersionService versionService) {
    this.service = versionService;
  }

  @Operation(summary = "Get version by uuid")
  @GetMapping(
      value = {
        "/v6/versions/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/versions/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v2/versions/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/latest/versions/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Version> getByUuid(@PathVariable UUID uuid) {
    Version version = service.getByUuid(uuid);
    return new ResponseEntity<>(version, version != null ? HttpStatus.OK : HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Update the version status")
  @PutMapping(
      value = {
        "/v6/versions/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/versions/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v2/versions/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/latest/versions/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Version update(@PathVariable UUID uuid, @RequestBody Version version, BindingResult errors)
      throws ValidationException, ServiceException {
    service.update(version);
    return version;
  }
}
