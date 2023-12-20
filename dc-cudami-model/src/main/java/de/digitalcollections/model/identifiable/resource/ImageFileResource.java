package de.digitalcollections.model.identifiable.resource;

import de.digitalcollections.model.file.MimeType;
import java.util.Objects;
import lombok.experimental.SuperBuilder;

/** An image file resource. Mimetype starts with "image/". */
@SuperBuilder(buildMethodName = "prebuild")
public class ImageFileResource extends FileResource {

  private int height;
  private int width;

  public ImageFileResource() {
    super();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ImageFileResource)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    ImageFileResource that = (ImageFileResource) o;
    return height == that.height && width == that.width;
  }

  /**
   * @return height in pixel
   */
  public int getHeight() {
    return height;
  }

  /**
   * @return width in pixel
   */
  public int getWidth() {
    return width;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), height, width);
  }

  @Override
  protected void init() {
    super.init();
    this.fileResourceType = FileResourceType.IMAGE;
    if (getMimeType() == null) {
      this.setMimeType(MimeType.MIME_IMAGE);
    }
  }

  /**
   * @param height height in pixel
   */
  public void setHeight(int height) {
    this.height = height;
  }

  /**
   * @param width width in pixel
   */
  public void setWidth(int width) {
    this.width = width;
  }

  public abstract static class ImageFileResourceBuilder<
          C extends ImageFileResource, B extends ImageFileResourceBuilder<C, B>>
      extends FileResourceBuilder<C, B> {

    @Override
    public C build() {
      C c = prebuild();
      c.init();
      setInternalReferences(c);
      return c;
    }
  }
}
