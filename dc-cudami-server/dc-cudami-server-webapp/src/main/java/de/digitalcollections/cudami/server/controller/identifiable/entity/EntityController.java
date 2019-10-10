package de.digitalcollections.cudami.server.controller.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.EntityService;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import java.util.LinkedHashSet;
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
@Api(description = "The entity controller", name = "Entity controller")
public class EntityController<E extends Entity> {

  @Autowired
  @Qualifier("entityServiceImpl")
  private EntityService<E> service;

  @ApiMethod(description = "Get related file resources of entity")
  @RequestMapping(value = {"/latest/entities/{uuid}/related/fileresources", "/v2/entities/{uuid}/related/fileresources"}, produces = "application/json", method = RequestMethod.GET)
  @ApiResponseObject
  LinkedHashSet<FileResource> getRelatedFileResources(@PathVariable UUID uuid) {
    return service.getRelatedFileResources(uuid);
  }
}
