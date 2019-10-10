package de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.entity.parts;

import de.digitalcollections.cudami.admin.backend.impl.repository.RepositoryEndpoint;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import feign.Param;
import feign.RequestLine;
import java.util.LinkedHashSet;
import java.util.UUID;

public interface EntityPartRepositoryEndpoint extends RepositoryEndpoint {

  @RequestLine("POST /latest/entityparts/{uuid}/related/entities/{entityUuid}")
  void addRelatedEntity(@Param("uuid") UUID uuid, @Param("entityUuid") UUID entityUuid);

  @RequestLine("POST /latest/entityparts/{uuid}/related/fileresources/{fileResourceUuid}")
  void addRelatedFileresource(
      @Param("uuid") UUID uuid, @Param("fileResourceUuid") UUID fileResourceUuid);

  @RequestLine("GET /latest/entityparts/{uuid}/related/entities")
  LinkedHashSet<Entity> getRelatedEntities(@Param("uuid") UUID uuid);

  @RequestLine("GET /latest/entityparts/{uuid}/related/fileresources")
  LinkedHashSet<FileResource> getRelatedFileResources(@Param("uuid") UUID uuid);

  @RequestLine("POST /latest/entityparts/{uuid}/related/entities")
  LinkedHashSet<Entity> saveRelatedEntities(
      @Param("uuid") UUID uuid, LinkedHashSet<Entity> convertFromGenericLinkedHashSet);

  @RequestLine("POST /latest/entityparts/{uuid}/related/fileresources")
  LinkedHashSet<FileResource> saveRelatedFileResources(
      @Param("uuid") UUID uuid, LinkedHashSet<FileResource> fileResources);
}
