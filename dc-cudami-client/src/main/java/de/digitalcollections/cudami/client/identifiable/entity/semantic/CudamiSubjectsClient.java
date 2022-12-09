package de.digitalcollections.cudami.client.identifiable.entity.semantic;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiRestClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.exception.http.client.ResourceNotFoundException;
import de.digitalcollections.model.semantic.Subject;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import org.apache.commons.codec.binary.Base64;

public class CudamiSubjectsClient extends CudamiRestClient<Subject> {

  public CudamiSubjectsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Subject.class, mapper, API_VERSION_PREFIX + "/subjects");
  }

  /**
   * Retrieves a subject by its namespace and id
   *
   * @param namespace the namespace. Must be plain text, not encoded in any way
   * @param id the id. Must be in plain text, not encoded in any way
   * @return the Tag or null
   * @throws TechnicalException in case of an error
   */
  public Subject getByIdentifier(String namespace, String id) throws TechnicalException {
    String namespaceAndId = namespace + ":" + id;

    String encodedNamespaceAndId =
        Base64.encodeBase64URLSafeString(namespaceAndId.getBytes(StandardCharsets.UTF_8));

    try {
      return doGetRequestForObject(
          String.format(baseEndpoint + "/identifier/%s", encodedNamespaceAndId));
    } catch (ResourceNotFoundException e) {
      return null;
    }
  }

  /**
   * Retrieves a subject by its type, namespace and id
   *
   * @param type the type. Must be plain text, not encoded in any wav
   * @param namespace the namespace. Must be plain text, not encoded in any way
   * @param id the id. Must be in plain text, not encoded in any way
   * @return the Subject or null
   * @throws TechnicalException in case of an error
   */
  public Subject getByTypeAndIdentifier(String type, String namespace, String id)
      throws TechnicalException {
    String typeAndNamespaceAndId = type + ":" + namespace + ":" + id;

    String encodedTypeAndNamespaceAndId =
        Base64.encodeBase64URLSafeString(typeAndNamespaceAndId.getBytes(StandardCharsets.UTF_8));

    try {
      return doGetRequestForObject(
          String.format(baseEndpoint + "/identifier/%s", encodedTypeAndNamespaceAndId));
    } catch (ResourceNotFoundException e) {
      return null;
    }
  }
}
