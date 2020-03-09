package de.digitalcollections.cudami.server.backend.impl.file.identifiable.resource;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.model.api.identifiable.resource.MimeType;
import java.net.URI;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.io.ResourceLoader;

public class FileResourceBinaryRepositoryTest {

  private static final String FOLDER_PATH = "/cudami/fileResources";
  private static FileResourceBinaryRepositoryImpl repository;

  @BeforeAll
  public static void setUp() {
    ResourceLoader rl = Mockito.mock(ResourceLoader.class);
    repository = new FileResourceBinaryRepositoryImpl(FOLDER_PATH, rl);
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
            + uuid
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
            + uuid;
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
            + uuid
            + ".bin";
    assertThat(uri.toString()).isEqualTo(expectedUri);
  }
}
