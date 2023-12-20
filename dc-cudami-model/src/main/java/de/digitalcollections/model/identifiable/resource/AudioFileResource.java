package de.digitalcollections.model.identifiable.resource;

import de.digitalcollections.model.file.MimeType;
import lombok.experimental.SuperBuilder;

/** An audio file resource. Mimetype starts with "audio/". */
@SuperBuilder(buildMethodName = "prebuild")
public class AudioFileResource extends FileResource {

  private int duration;

  public AudioFileResource() {
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
    this.fileResourceType = FileResourceType.AUDIO;
    if (getMimeType() == null) {
      this.setMimeType(MimeType.MIME_AUDIO);
    }
  }

  /**
   * @param duration duration in seconds
   */
  public void setDuration(int duration) {
    this.duration = duration;
  }

  public abstract static class AudioFileResourceBuilder<
          C extends AudioFileResource, B extends AudioFileResourceBuilder<C, B>>
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
