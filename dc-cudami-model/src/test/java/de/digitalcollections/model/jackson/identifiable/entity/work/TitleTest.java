package de.digitalcollections.model.jackson.identifiable.entity.work;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.text.Title;
import de.digitalcollections.model.text.TitleType;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The Title")
public class TitleTest extends BaseJsonSerializationTest {

  @DisplayName("can be serialized and deserialized")
  @Test
  public void testSerializeDeserialize() throws Exception {
    Title title =
        Title.builder()
            .titleType(TitleType.builder().mainType("main").subType("main").build())
            .text(
                LocalizedText.builder()
                    .text(Locale.GERMAN, "Titel")
                    .text(
                        new Locale.Builder().setLanguage("zh").setScript("hani").build(),
                        "圖註八十一難經辨眞")
                    .build())
            .textLocaleOfOriginalScript(
                new Locale.Builder().setLanguage("zh").setScript("hani").build())
            .build();

    checkSerializeDeserialize(title, "serializedTestObjects/identifiable/entity/work/Title.json");
  }

  @DisplayName("ignores a null locale, provided in the builder")
  @Test
  public void ignoreNullLocaleInBuilder() {
    Title title = Title.builder().textLocalesOfOriginalScripts(null).build();

    assertThat(title.getTextLocalesOfOriginalScripts()).isNotNull().isEmpty();
  }

  @DisplayName("can set locales in the builder")
  @Test
  public void setLocalesInBuilder() {
    Title title =
        Title.builder()
            .textLocaleOfOriginalScript(Locale.GERMAN)
            .textLocaleOfOriginalScript(Locale.ITALIAN)
            .build();

    assertThat(title.getTextLocalesOfOriginalScripts())
        .containsExactly(Locale.GERMAN, Locale.ITALIAN);
  }
}
