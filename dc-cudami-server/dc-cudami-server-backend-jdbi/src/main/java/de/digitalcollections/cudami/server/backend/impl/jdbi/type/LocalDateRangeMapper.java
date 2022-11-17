package de.digitalcollections.cudami.server.backend.impl.jdbi.type;

import de.digitalcollections.model.time.LocalDateRange;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.argument.ArgumentFactory;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;

public class LocalDateRangeMapper implements ArgumentFactory, ColumnMapper<LocalDateRange> {

  public LocalDateRangeMapper() {}

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

  @Override
  public LocalDateRange map(ResultSet r, int columnNumber, StatementContext ctx)
      throws SQLException {
    String value = r.getString(columnNumber);
    if (value == null) return null;
    Matcher valueParts =
        Pattern.compile(
                "^(?<lowbound>\\[|\\()(?<lower>[\\d-]+),(?<upper>[\\d-]+)(?<upbound>\\]|\\))$")
            .matcher(value);
    if (!valueParts.find()) return null;
    LocalDate lower = LocalDate.parse(valueParts.group("lower"));
    LocalDate upper = LocalDate.parse(valueParts.group("upper"));
    if (valueParts.group("lowbound").equals("(")) {
      // lower bound is not included so we add one day
      lower = lower.plusDays(1);
    }
    if (valueParts.group("upbound").equals(")")) {
      // upper bound is not included so we subtract one day
      upper = upper.minusDays(1);
    }
    return new LocalDateRange(lower, upper);
  }
}
