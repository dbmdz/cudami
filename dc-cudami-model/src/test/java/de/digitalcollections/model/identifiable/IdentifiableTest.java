package de.digitalcollections.model.identifiable;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.identifiable.entity.Website;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The Identifiable")
public class IdentifiableTest {
  @Test
  public void getPrimaryUrlAliasTest() {
    Identifiable identifiable = new Identifiable();

    // there are no url aliases at all (null)
    UrlAlias found = identifiable.getPrimaryUrlAlias(Locale.GERMAN, null);
    assertThat(found).isNull();

    // there are no url aliases for the given language
    Website website = Website.builder().randomUuid().build();
    UrlAlias urlAlias =
        UrlAlias.builder()
            .isPrimary()
            .slug("this-is-a-test")
            .targetLanguage(Locale.GERMAN)
            .website(website)
            .build();
    LocalizedUrlAliases localizedUrlAliases = new LocalizedUrlAliases(urlAlias);
    identifiable.setLocalizedUrlAliases(localizedUrlAliases);
    found = identifiable.getPrimaryUrlAlias(Locale.ENGLISH, null);
    assertThat(found).isNull();

    // there are url aliases for the given language, but not without website
    found = identifiable.getPrimaryUrlAlias(Locale.GERMAN, null);
    assertThat(found).isNull();

    // there are url aliases for the given language, but not the given website or without website
    Website otherWebsite = Website.builder().randomUuid().build();
    found = identifiable.getPrimaryUrlAlias(Locale.GERMAN, otherWebsite);
    assertThat(found).isNull();

    // there are url aliases for the given language, the one without website should be returned
    UrlAlias urlAliasWithoutWebsite =
        UrlAlias.builder()
            .isPrimary()
            .slug("this-is-a-test-without-website")
            .targetLanguage(Locale.GERMAN)
            .build();
    localizedUrlAliases.add(urlAliasWithoutWebsite);
    found = identifiable.getPrimaryUrlAlias(Locale.GERMAN, null);
    assertThat(found).isNotNull();
    assertThat(found.getSlug()).isEqualTo("this-is-a-test-without-website");

    // there are url aliases for the given language, but not with the given website - the one
    // without website should be returned
    found = identifiable.getPrimaryUrlAlias(Locale.GERMAN, otherWebsite);
    assertThat(found).isNotNull();
    assertThat(found.getSlug()).isEqualTo("this-is-a-test-without-website");

    found = identifiable.getPrimaryUrlAlias(Locale.GERMAN, website);
    assertThat(found).isNotNull();
    assertThat(found.getSlug()).isEqualTo("this-is-a-test");
  }

  @DisplayName("can remove an identifier by its key")
  @Test
  public void removeIdentifierByKey() {
    Identifiable identifiable =
        Identifiable.builder()
            .identifier(Identifier.builder().namespace("foo").id("bar").build())
            .identifier(Identifier.builder().namespace("baz").id("bla").build())
            .build();

    identifiable.removeIdentifier("foo");

    assertThat(identifiable.getIdentifierByNamespace("foo")).isNull();
    assertThat(identifiable.getIdentifiers()).hasSize(1);
    assertThat(identifiable.getIdentifierByNamespace("baz").getId()).isEqualTo("bla");
  }

  @DisplayName("ignores removing an identifier when no identifiers were set")
  @Test
  public void removeIdentifierByKeyWithEmptyIdentifiers() {
    Identifiable identifiable = Identifiable.builder().build();

    identifiable.removeIdentifier("foo");

    assertThat(identifiable).isNotNull();
  }

  @DisplayName("ignores removing an identifier with an empty or null key")
  @Test
  public void removeIdentifierByEmptyOrNullKey() {
    Identifiable identifiable = Identifiable.builder().build();

    identifiable.removeIdentifier(null);
    identifiable.removeIdentifier("");

    assertThat(identifiable).isNotNull();
  }

  @DisplayName("can set multiple identifiers in builder")
  @Test
  public void testMultipleIdentifiersInBuilder() {
    Identifier identifier1 = Identifier.builder().namespace("foo").id("bar").build();
    Identifier identifier2 = Identifier.builder().namespace("baz").id("bla").build();

    Identifiable actual =
        Identifiable.builder().identifier(identifier1).identifier(identifier2).build();

    assertThat(actual.getIdentifiers()).containsExactlyInAnyOrder(identifier1, identifier2);
  }
}
