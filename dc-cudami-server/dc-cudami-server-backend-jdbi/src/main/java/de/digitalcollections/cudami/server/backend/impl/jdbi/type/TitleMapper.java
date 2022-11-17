package de.digitalcollections.cudami.server.backend.impl.jdbi.type;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.model.identifiable.entity.work.Title;
import de.digitalcollections.model.identifiable.entity.work.TitleType;
import de.digitalcollections.model.text.LocalizedText;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;

public class TitleMapper implements ColumnMapper<Title> {

  private ObjectMapper objectMapper;
  private MainSubTypeMapper.TitleTypeMapper titleTypeMapper;

  public TitleMapper(ObjectMapper objectMapper, MainSubTypeMapper.TitleTypeMapper titleTypeMapper) {
    this.objectMapper = objectMapper;
    this.titleTypeMapper = titleTypeMapper;
  }

  @Override
  public Title map(ResultSet r, int columnNumber, StatementContext ctx) throws SQLException {
    String value = r.getString(columnNumber);
    if (value == null) return null;
    // looks like:
    // ("(MAIN,MAIN)","{""de"": ""Ein deutscher Titel""}","{de,en}")
    Matcher valueParts =
        Pattern.compile(
                "^[(]\\p{Punct}*?(?<titletype>[(].+?[)])\\p{Punct}*?,\\p{Punct}*?(?<text>[{].+?[}])\\p{Punct}*?,\\p{Punct}*?[{](?<orig>[\\w,_-]+)[}]\\p{Punct}*?[)]$",
                Pattern.UNICODE_CHARACTER_CLASS)
            .matcher(value);
    if (!valueParts.find()) {
      return null;
    }
    TitleType titleType = titleTypeMapper.createTypeFromString(valueParts.group("titletype"));
    LocalizedText titleText = null;
    try {
      titleText =
          objectMapper.readValue(
              valueParts.group("text").replaceAll("\"{2,}", "\""), LocalizedText.class);
    } catch (JsonProcessingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    Set<Locale> localesOfOriginalScripts =
        Stream.of(valueParts.group("orig").split(","))
            .map(s -> Locale.forLanguageTag(s))
            .collect(Collectors.toSet());
    return new Title(titleText, localesOfOriginalScripts, titleType);
  }
}
