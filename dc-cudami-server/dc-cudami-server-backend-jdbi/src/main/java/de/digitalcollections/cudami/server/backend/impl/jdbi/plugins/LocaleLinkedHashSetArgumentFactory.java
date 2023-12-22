package de.digitalcollections.cudami.server.backend.impl.jdbi.plugins;

import java.sql.Types;
import java.util.LinkedHashSet;
import java.util.Locale;
import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;

public class LocaleLinkedHashSetArgumentFactory
    extends AbstractArgumentFactory<LinkedHashSet<Locale>> {

  public LocaleLinkedHashSetArgumentFactory() {
    super(Types.ARRAY);
  }

  @Override
  protected Argument build(LinkedHashSet<Locale> value, ConfigRegistry config) {
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
