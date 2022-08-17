package de.digitalcollections.cudami.server.controller.advice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.jackson.DigitalCollectionsObjectMapper;
import de.digitalcollections.model.legal.License;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@DisplayName("The EnsureObjectTypeAdvice")
class EnsureObjectTypeAdviceTest {

  private EnsureObjectTypeAdvice advice;
  private MethodParameter methodParameter;
  private Type targetType;
  private Class<? extends HttpMessageConverter<?>> converterType;
  private ObjectMapper objectMapper;

  @BeforeEach
  public void beforeEach() throws IOException {
    advice = new EnsureObjectTypeAdvice();
    methodParameter = mock(MethodParameter.class);
    targetType = mock(Type.class);
    converterType = MappingJackson2HttpMessageConverter.class;
    objectMapper = new DigitalCollectionsObjectMapper();
  }

  @DisplayName("ignores DELETE requests")
  @Test
  public void doesNothingForDelete() {
    advice.setHttpServletRequest(buildRequest("DELETE", "https://foo.bar/v6/bla"));

    assertThat(advice.supports(methodParameter, targetType, converterType)).isFalse();
  }

  @DisplayName("ignores GET requests")
  @Test
  public void doesNothingForGet() {
    advice.setHttpServletRequest(buildRequest("GET", "https://foo.bar/v6/bla"));

    assertThat(advice.supports(methodParameter, targetType, converterType)).isFalse();
  }

  @DisplayName("ignores HEAD requests")
  @Test
  public void doesNothingForHead() {
    advice.setHttpServletRequest(buildRequest("HEAD", "https://foo.bar/v6/bla"));

    assertThat(advice.supports(methodParameter, targetType, converterType)).isFalse();
  }

  @DisplayName("handles POST requests for versions less than 7")
  @Test
  public void handlesForVersionLessThatSeven() {
    advice.setHttpServletRequest(buildRequest("POST", "https://foo.bar/v6/bla"));

    assertThat(advice.supports(methodParameter, targetType, converterType)).isTrue();
  }

  @DisplayName("handles PUT requests for versions less than 7")
  @Test
  public void handlesPutForVersionLessThatSeven() {
    advice.setHttpServletRequest(buildRequest("PUT", "https://foo.bar/v6/bla"));

    assertThat(advice.supports(methodParameter, targetType, converterType)).isTrue();
  }

  @DisplayName("handles PATCH requests for versions less than 7")
  @Test
  public void handlesPatchForVersionLessThatSeven() {
    advice.setHttpServletRequest(buildRequest("PATCH", "https://foo.bar/v6/bla"));

    assertThat(advice.supports(methodParameter, targetType, converterType)).isTrue();
  }

  @DisplayName("does not handle requests for version 7")
  @Test
  public void doesNotHandleForVersionSeven() {
    advice.setHttpServletRequest(buildRequest("POST", "https://foo.bar/v7/bla"));

    assertThat(advice.supports(methodParameter, targetType, converterType)).isFalse();
  }

  @DisplayName("handes requests for version 6 under different context")
  @Test
  public void canWorkForVersionSixUnderDifferentContext() {
    advice.setHttpServletRequest(buildRequest("POST", "https://foo.bar/cudami/v6/bla"));

    assertThat(advice.supports(methodParameter, targetType, converterType)).isTrue();
  }

  @DisplayName("does not handle requests for two digit versions")
  @Test
  public void canWorkForVersionTen() {
    advice.setHttpServletRequest(buildRequest("POST", "https://foo.bar/v10/bla"));

    assertThat(advice.supports(methodParameter, targetType, converterType)).isFalse();
  }

  @DisplayName("can work with an empty POST body")
  @Test
  public void emptyPostBody() throws IOException {
    HttpInputMessage httpInputMessage = buildHttpInputMessage("");
    HttpInputMessage fixedHttpInputMessage =
        advice.beforeBodyRead(httpInputMessage, methodParameter, targetType, converterType);
    String actual = new String(fixedHttpInputMessage.getBody().readAllBytes());
    assertThat(actual).isEqualTo("");
  }

  @DisplayName("can work with an empty JSON POST body")
  @Test
  public void emptyJSONPostBody() throws IOException {
    HttpInputMessage httpInputMessage = buildHttpInputMessage("{}");
    HttpInputMessage fixedHttpInputMessage =
        advice.beforeBodyRead(httpInputMessage, methodParameter, targetType, converterType);
    String actual = new String(fixedHttpInputMessage.getBody().readAllBytes());
    assertThat(actual).isEqualTo("{}");
  }

  @DisplayName("can fix the objectType in Identifier, when missing")
  @Test
  public void missingObjectTypeInIdentifier() throws IOException, JSONException {
    Identifier identifier = new Identifier("Foo", "Bar");

    // Build the JSON without the objectType attribute
    String missingObjectTypeJSON = objectMapper.writeValueAsString(identifier);
    missingObjectTypeJSON = missingObjectTypeJSON.replaceAll("\"objectType\":\"IDENTIFIER\",", "");
    assertThat(missingObjectTypeJSON).doesNotContain("IDENTIFIER");

    String expectedJSON = objectMapper.writeValueAsString(identifier);

    HttpInputMessage httpInputMessage = buildHttpInputMessage(missingObjectTypeJSON);
    HttpInputMessage fixedHttpInputMessage =
        advice.beforeBodyRead(httpInputMessage, methodParameter, targetType, converterType);
    String actual = new String(fixedHttpInputMessage.getBody().readAllBytes());
    JSONAssert.assertEquals(expectedJSON, actual, false);
  }

  @DisplayName("can fix the objectType in License, when missing")
  @Test
  public void missingObjectTypeInLicense() throws IOException, JSONException {
    License license = License.builder().acronym("acr").url("http://foo.bar").build();

    // Build the JSON without the objectType attribute
    String missingObjectTypeJSON = objectMapper.writeValueAsString(license);
    missingObjectTypeJSON = missingObjectTypeJSON.replaceAll("\"objectType\":\"LICENSE\",", "");
    assertThat(missingObjectTypeJSON).doesNotContain("LICENSE");

    String expectedJSON = objectMapper.writeValueAsString(license);

    HttpInputMessage httpInputMessage = buildHttpInputMessage(missingObjectTypeJSON);
    HttpInputMessage fixedHttpInputMessage =
        advice.beforeBodyRead(httpInputMessage, methodParameter, targetType, converterType);
    String actual = new String(fixedHttpInputMessage.getBody().readAllBytes());
    JSONAssert.assertEquals(expectedJSON, actual, false);
  }

  // -----------------------------------

  private HttpServletRequest buildRequest(String requestMethod, String requestUrl) {
    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getMethod()).thenReturn(requestMethod);
    when(req.getRequestURL()).thenReturn(new StringBuffer(requestUrl));
    return req;
  }

  private HttpInputMessage buildHttpInputMessage(String body) throws IOException {
    HttpInputMessage httpInputMessage = mock(HttpInputMessage.class);
    when(httpInputMessage.getBody())
        .thenReturn(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
    return httpInputMessage;
  }
}
