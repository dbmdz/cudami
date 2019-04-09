package de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.resource.CudamiFileResourceRepository;
import de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.IdentifiableRepositoryImpl;
import de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.IdentifiableRepositoryImpl.FindParams;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.exceptions.ResourceIOException;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.resource.FileResourceImpl;
import feign.form.FormData;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.logging.Level;
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
public class CudamiFileResourceRepositoryImpl<F extends FileResource> extends IdentifiableRepositoryImpl<F> implements CudamiFileResourceRepository<F> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CudamiFileResourceRepositoryImpl.class);
  @Value(value = "${cudami.server.address}")
  private String cudamiServerAddress;

  @Autowired
  private CudamiFileResourceRepositoryEndpoint endpoint;

  @Autowired
  ObjectMapper objectMapper;

  @Override
  public long count() {
    return endpoint.count();
  }

  @Override
  public F create() {
    return (F) new FileResourceImpl();
  }

  @Override
  public PageResponse<F> find(PageRequest pageRequest) {
    FindParams f = getFindParams(pageRequest);
    PageResponse<FileResource> pageResponse = endpoint.find(f.getPageNumber(), f.getPageSize(), f.getSortField(), f.getSortDirection(), f.getNullHandling());
    return getGenericPageResponse(pageResponse);
  }

  @Override
  public F findOne(UUID uuid) {
    return (F) endpoint.findOne(uuid);
  }

  @Override
  public F save(F identifiable) {
    return (F) endpoint.save(identifiable);
  }

  @Override
  public F save(FileResource fileResource, byte[] bytes) {
    String contentType = fileResource.getMimeType().getTypeName();
    String fileName = fileResource.getFilename();
    FormData formData = new FormData(contentType, fileName, bytes);
    return (F) endpoint.save(fileResource, formData);
  }

  @Override
  public F update(F identifiable) {
    return (F) endpoint.update(identifiable.getUuid(), identifiable);
  }

  @Override
  public F upload(InputStream inputStream, String filename, String contentType) throws ResourceIOException {
    try {
      filename = URLEncoder.encode(filename, StandardCharsets.UTF_8.toString()); // filenames with umlauts caused exception...
      HttpEntity entity = MultipartEntityBuilder.create()
              .addBinaryBody(contentType, inputStream, ContentType.create(contentType), filename)
              .build();
      return doPost(entity);
    } catch (Exception ex) {
      throw new ResourceIOException("Error saving uploaded file data", ex);
    }
  }

  @Override
  public F upload(byte[] bytes, String filename, String contentType) throws ResourceIOException {
    try {
      filename = URLEncoder.encode(filename, StandardCharsets.UTF_8.toString()); // filenames with umlauts caused exception...
      HttpEntity entity = MultipartEntityBuilder.create()
              .addBinaryBody(contentType, bytes, ContentType.create(contentType), filename)
              .build();
      return doPost(entity);
    } catch (Exception ex) {
      throw new ResourceIOException("Error saving uploaded file data", ex);
    }
  }

  private F doPost(HttpEntity entity) throws UnsupportedOperationException, IOException, ResourceIOException {
    HttpPost post = new HttpPost(cudamiServerAddress + "/latest/fileresources/new/upload");
    post.setEntity(entity);
    HttpClient client = HttpClientBuilder.create().build();
    HttpResponse response = client.execute(post);
    
    if (response.getStatusLine().getStatusCode() == HttpStatus.OK.value()) {
      FileResource fileResource = objectMapper.readValue(response.getEntity().getContent(), FileResource.class);
      return (F) fileResource;
    }
    throw new ResourceIOException("Error saving uploaded file data");
  }
}
