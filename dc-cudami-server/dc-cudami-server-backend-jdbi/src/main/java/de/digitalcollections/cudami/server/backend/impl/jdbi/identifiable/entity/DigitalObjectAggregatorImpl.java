package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.impl.identifiable.entity.DigitalObjectImpl;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class DigitalObjectAggregatorImpl /* implements BiFunction<T, U, R> */ {

  Map<UUID, DigitalObjectImpl> digitalObjects = new LinkedHashMap<>();
  // Map<UUID, Map<UUID, FileResource>> digitalObjectFileResources = new LinkedHashMap<>();
  // Map<UUID, Map<UUID, Identifier>> fileResourceIdentifiers = new LinkedHashMap<>();
  Map<UUID, Identifier> identifiers = new LinkedHashMap<>();

  UUID dfFileresourceUuid;

  public DigitalObjectAggregatorImpl() {}

  public Map<UUID, DigitalObjectImpl> getDigitalObjects() {
    return digitalObjects;
  }

  /*
  public Map<UUID, Map<UUID, FileResource>> getFileResources() {
    return digitalObjectFileResources;
  }

  public Map<UUID, FileResource> addFileResource(UUID doUUID, UUID frUUID, FileResource fileResource) {
    Map<UUID, FileResource> fileResourceMap = digitalObjectFileResources.get(doUUID);
    if (fileResourceMap == null) {
      fileResourceMap = new LinkedHashMap<>();
      digitalObjectFileResources.put(doUUID, fileResourceMap);
    }
    fileResourceMap.put(frUUID, fileResource);
    return fileResourceMap;
  }

  public Map<UUID, Map<UUID, Identifier>> getFileResourceIdentifiers() {
    return fileResourceIdentifiers;
  }
   */
  public UUID getDfFileresourceUuid() {
    return dfFileresourceUuid;
  }

  public void setDfFileresourceUuid(UUID dfFileresourceUuid) {
    this.dfFileresourceUuid = dfFileresourceUuid;
  }

  public Map<UUID, Identifier> getIdentifiers() {
    return identifiers;
  }

  public void setIdentifiers(Map<UUID, Identifier> identifiers) {
    this.identifiers = identifiers;
  }

  public void resetIdentifiers() {
    identifiers = new LinkedHashMap<>();
  }
}
