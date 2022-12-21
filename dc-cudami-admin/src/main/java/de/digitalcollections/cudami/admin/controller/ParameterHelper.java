package de.digitalcollections.cudami.admin.controller;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

/** Extract parameters from a HttpServletRequest */
public class ParameterHelper {

  public static final String UUID_PATTERN =
      "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

  /**
   * Extract a pair of strings from the path of an HttpRequest, separated by a (the first) colon.
   *
   * <p>The HttpRequest can be either plaintext (should not contain slashes) or BASE64 encoded.
   *
   * <p>If the request string ends with .json, that string is cut off.
   *
   * @param requestUri the URI whose path is evaluated
   * @param leadingPathRegex the regex, at whose end the evaulation will start
   * @return a pair of strings, separated by the first colon. The second string can contain multiple
   *     colons
   */
  public static Pair<String, String> extractPairOfStringsFromUri(
      String requestUri, String leadingPathRegex) {
    if (requestUri == null) {
      return Pair.of(null, null);
    }

    String paramString = requestUri.replaceFirst(leadingPathRegex, "").replaceFirst("\\.json$", "");

    if (!paramString.contains(":")) {
      paramString = new String(Base64.decodeBase64(paramString), StandardCharsets.UTF_8);
    }

    return extractPairOfStrings(paramString);
  }

  /**
   * Extract a pair of strings from a string, separated by a (the first) colon
   *
   * @param string the string
   * @return a pair of strings, separated by the first colon. The second string can contain multiple
   *     * colons
   */
  public static Pair<String, String> extractPairOfStrings(String string) {
    if (!string.contains(":")) {
      return Pair.of(string, null);
    }
    String[] params = string.split(":");

    String left = params[0];
    String right = String.join(":", Arrays.copyOfRange(params, 1, params.length));
    if (string.endsWith(":")) {
      right += ":";
    }
    return Pair.of(left, right);
  }

  public static Triple<String, String, String> extractTripleOfStringsFromUri(
      String requestUri, String leadingPathRegex) {
    if (requestUri == null) {
      return Triple.of(null, null, null);
    }

    String paramString = requestUri.replaceFirst(leadingPathRegex, "").replaceFirst("\\.json$", "");

    if (!paramString.contains(":")) {
      paramString = new String(Base64.decodeBase64(paramString), StandardCharsets.UTF_8);
    }

    return extractTripleOfStrings(paramString);
  }

  /**
   * Extract a triple of strings from a string, separated by a (the first and second) colon
   *
   * @param string the string
   * @return a triple of strings, separated by the first and second colon. The third string can
   *     contain multiple * colons
   */
  public static Triple<String, String, String> extractTripleOfStrings(String string) {
    if (!string.contains(":")) {
      return Triple.of(string, null, null);
    }
    String[] params = string.split(":");
    if (params.length < 3) {
      return Triple.of(params[0], params[1], null);
    }

    String left = params[0];
    String middle = params[1];
    String right = String.join(":", Arrays.copyOfRange(params, 2, params.length));
    if (string.endsWith(":")) {
      right += ":";
    }
    return Triple.of(left, middle, right);
  }
}
