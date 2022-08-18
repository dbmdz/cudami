package de.digitalcollections.cudami.server.controller.advice;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

@RestControllerAdvice
/** Remove this class, when no sub-v7 endpoints are offered any more! */
public class EnsureObjectTypeAdvice extends RequestBodyAdviceAdapter {

  private static final String ATTR_OBJECTTYPE = "objectType";
  private static final int LATEST_VERSION_WHICH_REQUIRES_FIX = 6;
  private static final Pattern VERSION_PATTERN = Pattern.compile(".*?/(v\\d+)/.*?");

  // For each request, we get a new httpServletRequest here
  @Autowired HttpServletRequest httpServletRequest;

  /**
   * Only for testing purposes
   *
   * @param httpServletRequest the HttpServletRequest of which Method and RequestURL are used
   */
  protected void setHttpServletRequest(HttpServletRequest httpServletRequest) {
    this.httpServletRequest = httpServletRequest;
  }

  @Override
  public boolean supports(
      MethodParameter methodParameter,
      Type targetType,
      Class<? extends HttpMessageConverter<?>> converterType) {

    // We only have to work, when we are called in a writing context (with body JSON data) in
    // an "old" (v1 - v6 and "latest") endpoint
    String method = httpServletRequest.getMethod().toUpperCase(Locale.ROOT);
    switch (method) {
      case "POST":
      case "PUT":
      case "PATCH":
        return checkVersion(httpServletRequest);
      default:
        return false;
    }
  }

  private boolean checkVersion(HttpServletRequest httpServletRequest) {
    Matcher matcher = VERSION_PATTERN.matcher(httpServletRequest.getRequestURL());
    if (matcher.matches()) {
      int version = Integer.parseInt(matcher.group(1).substring(1));
      if (version > LATEST_VERSION_WHICH_REQUIRES_FIX) {
        return false; // nothing to do, go on with further processing
      }
    }
    return true; // No version indicates "latest" as version info
  }

  @Override
  public HttpInputMessage beforeBodyRead(
      HttpInputMessage inputMessage,
      MethodParameter parameter,
      Type targetType,
      Class<? extends HttpMessageConverter<?>> converterType)
      throws IOException {
    String body = new String(inputMessage.getBody().readAllBytes(), StandardCharsets.UTF_8);
    HttpInputMessage fixedInputMessage = inputMessage;

    if (body.length() >= 2) {
      // everthing else cannot be valid json in parentheses
      JSONObject jsonObject = new JSONObject(body);
      jsonObject = fixJsonObject(jsonObject);
      fixedInputMessage =
          new FixedHttpInputMessage(inputMessage.getHeaders(), jsonObject.toString());
    }

    return super.beforeBodyRead(fixedInputMessage, parameter, targetType, converterType);
  }

  private static JSONObject fixJsonObject(JSONObject jsonObject) {
    // Check the JSON object itself
    if (!jsonObject.has(ATTR_OBJECTTYPE)) {
      guessAndSetObjectType(jsonObject);
    }

    // Check embedded JSON objects
    for (Iterator<String> keyIt = jsonObject.keys(); keyIt.hasNext(); ) {
      String key = keyIt.next();
      Object jsonObj = jsonObject.get(key);
      if (jsonObj instanceof JSONObject) {
        // Recurse down
        jsonObject.put(key, fixJsonObject((JSONObject) jsonObj));
      }
      if (jsonObj instanceof JSONArray) {
        // Iterate over all elements
        jsonObject.put(key, fixJsonArray((JSONArray) jsonObj));
      }
    }
    return jsonObject;
  }

  private static JSONArray fixJsonArray(JSONArray jsonArr) {
    for (int i = 0; i < jsonArr.length(); i++) {
      Object obj = jsonArr.get(i);
      if (obj instanceof JSONObject) {
        // Recurse down
        jsonArr.put(i, fixJsonObject((JSONObject) obj));
      }
      if (obj instanceof JSONArray) {
        // Iterate over all elements
        jsonArr.put(i, fixJsonArray((JSONArray) obj));
      }
    }
    return jsonArr;
  }

  /**
   * Try to guess the required ObjectType and set it.
   *
   * <p>This is pretty heuristic, but that's acceptable, since the cases, where the ObjectType
   * attribute is missing, are very limited and edgy (only a certain combination of a more recent
   * cudami service and a little too old cudami client)
   *
   * @param jsonObject the JSONObject, in which the objectType attribute is missing and will be set.
   */
  private static void guessAndSetObjectType(JSONObject jsonObject) {
    // Identifier
    if (jsonObject.has("namespace") && jsonObject.has("id")) {
      jsonObject.put(ATTR_OBJECTTYPE, "IDENTIFIER");
      return;
    }

    // License
    if (jsonObject.has("acronym") && jsonObject.has("url")) {
      jsonObject.put(ATTR_OBJECTTYPE, "LICENSE");
      return;
    }

    // Identifiable:
    if (jsonObject.has("identifiableObjectType")) {
      jsonObject.put(ATTR_OBJECTTYPE, "IDENTIFIABLE");
      return;
    }

    // IdentifierType
    if (jsonObject.has("label") && jsonObject.has("namespace") && jsonObject.has("pattern")) {
      jsonObject.put(ATTR_OBJECTTYPE, "IDENTIFIER_TYPE");
      return;
    }

    // Predicate
    if (jsonObject.has("description") && jsonObject.has("label") && jsonObject.has("value")) {
      jsonObject.put(ATTR_OBJECTTYPE, "PREDICATE");
      return;
    }

    // RenderingTemplate
    if (jsonObject.has("label") && jsonObject.has("description") && jsonObject.has("name")) {
      jsonObject.put(ATTR_OBJECTTYPE, "RENDERING_TEMPLATE");
      return;
    }

    // User
    if (jsonObject.has("email") && jsonObject.has("enabled") && jsonObject.has("passwordHash")) {
      jsonObject.put(ATTR_OBJECTTYPE, "USER");
      return;
    }

    // The headword is so un-specific, it can only be handled as very last attempt
    if (jsonObject.has("label") && jsonObject.has("locale")) {
      jsonObject.put(ATTR_OBJECTTYPE, "HEADWORD");
      return;
    }
  }

  private static class FixedHttpInputMessage implements HttpInputMessage {

    private final HttpHeaders httpHeaders;
    private final String body;

    public FixedHttpInputMessage(HttpHeaders httpHeaders, String body) {
      this.httpHeaders = httpHeaders;
      this.body = body;
    }

    @Override
    public InputStream getBody() throws IOException {
      return new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public HttpHeaders getHeaders() {
      return httpHeaders;
    }
  }
}
