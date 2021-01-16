package de.digitalcollections.cudami.server.backend.impl.jdbi;

import de.digitalcollections.cudami.server.backend.impl.database.AbstractPagingAndSortingRepositoryImpl;
import org.jdbi.v3.core.Jdbi;

public abstract class JdbiRepositoryImpl extends AbstractPagingAndSortingRepositoryImpl {

  protected final Jdbi dbi;
  protected final String mappingPrefix;
  protected final String tableAlias;
  protected final String tableName;

  public JdbiRepositoryImpl(Jdbi dbi, String tableName, String tableAlias, String mappingPrefix) {
    this.dbi = dbi;
    this.mappingPrefix = mappingPrefix;
    this.tableName = tableName;
    this.tableAlias = tableAlias;
  }

  public long count() {
    final String sql = "SELECT count(*) FROM " + tableName;
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOne().get());
    return count;
  }
}
