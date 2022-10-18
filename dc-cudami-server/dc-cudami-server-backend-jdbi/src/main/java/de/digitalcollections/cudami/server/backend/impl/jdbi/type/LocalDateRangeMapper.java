package de.digitalcollections.cudami.server.backend.impl.jdbi.type;

import de.digitalcollections.model.time.LocalDateRange;
import java.lang.reflect.Type;
import java.util.Optional;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.argument.ArgumentFactory;
import org.jdbi.v3.core.config.ConfigRegistry;

public class LocalDateRangeMapper implements ArgumentFactory {

  public LocalDateRangeMapper() {
    // TODO Auto-generated constructor stub
  }

  @Override
  public Optional<Argument> build(Type type, Object value, ConfigRegistry config) {
    if (!(value instanceof LocalDateRange)) {
      return Optional.empty();
    }
    LocalDateRange v = (LocalDateRange) value;
    return Optional.of(
        (position, statement, ctx) -> {
          statement.setString(position, String.format("[%s,%s]", v.getStart(), v.getEnd()));
        });
  }
}
