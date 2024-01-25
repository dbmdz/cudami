package de.digitalcollections.model.identifiable.resource;

import de.digitalcollections.model.file.MimeType;
import java.util.Objects;
import lombok.experimental.SuperBuilder;

/** A video file resource. Mimetype starts with "video/". */
@SuperBuilder(buildMethodName = "prebuild")
public class VideoFileResource extends FileResource {

  private int duration;

  public VideoFileResource() {
    super();
  }

  /**
   * @return duration in seconds
   */
  public int getDuration() {
    return duration;
  }

  @Override
  protected void init() {
    super.init();
    this.fileResourceType = FileResourceType.VIDEO;
    if (getMimeType() == null) {
      this.setMimeType(MimeType.MIME_VIDEO);
    }
  }

  /**
   * @param duration duration in seconds
   */
  public void setDuration(int duration) {
    this.duration = duration;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof VideoFileResource)) return false;
    if (!super.equals(o)) return false;
    VideoFileResource that = (VideoFileResource) o;
    return duration == that.duration;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), duration);
  }

  public abstract static class VideoFileResourceBuilder<
          C extends VideoFileResource, B extends VideoFileResourceBuilder<C, B>>
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
