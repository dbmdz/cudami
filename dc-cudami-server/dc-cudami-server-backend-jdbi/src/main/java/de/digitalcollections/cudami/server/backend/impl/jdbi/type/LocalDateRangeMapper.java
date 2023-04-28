package de.digitalcollections.cudami.server.backend.impl.jdbi.type;

import de.digitalcollections.model.time.LocalDateRange;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.argument.ArgumentFactory;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.springframework.util.StringUtils;

public class LocalDateRangeMapper implements ArgumentFactory, ColumnMapper<LocalDateRange> {

  private static final DateTimeFormatter formatter =
      DateTimeFormatter.ofPattern("yyyy-MM-dd GG", Locale.ROOT);

  public LocalDateRangeMapper() {}

  private String toSqlDate(LocalDate date) {
    if (date == null) return "";
    // SQL does not need AD era
    return "\"%s\"".formatted(date.format(formatter).replaceAll("\\sAD$", ""));
  }

  private LocalDate fromSqlDate(String date) {
    if (!StringUtils.hasText(date)) return null;
    // we must add the era if missing
    if (!date.matches(".+\\s(BC|AD)$")) date += " AD";
    return LocalDate.parse(date, formatter);
  }

  @Override
  public Optional<Argument> build(Type type, Object value, ConfigRegistry config) {
    if (!(value instanceof LocalDateRange)) {
      return Optional.empty();
    }
    LocalDateRange v = (LocalDateRange) value;
    return Optional.of(
        (position, statement, ctx) -> {
          statement.setString(
              position, String.format("[%s,%s]", toSqlDate(v.getStart()), toSqlDate(v.getEnd())));
        });
  }

  @Override
  public LocalDateRange map(ResultSet r, int columnNumber, StatementContext ctx)
      throws SQLException {
    String value = r.getString(columnNumber);
    if (value == null) return null;
    // worst case: ["2023-01-01 BC",2023-01-02)
    Matcher valueParts =
        Pattern.compile(
                "^(?<lowbound>[\\[(])\"?(?<lower>[\\d-]+(\\s\\w{2})?)?\"?,\"?(?<upper>[\\d-]+(\\s\\w{2})?)?\"?(?<upbound>[)\\]])$")
            .matcher(value);
    if (!valueParts.find()) return null;
    LocalDate lower = fromSqlDate(valueParts.group("lower"));
    LocalDate upper = fromSqlDate(valueParts.group("upper"));
    if (lower != null && valueParts.group("lowbound").equals("(")) {
      // lower bound is not included so we add one day
      lower = lower.plusDays(1);
    }
    if (upper != null && valueParts.group("upbound").equals(")")) {
      // upper bound is not included so we subtract one day
      upper = upper.minusDays(1);
    }
    return new LocalDateRange(lower, upper);
  }
}
