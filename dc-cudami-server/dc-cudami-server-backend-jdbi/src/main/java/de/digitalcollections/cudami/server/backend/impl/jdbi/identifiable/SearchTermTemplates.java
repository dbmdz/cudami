package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable;

public enum SearchTermTemplates {
  ILIKE_SEARCH("%s.%s ILIKE '%%' || :searchTerm || '%%'", "searchTerm"),
  ILIKE_STARTS_WITH("%s.%s ILIKE :searchTerm || '%%'", "searchTerm"),
  // FYI: [JSON Path
  // Functions](https://www.postgresql.org/docs/12/functions-json.html#FUNCTIONS-SQLJSON-PATH)
  // and [Data type](https://www.postgresql.org/docs/12/datatype-json.html#DATATYPE-JSONPATH)
  //
  // To insert `:searchTerm` into the `jsonpath` we must split it up;
  // the cast is necessary otherwise Postgres does not recognise it as `jsonpath` (that is
  // just a string practically).
  // Finds (case insensitively) labels that contain the search term, see `like_regex`
  // example in
  // https://www.postgresql.org/docs/12/functions-json.html#FUNCTIONS-SQLJSON-PATH
  JSONB_PATH(
      "jsonb_path_exists(%s.%s, ('$.%s ? (@ like_regex \"' || :searchTerm || '\" flag \"iq\")')::jsonpath)",
      "searchTerm"),
  ARRAY_CONTAINS("%s.%s @> :searchTermArray::TEXT[]", "searchTermArray");

  private final String template;
  public final String placeholder;

  SearchTermTemplates(String template, String placeholder) {
    this.template = template;
    this.placeholder = placeholder;
  }

  public String renderTemplate(Object... values) {
    return String.format(template, values);
  }
}
