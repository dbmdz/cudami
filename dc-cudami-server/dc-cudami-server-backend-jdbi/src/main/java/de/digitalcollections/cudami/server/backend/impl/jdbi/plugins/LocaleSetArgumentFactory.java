package de.digitalcollections.cudami.server.backend.impl.jdbi.plugins;

import java.sql.Types;
import java.util.Locale;
import java.util.Set;
import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;

public class LocaleSetArgumentFactory extends AbstractArgumentFactory<Set<Locale>> {

  public LocaleSetArgumentFactory() {
    super(Types.ARRAY);
  }

  @Override
  protected Argument build(Set<Locale> value, ConfigRegistry config) {
    if (value == null) {
      return null;
    }

    return (position, preparedStatement, statementContext) ->
        preparedStatement.setArray(
            position,
            statementContext
                .getConnection()
                .createArrayOf("varchar", value.stream().map(Locale::toLanguageTag).toArray()));
  }
}
