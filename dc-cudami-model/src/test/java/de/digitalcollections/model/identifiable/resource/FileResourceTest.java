package de.digitalcollections.model.identifiable.resource;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.model.file.MimeType;
import java.net.URI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The FileResource")
public class FileResourceTest {

  @Test
  public void testGetFilenameAndExtensionFromUri() {
    FileResource instance = new FileResource();
    instance.setUri(
        URI.create(
            "https://commons.wikimedia.org/wiki/Special:FilePath/DFG-logo-blau.svg?width=270"));
    String result = instance.getFilename();
    String expResult = "DFG-logo-blau.svg";
    assertThat(result).isEqualTo(expResult);

    String extension = instance.getFilenameExtension();
    assertThat(extension).isEqualTo("svg");
  }

  /** Test of getFilenameExtension method, of class FileResource. */
  @Test
  public void testGetFilenameExtension() {
    FileResource instance = new ImageFileResource();

    instance.setFilename("image001.jpg");
    String expResult = "jpg";
    String result = instance.getFilenameExtension();
    assertThat(result).isEqualTo(expResult);

    instance.setFilename("image001.txt.jpg");
    expResult = "jpg";
    result = instance.getFilenameExtension();
    assertThat(result).isEqualTo(expResult);

    instance.setFilename(".bashrc");
    expResult = "bashrc";
    result = instance.getFilenameExtension();
    assertThat(result).isEqualTo(expResult);

    instance.setFilename("image.j");
    expResult = "j";
    result = instance.getFilenameExtension();
    assertThat(result).isEqualTo(expResult);

    instance.setFilename("image.");
    expResult = null;
    result = instance.getFilenameExtension();
    assertThat(result).isEqualTo(expResult);

    instance.setFilename("image");
    expResult = null;
    result = instance.getFilenameExtension();
    assertThat(result).isEqualTo(expResult);

    instance.setFilename(".");
    expResult = null;
    result = instance.getFilenameExtension();
    assertThat(result).isEqualTo(expResult);

    instance.setFilename("..");
    expResult = null;
    result = instance.getFilenameExtension();
    assertThat(result).isEqualTo(expResult);

    instance.setFilename(null);
    expResult = null;
    result = instance.getFilenameExtension();
    assertThat(result).isEqualTo(expResult);
  }

  @Test
  public void testGetFilenameFromUri() {
    FileResource instance = new FileResource();
    instance.setUri(URI.create("http://localhost/iiif/some/other/default.jpg"));
    String result = instance.getFilename();
    String expResult = "default.jpg";
    assertThat(result).isEqualTo(expResult);
  }

  @Test
  public void testToString() {
    FileResource instance = new ImageFileResource();
    instance.setFilename("image001.xyz123");
    instance.setMimeType(MimeType.fromExtension("xyz123"));
    String result = instance.toString();
    assertThat(result).isNotEqualTo(null);
  }
}
