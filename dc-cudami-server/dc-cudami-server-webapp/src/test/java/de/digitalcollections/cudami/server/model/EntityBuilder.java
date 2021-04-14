package de.digitalcollections.cudami.server.model;

import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.EntityType;
import de.digitalcollections.model.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.text.LocalizedStructuredContent;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.text.StructuredContent;
import de.digitalcollections.model.text.contentblock.ContentBlock;
import de.digitalcollections.model.text.contentblock.Paragraph;
import de.digitalcollections.model.view.RenderingHintsPreviewImage;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.flywaydb.core.internal.util.StringUtils;

public class EntityBuilder<T extends Entity, B extends EntityBuilder> extends AbstractBuilder {

  protected T entity;
  Set<Identifier> identifiers;

  protected T createEntity() {
    return (T) new Entity();
  }

  protected EntityType getEntityType() {
    return EntityType.ENTITY;
  }

  public EntityBuilder() {
    entity = createEntity();
    entity.setEntityType(getEntityType());
  }

  public B withUuid(String uuid) {
    entity.setUuid(UUID.fromString(uuid));
    return (B) this;
  }

  public B atPath(String path) {
    entity.setUuid(extractFirstUuidFromPath(path));
    return (B) this;
  }

  public B createdAt(String createdAt) {
    entity.setCreated(LocalDateTime.parse(createdAt));
    return (B) this;
  }

  public B lastModifiedAt(String lastModifiedAt) {
    entity.setLastModified(LocalDateTime.parse(lastModifiedAt));
    return (B) this;
  }

  public B withIdentifier(String namespace, String id, String uuid) {
    if (identifiers == null) {
      identifiers = new HashSet<>();
    }
    Identifier identifier = new Identifier();
    identifier.setNamespace(namespace);
    identifier.setId(id);
    identifier.setUuid(UUID.fromString(uuid));
    identifiers.add(identifier);
    return (B) this;
  }

  public B withLabel(Locale locale, String localizedLabel) {
    LocalizedText label = entity.getLabel();
    if (label == null) {
      label = new LocalizedText();
    }
    label.setText(locale, localizedLabel);
    entity.setLabel(label);
    return (B) this;
  }

  public B withLabel(String nonlocalizedLabel) {
    entity.setLabel(nonlocalizedLabel);
    return (B) this;
  }

  public B withPreviewImage(String fileName, String uuid, String uri) {
    ImageFileResource previewImage =
        new PreviewImageBuilder(uuid).withFileName(fileName).withUri(uri).build();
    entity.setPreviewImage(previewImage);
    return (B) this;
  }

  public B withPreviewImage(String fileName, String uuid, String uri, MimeType mimeType) {
    ImageFileResource previewImage =
        new PreviewImageBuilder(uuid)
            .withFileName(fileName)
            .withUri(uri)
            .withMimeType(mimeType)
            .build();
    entity.setPreviewImage(previewImage);
    return (B) this;
  }

  public B withPreviewImage(
      String fileName, String uuid, String uri, MimeType mimeType, String httpBaseUrl) {
    ImageFileResource previewImage =
        new PreviewImageBuilder(uuid)
            .withFileName(fileName)
            .withUri(uri)
            .withMimeType(mimeType)
            .withHttpBaseUrl(httpBaseUrl)
            .build();
    entity.setPreviewImage(previewImage);
    return (B) this;
  }

  public B withRefId(long refId) {
    entity.setRefId(refId);
    return (B) this;
  }

  public B withOpenPreviewImageInNewWindow() {
    RenderingHintsPreviewImage previewImageRenderingHints = entity.getPreviewImageRenderingHints();
    if (previewImageRenderingHints == null) {
      previewImageRenderingHints = new RenderingHintsPreviewImage();
    }
    previewImageRenderingHints.setOpenLinkInNewWindow(true);
    entity.setPreviewImageRenderingHints(previewImageRenderingHints);
    return (B) this;
  }

  public B withUuidFromPath(String path) {
    entity.setUuid(extractFirstUuidFromPath(path));
    return (B) this;
  }

  public B withAltTextFromLabel() {
    RenderingHintsPreviewImage previewImageRenderingHints = entity.getPreviewImageRenderingHints();
    if (previewImageRenderingHints == null) {
      previewImageRenderingHints = new RenderingHintsPreviewImage();
    }
    previewImageRenderingHints.setAltText(entity.getLabel());
    entity.setPreviewImageRenderingHints(previewImageRenderingHints);
    return (B) this;
  }

  public B withTitleFromLabel() {
    RenderingHintsPreviewImage previewImageRenderingHints = entity.getPreviewImageRenderingHints();
    if (previewImageRenderingHints == null) {
      previewImageRenderingHints = new RenderingHintsPreviewImage();
    }
    previewImageRenderingHints.setTitle(entity.getLabel());
    entity.setPreviewImageRenderingHints(previewImageRenderingHints);
    return (B) this;
  }

  public B withDescription(Locale locale, String text) {
    LocalizedStructuredContent description = entity.getDescription();
    if (description == null) {
      description = new LocalizedStructuredContent();
    }
    StructuredContent localizedDescription = description.get(locale);
    if (localizedDescription == null) {
      localizedDescription = new StructuredContent();
    }
    ContentBlock paragraph = StringUtils.hasText(text) ? new Paragraph(text) : new Paragraph();
    localizedDescription.addContentBlock(paragraph);
    description.put(locale, localizedDescription);
    entity.setDescription(description);
    return (B) this;
  }

  public T build() {
    // Each identifier must get the UUID of the identifiable
    if (identifiers != null && !identifiers.isEmpty()) {
      entity.setIdentifiers(
          identifiers.stream()
              .peek(i -> i.setIdentifiable(entity.getUuid()))
              .collect(Collectors.toSet()));
    }
    return entity;
  }
}
