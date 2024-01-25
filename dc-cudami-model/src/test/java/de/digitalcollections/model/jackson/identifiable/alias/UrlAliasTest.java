package de.digitalcollections.model.jackson.identifiable.alias;

import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class UrlAliasTest extends BaseJsonSerializationTest {

  protected static UrlAlias createObject() {
    UrlAlias urlAlias = new UrlAlias();
    urlAlias.setCreated(LocalDateTime.of(2021, Month.JANUARY, 1, 0, 0, 0));
    urlAlias.setLastPublished(LocalDateTime.of(2021, Month.DECEMBER, 24, 12, 0, 0));
    urlAlias.setPrimary(true);
    urlAlias.setSlug("foobar");

    Collection target = Collection.builder().uuid("c31593a4-9620-484b-a4f5-4112731d953b").build();

    urlAlias.setTargetLanguage(Locale.GERMAN);
    urlAlias.setTarget(target);
    urlAlias.setUuid(UUID.fromString("d1dfbfbf-364e-4983-9d15-c33549145928"));
    Website website = new Website();
    website.setUuid(UUID.fromString("9b676195-f505-400c-acb2-b7aabb7c62f7"));
    urlAlias.setWebsite(website);
    return urlAlias;
  }

  @Test
  public void testSerializeDeserialize() throws Exception {
    UrlAlias urlAlias = createObject();
    checkSerializeDeserialize(urlAlias, "serializedTestObjects/identifiable/alias/UrlAlias.json");
  }
}
