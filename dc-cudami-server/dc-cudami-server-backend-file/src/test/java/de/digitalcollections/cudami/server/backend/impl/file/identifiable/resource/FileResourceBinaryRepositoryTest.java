package de.digitalcollections.cudami.server.backend.impl.file.identifiable.resource;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.identifiable.resource.VideoFileResource;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.io.ResourceLoader;

public class FileResourceBinaryRepositoryTest {

  private static final String FOLDER_PATH = "/cudami/fileResources";
  private static FileResourceBinaryRepositoryImpl repository;

  @BeforeAll
  public static void setUp() {
    ResourceLoader rl = Mockito.mock(ResourceLoader.class);
    repository = new FileResourceBinaryRepositoryImpl(FOLDER_PATH, null, null, rl);
  }

  @Test
  public void foobar() {
    MimeType mimeType = MimeType.MIME_IMAGE_JPEG;
    String extension = mimeType.getExtensions().get(0);
    String uuid = "135ec10b-ac65-4217-83fc-db5e9ff62cac";
    URI uri = repository.createUri(UUID.fromString(uuid), MimeType.MIME_IMAGE_JPEG);
    String expectedUri =
        "file://"
            + FOLDER_PATH
            + "/"
            + mimeType.getPrimaryType()
            + "/"
            + extension
            + "/135e/c10b/ac65/4217/83fc/db5e/9ff6/2cac/"
            + FileResourceBinaryRepositoryImpl.DEFAULT_FILENAME_WITHOUT_EXTENSION
            + "."
            + extension;
    assertThat(uri.toString()).isEqualTo(expectedUri);
  }

  @Test
  public void testCreateUriWithMimetypeAndWithoutExtension() {
    MimeType mimeType = MimeType.MIME_IMAGE;
    String uuid = "135ec10b-ac65-4217-83fc-db5e9ff62cac";
    URI uri = repository.createUri(UUID.fromString(uuid), MimeType.MIME_IMAGE);
    String expectedUri =
        "file://"
            + FOLDER_PATH
            + "/"
            + mimeType.getPrimaryType()
            + "/undefined/135e/c10b/ac65/4217/83fc/db5e/9ff6/2cac/"
            + FileResourceBinaryRepositoryImpl.DEFAULT_FILENAME_WITHOUT_EXTENSION;
    assertThat(uri.toString()).isEqualTo(expectedUri);
  }

  @Test
  public void testCreateUriWithoutMimetype() {
    String uuid = "135ec10b-ac65-4217-83fc-db5e9ff62cac";
    URI uri = repository.createUri(UUID.fromString(uuid), null);
    String expectedUri =
        "file://"
            + FOLDER_PATH
            + "/application/bin/135e/c10b/ac65/4217/83fc/db5e/9ff6/2cac/"
            + FileResourceBinaryRepositoryImpl.DEFAULT_FILENAME_WITHOUT_EXTENSION
            + ".bin";
    assertThat(uri.toString()).isEqualTo(expectedUri);
  }

  @DisplayName("can fill the attributes of a VideoFileResource")
  @Test
  public void fillAttributesImageFileResource() throws IOException {
    VideoFileResource videoFileResource = VideoFileResource.builder().build();
    videoFileResource.setHttpBaseUrl(new URL("http://foo.bar"));

    repository.fillAttributes(videoFileResource);

    assertThat(videoFileResource.getHttpBaseUrl()).isEqualTo(new URL("http://foo.bar"));
  }
}
