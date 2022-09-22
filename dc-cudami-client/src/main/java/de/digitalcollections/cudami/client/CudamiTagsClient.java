package de.digitalcollections.cudami.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.semantic.Tag;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import org.apache.commons.codec.binary.Base64;

public class CudamiTagsClient extends CudamiRestClient<Tag> {

  public CudamiTagsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Tag.class, mapper, API_VERSION_PREFIX + "/tags");
  }

  /**
   * Retrieves a tag by its namespace and id
   *
   * @param type the type. Must be plain text, not encoded in any wav
   * @param namespace the namespace. Must be plain text, not encoded in any way
   * @param id the id. Must be in plain text, not encoded in any way
   * @return the Tag or null
   * @throws TechnicalException in case of an error
   */
  public Tag getByTypeAndIdentifier(String type, String namespace, String id)
      throws TechnicalException {
    String namespaceAndId = type + ":" + namespace + ":" + id;

    String encodedNamespaceAndId =
        Base64.encodeBase64URLSafeString(namespaceAndId.getBytes(StandardCharsets.UTF_8));

    return doGetRequestForObject(
        String.format(baseEndpoint + "/identifier/%s", encodedNamespaceAndId));
  }
}
