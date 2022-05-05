package de.digitalcollections.cudami.server.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The ParameterHelper")
class ParameterHelperTest {

  @DisplayName("returns an empty pair, when the separator works on a null string")
  @Test
  public void nullPair() {
    Pair<String, String> actual = ParameterHelper.extractPairOfStringsFromUri("", "/foo/");
    assertThat(actual).isNotNull();
    assertThat(actual.getLeft()).isEqualTo("");
    assertThat(actual.getRight()).isNull();
  }

  @DisplayName("returns an empty pair, when the separator works on an empty string")
  @Test
  public void emptyPair() {
    Pair<String, String> actual = ParameterHelper.extractPairOfStringsFromUri(null, "/foo/");
    assertThat(actual).isNotNull();
    assertThat(actual.getLeft()).isNull();
    assertThat(actual.getRight()).isNull();
  }

  @DisplayName("returns only the left item of the pair, when the colon is missing")
  @Test
  public void missingColon() {
    Pair<String, String> actual = ParameterHelper.extractPairOfStringsFromUri("/foo/bar", "/foo/");
    assertThat(actual.getLeft()).isEqualTo("bar");
    assertThat(actual.getRight()).isNull();
  }

  @DisplayName("returns both items of the pair, when one colon is provided")
  @Test
  public void singleColon() {
    Pair<String, String> actual =
        ParameterHelper.extractPairOfStringsFromUri("/foo/bar:baz", "/foo/");
    assertThat(actual.getLeft()).isEqualTo("bar");
    assertThat(actual.getRight()).isEqualTo("baz");
  }

  @DisplayName(
      "allows multiple colons for the right item of the pair, when multiple colons are provided")
  @Test
  public void multipleColons() {
    Pair<String, String> actual =
        ParameterHelper.extractPairOfStringsFromUri("/foo/bar:baz:blubb:bla:", "/foo/");
    assertThat(actual.getLeft()).isEqualTo("bar");
    assertThat(actual.getRight()).isEqualTo("baz:blubb:bla:");
  }
}
