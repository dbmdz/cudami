package de.digitalcollections.cudami.server.backend.impl.jdbi;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

public interface FooRepository {

  @SqlQuery("SELECT bar FROM foo where bar = :param")
  public String getBar(@Bind("param") String bar);
}
