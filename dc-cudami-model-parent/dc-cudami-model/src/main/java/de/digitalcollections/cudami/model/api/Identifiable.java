package de.digitalcollections.cudami.model.api;

import java.io.Serializable;
import java.util.UUID;

/**
 * @param <T> unique id specifying instance
 */
public interface Identifiable<T extends Serializable> {

  T getId();

  void setId(T id);

  UUID getUuid();

  void setUuid(UUID uuid);
}
