package de.digitalcollections.cudami.server.backend.impl.jdbi.plugins;

import java.sql.Types;
import java.util.Locale;
import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;

public class LocaleArgumentFactory extends AbstractArgumentFactory<Locale> {

  public LocaleArgumentFactory() {
    super(Types.VARCHAR);
  }

  @Override
  protected Argument build(Locale locale, ConfigRegistry config) {
    return (position, preparedStatement, statementContext) ->
        preparedStatement.setObject(position, locale.toLanguageTag(), Types.VARCHAR);
  }
}
