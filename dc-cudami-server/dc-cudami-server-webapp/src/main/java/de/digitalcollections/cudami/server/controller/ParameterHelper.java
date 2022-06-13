package de.digitalcollections.cudami.server.controller;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.tuple.Pair;

/** Extract parameters from a HttpServletRequest */
public class ParameterHelper {

  /**
   * Extract a pair of strings from the path of an HttpRequest, separated by a (the first) colon.
   *
   * <p>The HttpRequest can be either plaintext (should not contain slashes) or BASE64 encoded.
   *
   * <p>If the request string ends with .json, that string is cut off.
   *
   * @param requestUri the URI whose path is evaluated
   * @param trailingPathRegex the regex, at whose end the evaulation will start
   * @return a pair of strings, separated by the first colon. The second string can contain multiple
   *     colons
   */
  public static Pair<String, String> extractPairOfStringsFromUri(
      String requestUri, String trailingPathRegex) {
    if (requestUri == null) {
      return Pair.of(null, null);
    }

    String paramString =
        requestUri.replaceFirst(trailingPathRegex, "").replaceFirst("\\.json$", "");

    if (Base64.isBase64(paramString)) {
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
}
