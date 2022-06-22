package de.digitalcollections.cudami.server.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The ParameterHelper")
class ParameterHelperTest {

  @DisplayName("can cope with a null uri")
  @Test
  public void testUnsetArgument() {
    Pair<String, String> actual =
        ParameterHelper.extractPairOfStringsFromUri(null, "^.*?/identifier/");
    Pair<String, String> expected = Pair.of(null, null);
    assertThat(actual).isEqualTo(expected);
  }

  @DisplayName("returns a non colon separated argument only as left item of the pair")
  @Test
  public void testNonColonSeparatedArgument() {
    Pair<String, String> actual =
        ParameterHelper.extractPairOfStringsFromUri(
            "https://foo.bar/identifier/non-id", "^.*?/identifier/");
    assertThat(actual.getRight()).isNull();
  }

  @DisplayName("can extract a colon separated pair of strings of an unencoded url")
  @Test
  public void testUnencoded() {
    Pair<String, String> actual =
        ParameterHelper.extractPairOfStringsFromUri(
            "https://foo.bar/identifier/bla:fasel/blubb:baz:", "^.*?/identifier/");
    Pair<String, String> expected = Pair.of("bla", "fasel/blubb:baz:");
    assertThat(actual).isEqualTo(expected);
  }

  @DisplayName(
      "can extract a colon separated pair of strings of an URL, which contains an Base64Encoded argument")
  @Test
  public void testEncodedStandard() {
    String encodedArgument =
        Base64.encodeBase64String("bla:fasel/blubb:baz:".getBytes(StandardCharsets.UTF_8));
    Pair<String, String> actual =
        ParameterHelper.extractPairOfStringsFromUri(
            "https://foo.bar/identifier/" + encodedArgument, "^.*?/identifier/");
    Pair<String, String> expected = Pair.of("bla", "fasel/blubb:baz:");
    assertThat(actual).isEqualTo(expected);
  }

  @DisplayName(
      "can extract a colon separated pair of strings of an URL, which contains an Base64Encoded URL safe argument")
  @Test
  public void testEncodedUrlSafe() {
    String encodedArgument =
        Base64.encodeBase64URLSafeString("bla:fasel/blubb:baz:".getBytes(StandardCharsets.UTF_8));
    Pair<String, String> actual =
        ParameterHelper.extractPairOfStringsFromUri(
            "https://foo.bar/identifier/" + encodedArgument, "^.*?/identifier/");
    Pair<String, String> expected = Pair.of("bla", "fasel/blubb:baz:");
    assertThat(actual).isEqualTo(expected);
  }
}
