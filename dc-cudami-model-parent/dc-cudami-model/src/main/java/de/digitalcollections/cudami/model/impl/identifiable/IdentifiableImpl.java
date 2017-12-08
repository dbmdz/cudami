package de.digitalcollections.cudami.model.impl.identifiable;

import de.digitalcollections.cudami.model.api.identifiable.Identifiable;
import java.util.UUID;

public class IdentifiableImpl implements Identifiable {

  private UUID uuid = UUID.randomUUID();

  @Override
  public UUID getUuid() {
    return uuid;
  }

  @Override
  public void setUuid(UUID uuid) {
    this.uuid = uuid;
  }

}
