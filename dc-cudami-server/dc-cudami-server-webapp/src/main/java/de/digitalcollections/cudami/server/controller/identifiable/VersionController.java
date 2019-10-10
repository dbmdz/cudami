package de.digitalcollections.cudami.server.controller.identifiable;

import de.digitalcollections.cudami.server.business.api.service.identifiable.VersionService;
import de.digitalcollections.model.api.identifiable.Version;
import java.util.UUID;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiResponseObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(description = "The version controller", name = "Version controller")
public class VersionController {

  @Autowired
  VersionService versionService;

  @ApiMethod(description = "Get version by uuid")
  @RequestMapping(value = {"/latest/versions/{uuid}", "/v2/versions/{uuid}"}, produces = "application/json", method = RequestMethod.GET)
  @ApiResponseObject
  public Version findById(@PathVariable UUID uuid) {
    return (Version) versionService.get(uuid);
  }

  @ApiMethod(description = "Update the version status")
  @RequestMapping(value = {"/latest/versions/{uuid}", "/v2/versions/{uuid}"}, produces = "application/json", method = RequestMethod.PUT)
  @ApiResponseObject
  public Version update(@PathVariable UUID uuid, @RequestBody Version version, BindingResult errors) throws Exception {
    return (Version) versionService.update(version);
  }
}
