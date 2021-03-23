package de.digitalcollections.cudami.server.controller.identifiable.versioning;

import de.digitalcollections.cudami.server.business.api.service.identifiable.versioning.VersionService;
import de.digitalcollections.model.identifiable.versioning.Version;
import java.util.UUID;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiResponseObject;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(description = "The version controller", name = "Version controller")
public class VersionController {

  private final VersionService versionService;

  public VersionController(VersionService versionService) {
    this.versionService = versionService;
  }

  @ApiMethod(description = "Get version by uuid")
  @GetMapping(
      value = {"/latest/versions/{uuid}", "/v2/versions/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public Version findById(@PathVariable UUID uuid) {
    return versionService.get(uuid);
  }

  @ApiMethod(description = "Update the version status")
  @PutMapping(
      value = {"/latest/versions/{uuid}", "/v2/versions/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public Version update(@PathVariable UUID uuid, @RequestBody Version version, BindingResult errors)
      throws Exception {
    return versionService.update(version);
  }
}
