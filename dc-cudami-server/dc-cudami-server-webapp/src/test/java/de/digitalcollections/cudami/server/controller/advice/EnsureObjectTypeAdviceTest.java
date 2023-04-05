package de.digitalcollections.cudami.server.controller.advice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.IdentifiableObjectType;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.IdentifierType;
import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.entity.item.Item;
import de.digitalcollections.model.jackson.DigitalCollectionsObjectMapper;
import de.digitalcollections.model.legal.License;
import de.digitalcollections.model.relation.Predicate;
import de.digitalcollections.model.security.User;
import de.digitalcollections.model.semantic.Headword;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.view.RenderingTemplate;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
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
    methodParameter = mock(MethodParameter.class);
    targetType = mock(Type.class);
    converterType = MappingJackson2HttpMessageConverter.class;
    objectMapper = new DigitalCollectionsObjectMapper();
  }

  @DisplayName("ignores DELETE requests")
  @Test
  public void doesNothingForDelete() {
    advice = new EnsureObjectTypeAdvice(buildRequest("DELETE", "https://foo.bar/v6/bla"));

    assertThat(advice.supports(methodParameter, targetType, converterType)).isFalse();
  }

  @DisplayName("ignores GET requests")
  @Test
  public void doesNothingForGet() {
    advice = new EnsureObjectTypeAdvice(buildRequest("GET", "https://foo.bar/v6/bla"));

    assertThat(advice.supports(methodParameter, targetType, converterType)).isFalse();
  }

  @DisplayName("ignores HEAD requests")
  @Test
  public void doesNothingForHead() {
    advice = new EnsureObjectTypeAdvice(buildRequest("HEAD", "https://foo.bar/v6/bla"));

    assertThat(advice.supports(methodParameter, targetType, converterType)).isFalse();
  }

  @DisplayName("handles POST requests for versions less than 7")
  @Test
  public void handlesForVersionLessThatSeven() {
    advice = new EnsureObjectTypeAdvice(buildRequest("POST", "https://foo.bar/v6/bla"));

    assertThat(advice.supports(methodParameter, targetType, converterType)).isTrue();
  }

  @DisplayName("handles PUT requests for versions less than 7")
  @Test
  public void handlesPutForVersionLessThatSeven() {
    advice = new EnsureObjectTypeAdvice(buildRequest("PUT", "https://foo.bar/v6/bla"));

    assertThat(advice.supports(methodParameter, targetType, converterType)).isTrue();
  }

  @DisplayName("handles PATCH requests for versions less than 7")
  @Test
  public void handlesPatchForVersionLessThatSeven() {
    advice = new EnsureObjectTypeAdvice(buildRequest("PATCH", "https://foo.bar/v6/bla"));

    assertThat(advice.supports(methodParameter, targetType, converterType)).isTrue();
  }

  @DisplayName("does not handle requests for version 7")
  @Test
  public void doesNotHandleForVersionSeven() {
    advice = new EnsureObjectTypeAdvice(buildRequest("POST", "https://foo.bar/v7/bla"));

    assertThat(advice.supports(methodParameter, targetType, converterType)).isFalse();
  }

  @DisplayName("handles requests for version 6 under different context")
  @Test
  public void canWorkForVersionSixUnderDifferentContext() {
    advice = new EnsureObjectTypeAdvice(buildRequest("POST", "https://foo.bar/cudami/v6/bla"));

    assertThat(advice.supports(methodParameter, targetType, converterType)).isTrue();
  }

  @DisplayName("does not handle requests for two digit versions")
  @Test
  public void canWorkForVersionTen() {
    advice = new EnsureObjectTypeAdvice(buildRequest("POST", "https://foo.bar/v10/bla"));

    assertThat(advice.supports(methodParameter, targetType, converterType)).isFalse();
  }

  @DisplayName("can work with an empty POST body")
  @Test
  public void emptyPostBody() throws IOException {
    advice = new EnsureObjectTypeAdvice(mock(HttpServletRequest.class));
    String actual = getActualJsonFromAdvice("");
    assertThat(actual).isEqualTo("");
  }

  @DisplayName("can work with an empty JSON POST body")
  @Test
  public void emptyJSONPostBody() throws IOException {
    advice = new EnsureObjectTypeAdvice(mock(HttpServletRequest.class));
    String actual = getActualJsonFromAdvice("{}");
    assertThat(actual).isEqualTo("{}");
  }

  @DisplayName("can fix the objectType in Identifier, when missing")
  @Test
  public void missingObjectTypeInIdentifier() throws IOException, JSONException {
    advice = new EnsureObjectTypeAdvice(mock(HttpServletRequest.class));
    Identifier identifier = Identifier.builder().namespace("Foo").id("Bar").build();
    String missingObjectTypeJSON = getMissingObjectTypeJSON(identifier);

    String expectedJSON = objectMapper.writeValueAsString(identifier);

    String actual = getActualJsonFromAdvice(missingObjectTypeJSON);
    JSONAssert.assertEquals(expectedJSON, actual, false);
  }

  @DisplayName("can fix the objectType in a list of identifiers, when missing")
  @Test
  public void missingObjectTypeInIdentifierList() throws IOException, JSONException {
    advice = new EnsureObjectTypeAdvice(mock(HttpServletRequest.class));
    List<Identifier> identifier =
        List.of(
            Identifier.builder().namespace("Foo").id("Bar").build(),
            Identifier.builder().namespace("Baz").id("Blubb").build());
    String missingObjectTypeJSON = getMissingObjectTypeJSON(identifier);

    String expectedJSON = objectMapper.writeValueAsString(identifier);

    String actual = getActualJsonFromAdvice(missingObjectTypeJSON);
    JSONAssert.assertEquals(expectedJSON, actual, false);
  }

  @DisplayName("can fix the objectType in License, when missing")
  @Test
  public void missingObjectTypeInLicense() throws IOException, JSONException {
    advice = new EnsureObjectTypeAdvice(mock(HttpServletRequest.class));
    License license = License.builder().acronym("acr").url("http://foo.bar").build();
    String missingObjectTypeJSON = getMissingObjectTypeJSON(license);

    String expectedJSON = objectMapper.writeValueAsString(license);

    String actual = getActualJsonFromAdvice(missingObjectTypeJSON);
    JSONAssert.assertEquals(expectedJSON, actual, false);
  }

  @DisplayName("can fix the objectType in Identifiable, when missing")
  @Test
  public void missingObjectTypeInIdentifiable() throws IOException, JSONException {
    advice = new EnsureObjectTypeAdvice(mock(HttpServletRequest.class));
    Identifiable identifiable =
        Identifiable.builder()
            .label("Hallo")
            .identifiableObjectType(IdentifiableObjectType.IDENTIFIABLE)
            .build();
    String missingObjectTypeJSON = getMissingObjectTypeJSON(identifiable);

    String expectedJSON = objectMapper.writeValueAsString(identifiable);

    String actual = getActualJsonFromAdvice(missingObjectTypeJSON);
    JSONAssert.assertEquals(expectedJSON, actual, false);
  }

  @DisplayName("can fix the objectType in IdentifierType, when missing")
  @Test
  public void missingObjectTypeInIdentifierType() throws IOException, JSONException {
    advice = new EnsureObjectTypeAdvice(mock(HttpServletRequest.class));
    IdentifierType identifierType =
        IdentifierType.builder().label("foo").pattern(".*+").namespace("bar").build();
    String missingObjectTypeJSON = getMissingObjectTypeJSON(identifierType);

    String expectedJSON = objectMapper.writeValueAsString(identifierType);

    String actual = getActualJsonFromAdvice(missingObjectTypeJSON);
    JSONAssert.assertEquals(expectedJSON, actual, false);
  }

  @DisplayName("can fix the objectType in Predicate, when missing")
  @Test
  public void missingObjectTypeInPredicate() throws IOException, JSONException {
    advice = new EnsureObjectTypeAdvice(mock(HttpServletRequest.class));
    Predicate predicate = new Predicate();
    predicate.setLabel(new LocalizedText(Locale.GERMAN, "Test"));
    predicate.setDescription(new LocalizedText(Locale.GERMAN, "Das ist ein Test"));
    predicate.setValue("foo");
    String missingObjectTypeJSON = getMissingObjectTypeJSON(predicate);

    String expectedJSON = objectMapper.writeValueAsString(predicate);

    String actual = getActualJsonFromAdvice(missingObjectTypeJSON);
    JSONAssert.assertEquals(expectedJSON, actual, false);
  }

  @DisplayName("can fix the objectType in RenderingTemplate, when missing")
  @Test
  public void missingObjectTypeInRenderingTemplate() throws IOException, JSONException {
    advice = new EnsureObjectTypeAdvice(mock(HttpServletRequest.class));
    RenderingTemplate renderingTemplate =
        RenderingTemplate.builder()
            .label(Locale.GERMAN, "Foo")
            .description(Locale.GERMAN, "Bar")
            .name("bar")
            .build();
    String missingObjectTypeJSON = getMissingObjectTypeJSON(renderingTemplate);

    String expectedJSON = objectMapper.writeValueAsString(renderingTemplate);

    String actual = getActualJsonFromAdvice(missingObjectTypeJSON);
    JSONAssert.assertEquals(expectedJSON, actual, false);
  }

  @DisplayName("can fix the objectType in User, when missing")
  @Test
  public void missingObjectTypeInUser() throws IOException, JSONException {
    advice = new EnsureObjectTypeAdvice(mock(HttpServletRequest.class));
    User user = new User();
    user.setEmail("foo@bar.bla");
    user.setEnabled(false);
    user.setPasswordHash("bar");
    String missingObjectTypeJSON = getMissingObjectTypeJSON(user);

    String expectedJSON = objectMapper.writeValueAsString(user);

    String actual = getActualJsonFromAdvice(missingObjectTypeJSON);
    JSONAssert.assertEquals(expectedJSON, actual, false);
  }

  @DisplayName("can fix the objectType in Headword, when missing")
  @Test
  public void missingObjectTypeInHeadword() throws IOException, JSONException {
    advice = new EnsureObjectTypeAdvice(mock(HttpServletRequest.class));
    Headword headword = new Headword();
    headword.setLabel("Foo");
    headword.setLocale(Locale.GERMAN);
    String missingObjectTypeJSON = getMissingObjectTypeJSON(headword);

    String expectedJSON = objectMapper.writeValueAsString(headword);

    String actual = getActualJsonFromAdvice(missingObjectTypeJSON);
    JSONAssert.assertEquals(expectedJSON, actual, false);
  }

  @DisplayName("can insert the objectType in objects of arrays")
  @Test
  public void testArrays() throws IOException, JSONException {
    advice = new EnsureObjectTypeAdvice(mock(HttpServletRequest.class));
    Item item =
        Item.builder()
            .holders(
                List.of(
                    Person.builder()
                        .identifier(Identifier.builder().namespace("Foo").id("Bar").build())
                        .build()))
            .build();
    String missingObjectTypeJSON = getMissingObjectTypeJSON(item);

    String expectedJSON = objectMapper.writeValueAsString(item);

    String actual = getActualJsonFromAdvice(missingObjectTypeJSON);
    JSONAssert.assertEquals(expectedJSON, actual, false);
  }

  @DisplayName("does not insert a second objectType, when one is already present")
  @Test
  public void noDoubleObjectType() throws IOException, JSONException {
    advice = new EnsureObjectTypeAdvice(mock(HttpServletRequest.class));
    DigitalObject digitalObject = DigitalObject.builder().label("Test").build();
    String filledObjectTypeJSON = objectMapper.writeValueAsString(digitalObject);

    String expectedJSON = objectMapper.writeValueAsString(digitalObject);

    String actual = getActualJsonFromAdvice(filledObjectTypeJSON);
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

  private String getActualJsonFromAdvice(String missingObjectTypeJSON) throws IOException {
    HttpInputMessage httpInputMessage = buildHttpInputMessage(missingObjectTypeJSON);
    HttpInputMessage fixedHttpInputMessage =
        advice.beforeBodyRead(httpInputMessage, methodParameter, targetType, converterType);
    String actual = new String(fixedHttpInputMessage.getBody().readAllBytes());
    return actual;
  }

  private String getMissingObjectTypeJSON(Object object) throws JsonProcessingException {
    String missingObjectTypeJSON = objectMapper.writeValueAsString(object);
    missingObjectTypeJSON = missingObjectTypeJSON.replaceAll("\"objectType\":\".*?\",", "");
    assertThat(missingObjectTypeJSON).doesNotContain("objectType");
    return missingObjectTypeJSON;
  }
}
