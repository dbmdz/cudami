package de.digitalcollections.cudami.server.controller.identifiable.entity.parts;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.parts.EntityPartService;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.parts.EntityPart;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import java.util.List;
import java.util.UUID;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiResponseObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(description = "The entitypart controller", name = "EntityPart controller")
public class EntityPartController<P extends EntityPart, E extends Entity> {

  @Autowired
  @Qualifier("entityPartServiceImpl")
  private EntityPartService<P, E> service;

  @ApiMethod(description = "Get related file resources of entity part")
  @RequestMapping(
      value = {
        "/latest/entityparts/{uuid}/related/fileresources",
        "/v2/entityparts/{uuid}/related/fileresources"
      },
      produces = "application/json",
      method = RequestMethod.GET)
  @ApiResponseObject
  List<FileResource> getRelatedFileResources(@PathVariable UUID uuid) {
    return service.getRelatedFileResources(uuid);
  }
}
