package de.digitalcollections.cudami.model.api.identifiable;

import java.util.UUID;

public interface Identifiable {

  UUID getUuid();

  void setUuid(UUID uuid);
}
