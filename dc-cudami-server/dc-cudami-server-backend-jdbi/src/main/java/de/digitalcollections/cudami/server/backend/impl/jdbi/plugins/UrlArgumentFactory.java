package de.digitalcollections.cudami.server.backend.impl.jdbi.plugins;

import java.net.URL;
import java.sql.Types;
import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;

public class UrlArgumentFactory extends AbstractArgumentFactory<URL> {

  public UrlArgumentFactory() {
    super(Types.VARCHAR);
  }

  @Override
  protected Argument build(URL value, ConfigRegistry config) {
    return (i, p, cx) -> p.setObject(i, value.toString(), Types.VARCHAR);
  }
}
