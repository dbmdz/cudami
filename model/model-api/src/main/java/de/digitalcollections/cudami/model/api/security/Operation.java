package de.digitalcollections.cudami.model.api.security;

import java.io.Serializable;

/**
 * An operation the system can execute.
 *
 * @param <ID> unique id specifying instance
 */
public interface Operation<ID extends Serializable> extends Identifiable<ID> {

  public static final String PREFIX = "OP_";

  String getName();

  void setName(String name);
}
