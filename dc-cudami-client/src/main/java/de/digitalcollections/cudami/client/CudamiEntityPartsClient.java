package de.digitalcollections.cudami.client;

import de.digitalcollections.model.api.identifiable.entity.parts.EntityPart;
import de.digitalcollections.model.impl.identifiable.entity.parts.EntityPartImpl;

public class CudamiEntityPartsClient extends CudamiBaseClient<EntityPartImpl> {

  public CudamiEntityPartsClient(String serverUrl) {
    super(serverUrl, EntityPartImpl.class);
  }

  public EntityPart create() {
    return new EntityPartImpl();
  }

  /*
    @RequestLine("POST /latest/entityparts/{uuid}/related/entities/{entityUuid}")
  void addRelatedEntity(@Param("uuid") UUID uuid, @Param("entityUuid") UUID entityUuid);

  @RequestLine("POST /latest/entityparts/{uuid}/related/fileresources/{fileResourceUuid}")
  void addRelatedFileresource(
      @Param("uuid") UUID uuid, @Param("fileResourceUuid") UUID fileResourceUuid);

  @RequestLine("GET /latest/entityparts/{uuid}/related/entities")
  List<Entity> getRelatedEntities(@Param("uuid") UUID uuid);

  @RequestLine("GET /latest/entityparts/{uuid}/related/fileresources")
  List<FileResource> getRelatedFileResources(@Param("uuid") UUID uuid);

  @RequestLine("POST /latest/entityparts/{uuid}/related/entities")
  List<Entity> saveRelatedEntities(@Param("uuid") UUID uuid, List<Entity> entities);

  @RequestLine("POST /latest/entityparts/{uuid}/related/fileresources")
  List<FileResource> saveRelatedFileResources(
      @Param("uuid") UUID uuid, List<FileResource> fileResources);
   */
}
