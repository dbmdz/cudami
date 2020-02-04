package de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.resource.FileResourceBinaryRepository;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.exceptions.ResourceIOException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

@Repository
public class FileResourceBinaryRepositoryImpl implements FileResourceBinaryRepository {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(FileResourceBinaryRepositoryImpl.class);

  @Value(value = "${cudami.server.address}")
  private String cudamiServerAddress;

  @Autowired ObjectMapper objectMapper;

  @Override
  public FileResource upload(InputStream inputStream, String filename, String contentType)
      throws ResourceIOException {
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
      throw new ResourceIOException("Error saving uploaded file data", ex);
    }
  }

  @Override
  public FileResource upload(byte[] bytes, String filename, String contentType)
      throws ResourceIOException {
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
      throw new ResourceIOException("Error saving uploaded file data", ex);
    }
  }

  private FileResource doPost(HttpEntity entity)
      throws UnsupportedOperationException, IOException, ResourceIOException {
    HttpPost post = new HttpPost(cudamiServerAddress + "/latest/files");
    post.setEntity(entity);
    HttpClient client = HttpClientBuilder.create().build();
    HttpResponse response = client.execute(post);

    if (response.getStatusLine().getStatusCode() == HttpStatus.OK.value()) {
      FileResource fileResource =
          objectMapper.readValue(response.getEntity().getContent(), FileResource.class);
      return fileResource;
    }
    throw new ResourceIOException("Error saving uploaded file data");
  }
}
