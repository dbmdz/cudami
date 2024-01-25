package de.digitalcollections.model.identifiable.resource;

import de.digitalcollections.model.file.MimeType;
import lombok.experimental.SuperBuilder;

/** A text file resource. Mimetype starts with "text/". */
@SuperBuilder(buildMethodName = "prebuild")
public class TextFileResource extends FileResource {

  public TextFileResource() {
    super();
  }

  @Override
  protected void init() {
    super.init();
    this.fileResourceType = FileResourceType.TEXT;
    if (getMimeType() == null) {
      this.setMimeType(MimeType.MIME_TEXT);
    }
  }

  public abstract static class TextFileResourceBuilder<
          C extends TextFileResource, B extends TextFileResourceBuilder<C, B>>
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
