package de.digitalcollections.cudami.server.backend.impl.jdbi.type;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.model.identifiable.entity.work.Title;
import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;
import org.jdbi.v3.core.array.SqlArrayType;

public class TitleMapper implements SqlArrayType<Title> {

  private ObjectMapper objectMapper;
  private MainSubTypeMapper.TitleTypeMapper titleTypeMapper;

  public TitleMapper(ObjectMapper objectMapper, MainSubTypeMapper.TitleTypeMapper titleTypeMapper) {
    this.objectMapper = objectMapper;
    this.titleTypeMapper = titleTypeMapper;
  }

  @Override
  public String getTypeName() {
    return "title";
  }

  @Override
  public Object convertArrayElement(Title element) {
    try {
      boolean hasLocaleList =
          element.getTextLocalesOfOriginalScripts() != null
              && !element.getTextLocalesOfOriginalScripts().isEmpty();
      return "("
          + (element.getTitleType() != null
              ? titleTypeMapper.convertArrayElement(element.getTitleType())
              : "")
          + ","
          + (element.getText() != null ? objectMapper.writeValueAsString(element.getText()) : "")
          + ","
          + (hasLocaleList
              ? String.format(
                  "{%s}", commaSeparatedLocales(element.getTextLocalesOfOriginalScripts()))
              : "")
          + ")";
    } catch (JsonProcessingException e) {
      return "ERROR processing title.text";
    }
  }

  private String commaSeparatedLocales(Collection<Locale> list) {
    return list.stream().map(locale -> locale.toString()).collect(Collectors.joining(","));
  }
}
