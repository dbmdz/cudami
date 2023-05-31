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
      "jsonb_path_exists(%s, ('$.%s ? (@ like_regex \"' || :searchTerm || '\" flag \"iq\")')::jsonpath)",
      "searchTerm"),
  ARRAY_CONTAINS("%s.%s::TEXT[] @> :searchTermArray::TEXT[]", "searchTermArray");

  private final String template;
  public final String placeholder;

  SearchTermTemplates(String template, String placeholder) {
    this.template = template;
    this.placeholder = placeholder;
  }

  /**
   * Fill the template with {@code values}. An optional {@code placeholderSuffix} can be supplied to
   * distinguish multiple usages.
   *
   * @param placeholderSuffix a string appended to the placeholder after an underscore, may be
   *     {@code null}
   * @param values the objects passed to {@link String#format(String, Object...)}
   * @return the formatted SQL
   */
  public String renderTemplate(String placeholderSuffix, Object... values) {
    String t = template;
    if (placeholderSuffix != null) {
      t = t.replace(":" + placeholder, ":%s_%s".formatted(placeholder, placeholderSuffix));
    }
    return String.format(t, values);
  }
}
