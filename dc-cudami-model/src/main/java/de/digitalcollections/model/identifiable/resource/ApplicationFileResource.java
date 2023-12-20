package de.digitalcollections.model.identifiable.resource;

import de.digitalcollections.model.file.MimeType;
import lombok.experimental.SuperBuilder;

/** An application file resource. Mimetype starts with "application/". */
@SuperBuilder(buildMethodName = "prebuild")
public class ApplicationFileResource extends FileResource {

  public ApplicationFileResource() {
    super();
  }

  @Override
  protected void init() {
    super.init();
    this.fileResourceType = FileResourceType.APPLICATION;
    if (getMimeType() == null) {
      this.setMimeType(MimeType.MIME_APPLICATION_OCTET_STREAM);
    }
  }

  public abstract static class ApplicationFileResourceBuilder<
          C extends ApplicationFileResource, B extends ApplicationFileResourceBuilder<C, B>>
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
