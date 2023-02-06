package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.work;

import de.digitalcollections.model.text.Title;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class TitleSqlHelper {

  public static BiFunction<String, Map<String, Object>, String> buildTitleSql(
      final List<Title> titles) {
    return (sql, bindings) -> {
      if (titles == null || titles.isEmpty()) {
        return sql.replace("{{titles}}", "NULL");
      }
      List<String> titleConstructors = new ArrayList<>();
      for (int i = 0; i < titles.size(); i++) {
        titleConstructors.add(
            String.format(
                "title_constructor(:titles_%1$d_mainType, :titles_%1$d_subType, "
                    + ":titles_%1$d_text::jsonb, :titles_%1$d_textLocales::varchar[])",
                i));
        Title title = titles.get(i);
        bindings.put(
            String.format("titles_%d_mainType", i),
            title.getTitleType() != null ? title.getTitleType().getMainType() : null);
        bindings.put(
            String.format("titles_%d_subType", i),
            title.getTitleType() != null ? title.getTitleType().getSubType() : null);
        bindings.put(String.format("titles_%d_text", i), title.getText());
        bindings.put(
            String.format("titles_%d_textLocales", i),
            title.getTextLocalesOfOriginalScripts() != null
                ? title.getTextLocalesOfOriginalScripts().stream()
                    .map(l -> l.toLanguageTag())
                    .toArray(n -> new String[n])
                : null);
      }
      return sql.replace("{{titles}}", "ARRAY[" + String.join(", ", titleConstructors) + "]");
    };
  }
}
