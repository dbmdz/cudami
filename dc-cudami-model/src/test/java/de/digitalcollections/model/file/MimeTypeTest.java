package de.digitalcollections.model.file;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import org.junit.jupiter.api.Test;

public class MimeTypeTest {

  @Test
  public void getTypeName() throws Exception {
    assertThat(MimeType.MIME_APPLICATION_JSON.getTypeName()).isEqualTo("application/json");
  }

  @Test
  public void getExtensions() throws Exception {
    assertThat(MimeType.MIME_IMAGE_JPEG.getExtensions()).containsExactly("jpg", "jpeg", "jpe");
  }

  @Test
  public void getPrimaryType() throws Exception {
    assertThat(MimeType.MIME_APPLICATION_JSON.getPrimaryType()).isEqualTo("application");
  }

  @Test
  public void getSubType() throws Exception {
    assertThat(MimeType.MIME_APPLICATION_XML.getSubType()).isEqualTo("xml");
  }

  @Test
  public void getSuffix() throws Exception {
    assertThat(MimeType.fromTypename("image/svg+xml").getSuffix()).isEqualTo("xml");
  }

  @Test
  public void getMimeTypeForMimeTypeWithoutExtensions() throws Exception {
    assertThat(MimeType.fromTypename("application/vnd.debian.binary-package").getPrimaryType())
        .isEqualTo("application");
    assertThat(MimeType.fromTypename("application/vnd.debian.binary-package").getSubType())
        .isEqualTo("vnd.debian.binary-package");
    assertThat(MimeType.fromTypename("application/vnd.debian.binary-package").getSuffix()).isNull();
    assertThat(MimeType.fromTypename("application/vnd.debian.binary-package").getExtensions())
        .isEmpty();
  }

  @Test
  public void returnsAppOctectStreamForUnknownMimetype1() throws Exception {
    assertThat(MimeType.fromTypename("foo/bar").getTypeName())
        .isEqualTo("application/octet-stream");
  }

  @Test
  public void returnsNullForUnknownMimetype2() throws Exception {
    assertThat(MimeType.fromExtension("xcfxy").getTypeName()).isEqualTo("application/octet-stream");
  }

  @Test
  public void testEquals() throws Exception {
    MimeType mime =
        MimeType.fromURI(
            URI.create(
                "file:/bsbstruc/content/bsb_content0009/bsb00092995/xml/standard/2.2/bsb00092995_page.xml"));
    assertThat(mime.matches(MimeType.MIME_APPLICATION_XML)).isTrue();
  }

  @Test
  public void testTotalWildcard() throws Exception {
    MimeType mime = MimeType.fromTypename("application/ecmascript");
    assertThat(mime.matches(MimeType.MIME_WILDCARD)).isTrue();
    assertThat(MimeType.MIME_WILDCARD.matches(mime)).isTrue();
  }

  @Test
  public void testPartialWildcard() throws Exception {
    assertThat(MimeType.MIME_IMAGE_PNG.matches(MimeType.MIME_IMAGE)).isTrue();
    assertThat(MimeType.MIME_IMAGE.matches(MimeType.MIME_IMAGE_PNG)).isTrue();
    assertThat(MimeType.MIME_IMAGE_TIF.matches(MimeType.MIME_IMAGE)).isTrue();
    assertThat(MimeType.MIME_IMAGE.matches(MimeType.MIME_IMAGE_TIF)).isTrue();
    assertThat(MimeType.MIME_APPLICATION_JSON.matches(MimeType.MIME_IMAGE)).isFalse();
    assertThat(MimeType.MIME_IMAGE.matches(MimeType.MIME_APPLICATION_JSON)).isFalse();
  }

  @Test
  public void testHandlesUppercase() throws Exception {
    assertThat(MimeType.fromFilename("INFO.TIF").matches(MimeType.MIME_IMAGE_TIF)).isTrue();
  }

  @Test
  public void testFromFilename() throws Exception {
    assertThat(MimeType.fromFilename("hahaha.tar.gz").getTypeName())
        .isEqualTo("application/octet-stream");
    assertThat(MimeType.fromFilename("hahaha.zip").getTypeName()).isEqualTo("application/zip");
    assertThat(
            MimeType.fromFilename("libjavascriptcoregtk-1.0-0_2.4.11-3ubuntu3_amd64.deb")
                .getTypeName())
        .isEqualTo("application/x-debian-package");
  }

  @Test
  public void knowsOfTypesWithoutExtensions() throws Exception {
    assertThat(MimeType.fromTypename("text/xml")).isNotNull();
    assertThat(MimeType.fromTypename("text/markdown")).isNotNull();
    assertThat(MimeType.fromTypename("image/*")).isEqualTo(MimeType.MIME_IMAGE);
  }
}
