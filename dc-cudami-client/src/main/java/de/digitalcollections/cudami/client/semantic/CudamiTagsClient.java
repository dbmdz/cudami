package de.digitalcollections.cudami.client.semantic;

import static de.digitalcollections.cudami.client.CudamiRestClient.API_VERSION_PREFIX;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiRestClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.exception.http.client.ResourceNotFoundException;
import de.digitalcollections.model.semantic.Tag;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import org.apache.commons.codec.binary.Base64;

public class CudamiTagsClient extends CudamiRestClient<Tag> {

  public CudamiTagsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Tag.class, mapper, API_VERSION_PREFIX + "/tags");
  }

  /**
   * Retrieves a tag by its value
   *
   * @param value the type. Must be plain text, not encoded in any way
   * @return the Tag or null
   * @throws TechnicalException in case of an error
   */
  public Tag getByValue(String value) throws TechnicalException {
    String encodedValue = Base64.encodeBase64URLSafeString(value.getBytes(StandardCharsets.UTF_8));

    try {
      return doGetRequestForObject(String.format(baseEndpoint + "/value/%s", encodedValue));
    } catch (ResourceNotFoundException e) {
      return null;
    }
  }
}
