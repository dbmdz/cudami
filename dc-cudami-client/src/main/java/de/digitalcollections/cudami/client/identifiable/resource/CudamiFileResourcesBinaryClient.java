package de.digitalcollections.cudami.client.identifiable.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.exception.ResourceIOException;
import de.digitalcollections.model.identifiable.resource.FileResource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;

public class CudamiFileResourcesBinaryClient {

  protected final ObjectMapper mapper;
  protected final URI serverUri;

  public CudamiFileResourcesBinaryClient(String serverUrl, ObjectMapper mapper) {
    this.mapper = mapper;
    this.serverUri = URI.create(serverUrl);
  }

  public FileResource create() {
    return new FileResource();
  }

  private FileResource doPost(HttpEntity entity) throws HttpException {
    try {
      HttpPost post = new HttpPost(serverUri + "/v5/files");
      post.setEntity(entity);
      HttpClient client = HttpClientBuilder.create().build();
      HttpResponse response = client.execute(post);

      if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
        FileResource fileResource =
            mapper.readValue(response.getEntity().getContent(), FileResource.class);
        return fileResource;
      }
      throw new ResourceIOException("Error saving uploaded file data");
    } catch (IOException ex) {
      throw new HttpException("Error posting data to server", ex);
    }
  }

  public FileResource upload(InputStream inputStream, String filename, String contentType)
      throws HttpException {
    try {
      filename =
          URLEncoder.encode(
              filename,
              StandardCharsets.UTF_8.toString()); // filenames with umlauts caused exception...
      HttpEntity entity =
          MultipartEntityBuilder.create()
              .addBinaryBody(contentType, inputStream, ContentType.create(contentType), filename)
              .build();
      return doPost(entity);
    } catch (Exception ex) {
      throw new HttpException("Error saving uploaded file data", ex);
    }
  }

  public FileResource upload(byte[] bytes, String filename, String contentType)
      throws HttpException {
    try {
      filename =
          URLEncoder.encode(
              filename,
              StandardCharsets.UTF_8.toString()); // filenames with umlauts caused exception...
      HttpEntity entity =
          MultipartEntityBuilder.create()
              .addBinaryBody(contentType, bytes, ContentType.create(contentType), filename)
              .build();
      return doPost(entity);
    } catch (Exception ex) {
      throw new HttpException("Error saving uploaded file data", ex);
    }
  }
}
