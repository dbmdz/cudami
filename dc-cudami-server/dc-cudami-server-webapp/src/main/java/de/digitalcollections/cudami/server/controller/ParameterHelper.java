package de.digitalcollections.cudami.server.controller;

import java.util.Arrays;
import org.apache.commons.lang3.tuple.Pair;

public class ParameterHelper {

  /**
   * Extract a pair of strings from the path of an HttpRequest, separated by a (the first) colon
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
    if (!paramString.contains(":")) {
      return Pair.of(paramString, null);
    }
    String[] params = paramString.split(":");

    String left = params[0];
    String right = String.join(":", Arrays.copyOfRange(params, 1, params.length));
    if (paramString.endsWith(":")) {
      right += ":";
    }
    return Pair.of(left, right);
  }
}
