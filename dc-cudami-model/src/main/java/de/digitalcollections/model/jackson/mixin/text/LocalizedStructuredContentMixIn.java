package de.digitalcollections.model.jackson.mixin.text;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.text.LocalizedStructuredContent;
import de.digitalcollections.model.text.StructuredContent;
import de.digitalcollections.model.view.ToCEntry;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@JsonDeserialize(as = LocalizedStructuredContent.class)
public interface LocalizedStructuredContentMixIn {

  @JsonIgnore
  void add(Locale locale, StructuredContent structuredContent);

  @JsonIgnore
  public Map<Locale, List<ToCEntry>> getTableOfContents();
}
