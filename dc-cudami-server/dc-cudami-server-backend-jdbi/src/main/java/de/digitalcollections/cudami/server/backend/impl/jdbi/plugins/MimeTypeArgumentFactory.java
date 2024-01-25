package de.digitalcollections.cudami.server.backend.impl.jdbi.plugins;

import de.digitalcollections.model.file.MimeType;
import java.sql.Types;
import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;

public class MimeTypeArgumentFactory extends AbstractArgumentFactory<MimeType> {

  public MimeTypeArgumentFactory() {
    super(Types.VARCHAR);
  }

  @Override
  protected Argument build(MimeType mimeType, ConfigRegistry config) {
    return (position, preparedStatement, statementContext) ->
        preparedStatement.setObject(position, mimeType.getTypeName(), Types.VARCHAR);
  }
}
