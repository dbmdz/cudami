package de.digitalcollections.model.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.Locale;

public class LocaleKeySerializer extends JsonSerializer<Locale> {
  @Override
  public void serialize(Locale locale, JsonGenerator gen, SerializerProvider serializers)
      throws IOException {
    gen.writeFieldName(locale.toLanguageTag());
  }
}
