package de.digitalcollections.model.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import java.io.IOException;
import java.util.Locale;

public class LocaleKeyDeserializer extends KeyDeserializer {
  @Override
  public Locale deserializeKey(String key, DeserializationContext ctx)
      throws IOException, JsonProcessingException {
    return Locale.forLanguageTag(key);
  }
}
