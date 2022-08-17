package de.digitalcollections.cudami.server.controller.advice;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
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
    String method = httpServletRequest.getMethod().toUpperCase(Locale.ROOT);

    Matcher matcher = VERSION_PATTERN.matcher(httpServletRequest.getRequestURL());
    if (matcher.matches()) {
      int version = Integer.parseInt(matcher.group(1).substring(1));
      if (version > LATEST_VERSION_WHICH_REQUIRES_FIX) {
        return false; // nothing to do, go on with further processing
      }
    }

    switch (method) {
      case "POST":
      case "PUT":
      case "PATCH":
        return true;
      default:
        return false;
    }
  }

  @Override
  public HttpInputMessage beforeBodyRead(
      HttpInputMessage inputMessage,
      MethodParameter parameter,
      Type targetType,
      Class<? extends HttpMessageConverter<?>> converterType)
      throws IOException {
    String body = new String(inputMessage.getBody().readAllBytes());
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
    if (!jsonObject.has(ATTR_OBJECTTYPE)) {
      if (jsonObject.has("namespace") && jsonObject.has("id")) {
        // We have an Identifier
        jsonObject.put(ATTR_OBJECTTYPE, "IDENTIFIER");
      }
      if (jsonObject.has("acronym") && jsonObject.has("url")) {
        // We have a license
        jsonObject.put(ATTR_OBJECTTYPE, "LICENSE");
      }
    }

    for (Iterator<String> keyIt = jsonObject.keys(); keyIt.hasNext(); ) {
      String key = keyIt.next();
      Object jsonObj = jsonObject.get(key);
      if (jsonObj instanceof JSONObject) {
        // Recurse down
        jsonObject.put(key, fixJsonObject((JSONObject) jsonObj));
      }
      if (jsonObj instanceof JSONArray) {
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
        jsonArr.put(i, fixJsonArray((JSONArray) obj));
      }
    }
    return jsonArr;
  }

  private class FixedHttpInputMessage implements HttpInputMessage {

    private final HttpHeaders httpHeaders;
    private final String body;

    public FixedHttpInputMessage(HttpHeaders httpHeaders, String body) {
      this.httpHeaders = httpHeaders;
      this.body = body;
    }

    @Override
    public InputStream getBody() throws IOException {
      return new ByteArrayInputStream(body.getBytes());
    }

    @Override
    public HttpHeaders getHeaders() {
      return httpHeaders;
    }
  }
}
